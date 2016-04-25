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
		StringBuilder formattedWikiText = formatSparql(new StringBuilder(wikiText));

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

	public StringBuilder formatSparql(StringBuilder wikiText) {

		System.out.println("Method started");

		StringBuilder tmpWikiText = wikiText;
		boolean quoted = false;
		int startSecelt = 0;
		int startWhere = 0;
		int depth = 0;
		boolean select = false;
		boolean where = false;


		tmpWikiText = addWhitespacesBeforeAndAfterBrackets(tmpWikiText);
		tmpWikiText = newLineAfterEachPoint(tmpWikiText);
		tmpWikiText = removeDoubleSpaces(tmpWikiText);
		tmpWikiText = removeFirstWhitepaceInLine(tmpWikiText);
		tmpWikiText = removeEmptyLines(tmpWikiText);
		tmpWikiText = addWhitespaceBeforePoints(tmpWikiText);

//		char currentSymbol = ' ';

		//Count Brackets and set Indices of Keywords
		for (int i = 0; i < tmpWikiText.length(); i++) {


//			currentSymbol = tmpWikiText.charAt(i);

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


						while (run) {
							//Count Brackets
							if (tmpWikiText.charAt(h) == '{') {
								bracketCounter++;
							}
							if (tmpWikiText.charAt(h) == '}') {

								bracketCounter--;
								if (bracketCounter == 0) {
									a = tmpWikiText.substring(0, i);
									b = tmpWikiText.substring(i, h + 1);
									c = tmpWikiText.substring(h + 1, tmpWikiText.length());

									StringBuilder tmp = new StringBuilder(b);
//									System.out.println("tmp = " + tmp);
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
						i = i + 3;
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

				//indent lines
				if (tmpWikiText.charAt(i) == '\n' && !checkLastChar(tmpWikiText, i)) {
					tmpWikiText = indent(tmpWikiText, i, depth);
				}

				//handle optionals and content
				if (tmpWikiText.toString().regionMatches(true, i, "optional", 0, 8)) {
					if (checkIfAttributesAreTooLong(tmpWikiText, i)) {
						tmpWikiText = addnewLinesBeforeAndAfterBrackets(tmpWikiText, i);
					}
				}

			}

			if (i > 5000) {
				return tmpWikiText;
			}
		}

		tmpWikiText = removeAllSpacesBeforeLinebreaks(tmpWikiText);
		tmpWikiText = removeEmptyLines(tmpWikiText);
		tmpWikiText = removeDoubleSpaces(tmpWikiText);


		return tmpWikiText;

	}


	public StringBuilder formatSparql(StringBuilder wikiText, int depth) {

		System.out.println("Method started");

		StringBuilder tmpWikiText = wikiText;
		boolean quoted = false;
		int startSecelt = 0;
		int startWhere = 0;
		boolean select = false;
		int newDepth = depth;


		tmpWikiText = addWhitespacesBeforeAndAfterBrackets(tmpWikiText);
		tmpWikiText = newLineAfterEachPoint(tmpWikiText);
		tmpWikiText = removeDoubleSpaces(tmpWikiText);
		tmpWikiText = removeFirstWhitepaceInLine(tmpWikiText);
		tmpWikiText = removeEmptyLines(tmpWikiText);
		tmpWikiText = addWhitespaceBeforePoints(tmpWikiText);

//		char currentSymbol = ' ';

		//Count Brackets and set Indices of Keywords
		for (int i = 0; i < tmpWikiText.length(); i++) {


//			currentSymbol = tmpWikiText.charAt(i);

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
					}
				}

				//Detect and save WHERE
				if (tmpWikiText.toString().regionMatches(true, i, "where", 0, 5)) {
					startWhere = i;
					tmpWikiText = checkIfKeywordIsWrongIndented(tmpWikiText, i);
					i = i + 3;
				}

				//Detect and save ORDER BY
				if (tmpWikiText.toString().regionMatches(true, i, "order by", 0, 8)) {
					tmpWikiText = checkIfKeywordIsWrongIndented(tmpWikiText, i);
					i++;
				}

				//Count Brackets
				if (tmpWikiText.charAt(i) == '{') {
					newDepth++;
				}
				if (tmpWikiText.charAt(i) == '}') {
					newDepth--;
				}

				//indent lines
				if (tmpWikiText.charAt(i) == '\n' && !checkLastChar(tmpWikiText, i)) {
					tmpWikiText = indent(tmpWikiText, i, newDepth);
				}

				//handle optionals and content
				if (tmpWikiText.toString().regionMatches(true, i, "optional", 0, 8)) {
					if (checkIfAttributesAreTooLong(tmpWikiText, i)) {
						tmpWikiText = addnewLinesBeforeAndAfterBrackets(tmpWikiText, i);
					}
				}

			}

			if (i > 5000) {
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
		if (tmpWikiText.charAt(i - 1) != '\n') {
			return true;
		}
		if (tmpWikiText.charAt(i - 1) != '\n' && tmpWikiText.charAt(i - 2) != '\n') {
			return true;
		}

		return false;
	}

	private StringBuilder indentLastBracket(StringBuilder tmpWikiText, int i) {

		if (tmpWikiText.charAt(i - 1) != '\n') {
			tmpWikiText = addNewLine(tmpWikiText, i);
		}
		if (tmpWikiText.charAt(i - 1) != '\n' && tmpWikiText.charAt(i - 2) != '\n') {
			tmpWikiText = addNewLine(tmpWikiText, i);
		}

		return tmpWikiText;

	}

	private StringBuilder checkIfKeywordIsWrongIndented(StringBuilder tmpWikiText, int i) {

//		if (tmpWikiText.charAt(i) != 'O') {
//			if (tmpWikiText.charAt(i - 2) != '\n') {
//				tmpWikiText = addNewLine(tmpWikiText, i);
//			}
//		}

		if (i > 0) {
			if (tmpWikiText.charAt(i - 1) != '\n'
//				&& tmpWikiText.charAt(i - 2) != '\n'
					) {
				if (tmpWikiText.charAt(i - 1) != '\t') {
					tmpWikiText = addNewLine(tmpWikiText, i);
				}

			}
		}
		return tmpWikiText;

	}


	private StringBuilder removeAllSpacesBeforeLinebreaks(StringBuilder tmpWikiText) {

		for (int i = 0; i < tmpWikiText.length(); i++) {
			if (tmpWikiText.charAt(i) == ' ' && tmpWikiText.charAt(i + 1) == '\n') {
				tmpWikiText.deleteCharAt(i);
			}
		}
		return tmpWikiText;
	}


	private StringBuilder addnewLinesBeforeAndAfterBrackets(StringBuilder tmpWikiText, int i) {


		boolean run = true;
		while (true) {
			if (tmpWikiText.charAt(i) == '{') {

				if (tmpWikiText.charAt(i + 2) != '\n') {
					tmpWikiText = addNewLine(tmpWikiText, i + 2);
				}
			}

			if (tmpWikiText.charAt(i) == '}') {
				if (tmpWikiText.charAt(i - 1) != '\n') {
					tmpWikiText = addNewLine(tmpWikiText, i);
					i++;
				}
				run = false;
				return tmpWikiText;
			}
			i++;
		}
	}

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

	private StringBuilder addWhitespacesBeforeAndAfterBrackets(StringBuilder tmpWikiText) {
		tmpWikiText = new StringBuilder(tmpWikiText.toString().replaceAll("\\{", " \\{ "));
		tmpWikiText = new StringBuilder(tmpWikiText.toString().replaceAll("\\}", " \\}"));
		tmpWikiText = new StringBuilder(tmpWikiText.toString().replaceAll("\\>", "\\> "));
		tmpWikiText = new StringBuilder(tmpWikiText.toString().replaceAll("\\<", " \\<"));
		return tmpWikiText;
	}

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

	private StringBuilder addNewLine(StringBuilder inputString, int position) {

		inputString.insert(position, "\n");
		return inputString;
	}

	private StringBuilder addTab(StringBuilder inputString, int position) {

		inputString.insert(position, "\t");
		return inputString;
	}

	private StringBuilder addWhitespace(StringBuilder inputString, int position) {

		inputString.insert(position, " ");
		return inputString;
	}


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

	private boolean checkLastChar(StringBuilder inputText, int position) {
		return inputText.length() - 1 <= position;
	}


}