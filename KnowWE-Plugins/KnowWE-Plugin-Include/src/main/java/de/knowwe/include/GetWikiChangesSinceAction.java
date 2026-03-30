package de.knowwe.include;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class GetWikiChangesSinceAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String sinceParameter = context.getParameter(InterWikiChanges.SINCE_PARAMETER);
		Instant since = parseSince(sinceParameter, context);
		if (since == null && sinceParameter != null && !sinceParameter.isBlank()) return;

		InterWikiChanges changes = InterWikiChanges.collect(since);
		context.setContentType(JSON);
		context.getWriter().write(changes.toJson().toString());
	}

	private Instant parseSince(String sinceParameter, UserActionContext context) throws IOException {
		if (sinceParameter == null || sinceParameter.isBlank()) return null;
		try {
			return Instant.parse(sinceParameter);
		}
		catch (DateTimeParseException ignored) {
			try {
				return Instant.ofEpochMilli(Long.parseLong(sinceParameter));
			}
			catch (NumberFormatException e) {
				context.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Invalid since parameter: " + sinceParameter);
				return null;
			}
		}
	}
}
