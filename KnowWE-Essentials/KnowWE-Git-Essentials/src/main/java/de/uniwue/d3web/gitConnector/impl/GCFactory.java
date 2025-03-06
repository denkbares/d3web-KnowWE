/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector.impl;

import de.uniwue.d3web.gitConnector.GitConnectorBranch;
import de.uniwue.d3web.gitConnector.GitConnectorCommit;
import de.uniwue.d3web.gitConnector.GitConnectorLog;
import de.uniwue.d3web.gitConnector.GitConnectorPull;
import de.uniwue.d3web.gitConnector.GitConnectorPush;
import de.uniwue.d3web.gitConnector.GitConnectorRepo;
import de.uniwue.d3web.gitConnector.GitConnectorRollback;
import de.uniwue.d3web.gitConnector.GitConnectorStatus;

public interface GCFactory {

	GitConnectorStatus createStatus();

	GitConnectorPull createPull();

	GitConnectorPush createPush();

	GitConnectorRollback createRollback();

	GitConnectorLog createLog();

	GitConnectorCommit createCommit();

	GitConnectorBranch createBranch();

	GitConnectorRepo createRepo();
}
