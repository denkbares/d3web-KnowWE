package de.uniwue.d3web.gitConnector.workflow.result;

public class PushCantReachOriginResultStage implements GitWorkflowResultStage{

	public final String resultString;

	public PushCantReachOriginResultStage(String resultString) {
		this.resultString = resultString;
	}

	@Override
	public boolean wasSuccessful() {
		return false;
	}

	@Override
	public String getResultString() {
		return resultString;
	}
}
