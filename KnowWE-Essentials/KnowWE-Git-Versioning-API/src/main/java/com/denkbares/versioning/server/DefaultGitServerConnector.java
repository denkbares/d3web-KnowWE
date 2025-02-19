/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

import java.io.File;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.JGitBackedGitConnector;

public class DefaultGitServerConnector implements GitServerConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGitServerConnector.class);

	private final String repoManagementServerURL;
	private final String gitRemoteURL;

	public DefaultGitServerConnector(String repoManagementServerURL, String gitRemoteURL) {
		this.repoManagementServerURL = repoManagementServerURL;
		this.gitRemoteURL = gitRemoteURL;
	}

	@Override
	public GitConnector getGitConnector(String folder) {
		return JGitBackedGitConnector.fromPath(folder);
	}

	@Override
	public GitConnector getOrInitGitConnectorTo(String folder, String repoName) {
		String gitDirectory = getGitConnector(folder).getGitDirectory();
		String folderName = gitDirectory.substring(gitDirectory.lastIndexOf(File.separator)+1);
		if (!folderName.equals(repoName)) {
			// we want another repo than currently initialized
			InitCommand init = Git.init();
			File gitDir = new File(folder);
			init.setGitDir(gitDir);

			try {
				init.call();
			}
			catch (GitAPIException e) {
				LOGGER.error("Git init failed on folder:" + folder);
				throw new RuntimeException(e);
			}

			CloneCommand cloneCommand = Git.cloneRepository();
			cloneCommand.setGitDir(gitDir);
			String repoURL = gitRemoteURL + "/" + repoName;
			cloneCommand.setURI(repoURL);

			try {
				cloneCommand.call();
			}
			catch (GitAPIException e) {
				LOGGER.error("Git clone failed for repo url:" + repoURL);
				throw new RuntimeException(e);
			}
		}
		return getGitConnector(folder);
	}


}
