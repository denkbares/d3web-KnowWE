package de.uniwue.d3web.gitConnector.impl.raw.push;

import de.uniwue.d3web.gitConnector.impl.raw.GitCommandResult;

public sealed interface PushCommandResult extends GitCommandResult permits PushCommandSuccess, PushCommandUnknownResult, PushCommandUnresolvedAddress {
}
