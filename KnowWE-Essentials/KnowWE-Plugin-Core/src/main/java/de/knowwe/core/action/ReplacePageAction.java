package de.knowwe.core.action;

import java.io.IOException;

import com.denkbares.strings.Strings;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;

public class ReplacePageAction extends AbstractAction {

	private String perform(UserActionContext context) throws IOException {
		String name = context.getTitle();
		String newText = context.getParameter(Attributes.TEXT);

		// Check for user access
		if (!Environment.getInstance().getWikiConnector().userCanEditArticle(name,
				context)) {
			return "perm";
		}

		newText = Strings.decodeURL(newText);

		// Remove any extra whitespace that might have gotten appended by
		// JSPWiki
		newText = newText.replaceAll("\\s*$", "");


		Environment.getInstance()
				.getWikiConnector()
				.writeArticleToWikiPersistence(name, newText, context, "DDOS");

		return "success";
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		String result = perform(context);
		if (result != null && context.getWriter() != null) {
			if (result.equals("perm")) {
				context.sendError(403, "You do not have the permission to edit this page.");
			}
			else {
				context.setContentType(HTML);
				context.getWriter().write(result);
			}
		}
	}
}
