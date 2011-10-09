package de.knowwe.instantedit.actions;

import java.io.IOException;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.UserActionContext;
import de.d3web.we.core.KnowWEEnvironment;

public class CheckCanEditPageAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String result = handle(context);
		if (result != null && context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(result);
		}
	}

	private String handle(UserActionContext context) throws IOException {

		String topic = context.getTitle();

		if (KnowWEEnvironment.getInstance().getArticle(context.getWeb(), topic) == null) {
			context.sendError(404, "Page '" + topic + "' could not be found.");
			return "{\"success\":false}";
		}

		if (KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(
				topic, context.getRequest())) {
			return "{\"success\":true}";
		}

		return "{\"success\":false}";
	}
}
