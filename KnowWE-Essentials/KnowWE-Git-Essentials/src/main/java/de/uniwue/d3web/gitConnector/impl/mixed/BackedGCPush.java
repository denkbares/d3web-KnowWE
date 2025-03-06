/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl.mixed;

import java.util.function.Supplier;

import de.uniwue.d3web.gitConnector.GitConnectorPush;
import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.jgit.JGitConnector;
import de.uniwue.d3web.gitConnector.impl.raw.push.PushCommandResult;

public class BackedGCPush implements GitConnectorPush {

	private final Supplier<JGitConnector> supplierJGit;
	private final Supplier<BareGitConnector> supplierBare;
	private GitConnectorPush jgGitPush;
	private GitConnectorPush barePush;

	BackedGCPush(Supplier<JGitConnector> supplierJGit, Supplier<BareGitConnector> supplierBare) {
		this.supplierJGit = supplierJGit;
		this.supplierBare = supplierBare;
	}

	void init() {
		if(jgGitPush == null) jgGitPush = supplierJGit.get().push();
		if(barePush == null) barePush = supplierBare.get().push();
	}

	@Override
	public PushCommandResult pushToOrigin(String userName, String passwordOrToken) {
		init();
		return this.jgGitPush.pushToOrigin(userName, passwordOrToken);
	}

	@Override
	public boolean pushAll() {
		init();
		if (this.barePush.gitInstalledAndReady()) {
			return this.barePush.pushAll();
		}
		return this.jgGitPush.pushAll();
	}

	@Override
	public boolean pushBranch(String branch) {
		init();
		if (this.barePush.gitInstalledAndReady()) {
			return this.barePush.pushBranch(branch);
		}
		return this.jgGitPush.pushBranch(branch);
	}

	@Override
	public boolean pushAll(String userName, String passwordOrToken) {
		init();
		if (jgGitPush.gitInstalledAndReady()) {
			return this.jgGitPush.pushAll(userName, passwordOrToken);
		}
		return this.barePush.pushAll(userName, passwordOrToken);
	}

	@Override
	public boolean pushBranch(String branch, String userName, String passwordOrToken) {
		init();
		if (jgGitPush.gitInstalledAndReady()) {
			return this.jgGitPush.pushBranch(branch, userName, passwordOrToken);
		}
		return this.barePush.pushBranch(branch, userName, passwordOrToken);
	}

	@Override
	public boolean gitInstalledAndReady() {
		init();
		return barePush.gitInstalledAndReady() || jgGitPush.gitInstalledAndReady();
	}
}
