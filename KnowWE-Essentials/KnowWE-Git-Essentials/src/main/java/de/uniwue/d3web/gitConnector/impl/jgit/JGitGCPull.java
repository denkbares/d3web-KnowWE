/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.jgit;

import java.io.IOException;
import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorPull;

public class JGitGCPull implements GitConnectorPull {

	Logger LOGGER = LoggerFactory.getLogger(JGitGCPull.class);

	private final Supplier<Git> gitS;
	private final Supplier<Repository> repositoryS;

	private Git git;
	private Repository repository;

	JGitGCPull(@NotNull Supplier<Git> git, @NotNull Supplier<Repository> repository) {
		this.gitS = git;
		this.repositoryS = repository;
	}

	@Override
	public boolean call(boolean rebase) {
		if (git == null) git = gitS.get();
		if (repository == null) repository = repositoryS.get();
		PullCommand pull = git.pull()
				.setRemote("origin")
				.setRemoteBranchName(currentBranch())
				.setStrategy(MergeStrategy.RESOLVE)
				.setRebase(true);
		PullResult pullResult = null;
		try {
			pullResult = pull.call();
		}
		catch (JGitInternalException ie) {
			LOGGER.error("internal jgit error", ie);
			try {
				switch (repository.getRepositoryState()) {
					case REBASING_INTERACTIVE, REBASING, REBASING_REBASING, REBASING_MERGE -> git.rebase()
							.setOperation(RebaseCommand.Operation.ABORT)
							.call();
				}
				pullResult = pull.setContentMergeStrategy(ContentMergeStrategy.OURS).call();
			}
			catch (Exception e) {
				LOGGER.error("internal jgit error", ie);
			}
		}
		catch (GitAPIException e) {
			LOGGER.error("jgit API exception", e);
			throw new RuntimeException(e);
		}
		return pullResult != null && pullResult.isSuccessful();
	}

	private String currentBranch() {
		try {
			return this.repository.getBranch();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
