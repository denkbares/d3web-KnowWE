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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.RepositoryInfo;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

public class GitLabGitServerConnector implements GitServerConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitLabGitServerConnector.class);

	private final String repoManagementServerURL, gitRemoteURL, repoManagementServerApiURL, repoManagementServerToken;

	public GitLabGitServerConnector(String repoManagementServerURL, String gitRemoteURL, String repoManagementServerApiURL, String repoManagementServerToken) {
		this.repoManagementServerURL = repoManagementServerURL;
		this.gitRemoteURL = gitRemoteURL;
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
			cloneRepo(repoName, branch, gitDir);
		}
		// only switch if we want another repo than the current one
		else if (!getGitConnector(pullTargetFolder).repo().repoName().equals(repoName)) {
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

			cloneRepo(repoName, branch, gitDir);
		}
		else {
			// this is a weird case that does a normal pull on the existing repo
		}
		return true;
	}

	private void cloneRepo(@NotNull String repoName, @Nullable String branch, File gitDir) {
		// initialize/clone new git connected to other repo
		String cloneBranch = (branch != null && !branch.isBlank()) ? branch : GitConnector.DEFAULT_BRANCH;
		try (Git result = Git.cloneRepository()
				.setURI(new File("").getAbsolutePath() + gitRemoteURL + repoName)
				.setBranch(cloneBranch)
				.setDirectory(gitDir)
				.call()) {
			// Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
		}
		catch (GitAPIException e) {
			LOGGER.error("Git clone failed for repo url:" + gitRemoteURL);
			throw new RuntimeException(e);
		}
	}
}
