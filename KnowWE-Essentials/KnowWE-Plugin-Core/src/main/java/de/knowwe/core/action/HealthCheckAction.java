package de.knowwe.core.action;

import java.io.IOException;

/**
 * A simple health check for the application that assumes that being able to execute this method is only possible if the
 * wiki as a whole is running proplerly.
 *
 * @author Alex Legler (denkbares GmbH)
 * @created 2024-01-26
 */
public class HealthCheckAction extends AbstractAction {
	@Override
	public void execute(UserActionContext context) throws IOException {
		context.setContentType(Action.JSON);
		context.getWriter().write("{\"status\": \"ok\"}");
	}
}
