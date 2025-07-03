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
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import de.uniwue.d3web.gitConnector.RepositoryInfo;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

public class GitLabGitServerConnector implements GitServerConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitLabGitServerConnector.class);

	private final String gitRemoteURL, gitUserName, repoManagementServerApiURL, repoManagementServerToken;

	public GitLabGitServerConnector(String gitRemoteURL, String gitUserName, String repoManagementServerApiURL, String repoManagementServerToken) {
		this.gitRemoteURL = gitRemoteURL;
		this.gitUserName = gitUserName;
		this.repoManagementServerApiURL = repoManagementServerApiURL;
		this.repoManagementServerToken = repoManagementServerToken;
	}

	@Override
	public GitConnector getGitConnector(@NotNull String folder) {
		return JGitBackedGitConnector.fromPath(folder);
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

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpUriRequest httpGet = new HttpGet(this.repoManagementServerApiURL + "/projects?simple=true&active=true&membership=true");
			httpGet.setHeader("PRIVATE-TOKEN", this.repoManagementServerToken);

			HttpResponse response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() >= 300) {
				throw new HttpException("Failed to load repositories");
			}
			String jsonString = EntityUtils.toString(response.getEntity());
			ObjectMapper objectMapper = new ObjectMapper();
			List<GitLabApiRepository> data = objectMapper.readValue(jsonString, new TypeReference<>() {
			});
			return data.stream()
					.map(repo -> new RepositoryInfo(repo.name, repo.http_url_to_repo, repo.web_url))
					.toList();
		}
		catch (Exception e) {
			throw new HttpException(e.getMessage());
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GitLabApiRepository(int id, String name, String ssh_url_to_repo, String http_url_to_repo,
									   String web_url) {
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
					new UsernamePasswordCredentialsProvider(this.gitUserName, this.repoManagementServerToken));
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
		CloneCommand clone = Git.cloneRepository().setURI(remoteURI);
		if (this.gitUserName != null && !this.gitUserName.isBlank()) {
			clone.setCredentialsProvider(
					new UsernamePasswordCredentialsProvider(this.gitUserName, this.repoManagementServerToken));
		}
		if (savePath != null) {
			clone.setDirectory(savePath);
		}
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
}
