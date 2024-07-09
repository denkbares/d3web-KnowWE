package de.uniwue.d3web.gitConnector.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.UserData;

public class BareGitConnector implements GitConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(BareGitConnector.class);
	public final String repositoryPath;
	public final boolean isGitInstalled;

	private BareGitConnector(String repositoryPath) {
		this.repositoryPath = repositoryPath;

		this.isGitInstalled = this.gitInstalled();
	}

	/**
	 * Decides whether a local git installation is available on the machine. Do not call this method ever, use the field
	 *
	 * @return Whether git is installed on the local machine
	 * @gitInstalled instead.
	 */
	private boolean gitInstalled() {
		String[] command = { "git", "--version" };

		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			Process process = processBuilder.start();
			int exitCode = process.waitFor();

			if (exitCode == 0) {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.toLowerCase().contains("git version")) {
							return true;
						}
					}
				}
			}
		}
		catch (IOException | InterruptedException e) {
			LOGGER.error("No git installed on the machine ... have to resort back to JGIT");
			return false;
		}
		return false;
	}

	@Override
	public boolean executeCommitGraph() {
		StopWatch sw = new StopWatch();
		sw.start();
		String label = "git commit graph command ";
		try {
			LOGGER.info("Starting execution of " + label);
			String command = "git commit-graph write --reachable --changed-paths";
			Process process = Runtime.getRuntime().exec(
					command, null, new File(this.repositoryPath));

			InputStream responseStream = process.getInputStream();
			int exitVal = process.waitFor();
			sw.stop();
			LOGGER.info("Execution of '" + command + "' took: " + sw);
			List<String> response = IOUtils.readLines(responseStream);
			String responseString = Strings.concat("\n", response);
			if (exitVal == 0) {
				LOGGER.info(label + "executed successfully. " + responseString);
			}
			else {
				LOGGER.warn(label + " terminated with error code: " + exitVal + " and message: " + responseString);
			}
		}
		catch (IOException | InterruptedException e) {
			LOGGER.error(label + " could not be run: " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public void cherryPick(String branch, List<String> commitHashesToCherryPick) {
		throw new NotImplementedException("TODO");
	}

	@Override
	public List<String> listChangedFilesForHash(String commitHash) {
		String command = "git diff-tree --no-commit-id --name-only -r " + commitHash;
		String result = RawGitExecutor.executeGitCommand(command, this.repositoryPath);
		return Arrays.asList(result.split("\n"));
	}

	@Override
	public List<String> commitHashesForFile(String file) {

		String[] command = null;
		command = new String[] { "git", "log", "--format=%H", file };

		String logOutput = RawGitExecutor.executeGitCommandWithTempFile(command, this.repositoryPath);

		List<String> commitHashes = new ArrayList<>();
		for (String line : logOutput.split("\n")) {
			String commitHash = line.replaceAll("\"", "").trim();
			commitHashes.add(commitHash);
		}
		Collections.reverse(commitHashes);
		return commitHashes;
	}

	@Override
	public List<String> commitHashesForFileSince(String file, Date date) {

		long epochTime = date.getTime() / 1000L;
		String[] command = new String[] { "git", "log", "--format=%H", "--since=@" + epochTime, file };

		String logOutput = RawGitExecutor.executeGitCommandWithTempFile(command, this.repositoryPath);

		List<String> commitHashes = new ArrayList<>();
		for (String line : logOutput.split("\n")) {
			String commitHash = line.replaceAll("\"", "").trim();
			if (!commitHash.isEmpty()) {
				commitHashes.add(commitHash);
			}
		}
		Collections.reverse(commitHashes);
		return commitHashes;
	}

	//TODO if version = -1then we may never know what the actual version is
	@Override
	public String commitHashForFileAndVersion(String file, int version) {
		int versionIndex = version - 1;
		List<String> strings = commitHashesForFile(file);

		if (versionIndex >= strings.size()) {
			LOGGER.warn("Requested a version higher than available for file: " + file);
			return null;
		}
		String commitHash = null;
		if (version == -1) {
			commitHash = strings.get(strings.size() - 1);
		}
		else {
			commitHash = strings.get(version - 1);
		}
		if (commitHash == null) {
			LOGGER.error("Can not obtain the according git hash");
		}

		return commitHash;
	}

	@Override
	public int numberOfCommitsForFile(String filePath) {
		String command = "git rev-list --count HEAD -- " + filePath;
		String s = RawGitExecutor.executeGitCommand(command, this.repositoryPath);

		return Integer.parseInt(s.trim());
	}

	@Override
	public List<String> getCommitsSince(Date timeStamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String sinceDate = dateFormat.format(timeStamp);

		String[] command = { "git", "log", "--format=%H", "--since=" + sinceDate };
		String response = RawGitExecutor.executeGitCommandWithTempFile(command, this.repositoryPath);

		List<String> commitHashes = new ArrayList<>();

		for (String line : response.split("\n")) {
			if (!line.isEmpty()) {
				commitHashes.add(line.trim());
			}
		}

		return commitHashes;
	}

	@Override
	public boolean isClean() {
		//TODO
		return false;
	}

	@Override
	public byte[] getBytesForPath(String path, int version) {
		String commitHash = this.commitHashForFileAndVersion(path, version);
		if (commitHash == null) {
			return null;
		}
		return getBytesForCommit(commitHash, path);
	}

	@Override
	public byte[] getBytesForCommit(String commitHash, String path) {
		String content = RawGitExecutor.executeGitCommand("git show " + commitHash + ":" + path, this.repositoryPath);
		if (content.contains(commitHash)) {
			LOGGER.error("Could not successfully execute: git show on : " + path);
			return null;
		}
		return content.getBytes();
	}

	@Override
	public boolean versionExists(String path, int version) {
		return commitHashForFileAndVersion(path, version) != null;
	}

	@Override
	public UserData userDataFor(String commitHash) {
		String[] command = { "git", "show", "--format='%an%n%ae%n%s'", "--no-patch" };
		String result = RawGitExecutor.executeGitCommand(command, this.repositoryPath);

		String[] split = result.split("\n");

		if (split.length < 3) {
			throw new IllegalStateException("Can not understand the commit...");
		}
		String authorName = split[0].replaceFirst("'", "").trim();
		String email = split[1].trim();
		String message = split[2].trim().substring(0, split[2].trim().length() - 1);
		return new UserData(authorName, email, message);
	}

	@Override
	public long commitTimeFor(String commitHash) {
		String[] command = { "git", "show", "--no-patch", "--format='%at'" };
		String result = RawGitExecutor.executeGitCommand(command, this.repositoryPath);
		return Long.parseLong(result.split("\n")[0].replaceAll("'", ""));
	}

	@Override
	public void performGC(boolean aggressive, boolean prune) {
		try {
			StopWatch sw = new StopWatch();
			sw.start();
			LOGGER.info("binary gc start");
			ProcessBuilder pb = new ProcessBuilder();
			pb.inheritIO().command("git", "gc", prune ? "--prune=now" : "").directory(new File(this.repositoryPath));
			Process git_gc = pb.start();
			git_gc.waitFor(2, TimeUnit.MINUTES);
			sw.stop();
			LOGGER.info("binary gc took " + sw.toString());
		}
		catch (InterruptedException e) {
			LOGGER.warn("External git process didn't end in 2 minutes, therefore cancel it");
		}
		catch (IOException e) {
			LOGGER.error("Error executing external git: " + e.getMessage(), e);
		}
	}

	@Override
	public void commitPathsForUser(String message, String author, String email, Set<String> paths) {
		throw new NotImplementedException("So far not implemented - use the JGit version");
	}

	@Override
	public void rollbackPaths(Set<String> pathsToRollback) {
		throw new NotImplementedException("So far not implemented - use the JGit version");
	}

	@Override
	public void moveFile(Path from, Path to, String user, String email, String message) {
		throw new NotImplementedException("So far not implemented - use the JGit version");
	}

	@Override
	public void deletePath(Path pathToDelete, UserData userData) {
		throw new NotImplementedException("So far not implemented - use the JGit version");
	}

	@Override
	public String changePath(Path pathToPut, UserData userData) {
		//TODO this needs the author/email to be set!
		String changedPath = Paths.get(this.repositoryPath).relativize(pathToPut).toString();

		addPath(changedPath);

		String[] commitCommand = new String[] { "git", "commit", "-m", userData.message };
		//and commit

		String commitResult = RawGitExecutor.executeGitCommand(commitCommand, this.repositoryPath);

		if (!commitResult.contains("1 file changed")) {
			LOGGER.error("Could not commit path: " + changedPath);
			return null;
		}

		return null;
	}

	@Override
	public void addPath(String path) {
		String addResult = RawGitExecutor.executeGitCommand("git add " + path, this.repositoryPath);
		if (!addResult.isEmpty()) {
			LOGGER.error("Could not add path: " + path);
		}
	}

	public static BareGitConnector fromPath(String repositoryPath) throws IllegalArgumentException {
		File file = new File(repositoryPath);

		if (!file.exists()) {
			LOGGER.info("Creating a new file for the git repository: " + repositoryPath);
			file.mkdirs();
		}

		String result = RawGitExecutor.executeGitCommand("git status", repositoryPath);
		String firstLine = "";
		if (result != null) {
			firstLine = result.trim().split("\n")[0];
		}
		if (firstLine.toLowerCase().contains("branch ")) {
			//we are golden this is a git repository!
			return new BareGitConnector(repositoryPath);
		}

		//this is no git repository, we should init it!

		result = RawGitExecutor.executeGitCommand("git init", repositoryPath);
		if (result.contains(".git")) {
			return new BareGitConnector(repositoryPath);
		}

		throw new IllegalArgumentException("Could not create repository");
	}
}
