/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.bare;

import java.util.function.Supplier;

import de.uniwue.d3web.gitConnector.GitConnectorPull;

public class BareGCPull implements GitConnectorPull {

	private String repositoryPath;
	private final Supplier<String> repositoryPathS;

	BareGCPull(Supplier<String> repositoryPath) {
		this.repositoryPathS = repositoryPath;
	}

	@Override
	public boolean call(boolean rebase, String origin) {
		if(repositoryPath == null) repositoryPath = repositoryPathS.get();
		String[] commitCommand = new String[] { "git", "pull" };
		if (rebase) {
			commitCommand = new String[] { "git", "pull", "--rebase" };
		}
		String result = RawGitExecutor.executeGitCommand(commitCommand, this.repositoryPath).trim();
		return result.isBlank() || result.startsWith("Already up to date.") || result.startsWith("Bereits aktuell.");
	}
}
