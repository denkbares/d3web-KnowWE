package de.uniwue.d3web.gitConnector.workflow.push.statemachine.states;

import java.util.List;

import de.uniwue.d3web.gitConnector.workflow.push.statemachine.ExamineCherryPickContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class CheckIsEmptyCommitState implements State {

	public State onEmpty;
	public State onNotEmpty;

	@Override
	public State next(GitWorkflowContext context) {
		if (context instanceof ExamineCherryPickContext cpc) {

			List<String> changedFiles = context.getGitConnector().listChangedFilesForHash(cpc.getCommitHash());
			if (changedFiles.isEmpty()) {
				cpc.setEmptyCommit(true);
				return this.onEmpty;
			}
			else {
				cpc.setEmptyCommit(false);
				return this.onNotEmpty;
			}
		}

		return null;
	}

	@Override
	public String description() {
		return "Checks whether a commit is empty, this means there are no files that are changed in this commit! If so passed into an endstate, otherwise pass back to previous state";
	}

	@Override
	public List<State> successors() {
		return List.of(onEmpty, onNotEmpty);
	}
}
