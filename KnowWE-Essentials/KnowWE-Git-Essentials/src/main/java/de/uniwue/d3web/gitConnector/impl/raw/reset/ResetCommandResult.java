package de.uniwue.d3web.gitConnector.impl.raw.reset;

import de.uniwue.d3web.gitConnector.impl.raw.GitCommandResult;

public sealed interface ResetCommandResult extends GitCommandResult permits CorruptedRepositoryResetCommandResult, PermissionDeniedCommandResult, ResetCommandSuccess, UnknownResetCommandResult {
}
