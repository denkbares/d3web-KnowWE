package de.knowwe.ontology.sparql;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Pretty formats a sparql query.
 * <p>
 * Created by Maximilian Brell on 01.03.16.
 */

// TODO: -Variablennamen umbenennen
//-Methodennamen umbenennen
//-UNION abfangen und damit umgehen
//-while-schleifen raus
//Maskierte " rausfiltern

public class SparqlFormatAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String wikiText = context.getParameter("wikiText");
		StringBuilder formattedWikiText = formatSparql(new StringBuilder(wikiText), 0);

		if (context.getWriter() != null) {
			context.setContentType("application/json; charset=UTF-8");
			JSONObject response = new JSONObject();
			try {
				response.put("wikiText", formattedWikiText.toString());
				response.write(context.getWriter());
			}
			catch (JSONException e) {
				throw new IOException(e);
			}
		}

	}

	public StringBuilder formatSparql(StringBuilder wikiText, int depth) {
		StringBuilder tmpWikiText = wikiText;
		boolean quoted = false;
		boolean select = false;

		tmpWikiText = addWhitespacesBeforeAndAfterBrackets(tmpWikiText);
		tmpWikiText = newLineAfterEachPoint(tmpWikiText);
		tmpWikiText = removeDoubleSpaces(tmpWikiText);
		tmpWikiText = removeFirstWhitepaceInLine(tmpWikiText);
		tmpWikiText = removeEmptyLines(tmpWikiText);
		tmpWikiText = addWhitespaceBeforePoints(tmpWikiText);

		// Count Brackets and set Indices of Keywords.
		for (int i = 0; i < tmpWikiText.length(); i++) {

			//Breaks the loop if tmpWikitext has more than 9999 characters to avoid an endless loop.
			if (i > 9999) {
				return tmpWikiText;
			}

			//Set quoted and !quoted
			if ((int) tmpWikiText.charAt(i) == 34) { // '"'
				if (!quoted) {
					tmpWikiText = addWhitespace(tmpWikiText, i);
					i++;
					quoted = true;
				}
				else {
					tmpWikiText = addWhitespace(tmpWikiText, i + 1);
					quoted = false;
				}
			}

			//Only work on text if the current character is not quoted or the last character of the text.
			if (quoted || checkLastChar(tmpWikiText, i)) continue;

			//Indent '%' right
			handleMarkupEnd(tmpWikiText, i);

			//Detect and save SELECT
			if (tmpWikiText.toString().regionMatches(true, i, "select", 0, 6)) {

				if (!select) {
					select = true;
					tmpWikiText = handleKeywordIndentation(tmpWikiText, i);
					i = i + 3;
				}
				else {
					int bracketCounter = 1;
					boolean run = true;
					int h = i + 2;
					String subQuery;

					//Count brackets and detect start and end of the subquery.
					while (run) {
						//Count Brackets
						if (tmpWikiText.charAt(h) == '{') {
							bracketCounter++;
						}
						if (tmpWikiText.charAt(h) == '}') {

							bracketCounter--;
							if (bracketCounter == 0) {
								subQuery = tmpWikiText.substring(i, h + 1);

								//recursive call of formatSparql with the subquery.
								StringBuilder tmp = formatSparql(new StringBuilder(subQuery), depth);
								tmpWikiText.delete(i, h + 1);
								tmpWikiText.insert(i, tmp);

								i += tmp.length();
								depth--;

								run = false;
							}
						}
						if (h == tmpWikiText.length() - 1) {
							run = false;
						}
						h++;
						if (h > 40000) {
							break;
						}
					}
				}
			}
			tmpWikiText = handleWhere(tmpWikiText, i, "where", 5);
			tmpWikiText = handleOrderBy(tmpWikiText, i);

			//Count Brackets
			if (tmpWikiText.charAt(i) == '{') {
				depth++;
			}
			if (tmpWikiText.charAt(i) == '}') {
				depth--;
				if (depth == 0) {
					if (checkIfLastBracketIsInNewLine(tmpWikiText, i)) {
						tmpWikiText = indentLastBracket(tmpWikiText, i);
						i++;
					}
				}
			}

			tmpWikiText = handleOptionals(tmpWikiText, i);
			tmpWikiText = handleIndents(depth, tmpWikiText, i);
		}

		tmpWikiText = removeAllSpacesBeforeLinebreaks(tmpWikiText);
		tmpWikiText = removeEmptyLines(tmpWikiText);
		tmpWikiText = removeDoubleSpaces(tmpWikiText);

		return tmpWikiText;
	}

	private StringBuilder handleWhere(StringBuilder tmpWikiText, int i, String where, int len) {
		if (tmpWikiText.toString().regionMatches(true, i, "where", 0, len)) {
			tmpWikiText = handleKeywordIndentation(tmpWikiText, i);
		}
		return tmpWikiText;
	}

	private void handleMarkupEnd(StringBuilder tmpWikiText, int i) {
		if (tmpWikiText.charAt(i) != '%') return;
		if (i <= 0) return;
		if (tmpWikiText.charAt(i - 1) == '\t' || tmpWikiText.charAt(i - 1) == ' ') {
			tmpWikiText.deleteCharAt(i - 1);
		}
	}

	private StringBuilder handleOrderBy(StringBuilder tmpWikiText, int i) {
		if (tmpWikiText.toString().regionMatches(true, i, "order by", 0, 8)) {
			tmpWikiText = handleKeywordIndentation(tmpWikiText, i);
		}
		return tmpWikiText;
	}

	private StringBuilder handleIndents(int depth, StringBuilder tmpWikiText, int i) {
		if (tmpWikiText.charAt(i) == '\n' && !checkLastChar(tmpWikiText, i)) {
			tmpWikiText = indent(tmpWikiText, i, depth);
		}
		return tmpWikiText;
	}

	private StringBuilder handleOptionals(StringBuilder tmpWikiText, int i) {
		if (tmpWikiText.toString().regionMatches(true, i, "optional", 0, 8)) {
			if (checkIfAttributesAreTooLong(tmpWikiText, i)) {
				tmpWikiText = addNewLinesBeforeAndAfterBrackets(tmpWikiText, i);
			}
		}
		return tmpWikiText;
	}

	private StringBuilder addWhitespaceBeforePoints(StringBuilder tmpWikiText) {
		for (int i = 0; i < tmpWikiText.length(); i++) {
			if (tmpWikiText.charAt(i) == '.' && tmpWikiText.charAt(i - 1) != ' ') {
				tmpWikiText = addWhitespace(tmpWikiText, i);
				i++;
			}
		}
		return tmpWikiText;
	}

	private boolean checkIfLastBracketIsInNewLine(StringBuilder tmpWikiText, int i) {
		return tmpWikiText.charAt(i - 1) != '\n' || tmpWikiText.charAt(i - 1) != '\n'
				&& tmpWikiText.charAt(i - 2) != '\n';
	}

	/* This method adds new lines before the last closing bracket if they are not already existing. */
	private StringBuilder indentLastBracket(StringBuilder tmpWikiText, int i) {

		if (tmpWikiText.charAt(i - 1) != '\n') {
			tmpWikiText = addNewLine(tmpWikiText, i);
		}
		if (tmpWikiText.charAt(i - 1) != '\n' && tmpWikiText.charAt(i - 2) != '\n') {
			tmpWikiText = addNewLine(tmpWikiText, i);
		}
		return tmpWikiText;
	}

	/* This method checks is a certain keyword is wrong indented. If so, it adds a new line.*/
	private StringBuilder handleKeywordIndentation(StringBuilder tmpWikiText, int i) {

		if (i > 0) {
			if (tmpWikiText.charAt(i - 1) != '\n') {
				if (tmpWikiText.charAt(i - 1) != '\t') {
					tmpWikiText = addNewLine(tmpWikiText, i);
				}
			}
		}
		return tmpWikiText;
	}

	/* Removes all whitespaces before linebreaks. This is necessary for other methods.*/
	private StringBuilder removeAllSpacesBeforeLinebreaks(StringBuilder tmpWikiText) {

		for (int i = 0; i < tmpWikiText.length(); i++) {
			if (!checkLastChar(tmpWikiText, i)) {
				if (tmpWikiText.charAt(i) == ' ' && tmpWikiText.charAt(i + 1) == '\n') {
					tmpWikiText.deleteCharAt(i);
				}
			}
		}
		return tmpWikiText;
	}

	/* Adds new lines before and after brackets if they aren't already there.*/
	private StringBuilder addNewLinesBeforeAndAfterBrackets(StringBuilder tmpWikiText, int i) {

		for (int f = i; i < tmpWikiText.length(); i++) {
			if (!checkLastChar(tmpWikiText, i)) {
				if (tmpWikiText.charAt(i) == '{') {
					if (tmpWikiText.charAt(i + 2) != '\n') {
						tmpWikiText = addNewLine(tmpWikiText, i + 2);
					}
				}
				if (tmpWikiText.charAt(i) == '}') {
					if (tmpWikiText.charAt(i - 1) != '\n') {
						tmpWikiText = addNewLine(tmpWikiText, i);
					}
					return tmpWikiText;
				}
			}
		}
		return tmpWikiText;
	}

	/* Goes through the String an searches all points.
	If a point is not masked and isn't followed by a new line, add a new line.
	*/
	private StringBuilder newLineAfterEachPoint(StringBuilder tmpWikiText) {

		boolean masked = false;
		for (int i = 0; i < tmpWikiText.length(); i++) {
			if (tmpWikiText.charAt(i) == '<') {
				masked = true;
			}
			if (tmpWikiText.charAt(i) == '>') {
				masked = false;
			}
			if (tmpWikiText.charAt(i) == '.' && tmpWikiText.charAt(i + 1) != '\n' && !masked) {
				tmpWikiText = addNewLine(tmpWikiText, i + 1);
				i++;
			}
		}
		return tmpWikiText;
	}

	/* Adds whitespaces before or after brackets*/
	private StringBuilder addWhitespacesBeforeAndAfterBrackets(StringBuilder tmpWikiText) {
		return new StringBuilder(tmpWikiText.toString()
				.replaceAll("\\{", " \\{ ")
				.replaceAll("\\}", " \\}")
				.replaceAll("\\>", "\\> ")
				.replaceAll("\\<", " \\<"));
	}

	/* Checks if attributes inside an OPTIONAL are too long for one line.*/
	private boolean checkIfAttributesAreTooLong(StringBuilder tmpWikiText, int i) {
		int startingBracket = 0;
		int endingBracket = -1;
		int point = -1;
		boolean masked = false;

		for (int loopCounter = i; loopCounter < tmpWikiText.length(); loopCounter++) {

			char currentChar = tmpWikiText.charAt(loopCounter);
			if (currentChar == '<') {
				masked = true;
			}
			if (currentChar == '>') {
				masked = false;
			}
			if (currentChar == '{' && !masked) {
				startingBracket = loopCounter;
			}
			if (currentChar == '}' && !masked) {
				endingBracket = loopCounter;
				break;
			}
			if (currentChar == '.' && !masked) {
				point = loopCounter;
			}
		}
		return (startingBracket < point && point < endingBracket);
	}

	/* Removes all empty lines but only if there are two or more in a row. A single line keeps standing. */
	private StringBuilder removeEmptyLines(StringBuilder tmpWikiText) {

		for (int i = 0; i < tmpWikiText.length(); i++) {
			if (tmpWikiText.charAt(i) == '\n') {
				boolean hasNextNewline = true;
				// TODO: for-loop
				while (hasNextNewline && !checkLastChar(tmpWikiText, i + 1)) {
					if (tmpWikiText.charAt(i + 1) == '\n' && tmpWikiText.charAt(i + 2) == '\n') {
						tmpWikiText.deleteCharAt(i + 1);
					}
					else {
						hasNextNewline = false;
					}
				}
			}
		}
		return tmpWikiText;
	}

	/* Indent the current line with tabs in number of 'depth' */
	private StringBuilder indent(StringBuilder tmpWikiText, int index, int depth) {

		if (tmpWikiText.charAt(index + 1) == '}') {
			depth--;
		}
		if (depth > 0) {
			while (depth > 0) {
				tmpWikiText = addTab(tmpWikiText, index + 1);
				depth--;
			}
		}
		return tmpWikiText;
	}

	/* Finds an removes all Whitespaces at the beginning of the line.*/
	private StringBuilder removeFirstWhitepaceInLine(StringBuilder tmpWikiText) {

		for (int i = 0; i < tmpWikiText.length(); i++) {
			if ((int) tmpWikiText.charAt(i) == '\n' || (i == 0 && (tmpWikiText.charAt(i) == '\t' || tmpWikiText.charAt(i) == ' '))) {
				boolean hasNextWhiteSpace = true;
				while (hasNextWhiteSpace && !checkLastChar(tmpWikiText, i)) {

					if (tmpWikiText.charAt(i + 1) == '\t' || tmpWikiText.charAt(i + 1) == ' ') {
						tmpWikiText.deleteCharAt(i + 1);
					}
					else {
						hasNextWhiteSpace = false;
					}
				}
			}
		}
		return tmpWikiText;
	}

	/* Adds a single new line at 'position' */
	private StringBuilder addNewLine(StringBuilder inputString, int position) {
		inputString.insert(position, "\n");
		return inputString;
	}

	/* Adds a single Tab at 'position' */
	private StringBuilder addTab(StringBuilder inputString, int position) {
		inputString.insert(position, "\t");
		return inputString;
	}

	/* Adds a single Whitespace at 'position' */
	private StringBuilder addWhitespace(StringBuilder inputString, int position) {
		inputString.insert(position, " ");
		return inputString;
	}

	/* Finds and deletes all unnecessary whitespaces */
	private StringBuilder removeDoubleSpaces(StringBuilder tmpWikiText) {
		for (int i = 0; i < tmpWikiText.length(); i++) {
			if (!checkLastChar(tmpWikiText, i)) {
				if (tmpWikiText.charAt(i) == ' ' && tmpWikiText.charAt(i + 1) == ' ') {
					tmpWikiText.deleteCharAt(i + 1);
					i--;
				}
			}
		}
		return tmpWikiText;
	}

	/* Checks if the character at 'Position' ist the last character of the inputText*/
	private boolean checkLastChar(StringBuilder inputText, int position) {
		return inputText.length() - 1 <= position;
	}

}