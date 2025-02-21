package de.uniwue.d3web.gitConnector.impl.raw.merge;

import de.uniwue.d3web.gitConnector.impl.raw.GitCommandResult;

public sealed interface GitMergeCommandResult extends GitCommandResult permits GitMergeResultBehindUpstream, GitMergeResultConflict, GitMergeResultInsufficientRights, GitMergeResultNotAGit, GitMergeResultSuccess, GitMergeResultUnknownResult, GitMergeResultUnmergedFiles, GitMergeResultUnstagedChanges {
}
