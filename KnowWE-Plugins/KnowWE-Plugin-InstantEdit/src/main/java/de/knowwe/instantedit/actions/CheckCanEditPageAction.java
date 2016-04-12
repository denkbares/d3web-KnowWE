package de.knowwe.instantedit.actions;

import java.io.IOException;

import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class CheckCanEditPageAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String topic = context.getTitle();

		String result = "{\"canedit\":false}";

		if (Environment.getInstance().getArticle(context.getWeb(), topic) != null) {
			if (Environment.getInstance().getWikiConnector().userCanEditArticle(
					topic, context.getRequest())) {
				result = "{\"canedit\":true}";
			}
		}

		if (context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(result);
		}
	}

}
