package de.uniwue.d3web.gitConnector.impl.raw;

import java.util.Map;

public interface RawGitCommand<T extends GitCommandResult> {

	String[] getCommand();

	String getRepositoryPath();

	Map<String,String> getEnvironmentParams();

	T execute();
}
