/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.bare;

import java.util.function.Supplier;

import de.uniwue.d3web.gitConnector.GitConnectorPush;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommand;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandResult;

public class BareGCPush implements GitConnectorPush {


	private String repositoryPath;
	private final Supplier<String> repositoryPathS;

	BareGCPush(Supplier<String> repositoryPath) {
		this.repositoryPathS = repositoryPath;
	}

	@Override
	public boolean pushAll() {
		if(repositoryPath == null) repositoryPath = repositoryPathS.get();

		String[] commitCommand = new String[] { "git", "push" };
		String result = RawGitExecutor.executeGitCommand(commitCommand, this.repositoryPath);
		return true;
	}

	@Override
	public boolean pushBranch(String branch) {
		if(repositoryPath == null) repositoryPath = repositoryPathS.get();

		String[] commitCommand = new String[] { "git", "push", "origin", branch };
		String result = RawGitExecutor.executeGitCommand(commitCommand, this.repositoryPath);
		return true;
	}

	@Override
	public boolean pushAll(String userName, String passwordOrToken) {
		// TODO: should use authorization!
		return pushAll();
	}

	@Override
	public boolean pushBranch(String branch, String userName, String passwordOrToken) {
		// TODO: should use authorization!
		return pushBranch(branch);
	}

	@Override
	public PushCommandResult pushToOrigin(String userName, String passwordOrToken) {
		if(repositoryPath == null) repositoryPath = repositoryPathS.get();
		PushCommand pushCommand = new PushCommand(this.repositoryPath, userName, passwordOrToken);
		return pushCommand.execute();
	}

	@Override
	public boolean gitInstalledAndReady() {
		if(repositoryPath == null) repositoryPath = repositoryPathS.get();
		// TODO: is this efficient enough to be called often?
		return new BareGitConnector(repositoryPath).gitInstalledAndReady();
	}
}
