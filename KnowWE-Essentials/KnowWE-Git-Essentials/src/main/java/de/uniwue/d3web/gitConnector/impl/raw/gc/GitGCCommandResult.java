package de.uniwue.d3web.gitConnector.impl.raw.gc;

import de.uniwue.d3web.gitConnector.impl.raw.GitCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResultNotAGit;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusResultSuccess;

public sealed interface GitGCCommandResult extends GitCommandResult permits GitGCCommandResultSuccess, GitGCUnknownResultSuccess {
}


