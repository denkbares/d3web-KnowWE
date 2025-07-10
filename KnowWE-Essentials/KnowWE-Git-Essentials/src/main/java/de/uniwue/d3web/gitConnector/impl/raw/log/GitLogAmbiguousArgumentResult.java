package de.uniwue.d3web.gitConnector.impl.raw.log;

import de.uniwue.d3web.gitConnector.impl.raw.gitexceptions.GitAmbigiuousArgumentCommandResult;

public final class GitLogAmbiguousArgumentResult implements GitLogCommandResult, GitAmbigiuousArgumentCommandResult {

	private String ambiguousArgument;

	public GitLogAmbiguousArgumentResult(String ambiguousArgument) {
		this.ambiguousArgument = ambiguousArgument;
	}
}
