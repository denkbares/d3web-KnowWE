package de.uniwue.d3web.gitConnector.workflow.push.statemachine.states;

import java.util.ArrayList;
import java.util.List;

import de.uniwue.d3web.gitConnector.workflow.push.statemachine.ExamineCherryPickContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowContext;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

public class CheckIsIgnoredCommitState implements State {

	public State onAllIgnored;
	public State onPartialIgnored;
	public State onNoneIgnored;

	@Override
	public State next(GitWorkflowContext context) {
		if (context instanceof ExamineCherryPickContext cpc) {

			List<String> changedFiles = context.getGitConnector().listChangedFilesForHash(cpc.getCommitHash());

			List<String> ignoredFiles = new ArrayList<>();
			for (String changedFile : changedFiles) {
				if (cpc.getGitConnector().isIgnored(changedFile)) {
					ignoredFiles.add(changedFile);
				}
			}
			if (ignoredFiles.containsAll(changedFiles)) {
				return onAllIgnored;
			}
			if (ignoredFiles.isEmpty()) {
				return onNoneIgnored;
			}
			if (ignoredFiles.size() > 0) {
				return onPartialIgnored;
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
		return List.of(onAllIgnored, onPartialIgnored, onNoneIgnored);
	}
}
