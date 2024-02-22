package de.knowwe.instantedit.actions;

import java.io.IOException;

import org.json.JSONObject;

import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class CheckCanEditPageAction extends AbstractAction {

	private static final String CAN_EDIT = "canedit";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String topic = context.getTitle();
		JSONObject response = new JSONObject();
		response.put(CAN_EDIT, false); // default

		if (Environment.getInstance().getArticle(context.getWeb(), topic) != null) {
			if (Environment.getInstance().getWikiConnector().userCanEditArticle(topic, context.getRequest())) {
				response.put(CAN_EDIT, true);
			}
		}

		if (context.getWriter() != null) {
			context.setContentType(HTML);
			response.write(context.getWriter());
		}
	}
}
