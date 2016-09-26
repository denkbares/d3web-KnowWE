package de.knowwe.ontology.sparql;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Pretty formats a sparql query.
 * <p>
 * Created by Maximilian Brell on 01.03.16.
 * Edited and redesigned by Adrian Müller on 22.09.16.
 */

public class SparqlFormatAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String wikiText = context.getParameter("wikiText");
		String formattedWikiText = new Formatter(wikiText).formatSparql();

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

	public static class Formatter {
		StringBuilder tmpWikiText;
		boolean quoted = false;
		boolean select = false;
		boolean optional = false, minus = false; // for holding in one line
		int depth = 0;
		int addDepth = 0; // additional indents for sentenceparts
		TriplePart currentPart = TriplePart.NONE;

		public Formatter(String wikiText) {
			tmpWikiText = new StringBuilder(wikiText);
		}

		public String formatSparql() {

			removeUnwantedSpaces();
			// add a newline to end if not existent, cause last char will not be processed
			guaranteeNext(tmpWikiText.length() - 1, '\n');

			// Guarantee that next char is existent if nothing changed
			for (int i = 0; i < tmpWikiText.length() - 1; i++) {

				// Don't format when quoted
				if (quoted && tmpWikiText.charAt(i) == '"') {
					quoted = false;
					continue;
				}

				int resIndex = handleKeywords(i);
				if (resIndex > i) {
					i = resIndex;
					continue;
				}

				i = handelChar(i);
			}

			removeUnwantedSpaces();
			return tmpWikiText.toString();
		}

		private int handelChar(int i) {
			switch (tmpWikiText.charAt(i)) {
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
				case '%':
					i = handlePercent(i);
					break;
				case '#':
					i = skipLine(i);
					break;
				case '"':
					currentPart = currentPart.next();
					quoted = true;
					break;
				case '(':
					i = skip(i, '(', ')');
					break;
				case '<':
					i = skip(i, '<', '>');
					break;
				//case ')': // let default do that
				//case '>':
				//	break;
				case '.':
					i = handlePoint(i);
					break;
				case ';':
					i = handleSemicolon(i);
					break;
				// " already caught
				default:
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

		private int handleKeywords(int i) {
			if (tmpWikiText.toString().regionMatches(true, i, "select", 0, 6)) {
				if (!select) {
					select = true;
					i = handleKeywordIndentation(i, false);
					i += 6;
				}
			}
			else if (tmpWikiText.toString().regionMatches(true, i, "where", 0, 5)) {
				i = handleKeywordIndentation(i, true);
				if (select) {
					select = false;
					resetIndentSpecials();
					i = correctIndentation(i);
				}
				i += 4;
			}
			else if (tmpWikiText.toString().regionMatches(true, i, "order by", 0, 8)) {
				i = handleKeywordIndentation(i, true);
				i += 7;
				currentPart = TriplePart.SECOND;
			}
			else if (tmpWikiText.toString().regionMatches(true, i, "union", 0, 5)) {
				i += 4;
				removeFollowingSpaces(i, false);
				guaranteeNext(i, ' ');
			}
			else if (tmpWikiText.toString().regionMatches(true, i, "optional", 0, 8)) {
				i = handleKeywordIndentation(i, false);
				i += 7;
				removeFollowingSpaces(i, false);
				guaranteeNext(i, ' ');
				optional = true;
			}
			else if (tmpWikiText.toString().regionMatches(true, i, "minus", 0, 5)) {
				i = handleKeywordIndentation(i, false);
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
			currentPart = TriplePart.NONE;
			if (depth > addDepth) {
				depth -= addDepth;
			}
			else {
				depth = 0;
			}
			addDepth = 0;
			return i;
		}

		private int handleCloseBracket(int i) {
			// reset indent
			currentPart = TriplePart.NONE;
			if (depth > addDepth + 1) {
				depth -= addDepth;
				depth--;
			}
			else {
				depth = 0;
			}
			addDepth = 0;
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

		private int handleNewline(int i) {
			// remove the spaces at the beginning of the next line
			removeFollowingSpaces(i, false);
			if (currentPart == TriplePart.THIRD) {
				resetIndentSpecials();
			}
			// remove more than one empty line
			if (tmpWikiText.charAt(i + 1) == '\n') {
				if (select) {
					resetIndentSpecials();
					select = false;
				}
				removeFollowingSpaces(i + 1, true);
				// remove the second newline on fileend
				if (i + 1 == tmpWikiText.length() - 1) {
					tmpWikiText.deleteCharAt(i + 1);
					return i;
				}
			}
			// indent next line
			indent(i);
			return i;
		}

		private int handlePoint(int i) {
			// guarantee Space before
			if (i > 0 && tmpWikiText.charAt(i - 1) != ' ') {
				tmpWikiText.insert(i++, ' ');
			}
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

		private int handleSemicolon(int i) {
			resetIndentSpecials();
			addDepth++;
			depth++;
			currentPart = TriplePart.FIRST;
			// guarantee Space before
			if (i > 0 && tmpWikiText.charAt(i - 1) != ' ') {
				tmpWikiText.insert(i++, ' ');
			}
			guaranteeNext(i, '\n');
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

		private void guaranteeNext(int i, char required) {
			if (i < tmpWikiText.length() - 1) {
				if (tmpWikiText.charAt(i + 1) != required) {
					tmpWikiText.insert(i + 1, required);
				}
			}
			else {
				tmpWikiText.append('\n');
			}
		}

		private int skipLine(int i) {
			while (i < tmpWikiText.length() - 1 && tmpWikiText.charAt(i) != '\n') i++;
			return --i; // get '\n' (last char is \n as well) in next loop
		}

		private int skip(int i, char opening, char closing) {
			currentPart = currentPart.next();
			i++; // skip the opening char
			int cnt = 1;
			for (; i < tmpWikiText.length() - 1; i++) {
				if (tmpWikiText.charAt(i) == closing) {
					cnt--;
					if (cnt == 0) break;
				}
				else if (tmpWikiText.charAt(i) == opening) {
					cnt++;
				}
			}
			return --i; //next loop char is closing char (for processing end of triplepart)
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

		private void resetIndentSpecials() {
			currentPart = TriplePart.NONE;
			if (depth > addDepth) {
				depth -= addDepth;
			}
			else {
				depth = 0;
			}
			addDepth = 0;
			optional = false;
			minus = false;
		}

		/* Removes all following tabs and whitespaces and optional newlines. */
		private void removeFollowingSpaces(int i, boolean deleteNewlines) {
			if (i < tmpWikiText.length() - 1) {
				char next = tmpWikiText.charAt(i + 1);
				while (next == '\t' || next == ' ' || (deleteNewlines && next == '\n')) {
					tmpWikiText.deleteCharAt(i + 1);
					if (i < tmpWikiText.length() - 1) {
						next = tmpWikiText.charAt(i + 1);
					}
					else {
						break;
					}
				}
			}
		}

		/*
		 * Removes all
		 * - newlines at beginning.
		 * - multiple whitespaces, so one space remains.
		 * - tabs and whitespaces at the end of lines.
		 * - whitespaces before a tab
		 */
		private void removeUnwantedSpaces() {
			while (tmpWikiText.length() > 0 && tmpWikiText.charAt(0) == '\n') {
				tmpWikiText.deleteCharAt(0);
			}
			boolean emptyLine = true;
			int i = 0;
			for (; i < tmpWikiText.length() - 1; i++) {
				char next = tmpWikiText.charAt(i + 1);
				switch (tmpWikiText.charAt(i)) {
					case ' ':
						// spaces before at the end of a line (-> before newline)
						// spaces before tabs and other spaces
						if (next == '\t' || next == ' ' || next == '\n') tmpWikiText.deleteCharAt(i--);
						break;
					case '\t':
						// tabs at the end of the line are unwanted, not in empty lines
						if (!emptyLine && next == '\n') {
							tmpWikiText.deleteCharAt(i--);
						}
						else if (next == ' ') tmpWikiText.deleteCharAt(i + 1);
						break;
					case '\n':
						emptyLine = true;
						break;
					default:
						emptyLine = false;
						break;
				}
			}
		}

		/* Checks if a certain keyword is wrong indented. If so, it adds a new line.*/
		private int handleKeywordIndentation(int i, boolean allowWhitespace) {
			if (i > 0) {
				char prev = tmpWikiText.charAt(i - 1);
				if (prev == '\t' || prev == '\n' || (allowWhitespace && prev == ' ')) {
					return i;
				}
				tmpWikiText.insert(i, "\n");
				indent(i);
				i += depth;
				i++;
			}
			return i;
		}

		/* Indent the current line with tabs in number of 'depth' */
		private void indent(int i) {
			for (int j = 0; j < depth; j++) tmpWikiText.insert(i + 1, "\t");
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

	private enum TriplePart {
		FIRST, SECOND, THIRD, NONE;

		public TriplePart next() {
			switch (this) {
				case NONE:
					return TriplePart.FIRST;
				case FIRST:
					return TriplePart.SECOND;
				case SECOND:
					return TriplePart.THIRD;
				default:
					return TriplePart.NONE;
			}
		}
	}
}