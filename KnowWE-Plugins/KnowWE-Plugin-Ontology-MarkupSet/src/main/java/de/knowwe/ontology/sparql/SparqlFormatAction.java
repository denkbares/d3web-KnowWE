package de.knowwe.ontology.sparql;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.AbstractTripleFormatter;

/**
 * Pretty formats a SPARQL query.
 * <p>
 * Created by Maximilian Brell on 01.03.16.
 * Edited and redesigned by Adrian Müller on 22.09.16.
 */

public class SparqlFormatAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String wikiText = context.getParameter("wikiText");
		String formattedWikiText = new SparqlFormatter(wikiText).format();

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

	public static class SparqlFormatter extends AbstractTripleFormatter {
		boolean select = false;
		boolean optional = false, minus = false; // for holding in one line

		public SparqlFormatter(String wikiText) {
			super(wikiText);
		}

		@Override
		protected int handleChar(int i) {
			switch (tmpWikiText.charAt(i)) {
				case ',':
					currentPart = currentPart.prev();
					break;
				case '{':
					i = handleOpenBracket(i);
					break;
				case '}':
					i = handleCloseBracket(i);
					break;
				case '\n':
					i = handleNewline(i);
					break;
				case ' ':
				case '\t':
					break;
				case '@':
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
			if (tmpWikiText.toString().regionMatches(true, i, "select", 0, 6)) {
				if (!select) {
					select = true;
					i = bringOnOwnLine(i);
					i += 6;
				}
			}
			else if (tmpWikiText.toString().regionMatches(true, i, "where", 0, 5)) {
				if (i > 0 && tmpWikiText.charAt(i - 1) != ' ') {
					i = bringOnOwnLine(i);
				}
				if (select) {
					select = false;
					resetIndentSpecials();
					i = correctIndentation(i);
				}
				i += 4;
			}
			else if (tmpWikiText.toString().regionMatches(true, i, "order by", 0, 8)) {
				if (i > 0 && tmpWikiText.charAt(i - 1) != ' ') {
					i = bringOnOwnLine(i);
				}
				i += 7;
				currentPart = TriplePart.SECOND;
			}
			else if (tmpWikiText.toString().regionMatches(true, i, "union", 0, 5)) {
				i += 4;
				removeFollowingSpaces(i, false);
				guaranteeNext(i, ' ');
			}
			else if (tmpWikiText.toString().regionMatches(true, i, "optional", 0, 8)) {
				i = bringOnOwnLine(i);
				i += 7;
				removeFollowingSpaces(i, false);
				guaranteeNext(i, ' ');
				optional = true;
			}
			else if (tmpWikiText.toString().regionMatches(true, i, "minus", 0, 5)) {
				i = bringOnOwnLine(i);
				i += 4;
				removeFollowingSpaces(i, false);
				guaranteeNext(i, ' ');
				minus = true;
			}
			return i;
		}

		private int handleOpenBracket(int i) {
			depth++;
			if (i > 0) {
				char prev = tmpWikiText.charAt(i - 1);
				if (prev != '\t' && prev != '\n' && prev != ' ') {
					tmpWikiText.insert(i++, ' ');
				}
			}
			removeFollowingSpaces(i, false);
			// Push a following { oder } on a new line
			if (tmpWikiText.charAt(i + 1) == '{' || tmpWikiText.charAt(i + 1) == '}') {
				tmpWikiText.insert(i + 1, '\n');
			}
			else if (tmpWikiText.charAt(i + 1) != '\n') {
				if (optional || minus) {
					tmpWikiText.insert(i + 1, '\n');
				}
				else {
					tmpWikiText.insert(i + 1, '\t');
				}
			}
			super.resetIndentSpecials();
			return i;
		}

		private int handleCloseBracket(int i) {
			// reset indent
			depth--;
			super.resetIndentSpecials();
			// correct indent
			i = correctIndentation(i);
			// bring it on next line, if there's a non-space-char before
			if (i > 0 && (tmpWikiText.charAt(i - 1) != '\t' && tmpWikiText.charAt(i - 1) != '\n')) {
				if (optional || minus) {
					if (tmpWikiText.charAt(i - 1) != ' ') {
						tmpWikiText.insert(i++, ' ');
					}
					i = correctToWhitespaceAfterLastOpenBracket(i);
				}
				else {
					tmpWikiText.insert(i, '\n');
					indent(i);
					// skip tabs and itself
					i += depth + 1;
				}
			}
			resetIndentSpecials();
			removeFollowingSpaces(i, false);
			// Push a following { oder } on a new line
			if (tmpWikiText.charAt(i + 1) == '{' || tmpWikiText.charAt(i + 1) == '}') {
				tmpWikiText.insert(i + 1, '\n');
			}
			// add space if there is something else in this line
			if (tmpWikiText.charAt(i + 1) != '\n') {
				tmpWikiText.insert(i + 1, ' ');
			}
			return i;
		}

		@Override
		protected int handleNewline(int i) {
			i = super.handleNewline(i);
			removeFollowingSpaces(i, false);
			if (select && tmpWikiText.charAt(i + 1) == '\n') {
				resetIndentSpecials();
				select = false;
			}
			indent(i);
			return i;
		}

		@Override
		protected int handlePoint(int i) {
			i = guaranteeBefore(i, ' ');
			removeFollowingSpaces(i, false);
			if (optional || minus) {
				i = searchForCloseBracket(i);
				if (tmpWikiText.charAt(i + 1) != ' ') {
					guaranteeNext(i, '\n');
				}
			}
			else {
				guaranteeNext(i, '\n');
			}
			resetIndentSpecials();
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
					if (!select && (prev == '\t' || prev == ' ')) currentPart = currentPart.next();
					if (next == '\t' || next == ' ') {
						removeFollowingSpaces(i, false);
						tmpWikiText.insert(i + 1, ' ');
					}
					else if (next == '\n') {
						if (select && addDepth == 1) {
							break; // select arguments on new lines are only one time indented
						}
						addDepth++;
						depth++;
					}
					break;
				case THIRD:
					if (next == '\t' || next == ' ') {
						removeFollowingSpaces(i, false);
						if (optional || minus) {
							i = searchForCloseBracket(i);
						}
						else {
							tmpWikiText.insert(++i, ' ');
						}
					}
					else if (next == '\n') {
						if (select) {
							// SELECT has any number of arguments
							break;
						}
						if (optional || minus) {
							i = searchForCloseBracket(i);
						}
					}
					break;
			}
			return i;
		}

		private int searchForCloseBracket(int i) {
			if (tmpWikiText.charAt(i + 1) == '}') {
				tmpWikiText.insert(++i, ' ');
				i = searchForCloseBracket_handleFormat(i);
			}
			else if (tmpWikiText.charAt(i + 1) == '\n') {
				if (i < tmpWikiText.length() - 2) {
					removeFollowingSpaces(i + 1, true);
					if (tmpWikiText.charAt(i + 2) == '}') {
						tmpWikiText.deleteCharAt(i + 1);
						tmpWikiText.insert(++i, ' ');
						i = searchForCloseBracket_handleFormat(i);
					}
				}
			}
			return i; // so charAt(i) is in next loop '\n'
		}

		private int searchForCloseBracket_handleFormat(int i) {
			i++; // goto '}'
			i = correctToWhitespaceAfterLastOpenBracket(i);
			if (depth > 0) {
				depth--; // } found but skipped
			}
			removeFollowingSpaces(i, false);
			// Push a following { oder } on a new line
			if (tmpWikiText.charAt(i + 1) == '{' || tmpWikiText.charAt(i + 1) == '}') {
				tmpWikiText.insert(i + 1, '\n');
			}
			guaranteeNext(i, '\n');
			resetIndentSpecials();
			return i;
		}

		private int correctToWhitespaceAfterLastOpenBracket(int i) {
			int cnt = 1;
			int j = i;
			while (--j > 0) {
				if (tmpWikiText.charAt(j) == '{') {
					cnt--;
					if (cnt == 0) break;
				}
				else if (tmpWikiText.charAt(j) == '}') {
					cnt++;
				}
			}
			char next = tmpWikiText.charAt(j + 1);
			while (next == '\t' || next == ' ' || next == '\n') {
				tmpWikiText.deleteCharAt(j + 1);
				i--;
				if (j < tmpWikiText.length() - 1) {
					next = tmpWikiText.charAt(j + 1);
				}
			}
			tmpWikiText.insert(j + 1, ' ');
			return ++i;
		}

		@Override
		protected void resetIndentSpecials() {
			super.resetIndentSpecials();
			optional = false;
			minus = false;
		}
	}
}
