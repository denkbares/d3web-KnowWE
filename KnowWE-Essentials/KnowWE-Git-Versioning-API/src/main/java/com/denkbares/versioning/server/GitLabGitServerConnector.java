/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
import de.uniwue.d3web.gitConnector.impl.GitConnectorParent;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

/**
 * The connection to a GitLab server: git over http with the credentials the source hands out, and the REST API with
 * the same secret as a bearer token, which carries a personal access token and a delegated OAuth token alike. The
 * credentials are asked for on every contact, so a source handing out short-lived ones, such as a launcher's
 * {@link LauncherCredentialProvider}, is honoured.
 */
public class GitLabGitServerConnector implements GitServerConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitLabGitServerConnector.class);

	private final String gitRemoteURL, encodedGroupPath, serverApiURL;
	private final Supplier<UserCredentials> credentials;
	private static final String GITLAB_BRANCH_PATH = "/-/tree/";

	/**
	 * A connector acting with one fixed username and token for its whole life, the shape of a standalone wiki with
	 * static properties.
	 */
	public GitLabGitServerConnector(String url, String groupPath, String gitUserName, String serverApiURL, String serverToken) {
		this(url, groupPath, serverApiURL, GitConnectorParent.fixed(new UserCredentials(gitUserName, serverToken)));
	}

	/**
	 * @param credentials where the credentials for every contact with the server come from, or null to act
	 *                    anonymously
	 */
	public GitLabGitServerConnector(String url, String groupPath, String serverApiURL, @Nullable Supplier<UserCredentials> credentials) {
		// make sure that gitRemoteURL always ends with / to prevent bugs
		if (!url.endsWith("/")) {
			url += "/";
		}
		this.gitRemoteURL = url + groupPath + "/";
		this.encodedGroupPath = groupPath.replaceAll("/", "%2F");
		this.serverApiURL = serverApiURL;
		this.credentials = credentials;
	}

	@Override
	public GitConnector getGitConnector(@NotNull String folder) {
		return JGitBackedGitConnector.fromPath(folder, credentials);
	}

	/**
	 * The credentials for a contact with the server happening now, or null if there are none.
	 */
	private @Nullable UserCredentials credentials() {
		return credentials == null ? null : credentials.get();
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
			String url = String.format("%s/groups/%s/projects?simple=true&active=true&membership=true&per_page=200", this.serverApiURL, this.encodedGroupPath);
			// So far only repos in this particular group are listed. This allows us to be able to uniquely identify repos only by their name.
			// In the future add "&include_subgroups=true" and repos need to be identified by namespace_with_path instead.
			HttpGet request = new HttpGet(url);
			request.setHeader(getRequestTokenHeader());

			HttpResponse response = httpClient.execute(request);
			String jsonString = requireBody(response, "Loading GitLab repositories");
			ObjectMapper objectMapper = new ObjectMapper();
			List<GitLabApiRepository> data = objectMapper.readValue(jsonString, new TypeReference<>() {
			});
			return data.stream()
					.map(repo -> new RepositoryInfo(repo.id, repo.name, repo.path, repo.http_url_to_repo, repo.web_url))
					.toList();
		}
		catch (HttpException e) {
			throw e;
		}
		catch (IOException e) {
			LOGGER.warn("Failed to load GitLab repositories.", e);
			throw transientFailure("Loading GitLab repositories", e);
		}
		catch (Exception e) {
			LOGGER.warn("Failed to load GitLab repositories.", e);
			throw unknownFailure("Loading GitLab repositories", e);
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GitLabApiRepository(int id, String name, String path, String ssh_url_to_repo,
	                                   String http_url_to_repo,
	                                   String web_url) {
	}

	/**
	 * The secret as a bearer token, which GitLab accepts for personal access tokens and OAuth tokens alike, where the
	 * PRIVATE-TOKEN header carries only the former.
	 */
	private BasicHeader getRequestTokenHeader() throws GitServerException {
		UserCredentials current = credentials();
		if (current == null) {
			throw new GitServerException(GitServerException.Reason.NOT_AUTHORIZED, 0,
					"There are no credentials for the git server, so it cannot be asked anything.");
		}
		return new BasicHeader("Authorization", "Bearer " + current.password);
	}

	/**
	 * The body of a successful response. A response that is not successful is logged and turned into a
	 * {@link GitServerException} naming what the status means for this operation, with the sentence the server
	 * wrote where it wrote one.
	 */
	private String requireBody(HttpResponse response, String operation) throws IOException, GitServerException {
		return requireBody(response, operation, Map.of());
	}

	/**
	 * @param statusReasons what a status means for this operation in particular, where it differs from the general
	 *                      reading of it
	 */
	private String requireBody(HttpResponse response, String operation,
							   Map<Integer, GitServerException.Reason> statusReasons) throws IOException, GitServerException {
		int statusCode = response.getStatusLine().getStatusCode();
		String responseBody = readResponseBody(response);
		if (statusCode < 300) {
			return responseBody;
		}
		throw failure(response, operation, responseBody, statusReasons);
	}

	private GitServerException failure(HttpResponse response, String operation, String responseBody,
									   Map<Integer, GitServerException.Reason> statusReasons) {
		int statusCode = response.getStatusLine().getStatusCode();
		if (responseBody.isBlank()) {
			LOGGER.warn("{} failed. GitLab response status: {}", operation, response.getStatusLine());
		}
		else {
			LOGGER.warn("{} failed. GitLab response status: {}. Response body: {}", operation, response.getStatusLine(), responseBody);
		}
		GitServerException.Reason reason = statusReasons.getOrDefault(statusCode, reasonOf(statusCode));
		String serverMessage = serverMessage(responseBody);
		String message = operation + " failed with status " + statusCode
				+ (serverMessage.isBlank() ? "" : ": " + serverMessage);
		return new GitServerException(reason, statusCode, message);
	}

	/**
	 * What a status means where the operation it was answered to says nothing more specific about it.
	 */
	private static GitServerException.Reason reasonOf(int statusCode) {
		return switch (statusCode) {
			case HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_FORBIDDEN -> GitServerException.Reason.NOT_AUTHORIZED;
			case HttpStatus.SC_NOT_FOUND, HttpStatus.SC_GONE -> GitServerException.Reason.NOT_FOUND;
			case HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_UNPROCESSABLE_ENTITY -> GitServerException.Reason.REJECTED;
			case HttpStatus.SC_CONFLICT -> GitServerException.Reason.CONFLICT;
			// 429 is the rate limit, which http core does not name
			case HttpStatus.SC_REQUEST_TIMEOUT, 429 -> GitServerException.Reason.TRANSIENT;
			default -> statusCode >= 500 ? GitServerException.Reason.TRANSIENT : GitServerException.Reason.UNKNOWN;
		};
	}

	/**
	 * The sentence GitLab wrote about a failure, taken from the message or error field of the body, which carries a
	 * text, a list of texts, or a map of field names to texts alike. Empty where the body says nothing.
	 */
	private static String serverMessage(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			return "";
		}
		try {
			JsonNode root = new ObjectMapper().readTree(responseBody);
			JsonNode message = root.has("message") ? root.get("message") : root.get("error");
			if (message == null) {
				return "";
			}
			StringBuilder texts = new StringBuilder();
			collectTexts(message, texts);
			return texts.toString().trim();
		}
		catch (Exception e) {
			// a body that is no json of the expected shape says nothing beyond the status
			return "";
		}
	}

	private static void collectTexts(JsonNode node, StringBuilder texts) {
		if (node.isValueNode()) {
			if (!texts.isEmpty()) texts.append(" ");
			texts.append(node.asText());
			return;
		}
		node.forEach(child -> collectTexts(child, texts));
	}

	private String readResponseBody(HttpResponse response) throws IOException {
		if (response.getEntity() == null) {
			return "";
		}
		return EntityUtils.toString(response.getEntity());
	}

	/**
	 * A failure of the http contact itself, which says nothing about the request and may well pass.
	 */
	private static GitServerException transientFailure(String operation, Exception cause) {
		return new GitServerException(GitServerException.Reason.TRANSIENT, 0,
				operation + " failed: " + cause.getMessage(), cause);
	}

	private static GitServerException unknownFailure(String operation, Exception cause) {
		return new GitServerException(GitServerException.Reason.UNKNOWN, 0,
				operation + " failed: " + cause.getMessage(), cause);
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
		withCredentials(clone);
		try (Git git = clone.call()) {
			// Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
		}
		catch (GitAPIException e) {
			LOGGER.error("Git clone failed for configured GitLab repository URL.", e);
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
			LOGGER.error("Internal JGit error while cloning repository.", e);
			throw new RuntimeException("Internal JGit error", e);
		}
		catch (GitAPIException e) {
			LOGGER.error("Git clone failed for remote repository.", e);
			throw new RuntimeException("Git clone failed for repo url: " + remoteURI, e);
		}
	}

	/**
	 * Hands the clone the credentials as they stand now, if there are any.
	 */
	private void withCredentials(CloneCommand clone) {
		UserCredentials current = credentials();
		if (current != null && current.user != null && !current.user.isBlank()) {
			clone.setCredentialsProvider(new UsernamePasswordCredentialsProvider(current.user, current.password));
		}
	}

	private CloneCommand prepareCloneCommand(String remoteURI, File savePath) {
		CloneCommand clone = Git.cloneRepository().setURI(remoteURI);
		withCredentials(clone);
		if (savePath != null) {
			clone.setDirectory(savePath);
		}
		return clone;
	}

	@Override
	public int getRepositoryId(String repoName, String httpUrl) throws HttpException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			String url = String.format("%s/groups/%s/projects?simple=true&active=true&membership=true&search=%s", this.serverApiURL, this.encodedGroupPath, repoName);
			HttpGet request = new HttpGet(url);
			request.setHeader(getRequestTokenHeader());

			HttpResponse response = httpClient.execute(request);
			String jsonString = requireBody(response, "Finding GitLab repository");
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
		catch (HttpException e) {
			throw e;
		}
		catch (IOException e) {
			LOGGER.warn("Failed to find GitLab repository '{}'.", repoName, e);
			throw transientFailure("Finding GitLab repository", e);
		}
		catch (Exception e) {
			LOGGER.warn("Failed to find GitLab repository '{}'.", repoName, e);
			throw new GitServerException(GitServerException.Reason.NOT_FOUND, 0,
					"The repository '" + repoName + "' is not among the repositories of this server.", e);
		}
	}

	@Override
	public String getBranchURL(GitConnector gitConnector) {
		return gitConnector.repo()
				.repoUrl()
				.replace(".git", GITLAB_BRANCH_PATH + gitConnector.branches().currentBranch());
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
			String jsonString = requireBody(response, "Listing GitLab merge requests");
			ObjectMapper objectMapper = new ObjectMapper();
			List<GitLabApiMergeRequest> data = objectMapper.readValue(jsonString, new TypeReference<>() {
			});
			return data.stream().map(GitLabGitServerConnector::toMergeRequest).toList();
		}
		catch (HttpException e) {
			throw e;
		}
		catch (IOException e) {
			LOGGER.warn("Failed to list GitLab merge requests for repository {} and source branch '{}'.", repositoryId, sourceBranch, e);
			throw transientFailure("Listing GitLab merge requests", e);
		}
		catch (Exception e) {
			LOGGER.warn("Failed to list GitLab merge requests for repository {} and source branch '{}'.", repositoryId, sourceBranch, e);
			throw unknownFailure("Listing GitLab merge requests", e);
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
		String operation = "Creating GitLab merge request";
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(
					this.serverApiURL + "/projects/" + repositoryId + "/merge_requests"
			);
			httpPost.setHeader(getRequestTokenHeader());

			JsonObject requestDto = new JsonObject();
			requestDto.addProperty("source_branch", sourceBranch);
			requestDto.addProperty("target_branch", targetBranch);
			requestDto.addProperty("squash", true);
			String prefix = "";
			if (sourceBranch.startsWith("Task")) prefix = String.format("[%s] ", sourceBranch);
			requestDto.addProperty("title", String.format(prefix + "Merge Request from %s to %s", sourceBranch, targetBranch));
			requestDto.addProperty("description", "This is an automatic merge request from " + sourceBranch + " to " + targetBranch + ".");
			httpPost.setEntity(new StringEntity(requestDto.toString(), ContentType.APPLICATION_JSON));

			HttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT) {
				return existingMergeRequest(response, operation, repositoryId, sourceBranch, targetBranch);
			}
			String jsonResponseString = requireBody(response, operation);
			ObjectMapper objectMapper = new ObjectMapper();
			GitLabApiMergeRequest responseDto = objectMapper.readValue(jsonResponseString, new TypeReference<>() {
			});
			return toMergeRequest(responseDto);
		}
		catch (HttpException e) {
			throw e;
		}
		catch (IOException e) {
			LOGGER.warn("Failed to create GitLab merge request from '{}' to '{}' in repository {}.", sourceBranch, targetBranch, repositoryId, e);
			throw transientFailure(operation, e);
		}
		catch (Exception e) {
			LOGGER.warn("Failed to create GitLab merge request from '{}' to '{}' in repository {}.", sourceBranch, targetBranch, repositoryId, e);
			throw unknownFailure(operation, e);
		}
	}

	/**
	 * What a refused creation means, which GitLab answers with the same status whether the branch carries an open
	 * merge request already or the request is impossible for another reason. An open merge request onto the same
	 * target is the one that was asked for and is answered instead; one onto another target is named as such, so a
	 * caller can point at it; anything else keeps the sentence the server wrote.
	 */
	private MergeRequest existingMergeRequest(
		HttpResponse response,
		String operation,
		int repositoryId,
		String sourceBranch,
		String targetBranch
	) throws IOException, HttpException {
		String responseBody = readResponseBody(response);
		List<MergeRequest> open = openMergeRequests(repositoryId, sourceBranch);
		MergeRequest sameTarget = open.stream()
				.filter(candidate -> candidate.targetBranch().equals(targetBranch))
				.findFirst()
				.orElse(null);
		if (sameTarget != null) {
			LOGGER.info("Merge request {} from '{}' to '{}' in repository {} is open already, continuing with it.",
					sameTarget.id(), sourceBranch, targetBranch, repositoryId);
			return sameTarget;
		}
		MergeRequest otherTarget = open.stream().findFirst().orElse(null);
		if (otherTarget != null) {
			throw new GitServerException(GitServerException.Reason.MERGE_REQUEST_EXISTS, HttpStatus.SC_CONFLICT,
					"The branch '" + sourceBranch + "' has an open merge request onto '" + otherTarget.targetBranch()
							+ "', so it cannot be merged into '" + targetBranch + "' as well.", otherTarget, null);
		}
		throw failure(response, operation, responseBody, Map.of());
	}

	/**
	 * The merge requests of the branch that are still to be decided, newest first.
	 */
	private List<MergeRequest> openMergeRequests(int repositoryId, String sourceBranch) throws HttpException {
		return listMergeRequests(repositoryId, sourceBranch).stream().filter(MergeRequest::isOpen).toList();
	}

	private static MergeRequest toMergeRequest(GitLabApiMergeRequest dto) {
		return new MergeRequest(
				dto.iid, dto.title, dto.source_branch, dto.target_branch,
				state(dto.state), mergeStatus(dto.merge_status), detail(dto), dto.web_url);
	}

	/**
	 * What the server says about the mergeability beyond the coarse status, which is the detailed status where it
	 * names one, and the error of the last merge attempt otherwise.
	 */
	private static @Nullable String detail(GitLabApiMergeRequest dto) {
		if (dto.detailed_merge_status != null && !dto.detailed_merge_status.isBlank()) return dto.detailed_merge_status;
		if (dto.merge_error != null && !dto.merge_error.isBlank()) return dto.merge_error;
		return null;
	}

	private static MergeRequestState state(@Nullable String state) {
		try {
			return MergeRequestState.valueOf(String.valueOf(state).toUpperCase());
		}
		catch (IllegalArgumentException e) {
			LOGGER.warn("The git server named an unknown merge request state '{}'.", state);
			return MergeRequestState.OPENED;
		}
	}

	/**
	 * A status the server does not name, or names in a word this code does not know, is read as unchecked, which is
	 * the one reading that keeps a caller from merging on an assumption.
	 */
	private static MergeRequestStatus mergeStatus(@Nullable String mergeStatus) {
		try {
			return MergeRequestStatus.valueOf(String.valueOf(mergeStatus).toUpperCase());
		}
		catch (IllegalArgumentException e) {
			LOGGER.warn("The git server named an unknown merge status '{}'.", mergeStatus);
			return MergeRequestStatus.UNCHECKED;
		}
	}

	@Override
	public MergeRequest mergeMergeRequest(int repositoryId, int mergeRequestIid, String commitMessage) throws RuntimeException, HttpException {
		String operation = "Merging GitLab merge request";
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPut request = new HttpPut(
					this.serverApiURL + "/projects/" + repositoryId + "/merge_requests/" + mergeRequestIid + "/merge"
			);
			request.setHeader(getRequestTokenHeader());

			JsonObject dto = new JsonObject();
			// The auto_merge property is currently not working. It should be set to false by default, but it is working as it is set to true.
			// Maybe in the next Gitlab version the behavior is fixed again.
			// dto.addProperty("auto_merge", true);

			dto.addProperty("squash_commit_message", commitMessage);

			// TODO could be changed to true if everything works as expected
			dto.addProperty("should_remove_source_branch", false);
			request.setEntity(new StringEntity(dto.toString(), ContentType.APPLICATION_JSON));

			HttpResponse response = httpClient.execute(request);
			String jsonString = requireBody(response, operation, MERGE_STATUS_REASONS);
			ObjectMapper objectMapper = new ObjectMapper();
			GitLabApiMergeRequest data = objectMapper.readValue(jsonString, new TypeReference<>() {
			});

			return toMergeRequest(data);
		}
		catch (HttpException e) {
			throw e;
		}
		catch (IOException e) {
			LOGGER.warn("Failed to merge GitLab merge request {} in repository {}.", mergeRequestIid, repositoryId, e);
			throw transientFailure(operation, e);
		}
		catch (Exception e) {
			LOGGER.warn("Failed to merge GitLab merge request {} in repository {}.", mergeRequestIid, repositoryId, e);
			throw unknownFailure(operation, e);
		}
	}

	/**
	 * What the statuses of a refused merge mean, which GitLab reads differently here than elsewhere: 405 for a merge
	 * request that is not in a state to be merged, 406 for branches that contradict each other, and 409 for a branch
	 * that moved on since.
	 */
	private static final Map<Integer, GitServerException.Reason> MERGE_STATUS_REASONS = Map.of(
			HttpStatus.SC_METHOD_NOT_ALLOWED, GitServerException.Reason.NOT_MERGEABLE,
			HttpStatus.SC_NOT_ACCEPTABLE, GitServerException.Reason.CONFLICT,
			HttpStatus.SC_CONFLICT, GitServerException.Reason.OUT_OF_DATE
	);
}
