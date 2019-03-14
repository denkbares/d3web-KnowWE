package de.knowwe.ontology.turtle;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.AbstractTripleFormatter;

/**
 * Pretty formats turtles.
 * <p>
 * Created by Adrian Müller on 22.09.16.
 */

public class TurtleFormatAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String wikiText = context.getParameter("wikiText");
		String formattedWikiText = new TurtleFormatter(wikiText).format();

		if (context.getWriter() != null) {
			context.setContentType(JSON);
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

	public static class TurtleFormatter extends AbstractTripleFormatter {

		public TurtleFormatter(String wikiText) {
			super(wikiText);
		}

		@Override
		protected int handleChar(int i) {
			switch (tmpWikiText.charAt(i)) {
				case ',':
					currentPart = currentPart.prev();
					removeFollowingSpaces(i, false);
					if (tmpWikiText.charAt(i + 1) == '\n') {
						if (addDepth < 2) {
							addDepth++;
							depth++;
						}
					}
					else {
						tmpWikiText.insert(i + 1, ' ');
					}
					break;
				case '\n':
					i = handleNewline(i);
					break;
				case ' ':
				case '\t':
					break;
				//case '@':
				case '%':
					i = handlePercent(i);
					break;
				case '#':
					i = skipLine(i);
					break;
				case '(':
					i = skip(i, '(', ')');
					break;
				case '<':
					i = skip(i, '<', '>');
					break;
				case '[':
					i = skip(i, '[', ']');
					break;
				case '.':
					i = handlePoint(i);
					break;
				case ';':
					i = handleSemicolon(i);
					break;
				// " already caught
				case '"':
					quoted = true;
				default: // fall-through with intention
					i = processTriplePart(i);
					break;
			}
			return i;
		}

		private int handlePercent(int i) {
			if (tmpWikiText.toString().regionMatches(true, i, "%%sparql", 0, 8)) {
				i += 7;
				guaranteeNext(i, '\n');
				return i;
			}
			resetIndentSpecials();
			i = correctIndentation(i);
			return i;
		}

		@Override
		protected int handleKeywords(int i) {

			return i;
		}

		private int processTriplePart(int i) {
			char prev;
			if (i == 0) {
				prev = '\n'; // this illusion is okay since nothing down tries to change it
			}
			else {
				prev = tmpWikiText.charAt(i - 1);
			}
			char next = tmpWikiText.charAt(i + 1);
			switch (currentPart) {
				case NONE:
					if ((i == 0 || prev == '\t' || prev == ' ' || prev == '\n')) {
						currentPart = currentPart.next();
					}
					break;
				case FIRST:
				case SECOND:
					// if second or third are on a new line, they are indented, so there cannot be '\n' before
					if (prev == '\t' || prev == ' ') currentPart = currentPart.next();
					if (next == '\t' || next == ' ') {
						removeFollowingSpaces(i, false);
						tmpWikiText.insert(i + 1, ' ');
					}
					else if (next == '\n') {
						addDepth++;
						depth++;
					}
					break;
				case THIRD:
					if (next == '\t' || next == ' ') {
						removeFollowingSpaces(i, false);
						tmpWikiText.insert(++i, ' ');
					}
					break;
			}
			return i;
		}

	}
}
