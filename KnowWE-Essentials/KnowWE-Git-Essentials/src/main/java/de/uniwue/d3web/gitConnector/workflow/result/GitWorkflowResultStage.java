package de.uniwue.d3web.gitConnector.workflow.result;


public interface GitWorkflowResultStage {

	boolean wasSuccessful();

	String getResultString();
}
