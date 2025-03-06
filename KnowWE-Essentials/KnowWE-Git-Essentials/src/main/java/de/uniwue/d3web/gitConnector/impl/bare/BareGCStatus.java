/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.bare;

import java.io.File;
import java.util.Arrays;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnectorStatus;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommand;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;

class BareGCStatus implements GitConnectorStatus {

	private static final Logger LOGGER = LoggerFactory.getLogger(BareGCStatus.class);


	private String repositoryPath;
	private final Supplier<String> repositoryPathS;

	BareGCStatus(Supplier<String> repositoryPath) {
		this.repositoryPathS = repositoryPath;
	}

	@Override
	public FileStatus ofFile(@NotNull String file) {
			if(repositoryPath == null) repositoryPath = repositoryPathS.get();
			String[] statusCommand = new String[] { "git", "status", "--porcelain", file };
			String result = RawGitExecutor.executeGitCommand(statusCommand, this.repositoryPath).trim();

			if (result.isEmpty()) {
				File localFile = new File(repositoryPath + File.separator + file);
				if (!localFile.exists()) {
					return GitConnectorStatus.FileStatus.NotExisting;
				}
			}

			if (result.isBlank()) {
				// this the normal case : file is existing but not listed by git statuss
				return GitConnectorStatus.FileStatus.Committed_Clean;
			}
			String statusCode = result.substring(0, 1);
			if (statusCode.equals("?")) return GitConnectorStatus.FileStatus.Untracked;
			if (statusCode.trim().equals("A")) return GitConnectorStatus.FileStatus.Staged;
			if (statusCode.trim().equals("D")) return GitConnectorStatus.FileStatus.Committed_Deleted;
			if (statusCode.trim().equals("M")) return GitConnectorStatus.FileStatus.Committed_Modified;

			return GitConnectorStatus.FileStatus.NotExisting;
	}

	@Override
	public GitStatusCommandResult get() {
		if(repositoryPath == null) repositoryPath = repositoryPathS.get();
		GitStatusCommand gitStatusCommand = new GitStatusCommand(this.repositoryPath);
		return gitStatusCommand.execute();
	}

	@Override
	public boolean isClean() {
		if(repositoryPath == null) repositoryPath = repositoryPathS.get();
		String[] command = { "git", "status" };
		String response = RawGitExecutor.executeGitCommand(command, this.repositoryPath);
		// we do not know the language (and cannot set the language, as not every git installation comes with the language package)
		String[] dirtyKeyWords = { "new file", "modified", "deleted", "untracked" };
		boolean isDirty = Arrays.stream(dirtyKeyWords).toList().stream().anyMatch(key -> response.contains(key));
		boolean isClean = !isDirty;
		LOGGER.info("isClean: git status result is: " + isClean + "(" + response + ")");
		return isClean;
	}
}
