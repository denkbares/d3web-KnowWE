package de.uniwue.d3web.gitConnector.workflow.push.statemachine;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.workflow.push.statemachine.states.CheckIsEmptyCommitState;
import de.uniwue.d3web.gitConnector.workflow.push.statemachine.states.CheckIsIgnoredCommitState;
import de.uniwue.d3web.gitConnector.workflow.push.statemachine.states.EmptyCommitFinalState;
import de.uniwue.d3web.gitConnector.workflow.statemachine.GitWorkflowStateMachine;
import de.uniwue.d3web.gitConnector.workflow.statemachine.State;

/**
 * This state machine verifies (in a very complex manner...) if a commit, represented by its hash can be cherry-picked onto a specified branch
 */
public class CherryPickCompatibilityStateMachine implements GitWorkflowStateMachine<ExamineCherryPickContext> {

	private final String commitHash;
	private final GitConnector gitConnector;

	public CherryPickCompatibilityStateMachine(String commitHash, GitConnector gitConnector) {
		this.commitHash = commitHash;
		this.gitConnector = gitConnector;
	}

	@Override
	public State getInitialState() {
		/**
		 * Init the automaton
		 */
		//generate states
		CheckIsEmptyCommitState checkIsEmptyCommitState = new CheckIsEmptyCommitState();
		EmptyCommitFinalState emptyCommitFinalState = new EmptyCommitFinalState();
		CheckIsIgnoredCommitState isIgnoredCommitState = new CheckIsIgnoredCommitState();


		//and init transitions
		checkIsEmptyCommitState.onEmpty = emptyCommitFinalState;
		checkIsEmptyCommitState.onNotEmpty = isIgnoredCommitState;

		isIgnoredCommitState.onAllIgnored = emptyCommitFinalState;
		//this means we can never recover as the damage is already done, a commit contains a file that is ignored!
		isIgnoredCommitState.onPartialIgnored = new ErrorState("Some files of the commit are ignored (but not all of them, so we cannot simply ignore the commit!)");

		//we use this as our initial state
		return checkIsEmptyCommitState;
	}

	@Override
	public ExamineCherryPickContext getInitialContext() {
		return new ExamineCherryPickContext(gitConnector, this.commitHash);
	}
}
