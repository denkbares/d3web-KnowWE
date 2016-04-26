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
			} catch (JSONException e) {
				throw new IOException(e);
			}
		}

	}

	public StringBuilder formatSparql(StringBuilder wikiText, int depth) {
		StringBuilder tmpWikiText = wikiText;
		boolean quoted = false;
		int startSecelt = 0;
		int startWhere = 0;
		boolean select = false;
		boolean where = false;

		tmpWikiText = addWhitespacesBeforeAndAfterBrackets(tmpWikiText);
		tmpWikiText = newLineAfterEachPoint(tmpWikiText);
		tmpWikiText = removeDoubleSpaces(tmpWikiText);
		tmpWikiText = removeFirstWhitepaceInLine(tmpWikiText);
		tmpWikiText = removeEmptyLines(tmpWikiText);
		tmpWikiText = addWhitespaceBeforePoints(tmpWikiText);

		// Count Brackets and set Indices of Keywords.
		for (int i = 0; i < tmpWikiText.length(); i++) {

			//Set quoted and !quoted
			if ((int) tmpWikiText.charAt(i) == 34) {
				if (!quoted) {
					tmpWikiText = addWhitespace(tmpWikiText, i);
					i++;
					quoted = true;
				} else {
					tmpWikiText = addWhitespace(tmpWikiText, i + 1);
					quoted = false;
				}
			}

			//Only work on text if the current character is not quoted or the last character of the text.
			if (!quoted && !checkLastChar(tmpWikiText, i)) {

				//Indent '%' right
				if (tmpWikiText.charAt(i) == '%') {
					if (i > 0) {
						if (tmpWikiText.charAt(i - 1) == '\t' || tmpWikiText.charAt(i - 1) == ' ') {
							tmpWikiText.deleteCharAt(i - 1);
						}
					}
				}

				//Detect and save SELECT
				if (tmpWikiText.toString().regionMatches(true, i, "select", 0, 6)) {

					if (!select) {
						select = true;
						startSecelt = i;
						tmpWikiText = checkIfKeywordIsWrongIndented(tmpWikiText, i);
						i = i + 3;
					} else {
						int bracketCounter = 1;
						boolean run = true;
						int h = i + 2;
						String a = "";
						String b = "";
						String c = "";

						//Count brackets and detect start and end of the subquery.
						while (run) {
							//Count Brackets
							if (tmpWikiText.charAt(h) == '{') {
								bracketCounter++;
							}
							if (tmpWikiText.charAt(h) == '}') {

								bracketCounter--;
								if (bracketCounter == 0) {
									/*Divide tmpWikiText into 3 sections. Section one contains the
									 text before the subquery, section two contains the text of the
									 subquery and section three contains the rest.*/
									a = tmpWikiText.substring(0, i);
									b = tmpWikiText.substring(i, h + 1);
									c = tmpWikiText.substring(h + 1, tmpWikiText.length());

									StringBuilder tmp = new StringBuilder(b);
									//recursive call of formatSparql with the subquery.
									tmp = formatSparql(tmp, depth);

									StringBuilder one = new StringBuilder(a);
									one.append(tmp);
									i = one.length();
									depth--;
									one.append(c);
									tmpWikiText = one;
									run = false;
								}
							}
							if (h == tmpWikiText.length() - 1) {
								run = false;
							}
							h++;
						}
					}
				}

				//Detect and save WHERE
				if (tmpWikiText.toString().regionMatches(true, i, "where", 0, 5)) {
					if (!where) {
						where = true;
						tmpWikiText = checkIfKeywordIsWrongIndented(tmpWikiText, i);
					}
				}

				//Detect and save ORDER BY
				if (tmpWikiText.toString().regionMatches(true, i, "order by", 0, 8)) {
					tmpWikiText = checkIfKeywordIsWrongIndented(tmpWikiText, i);
				}

				//Count Brackets
				if (tmpWikiText.charAt(i) == '{') {
					depth++;
				}
				if (tmpWikiText.charAt(i) == '}') {
					depth--;
					if (depth == 0) {
						if (checkIFLastBracketIsInNewLine(tmpWikiText, i)) {
							tmpWikiText = indentLastBracket(tmpWikiText, i);
							i++;
						}
					}
				}

				//handle optionals and content
				if (tmpWikiText.toString().regionMatches(true, i, "optional", 0, 8)) {
					if (checkIfAttributesAreTooLong(tmpWikiText, i)) {
						tmpWikiText = addnewLinesBeforeAndAfterBrackets(tmpWikiText, i);
					}
				}

				//indent lines
				if (tmpWikiText.charAt(i) == '\n' && !checkLastChar(tmpWikiText, i)) {
					tmpWikiText = indent(tmpWikiText, i, depth);
				}
			}

			//Breaks the loop if tmpWikitext has more than 9999 characters to avoid an endless loop.
			if (i > 9999) {
				return tmpWikiText;
			}
		}
		tmpWikiText = removeAllSpacesBeforeLinebreaks(tmpWikiText);
		tmpWikiText = removeEmptyLines(tmpWikiText);
		tmpWikiText = removeDoubleSpaces(tmpWikiText);

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

	private boolean checkIFLastBracketIsInNewLine(StringBuilder tmpWikiText, int i) {

		return tmpWikiText.charAt(i - 1) != '\n' || tmpWikiText.charAt(i - 1) != '\n' && tmpWikiText.charAt(i - 2) != '\n';
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
	private StringBuilder checkIfKeywordIsWrongIndented(StringBuilder tmpWikiText, int i) {

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
	private StringBuilder addnewLinesBeforeAndAfterBrackets(StringBuilder tmpWikiText, int i) {

		while (true) {
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
			i++;
		}
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
		tmpWikiText = new StringBuilder(tmpWikiText.toString().replaceAll("\\{", " \\{ "));
		tmpWikiText = new StringBuilder(tmpWikiText.toString().replaceAll("\\}", " \\}"));
		tmpWikiText = new StringBuilder(tmpWikiText.toString().replaceAll("\\>", "\\> "));
		tmpWikiText = new StringBuilder(tmpWikiText.toString().replaceAll("\\<", " \\<"));
		return tmpWikiText;
	}

	/* Checks if attributes inside an OPTIONAL are too long for one line.*/
	private boolean checkIfAttributesAreTooLong(StringBuilder tmpWikiText, int i) {

		boolean run = true;
		int countSpaces = 0;
		int loopCounter = i;
		int startingBracket = 0;
		int endingBracket = -1;
		int point = -1;
		boolean masked = false;


		while (run) {
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
				run = false;
			}
			if (currentChar == '.' && !masked) {
				point = loopCounter;
			}
			loopCounter++;
		}
		return (startingBracket < point && point < endingBracket);
	}

	/* Removes all empty lines but only if there are two or more in a row. A single line keeps standing. */
	private StringBuilder removeEmptyLines(StringBuilder tmpWikiText) {

		for (int i = 0; i < tmpWikiText.length(); i++) {
			if ((int) tmpWikiText.charAt(i) == '\n') {
				boolean hasNextNewline = true;
				while (hasNextNewline && !checkLastChar(tmpWikiText, i + 1)) {
					if (tmpWikiText.charAt(i + 1) == '\n' && tmpWikiText.charAt(i + 2) == '\n') {
						tmpWikiText.deleteCharAt(i + 1);
					} else {
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
		while (depth > 0) {
			tmpWikiText = addTab(tmpWikiText, index + 1);
			depth--;
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
					} else {
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
		int counter = 0;
		while (counter < tmpWikiText.length()) {

			if (!checkLastChar(tmpWikiText, counter)) {
				if (tmpWikiText.charAt(counter) == ' ' && tmpWikiText.charAt(counter + 1) == ' ') {
					tmpWikiText.deleteCharAt(counter + 1);
					counter--;
				}
			}
			counter++;
		}
		return tmpWikiText;
	}

	/* Checks if the character at 'Position' ist the last character of the inputText*/
	private boolean checkLastChar(StringBuilder inputText, int position) {
		return inputText.length() - 1 <= position;
	}


}