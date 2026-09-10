package de.knowwe.core.utils.progress;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;

public abstract class OperationAction extends AbstractAction {

	private static final String OPERATION_ID = "OperationID";

	@Override
	public void execute(UserActionContext context) throws IOException {
		// getSection also asserts the read access rights of the user for the requested section
		Section<?> section = getSection(context);

		String operationID = context.getParameter(OPERATION_ID);
		LongOperation operation = LongOperationUtils.getLongOperation(section, operationID);
		if (operation == null) {
			context.sendError(404, "no such operation to be executed");
			return;
		}

		execute(context, section, operation);
	}

	public abstract void execute(UserActionContext context, Section<?> section, LongOperation operation) throws IOException;
}
