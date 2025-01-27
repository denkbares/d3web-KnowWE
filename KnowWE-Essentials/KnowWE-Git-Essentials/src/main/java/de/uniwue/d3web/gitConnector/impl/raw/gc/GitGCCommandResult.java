package de.uniwue.d3web.gitConnector.impl.raw.gc;

import de.uniwue.d3web.gitConnector.impl.raw.GitCommandResult;

public sealed interface GitGCCommandResult extends GitCommandResult permits GitGCCommandResultSuccess, GitGCUnknownResultSuccess {
}


