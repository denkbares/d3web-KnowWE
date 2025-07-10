package de.uniwue.d3web.gitConnector.impl.raw.log;

import de.uniwue.d3web.gitConnector.impl.raw.GitCommandResult;

public sealed interface GitLogCommandResult extends GitCommandResult permits GitLogAmbiguousArgumentResult, GitLogCommandResultSuccess, GitLogGitRepositoryWithoutCommitsResult, GitLogNotAGitRepositoryResult, GitLogUnknownResult {
}

