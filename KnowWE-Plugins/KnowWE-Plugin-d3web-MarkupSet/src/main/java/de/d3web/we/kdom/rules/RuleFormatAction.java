package de.d3web.we.kdom.rules;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.AbstractFormatter;

/**
 * Pretty formats rules.
 * <p>
 * Created by Adrian Müller on 26.09.16.
 */

public class RuleFormatAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String wikiText = context.getParameter("wikiText");
		String formattedWikiText = new RuleFormatter(wikiText).format();

		if (context.getWriter() != null) {
			context.setContentType("application/json; charset=UTF-8");
			JSONObject response = new JSONObject();
			try {
				response.put("wikiText", formattedWikiText);
				response.write(context.getWriter());
			}
			catch (JSONException e) {
				throw new IOException(e);
			}
		}
	}

	public static class RuleFormatter extends AbstractFormatter {

		public RuleFormatter(String wikiText) {
			super(wikiText);
		}

		@Override
		protected int handleChar(int i) {
			switch (tmpWikiText.charAt(i)) {
				case '(':
					depth++;
					break;
				case ')':
					depth--;
					break;
				case '"':
					quoted = true;
					break;
				case '\n':
					i = handleNewline(i);
					break;
				default:
					break;
			}
			return i;
		}

		@Override
		protected int handleKeywords(int i) {
			String[] ifKeywords = { "IF", "WENN" },
					otherKeywords = { "AND", "UND", "OR", "ODER" },
					thenKeywords = { "THEN", "DANN" };
			String wikiString = tmpWikiText.toString();

			for (String keyword : ifKeywords) {
				if (wikiString.regionMatches(false, i, keyword, 0, keyword.length())) {
					i = bringOnOwnLine(i);
					depth++;
					i += keyword.length() - 1;
					removeFollowingSpaces(i, true);
					tmpWikiText.insert(i + 1, ' ');
					return i;
				}
			}
			for (String keyword : otherKeywords) {
				if (wikiString.regionMatches(false, i, keyword, 0, keyword.length())) {
					i = bringOnOwnLine(i);
					i += keyword.length() - 1;
					removeFollowingSpaces(i, true);
					tmpWikiText.insert(i + 1, ' ');
					return i;
				}
			}
			for (String keyword : thenKeywords) {
				if (wikiString.regionMatches(false, i, keyword, 0, keyword.length())) {
					depth--;
					i = bringOnOwnLine(i);
					i += keyword.length() - 1;
					removeFollowingSpaces(i, true);
					tmpWikiText.insert(i + 1, ' ');
					return i;
				}
			}
			if (wikiString.regionMatches(false, i, "//", 0, 2)) {
				i = skipLine(i);
				return i;
			}
			return i;
		}
	}
}