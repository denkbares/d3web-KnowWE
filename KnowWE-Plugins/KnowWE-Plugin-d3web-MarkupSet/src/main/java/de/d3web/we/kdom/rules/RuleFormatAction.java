package de.d3web.we.kdom.rules;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.AbstractFormatter;

/**
 * Pretty formats a rule.
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
					removeFollowingSpaces(tmpWikiText, i, false);
					// remove more than one empty line
					if (tmpWikiText.charAt(i + 1) == '\n') {
						removeFollowingSpaces(tmpWikiText, i + 1, true);
						// remove the second newline on fileend
						if (i + 1 == tmpWikiText.length() - 1) {
							tmpWikiText.deleteCharAt(i + 1);
							return i;
						}
					}
					// indent next line
					indent(i);
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
			int changeDepth = 1;
			for (String keyword : ifKeywords) {
				if (wikiString.regionMatches(false, i, keyword, 0, keyword.length())) {
					i = handleKeywordIndentation(i);
					depth++;
					i += keyword.length() - 1;
					removeFollowingSpaces(tmpWikiText, i, true);
					tmpWikiText.insert(i + 1, ' ');
					return i;
				}
			}
			for (String keyword : otherKeywords) {
				if (wikiString.regionMatches(false, i, keyword, 0, keyword.length())) {
					i = handleKeywordIndentation(i);
					i += keyword.length() - 1;
					removeFollowingSpaces(tmpWikiText, i, true);
					tmpWikiText.insert(i + 1, ' ');
					return i;
				}
			}
			for (String keyword : thenKeywords) {
				if (wikiString.regionMatches(false, i, keyword, 0, keyword.length())) {
					depth--;
					i = handleKeywordIndentation(i);
					i += keyword.length() - 1;
					removeFollowingSpaces(tmpWikiText, i, true);
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

		/* Checks if a certain keyword is wrong indented. If so, it adds a new line.*/
		private int handleKeywordIndentation(int i) {
			if (i > 0) {
				i = removePreviousSpaces(i, false);
				if (tmpWikiText.charAt(i - 1) != '\n') {
					tmpWikiText.insert(i++, "\n");
				}
				indent(i - 1);
				i += depth;
			}
			return i;
		}

		/* deletes previous tabs and indents again */
		private int correctIndentation(int i) {
			while (i > 0 && tmpWikiText.charAt(i - 1) == '\t') {
				tmpWikiText.deleteCharAt(--i);
			}
			if (i > 0 && tmpWikiText.charAt(i - 1) == '\n') {
				indent(i - 1);
				i += depth;
			}
			return i;
		}
	}
}