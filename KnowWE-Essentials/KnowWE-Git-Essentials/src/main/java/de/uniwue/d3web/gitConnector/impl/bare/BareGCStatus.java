/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.bare;

import java.io.File;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.GitConnectorStatus;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommand;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;

class BareGCStatus implements GitConnectorStatus {

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
}
