package de.knowwe.core.action;

import java.io.IOException;

import de.knowwe.core.action.Action.Access;

/**
 * A simple health check for the application that assumes that being able to execute this method is only possible if the
 * wiki as a whole is running proplerly.
 *
 * @author Alex Legler (denkbares GmbH)
 * @created 2024-01-26
 */
public class HealthCheckAction extends AbstractAction {

	/**
	 * A health check is asked by whoever runs the wiki, before any user could sign in.
	 */
	@Override
	public Access requiredAccess() {
		return Access.NONE;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		context.setContentType(Action.JSON);
		context.getWriter().write("{\"status\": \"ok\"}");
	}
}
