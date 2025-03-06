/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.jgit;

import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnectorPush;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandSuccess;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandUnknownResult;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandUnresolvedAddress;

public class JGitGCPush implements GitConnectorPush {

	Logger LOGGER = LoggerFactory.getLogger(JGitGCPull.class);

	private final Supplier<Git> gitS;
	private final Supplier<Repository> repositoryS;

	private Git git;
	private Repository repository;

	JGitGCPush(@NotNull Supplier<Git> git, @NotNull Supplier<Repository> repository) {
		this.gitS = git;
		this.repositoryS = repository;
	}

	private void init() {
		if(git == null) git = gitS.get();
		if(repository == null) repository = repositoryS.get();
	}

	@Override
	public PushCommandResult pushToOrigin(String userName, String passwordOrToken) {
		init();
		PushCommand push = git.push();
		//check if we have credentials
		if (userName != null && passwordOrToken != null) {
			push = push.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passwordOrToken));
		}

		try {
			Iterable<PushResult> origin = push.setRemote("origin").setPushAll().call();
		}
		//TODO pretty inconsice
		catch (GitAPIException e) {
			if (e.getMessage().contains("UnresolvedAddressException")) {
				return new PushCommandUnresolvedAddress(e.getMessage());
			}
			return new PushCommandUnknownResult();
		}
		return new PushCommandSuccess();
	}

	@Override
	public boolean pushAll(String userName, String passwordOrToken) {
		init();
		try {
			LOGGER.info("Pushing all branches to remote...");
			git.push()
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passwordOrToken))
					.setPushAll()
					.call();
			return true;
		}
		catch (GitAPIException e) {
			LOGGER.error("Error pushing all branches to remote", e);
			return false;
		}
	}

	@Override
	public boolean pushBranch(String branch, String userName, String passwordOrToken) {
		init();
		try {
			LOGGER.info("Pushing branch: {} to remote...", branch);
			git.push()
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passwordOrToken))
					.setRemote("origin")
					.add(branch)
					.call();
			return true;
		}
		catch (GitAPIException e) {
			LOGGER.error("Error pushing branch {} to remote", branch, e);
			return false;
		}
	}

	@Override
	public boolean gitInstalledAndReady() {
		// should be fine
		return true;
	}
}
