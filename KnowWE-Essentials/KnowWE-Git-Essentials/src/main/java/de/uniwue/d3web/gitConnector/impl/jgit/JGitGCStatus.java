/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.jgit;

import java.io.File;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.GitConnectorStatus;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;

class JGitGCStatus implements GitConnectorStatus {

	private final Supplier<Git> gitS;
	private final Supplier<Repository> repositoryS;

	private Git git;
	private Repository repository;

	JGitGCStatus(@NotNull  Supplier<Git> git, @NotNull Supplier<Repository> repository) {
		this.gitS = git;
		this.repositoryS = repository;
	}

	@Override
	public FileStatus ofFile(@NotNull String file) {
		if(git == null) git = gitS.get();
		if(repository == null) repository = repositoryS.get();
		File localFile = new File(repository.getWorkTree() + File.separator + file);
		boolean fileExists = localFile.exists();

		Status status;
		try {
			status = git.status().call();
		}
		catch (GitAPIException e) {
			throw new RuntimeException(e);
		}

		// filter for untracked files
		Set<String> untracked = status.getUntracked();
		Set<String> added = status.getAdded();
		Set<String> changed = status.getChanged();
		Set<String> modified = status.getModified();
		Set<String> removed = status.getRemoved();
		Set<String> missing = status.getMissing();

		if (added.contains(file)) {
			return GitConnectorStatus.FileStatus.Staged;
		}
		if (changed.contains(file)) {
			return null;
		}
		if (modified.contains(file)) {
			return GitConnectorStatus.FileStatus.Committed_Modified;
		}
		if (removed.contains(file)) {
			return GitConnectorStatus.FileStatus.Committed_Deleted;
		}
		if (missing.contains(file)) {
			return GitConnectorStatus.FileStatus.Committed_Deleted;
		}
		if (untracked.contains(file)) {
			return GitConnectorStatus.FileStatus.Untracked;
		}

		// this is the normal case, file is existing, but not listed in git status
		if (fileExists) {
			return GitConnectorStatus.FileStatus.Committed_Clean;
		}
		else {
			return GitConnectorStatus.FileStatus.NotExisting;
		}
	}

	@Override
	public GitStatusCommandResult get() {
		throw new NotImplementedException("TODO");
	}

	@Override
	public boolean isClean() {
		if(git == null) git = gitS.get();
		if(repository == null) repository = repositoryS.get();
		try {
			Status call = new Git(this.repository).status().call();
			return call.isClean();
		}
		catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}
}
