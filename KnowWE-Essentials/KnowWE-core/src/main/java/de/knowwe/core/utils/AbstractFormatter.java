package de.knowwe.core.utils;

/**
 * Created by ad on 30.09.16.
 */
public abstract class AbstractFormatter {

	protected int depth = 0;
	protected boolean quoted = false;
	protected StringBuilder tmpWikiText;

	protected AbstractFormatter(String wikiText) {
		tmpWikiText = new StringBuilder(wikiText);
	}

	public String format() {

		removeUnwantedSpaces(tmpWikiText);
		// add a newline to end if not existent, cause last char will not be processed
		guaranteeNext(tmpWikiText.length() - 1, '\n');

		// Guarantee that next char is existent if nothing changed
		for (int i = 0; i < tmpWikiText.length() - 1; i++) {

			// Don't format when quoted
			if (quoted) {
				if (tmpWikiText.charAt(i) == '"') {
					quoted = false;
				}
				continue;
			}

			if (i == 0 || (i > 0 && (tmpWikiText.charAt(i - 1) == ' ' || tmpWikiText.charAt(i - 1) == '\t' || tmpWikiText
					.charAt(i - 1) == '\n'))) {
				int resIndex = handleKeywords(i);
				if (resIndex > i) {
					i = resIndex;
					continue;
				}
			}

			i = handleChar(i);
		}

		removeUnwantedSpaces(tmpWikiText);

		return tmpWikiText.toString();
	}

	protected abstract int handleKeywords(int i);

	protected abstract int handleChar(int i);

	protected void guaranteeNext(int i, char required) {
		if (i < tmpWikiText.length() - 1) {
			if (tmpWikiText.charAt(i + 1) != required) {
				tmpWikiText.insert(i + 1, required);
			}
		}
		else {
			tmpWikiText.append('\n');
		}
	}

	/* Removes all following tabs and whitespaces and optional newlines. */
	protected void removeFollowingSpaces(StringBuilder tmpWikiText, int i, boolean deleteNewlines) {
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

	protected int removePreviousSpaces(int i, boolean deleteNewlines) {
		if (i > 0) {
			char prev = tmpWikiText.charAt(i - 1);
			while (prev == '\t' || prev == ' ' || (deleteNewlines && prev == '\n')) {
				tmpWikiText.deleteCharAt(--i);
				if (i > 0) {
					prev = tmpWikiText.charAt(i - 1);
				}
				else {
					break;
				}
			}
		}
		return i;
	}

	/*
 	* Removes all
 	* - newlines at beginning.
 	* - multiple whitespaces, so one space remains.
 	* - tabs and whitespaces at the end of lines.
 	* - whitespaces before a tab
 	*/
	protected void removeUnwantedSpaces(StringBuilder tmpWikiText) {
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

	protected int skipLine(int i) {
		while (i < tmpWikiText.length() - 1 && tmpWikiText.charAt(i) != '\n') i++;
		return --i; // get '\n' (last char is \n as well) in next loop
	}

	/* Indent the current line with tabs in number of 'depth' */
	protected void indent(int i) {
		for (int j = 0; j < depth; j++) tmpWikiText.insert(i + 1, "\t");
	}

	protected int skip(int i, char opening, char closing) {
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

	protected int lineLength(int i) {
		int charsBefore = 0;
		int charsAfter = 0;
		for (int j = i; j > 0 && tmpWikiText.charAt(j - 1) != '\n'; j--) {
			charsBefore++;
		}
		for (int j = i; j < tmpWikiText.length() - 1 && tmpWikiText.charAt(i - 1) != '\n'; j++) {
			charsAfter++;
			i++;
		}
		return charsBefore + 1 + charsAfter;
	}
}
