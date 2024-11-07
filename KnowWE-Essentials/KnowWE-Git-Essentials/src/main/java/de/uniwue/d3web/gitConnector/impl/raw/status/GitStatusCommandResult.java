package de.uniwue.d3web.gitConnector.impl.raw.status;

import de.uniwue.d3web.gitConnector.impl.raw.GitCommandResult;

public sealed interface GitStatusCommandResult extends GitCommandResult permits GitStatusCommandResultNotAGit, GitStatusResultSuccess {
}


