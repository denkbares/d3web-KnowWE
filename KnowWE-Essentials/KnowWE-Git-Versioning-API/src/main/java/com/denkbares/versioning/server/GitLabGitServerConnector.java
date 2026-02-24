/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.UserCredentials;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

public class GitLabGitServerConnector implements GitServerConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitLabGitServerConnector.class);

	private final String gitRemoteURL, groupPath, gitUserName, serverApiURL, serverToken;

	public GitLabGitServerConnector(String url, String groupPath, String gitUserName, String serverApiURL, String serverToken) {
		// make sure that gitRemoteURL always ends with / to prevent bugs
		if (!url.endsWith("/")) {
			url += "/";
		}
		this.gitRemoteURL = url + groupPath + "/";
		this.groupPath = groupPath;
		this.gitUserName = gitUserName;
		this.serverApiURL = serverApiURL;
		this.serverToken = serverToken;
	}

	@Override
	public GitConnector getGitConnector(@NotNull String folder) {
		UserCredentials userCredentials = new UserCredentials(this.gitUserName, this.serverToken);
		return JGitBackedGitConnector.fromPath(folder, userCredentials);
	}

	@Override
	public GitConnector getOrInitGitConnectorTo(@NotNull String pullTargetFolder, @NotNull String repoName, @Nullable String branch) {
		File gitDir = new File(pullTargetFolder);

		if (!gitDir.exists()) {
			// we want to pull a new repo into a new folder
			cloneRepository(repoName, branch, gitDir);
		}
		// only switch if we want another repo than the current one
		else if (!gitDir.getName().equalsIgnoreCase(repoName)) {
			boolean success = switchFolderToOtherRepoAndBranch(pullTargetFolder, repoName, branch);
			if (!success) return null;
		}
		return getGitConnector(pullTargetFolder);
	}

	@Override
	public List<RepositoryInfo> listRepositories() throws HttpException {
		String groupPath = this.groupPath.replaceAll("/", "%2F");
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			String url = String.format("%s/groups/%s/projects?simple=true&active=true&membership=true&per_page=200", this.serverApiURL, groupPath);
			// So far only repos in this particular group are listed. This allows us to be able to uniquely identify repos only by their name.
			// In the future add "&include_subgroups=true" and repos need to be identified by namespace_with_path instead.
			HttpGet request = new HttpGet(url);
			request.setHeader(getRequestTokenHeader());

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() >= 300) {
				throw new HttpException("Failed to load repositories");
			}
			String jsonString = EntityUtils.toString(response.getEntity());
			ObjectMapper objectMapper = new ObjectMapper();
			List<GitLabApiRepository> data = objectMapper.readValue(jsonString, new TypeReference<>() {
			});
			return data.stream()
					.map(repo -> new RepositoryInfo(repo.id, repo.name, repo.path, repo.http_url_to_repo, repo.web_url))
					.toList();
		}
		catch (Exception e) {
			throw new HttpException(e.getMessage());
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GitLabApiRepository(int id, String name, String path, String ssh_url_to_repo,
									   String http_url_to_repo,
									   String web_url) {
	}

	private BasicHeader getRequestTokenHeader() {
		return new BasicHeader("PRIVATE-TOKEN", this.serverToken);
	}

	@Override
	public String getGitRemoteURL() {
		return gitRemoteURL;
	}

	private boolean switchFolderToOtherRepoAndBranch(@NotNull String pullTargetFolder, @NotNull String repoName, @Nullable String branch) {
		GitConnector oldGitConnector = getGitConnector(pullTargetFolder);
		String oldRepoName = oldGitConnector.repo().repoName();
		if (oldRepoName.isBlank()) {
			throw new RuntimeException("Something went wrong. Could not switch the repository.");
		}
		if (!oldRepoName.equals(repoName)) {
			// we want another repo than currently initialized
			if (!oldGitConnector.status().isClean()) {
				LOGGER.info("Can not switch Repo as old local repo is not clean");
				return false;
			}

			// delete old repo
			File gitDir = new File(pullTargetFolder);
			try {
				FileUtils.deleteDirectory(gitDir);
			}
			catch (IOException e) {
				LOGGER.error("Could not delete directory for repo change: " + gitDir + " : " + e.getMessage());
				throw new RuntimeException(e);
			}

			cloneRepository(repoName, branch, gitDir);
		}
		else {
			// this is a weird case that does a normal pull on the existing repo
		}
		return true;
	}

	private void cloneRepository(@NotNull String repoName, @Nullable String branch, File savePath) {
		// initialize/clone new git connected to other repo
		CloneCommand clone = Git.cloneRepository()
				.setURI(new File("").getAbsolutePath() + gitRemoteURL + repoName)
				.setDirectory(savePath);
		if (branch != null && !branch.isBlank()) {
			clone.setBranch(branch);
		}
		if (this.gitUserName != null && !this.gitUserName.isBlank()) {
			clone.setCredentialsProvider(
					new UsernamePasswordCredentialsProvider(this.gitUserName, this.serverToken));
		}
		try (Git git = clone.call()) {
			// Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
		}
		catch (GitAPIException e) {
			LOGGER.error("Git clone failed for repo url: " + gitRemoteURL);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void cloneRepository(String remoteURI, File savePath) throws RuntimeException {
		CloneCommand clone = prepareCloneCommand(remoteURI, savePath);
		try {
			clone.call().close();
		}
		catch (JGitInternalException e) {
			throw new RuntimeException("Internal JGit error", e);
		}
		catch (GitAPIException e) {
			throw new RuntimeException("Git clone failed for repo url: " + remoteURI);
		}
	}

	@Override
	public void cloneRepositoryShallow(String remoteURI, File savePath) throws RuntimeException {
		CloneCommand clone = prepareCloneCommand(remoteURI, savePath).setDepth(1);
		if (this.gitUserName != null && !this.gitUserName.isBlank()) {
			clone.setCredentialsProvider(
					new UsernamePasswordCredentialsProvider(this.gitUserName, this.serverToken));
		}

		try (Git result = clone.call()) {
		}
		catch (JGitInternalException e) {
			throw new RuntimeException("Internal JGit error", e);
		}
		catch (GitAPIException e) {
			throw new RuntimeException("Git clone failed for repo url: " + remoteURI + ". " + e.getMessage());
		}
	}

	private CloneCommand prepareCloneCommand(String remoteURI, File savePath) {
		CloneCommand clone = Git.cloneRepository().setURI(remoteURI);
		if (this.gitUserName != null && !this.gitUserName.isBlank()) {
			clone.setCredentialsProvider(
					new UsernamePasswordCredentialsProvider(this.gitUserName, this.serverToken));
		}
		if (savePath != null) {
			clone.setDirectory(savePath);
		}
		return clone;
	}

	@Override
	public int getRepositoryId(String repoName, String httpUrl) throws HttpException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			String url = String.format("%s/groups/%s/projects?simple=true&active=true&membership=true&search=%s", this.serverApiURL, groupPath, repoName);
			HttpGet request = new HttpGet(url);
			request.setHeader(getRequestTokenHeader());

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() >= 300) {
				throw new HttpException("Failed to find repository");
			}

			String jsonString = EntityUtils.toString(response.getEntity());
			ObjectMapper objectMapper = new ObjectMapper();
			List<GitLabApiRepository> data = objectMapper.readValue(jsonString, new TypeReference<>() {
			});
			String folderUrl = httpUrl.replaceFirst("//(.*@)", "//");

			return data.stream()
					.filter(repo -> repo.http_url_to_repo.equalsIgnoreCase(folderUrl))
					.map(repo -> repo.id)
					.findFirst()
					.orElseThrow();
		}
		catch (Exception e) {
			throw new HttpException(e.getMessage());
		}
	}

	@Override
	public List<MergeRequest> listMergeRequests(int repositoryId, @Nullable String sourceBranch) throws HttpException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			String branchFlag = "";
			if (sourceBranch != null && !sourceBranch.isBlank()) branchFlag = "?source_branch=" + sourceBranch;
			HttpGet request = new HttpGet(
					this.serverApiURL + "/projects/" + repositoryId + "/merge_requests" + branchFlag
			);
			request.setHeader(getRequestTokenHeader());

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() >= 300) {
				throw new HttpException("Failed to find merge requests");
			}
			String jsonString = EntityUtils.toString(response.getEntity());
			ObjectMapper objectMapper = new ObjectMapper();
			List<GitLabApiMergeRequest> data = objectMapper.readValue(jsonString, new TypeReference<>() {
			});
			return data.stream()
					.map(mr ->
							new MergeRequest(
									mr.iid, mr.title, mr.source_branch, mr.target_branch,
									MergeRequestState.valueOf(mr.state.toUpperCase()),
									MergeRequestStatus.valueOf(mr.merge_status.toUpperCase()),
									mr.web_url
							))
					.toList();
		}
		catch (Exception e) {
			throw new HttpException(e.getMessage());
		}
	}

	@Override
	public List<MergeRequest> listMergeRequests(int repositoryId) throws HttpException {
		return this.listMergeRequests(repositoryId, null);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record GitLabApiMergeRequest(int id, int iid, String title, String state,
										String target_branch, String source_branch,
										String merge_status, String detailed_merge_status,
										String merge_error, String web_url) {
	}

	@Override
	public MergeRequest createMergeRequest(int repositoryId, String sourceBranch, String targetBranch) throws RuntimeException, HttpException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(
					this.serverApiURL + "/projects/" + repositoryId + "/merge_requests"
			);
			httpPost.setHeader(getRequestTokenHeader());

			JsonObject requestDto = new JsonObject();
			requestDto.addProperty("source_branch", sourceBranch);
			requestDto.addProperty("target_branch", targetBranch);
			requestDto.addProperty("squash", false);
			String prefix = "";
			if (sourceBranch.startsWith("Task")) prefix = String.format("[%s] ", sourceBranch);
			requestDto.addProperty("title", String.format(prefix + "Merge Request from %s to %s", sourceBranch, targetBranch));
			requestDto.addProperty("description", "This is an automatic merge request from " + sourceBranch + " to " + targetBranch + ".");
			httpPost.setEntity(new StringEntity(requestDto.toString(), ContentType.APPLICATION_JSON));

			HttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() >= 300) {
				throw new HttpException("Failed to create merge request");
			}
			String jsonResponseString = EntityUtils.toString(response.getEntity());
			ObjectMapper objectMapper = new ObjectMapper();
			GitLabApiMergeRequest responseDto = objectMapper.readValue(jsonResponseString, new TypeReference<>() {
			});
			return new MergeRequest(
					responseDto.iid, responseDto.title,
					responseDto.source_branch, responseDto.target_branch,
					MergeRequestState.valueOf(responseDto.state.toUpperCase()),
					MergeRequestStatus.valueOf(responseDto.merge_status.toUpperCase()),
					responseDto.web_url);
		}
		catch (Exception e) {
			throw new HttpException(e.getMessage());
		}
	}

	@Override
	public MergeRequest mergeMergeRequest(int repositoryId, int mergeRequestIid) throws RuntimeException, HttpException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPut request = new HttpPut(
					this.serverApiURL + "/projects/" + repositoryId + "/merge_requests/" + mergeRequestIid + "/merge"
			);
			request.setHeader(getRequestTokenHeader());

			JsonObject dto = new JsonObject();
			dto.addProperty("auto_merge", true);
			// TODO could be changed to true if everything works as expected
			dto.addProperty("should_remove_source_branch", false);
			request.setEntity(new StringEntity(dto.toString(), ContentType.APPLICATION_JSON));

			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() >= 300) {
				throw new HttpException("Failed to merge branch");
			}
			String jsonString = EntityUtils.toString(response.getEntity());
			ObjectMapper objectMapper = new ObjectMapper();
			GitLabApiMergeRequest data = objectMapper.readValue(jsonString, new TypeReference<>() {
			});

			return new MergeRequest(
					data.iid, data.title,
					data.source_branch, data.target_branch,
					MergeRequestState.valueOf(data.state.toUpperCase()),
					MergeRequestStatus.valueOf(data.merge_status.toUpperCase()),
					data.web_url);
		}
		catch (Exception e) {
			throw new HttpException(e.getMessage());
		}
	}
}
