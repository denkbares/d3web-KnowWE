package de.knowwe.instantedit.actions;

import java.io.IOException;

import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class CheckCanEditPageAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String topic = context.getTitle();

		if (KnowWEEnvironment.getInstance().getArticle(context.getWeb(), topic) == null) {
			context.sendError(404, "Page '" + topic + "' could not be found.");
			return;
		}

		String result = "{\"canedit\":false}";

		if (KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(
				topic, context.getRequest())) {
			result = "{\"canedit\":true}";
		}

		if (context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(result);
		}
	}

}
