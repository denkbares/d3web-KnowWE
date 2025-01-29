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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.raw.merge.GitMergeCommand;
import de.uniwue.d3web.gitConnector.impl.raw.merge.GitMergeCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommand;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommand;
import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.reset.ResetCommandSuccess;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommand;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusResultSuccess;
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
			Process process = Runtime.getRuntime().exec(command, null, new File(this.repositoryPath));

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
	public String cherryPick(List<String> commitHashesToCherryPick) {
		if(commitHashesToCherryPick.isEmpty())
			return "";
		String hashes = commitHashesToCherryPick.stream().collect(Collectors.joining(" "));
		UserData userdata = userDataFor(commitHashesToCherryPick.get(0));

		Map<String, String> environment = new HashMap<>();
		environment.put("GIT_COMMITTER_NAME", userdata.user);
		environment.put("GIT_COMMITTER_EMAIL", userdata.email);

		String[] command = { "git", "cherry-pick", hashes, "--" };
		String cherryPickResult = RawGitExecutor.executeGitCommandWithEnvironment(command, this.repositoryPath,environment);
		if (cherryPickResult.contains("error: could not apply") || cherryPickResult.contains("CONFLICT")
				|| cherryPickResult.contains("Automatic cherry-pick failed")) {
			return cherryPickResult;
		}
		//an empty string signals success (OMG such great implementation!)
		return "";
	}

	@Override
	public List<String> listChangedFilesForHash(String commitHash) {
		String[] command = new String[] { "git", "show", "--no-commit-id", "--name-only", "--pretty=format:\"\"", commitHash };
		String result = new String(RawGitExecutor.executeGitCommandWithTempFile(command, this.repositoryPath));
		List<String> list = new ArrayList<>(Arrays.asList(result.split("\n")));
		list.remove("");
		list.remove("\"\"");
		return list;
	}

	@Override
	public String getGitDirectory() {
		return this.repositoryPath;
	}

	@Override
	public String currentBranch() {
		String currentBranch = RawGitExecutor.executeGitCommand("git branch --show-current", this.repositoryPath);
		if (currentBranch == null || currentBranch.isEmpty()) {
			throw new IllegalStateException("Can not access the current branch");
		}
		return currentBranch.trim();
	}

	@Override
	public String currentHEAD() {
		String currentHEAD = RawGitExecutor.executeGitCommand("git rev-parse HEAD", this.repositoryPath);
		if (currentHEAD == null || currentHEAD.isEmpty()) {
			throw new IllegalStateException("Can not access the current HEAD");
		}
		return currentHEAD.trim();
	}

	@Override
	public List<String> commitsBetween(String commitHashFrom, String commitHashTo) {
		if (Objects.equals(commitHashFrom, commitHashTo)) {
			return Collections.emptyList();
		}
		String commitHashes = RawGitExecutor.executeGitCommand("git log --pretty=format:\"%H\" " + commitHashFrom + ".." + commitHashTo, this.repositoryPath);
		List<String> hashesBetween = new ArrayList<>();
		for (String line : commitHashes.split("\n")) {
			if (line.trim().isEmpty()) {
				continue;
			}
			String commitHash = line.replaceAll("\"", "").trim();
			hashesBetween.add(commitHash);
		}
		//TODO i am not sure if i need to reverse this list!
		Collections.reverse(hashesBetween);
		return hashesBetween;
	}

	@Override
	public List<String> commitsBetweenForFile(String commitHashFrom, String commitHashTo, String path) {

		if (Objects.equals(commitHashFrom, commitHashTo)) {
			return Collections.emptyList();
		}
		String[] command = null;
		command = new String[] { "git", "log", "--format=%H", commitHashFrom + ".." + commitHashTo, "--", path };

		String logOutput = new String(RawGitExecutor.executeGitCommandWithTempFile(command, this.repositoryPath));

		List<String> commitHashes = new ArrayList<>();
		for (String line : logOutput.split("\n")) {
			if (line.trim().isEmpty()) {
				continue;
			}
			String commitHash = line.replaceAll("\"", "").trim();
			commitHashes.add(commitHash);
		}
		Collections.reverse(commitHashes);
		return commitHashes;
	}

	@Override
	public List<String> commitHashesForFile(String file) {

		String[] command = null;
		command = new String[] { "git", "log", "--format=%H", file };

		String logOutput = new String(RawGitExecutor.executeGitCommandWithTempFile(command, this.repositoryPath));

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

	@Override
	public List<String> commitHashesForFileInBranch(String file, String branchName) {
		String[] command = null;
		command = new String[] { "git", "log", branchName, "--format=%H", file };

		String logOutput = new String(RawGitExecutor.executeGitCommandWithTempFile(command, this.repositoryPath));

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

	@Override
	public List<String> commitHashesForFileSince(String file, Date date) {

		long epochTime = date.getTime() / 1000L;
		String[] command = new String[] { "git", "log", "--format=%H", "--since=@" + epochTime, file };

		String logOutput = new String(RawGitExecutor.executeGitCommandWithTempFile(command, this.repositoryPath));

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

	@Override
	public void destroy() {
		// what to destroy? -> currently nothing
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
		String response = new String(RawGitExecutor.executeGitCommandWithTempFile(command, this.repositoryPath));

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
		byte[] content = RawGitExecutor.executeGitCommandWithTempFile(new String[] { "git", "--no-pager", "show", commitHash + ":" + path }, this.repositoryPath);
		if (content == null) {
			LOGGER.error("Could not successfully execute: git show on : " + path);
			return null;
		}
		return content;
	}

	@Override
	public long getFilesizeForCommit(String commitHash, String path) {
		String filesize = RawGitExecutor.executeGitCommand("git cat-file -s " + commitHash + ":" + path, this.repositoryPath);
		if (filesize.trim().isEmpty()) {
			return -1;
		}
		return Long.parseLong(filesize.trim());
	}

	@Override
	public boolean versionExists(String path, int version) {
		return commitHashForFileAndVersion(path, version) != null;
	}

	@Override
	public UserData userDataFor(String commitHash) {
		String[] command = { "git", "show", "--format='%an%n%ae%n%s'", "--no-patch", commitHash };
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
		String[] command = { "git", "show", "--no-patch", "--format='%at'", commitHash };
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
	public String commitPathsForUser(String message, String author, String email, Set<String> paths) {
		throw new NotImplementedException("So far not implemented - use the JGit version");
	}

	@Override
	public void rollbackPaths(Set<String> pathsToRollback) {
		throw new NotImplementedException("So far not implemented - use the JGit version");
	}

	@Override
	public String moveFile(Path from, Path to, String user, String email, String message) {
		throw new NotImplementedException("So far not implemented - use the JGit version");
	}

	@Override
	public String deletePath(String pathToDelete, UserData userData, boolean cached) {
		throw new NotImplementedException("So far not implemented - use the JGit version");
	}

	@Override
	public String deletePaths(List<String> pathsToDelete, UserData userData, boolean cached) {
		String joinedPaths = String.join(" ", pathsToDelete);
		String prefix = cached ? "git rm --cached " : "git rm ";
		String rmResult = RawGitExecutor.executeGitCommand(prefix + joinedPaths, this.repositoryPath);
		if (!Arrays.stream(rmResult.split("\n")).allMatch(line -> line.startsWith("rm"))) {
			LOGGER.error("Could not delete paths: " + pathsToDelete);
		}
		return null;
	}

	@Override
	public String changePath(Path pathToPut, UserData userData) {
		String changedPath = null;
		try {
			Path repository = Paths.get(repositoryPath).toRealPath(); // Resolves symlinks
			Path target = pathToPut.toRealPath();         // Resolves symlinks
			changedPath = repository.relativize(target).toString();
		}
		catch (IOException e) {

			changedPath = Paths.get(this.repositoryPath).relativize(pathToPut).toString();
		}

		addPath(changedPath);

		String authorName = userData.user;
		if (authorName == null) {
			authorName = "Unknown User";
		}
		String authorEmail = userData.email;
		if (authorEmail == null) {
			authorEmail = "unknown@unknown.com";
		}

		String[] commitCommand = new String[] {
				"git", "commit", "--author=" + authorName + " <" + authorEmail + ">", "-m", userData.message
		};
		//and commit
		// Ensure committer identity is set
		Map<String, String> environment = new HashMap<>();
		environment.put("GIT_COMMITTER_NAME", authorName);
		environment.put("GIT_COMMITTER_EMAIL", authorEmail);

		String commitResult = RawGitExecutor.executeGitCommandWithEnvironment(commitCommand, this.repositoryPath,environment);

		if (!commitResult.contains("1 file changed")) {
			LOGGER.error("Could not commit path: " + changedPath);
			LOGGER.info(commitResult);
			return null;
		}
		List<String> commitHashes = commitHashesForFile(changedPath);
		return commitHashes.get(commitHashes.size() - 1);
	}

	@Override
	public void addPath(String path) {
		String addResult = RawGitExecutor.executeGitCommand("git add " + path, this.repositoryPath);
		if (!addResult.isEmpty()) {
			LOGGER.error("Could not add path: " + path);
			LOGGER.info(addResult);
		}
	}

	@Override
	public void addPaths(List<String> paths) {
		String joinedPaths = String.join(" ", paths);
		String addResult = RawGitExecutor.executeGitCommand("git add " + joinedPaths, this.repositoryPath);
		if (!addResult.isEmpty()) {
			LOGGER.error("Could not add paths: " + paths);
			LOGGER.info(addResult);
		}
	}

	@Override
	public boolean isIgnored(String path) {
		String ignoreResult = RawGitExecutor.executeGitCommand("git check-ignore " + path, this.repositoryPath);
		return !ignoreResult.trim().isEmpty();
	}

	@Override
	public String commitForUser(UserData userData) {
		String[] gitCommand = { "git", "commit", "--author=" + userData.user + " <" + userData.email + ">", "-m", userData.message };
		String commitResult = RawGitExecutor.executeGitCommand(gitCommand, this.repositoryPath);
		if (!commitResult.contains(userData.message)) {
			throw new IllegalStateException("Commit failed! for command: " + Arrays.toString(gitCommand) + "obtained result: \n" + commitResult);
		}

		return currentHEAD();
	}

	@Override
	public String commitForUser(UserData userData, long timeStamp) {
		Map<String, String> environment = Map.of("GIT_AUTHOR_DATE", String.valueOf(timeStamp), "GIT_COMMITTER_DATE", String.valueOf(timeStamp));
		String[] gitCommand = { "git", "commit", "--author=" + userData.user + " <" + userData.email + ">", "-m", userData.message };
		String commitResult = RawGitExecutor.executeGitCommandWithEnvironment(gitCommand, this.repositoryPath, environment);
		if (!commitResult.contains(userData.message)) {
			throw new IllegalStateException("Commit failed! for command: " + Arrays.toString(gitCommand) + "obtained result: \n" + commitResult);
		}

		return currentHEAD();
	}

	@Override
	public boolean isRemoteRepository() {
		throw new NotImplementedException("TODO");
	}

	@Override
	public List<String> listBranches() {
		if (!this.isGitInstalled) {
			return Collections.emptyList();
		}
		String[] gitCommand = { "git", "branch" };
		String branchResult = RawGitExecutor.executeGitCommand(gitCommand, this.repositoryPath);

		List<String> branches = new ArrayList<>();
		for (String branch : branchResult.split("\n")) {
			String trim = branch.trim();
			//active branch
			if (trim.startsWith("*")) {
				trim = trim.substring(1).trim();
			}
			branches.add(trim);
		}
		return branches;
	}

	@Override
	public List<String> listCommitsForBranch(String branchName) {
		String[] command = null;
		command = new String[] { "git", "log", branchName, "--format=%H" };

		String logOutput = new String(RawGitExecutor.executeGitCommandWithTempFile(command, this.repositoryPath));

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

	@Override
	public boolean switchToBranch(String branch, boolean createBranch) {
		String[] command = null;
		if (createBranch) {
			command = new String[] { "git", "checkout", "-b", branch };
		}
		else {
			command = new String[] { "git", "checkout", branch };
		}
		String logOutput = RawGitExecutor.executeGitCommand(command, this.repositoryPath);

		return currentBranch().equals(branch);
	}

	@Override
	public boolean createBranch(String branchName, String branchNameToBaseOn, boolean switchToBranch) {
		String[] command = null;

		command = new String[] { "git", "branch", branchName, branchNameToBaseOn };
		String logOutput = RawGitExecutor.executeGitCommand(command, this.repositoryPath);

		if (logOutput.isEmpty() && !switchToBranch) {
			return true;
		}

		if (switchToBranch) {
			return switchToBranch(branchName, false);
		}
		return false;
	}

	@Override
	public boolean untrackPath(String path) {

		//check if the file exists already
		File file = Paths.get(getGitDirectory(), path).toFile();

		if (!file.exists() || file.length() == 0) {
			try {
				//create the file
				file.getParentFile().mkdirs();
				Files.writeString(file.toPath(), "");
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		String[] command = new String[] { "git", "rm", "--cached", path };
		String output = RawGitExecutor.executeGitCommand(command, this.repositoryPath);

		if (output.startsWith("rm")) {
			return true;
		}
		return false;
	}

	@Override
	public boolean addNoteToCommit(String noteText, String commitHash, String namespace) {
		return false;
	}

	@Override
	public boolean copyNotes(String commitHashFrom, String commitHashTo) {
		return false;
	}

	@Override
	public Map<String, String> retrieveNotesForCommit(String commitHash) {
		return Map.of();
	}

	@Override
	public GitStatusCommandResult status() {
		GitStatusCommand gitStatusCommand = new GitStatusCommand(this.repositoryPath);
		return gitStatusCommand.execute();
	}

	@Override
	public void abortCherryPick() {
		RawGitExecutor.executeGitCommand(new String[] { "git", "cherry-pick", "--abort" }, this.repositoryPath);
	}

	@Override
	public GitMergeCommandResult mergeBranchToCurrentBranch(String branchName) {
		GitMergeCommand gitMergeCommand = new GitMergeCommand(this.repositoryPath, branchName);
		return gitMergeCommand.execute();
	}

	@Override
	public PushCommandResult pushToOrigin(String userName, String passwordOrToken) {
		PushCommand pushCommand = new PushCommand(this.repositoryPath, userName, passwordOrToken);
		return pushCommand.execute();
	}

	@Override
	public ResetCommandResult resetToHEAD() {
		ResetCommand command = new ResetCommand(this.repositoryPath);
		return command.execute();
	}

	public static BareGitConnector fromPath(String repositoryPath) throws IllegalArgumentException {
		LOGGER.info("Init BareGitConnector at path: " + repositoryPath);
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
		LOGGER.info("Git status did no succeed, ended with: " + result);
		//this is no git repository, we should init it!

		result = RawGitExecutor.executeGitCommand("git init", repositoryPath);
		if (result.contains(".git")) {
			return new BareGitConnector(repositoryPath);
		}

		throw new IllegalArgumentException("Could not create repository: " + result);
	}
}
