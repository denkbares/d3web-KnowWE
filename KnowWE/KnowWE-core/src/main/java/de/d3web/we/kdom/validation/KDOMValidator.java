/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.kdom.validation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public class KDOMValidator {

	private static KDOMValidator fileHandlerInstance;

	private static KDOMValidator consoleInstance;

	private static KDOMValidator sectionzierInstance;

	private static KDOMValidator tagHandlerInstance;

	private final Logger logger;

	private StringBuilder builder = new StringBuilder();

	private final String name;

	private boolean logging = true;

	private boolean correcting = true;

	private boolean verbose = false;

	private boolean useBuilder = false;

	private boolean htmlAllowed = false;

	private static ResourceBundle rb;

	private int textLength = 50;

	private Level level;

	public int getTextLength() {
		return textLength;
	}

	public void setTextLength(int textLength) {
		this.textLength = textLength;
	}

	public boolean isLogging() {
		return logging;
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}

	public boolean isCorrecting() {
		return correcting;
	}

	public void setCorrecting(boolean correcting) {
		this.correcting = correcting;
	}

	public boolean isUseBuilder() {
		return useBuilder;
	}

	public void setUseBuilder(boolean useBuilder) {
		this.useBuilder = useBuilder;
	}

	public static ResourceBundle getResourceBundle() {
		if (rb == null) {
			rb = ResourceBundle.getBundle("KnowWE_config");
		}
		return rb;
	}

	private KDOMValidator(String type) {
		verbose = true;
		this.name = type + KDOMValidator.class.getName();
		this.logger = Logger.getLogger(name);
		this.setLevel(Level.ALL);

		getResourceBundle();

		if (type.equals("FileHandler")) {
			setCorrecting(true);
			setLogging(true);
			boolean logToConsole = false;
			try {
				FileHandler fh = new FileHandler(KnowWEEnvironment.getInstance().getContext()
						.getRealPath("") + rb.getString("validator.logFile"));
				fh.setFormatter(new ValidatorFormatter());

				logger.addHandler(fh);
				logger.setUseParentHandlers(false);
			}
			catch (Exception e) {
				logToConsole = true;
				log(Level.INFO,
						e.getClass().getSimpleName()
								+ " during Validator init, using default values.");
			}
			try {
				if (logToConsole || rb.getString("validator.logToConsole").contains("true")) {
					logger.addHandler(new ConsoleHandler());
				}
				else {
					logger.removeHandler(new ConsoleHandler());
				}
				if (rb.getString("validator.verbose").contains("true")) {
					verbose = true;
				}
				else {
					verbose = false;
				}

				if (rb.getString("validator.severeOnly").contains("true")) {
					log(Level.INFO,
							"Level is set to 'severe' -> check KnowWE_config to change");
					setLevel(Level.SEVERE);
				}
				else {
					setLevel(Level.ALL);
				}
			}
			catch (MissingResourceException e) {
				log(Level.INFO,
						e.getClass().getSimpleName()
								+ " during Validator init, using default values.");
			}
		}
		if (type.equals("Sectionizer")) {
			setLogging(false);
			setCorrecting(false);
		}
		if (type.equals("TagHandler")) {
			useBuilder = true;
			htmlAllowed = true;
		}

	}

	public static synchronized KDOMValidator getFileHandlerInstance() {
		if (fileHandlerInstance == null) {
			fileHandlerInstance = new KDOMValidator("FileHandler");
		}
		return fileHandlerInstance;
	}

	public static synchronized KDOMValidator getConsoleInstance() {
		if (consoleInstance == null) {
			consoleInstance = new KDOMValidator("Console");
		}
		return consoleInstance;
	}

	public static synchronized KDOMValidator getSectionizerInstance() {
		if (sectionzierInstance == null) {
			sectionzierInstance = new KDOMValidator("Sectionzier");
		}
		return sectionzierInstance;
	}

	public static synchronized KDOMValidator getTagHandlerInstance() {
		if (tagHandlerInstance == null) {
			tagHandlerInstance = new KDOMValidator("TagHandler");
		}
		return tagHandlerInstance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private void log(Level level, String msg) {
		if (logging) {
			if (useBuilder) {
				if (level.intValue() >= this.level.intValue()) {
					builder.append((htmlAllowed ? "<li>" : "") + msg + (htmlAllowed ? "</li>" : ""));
				}
			}
			else {
				logger.log(level, msg);
			}
		}
	}

	public void setLevel(Level level) {
		logger.setLevel(level);
		this.level = level;
	}

	public boolean validateArticle(KnowWEArticle art) {
		Level level = logger.getLevel();
		if (logging) {
			setLevel(Level.INFO);
			log(Level.INFO,
					"Starting to validate Article '" + art.getTitle() + "'" + (verbose ? "\n" : ""));
			setLevel(level);
		}

		boolean valid = validateSubTree(art.getSection());

		if (logging) {
			setLevel(Level.INFO);
			log(Level.INFO,
					"Finished validating Article '" + art.getTitle() + "'"
							+ (verbose ? "\n\n" : ""));
			setLevel(level);
		}
		return valid;
	}

	public boolean validateSubTree(Section root) {
		boolean valid = true;
		Set<String> IDs = new HashSet<String>();
		Set<String> duplicateIDs = new HashSet<String>();
		List<Section> nodes = new ArrayList<Section>();
		root.getAllNodesPreOrder(nodes);
		for (Section node : nodes) {
			if (IDs.contains(node.getID()) && !duplicateIDs.contains(node.getID())) {
				duplicateIDs.add(node.getID());
				valid = false;
			}
			else {
				IDs.add(node.getID());
			}
			if (!validateNode(node)) {
				valid = false;
			}
		}
		if (logging && !duplicateIDs.isEmpty()) {
			// Collections.sort(duplicateIDs);
			StringBuilder idString = new StringBuilder();
			int counter = 0;
			for (String id : duplicateIDs) {
				idString.append(id + " ");
				counter++;
				if (counter == 10) {
					idString.append("\n");
					counter = 0;
				}
			}
			log(Level.SEVERE,
					"The following IDs are not unique:\n" + idString + (verbose ? "\n" : ""));
		}

		return valid;
	}

	public boolean validateNode(Section node) {
		boolean valid = true;
		if (node == null) {
			log(Level.SEVERE,
					"Node is null" + (verbose ? "\n" : ""));
			return false;
		}

		if (node.getOriginalText() == null) {
			log(Level.SEVERE,
					"Text of node '"
							+ (htmlAllowed ? "<span title='" + node.getID() + "'>"
									+ node.getShortId()
									+ "</span>" : node.getID()) + "' is null"
							+ (verbose ? "\n" : ""));
			valid = false;
		}

		if (node.getObjectType() == null) {
			log(Level.SEVERE,
					"ObjectType of node '"
							+ (htmlAllowed ? "<span title='" + node.getID() + "'>"
									+ node.getShortId()
									+ "</span>" : node.getID()) + "' is null"
							+ (verbose ? "\n" : ""));
			valid = false;
		}

		if (node.getFather() == null
				&& !(node.getObjectType() != null
					&& node.getObjectType() instanceof KnowWEArticle)) {
			log(Level.WARNING,
					"Father of node '"
							+ (htmlAllowed ? "<span title='" + node.getID() + "'>"
									+ node.getShortId()
									+ "</span>" : node.getID()) + "' is null"
							+ (verbose ? "\n" : ""));
			valid = false;
		}

		// if ((node.getChildren() == null || node.getChildren().isEmpty())
		// && !(node.getObjectType() != null
		// && node.getObjectType() instanceof PlainText)) {
		// Logger.getLogger(name).log(Level.WARNING,
		// "Children of node '" + node.getShortId() + "' are null/empty" +
		// (verbose ? "\n" : ""));
		// valid = false;
		// }

		if (node.getOffSetFromFatherText() <= -1) {
			log(Level.WARNING,
					"OffSetFromFatherText of node '"
							+ (htmlAllowed ? "<span title='" + node.getID() + "'>"
									+ node.getShortId()
									+ "</span>" : node.getID()) + "' isn't set"
							+ (verbose ? "\n" : ""));
			if (correcting
					&& node.getFather() != null
					&& node.getFather().getOriginalText() != null
					&& containsOneOf(node.getFather().getOriginalText(), node.getOriginalText())) {
				node.setOffSetFromFatherText(node.getFather().getOriginalText().indexOf(
						node.getOriginalText()));
				log(Level.INFO,
						"Set OffSetFromFatherText in node '"
								+ (htmlAllowed ? "<span title='" + node.getID() + "'>"
										+ node.getShortId()
										+ "</span>" : node.getID())
								+ "' to " + node.getOffSetFromFatherText()
								+ (verbose ? "\nFather:\n<" + node.getFather().getOriginalText()
										+ ">\nNode:\n<" + node.getOriginalText() + ">\n" : ""));
			}
			else {
				valid = false;
			}
		}
		else if (node.getFather() != null
				&& node.getFather().getOriginalText() != null
				&& node.getOriginalText() != null
				&& node.getOffSetFromFatherText() < node.getFather().getOriginalText().length()
				&& node.getFather().getOriginalText()
						.substring(node.getOffSetFromFatherText()).length()
					>= node.getOriginalText().length()
				&& !node.getFather().getOriginalText()
						.substring(node.getOffSetFromFatherText())
						.startsWith(node.getOriginalText())) {

			log(Level.WARNING,
					"OffSetFromFatherText of node '"
							+ (htmlAllowed ? "<span title='" + node.getID() + "'>"
									+ node.getShortId()
									+ "</span>" : node.getID()) + "' doesn't fit"
							+ (verbose ? "\n" : ""));

			if (correcting
					&& containsOneOf(node.getFather().getOriginalText(), node.getOriginalText())) {
				int old = node.getOffSetFromFatherText();
				int offset = node.getFather().getOriginalText().indexOf(node.getOriginalText());
				boolean isStart = offset <= textLength;
				int beginIndex = isStart ? 0 : offset - textLength;
				boolean isEnd1 = offset + textLength > node.getFather().getOriginalText().length();
				int endIndex1 = isEnd1 ? node.getFather().getOriginalText().length() : offset
						+ textLength;
				boolean isEnd2 = textLength > node.getOriginalText().length();
				int endIndex2 = isEnd2 ? node.getOriginalText().length() : textLength;
				node.setOffSetFromFatherText(offset);
				log(Level.INFO,
						"Corrected OffSetFromFatherText in node '"
								+ (htmlAllowed ? "<span title='" + node.getID()
										+ "'>" + node.getShortId() + "</span>" : node.getID())
								+ "'. "
								+ "Old: "
								+ old
								+ ", New: "
								+ node.getOffSetFromFatherText()
								+ (verbose ? "\nFather:\n"
										+ (isStart ? "<" : "...")
										+ node.getFather().getOriginalText().substring(beginIndex,
												endIndex1)
										+ (isEnd1 ? ">" : "...") + "\nNode:\n<"
										+ node.getOriginalText().substring(0, endIndex2)
										+ (isEnd2 ? ">" : "...") + "\n" : ""));
			}
			else {
				valid = false;
			}
		}

		if (node.getChildren() != null && node.getChildren().size() > 0) {
			StringBuilder builder = new StringBuilder();
			List<Section> children = node.getChildren();
			for (Section child : children) {
				if (child != null) {
					if (child.getOriginalText() != null) {
						builder.append(child.getOriginalText());
					}
					if (child.getFather() == null || !child.getFather().equals(node)) {
						if (child.getFather() == null) {
							log(Level.WARNING,
									"Father of node '"
											+ (htmlAllowed ? "<span title='" + node.getID() + "'>"
													+ node.getShortId() + "</span>" : node.getID())
											+ "' is null" + (verbose ? "\n" : ""));
						}
						else {
							log(Level.WARNING,
									"Node '" + (htmlAllowed ? "<span title='" + node.getID() + "'>"
											+ node.getShortId() + "</span>" : node.getID())
											+ "' has wrong father" + (verbose ? "\n" : ""));
						}
						if (correcting
								&& containsOneOf(node.getOriginalText(), child.getOriginalText())) {
							// TODO: Sicher genug? Genauer?
							child.setFather(node);
							Logger.getLogger(name).log(
									Level.INFO,
									"Repaired missing/wrong link to father in node '"
											+ (htmlAllowed ? "<span title='" + node.getID() + "'>"
													+ node.getShortId()
													+ "</span>" : node.getID()) + "'"
											+ (verbose ? "\n" : ""));

							int offset = node.getOriginalText().indexOf(child.getOriginalText());
							boolean isStart = offset <= textLength;
							int beginIndex = isStart ? 0 : offset - textLength;
							boolean isEnd1 = offset + textLength > node.getOriginalText().length();
							int endIndex1 = isEnd1 ? node.getOriginalText().length() : offset
									+ textLength;
							boolean isEnd2 = textLength > child.getOriginalText().length();
							int endIndex2 = isEnd2 ? child.getOriginalText().length() : textLength;
							child.setOffSetFromFatherText(offset);
							log(Level.INFO,
									"Set OffSetFromFatherText in node '"
											+ (htmlAllowed ? "<span title='"
													+ node.getID() + "'>" + node.getShortId()
													+ "</span>" : node.getID())
											+ "' to "
											+ offset
											+ (verbose ? "\nFather:\n"
													+ (isStart ? "<" : "...")
													+ node.getOriginalText().substring(beginIndex,
															endIndex1)
													+ (isEnd1 ? ">" : "...")
													+ "\nNode:\n<"
													+ child.getOriginalText().substring(0,
															endIndex2)
													+ (isEnd2 ? ">" : "...") + "\n" : ""));
						}
						else {
							valid = false;
						}
					}
				}

			}
			if (!node.getOriginalText().equals(builder.toString())) {
				if (logging) {
					log(Level.SEVERE, "Text of children doesn't fit to text of node '"
							+ (htmlAllowed ? "<span title='" + node.getID() + "'>"
									+ node.getShortId()
									+ "</span>" : node.getID()) + "'");
					if (verbose) {
						int lDist = StringUtils.getLevenshteinDistance(node.getOriginalText(),
								builder.toString());
						log(Level.INFO, " Levenshtein distance = " + lDist);
						String a = node.getOriginalText();
						String b = builder.toString();
						if (a.length() > 500) {
							a = a.substring(0, 500) + "...";
						}
						if (b.length() > 500) {
							b = b.substring(0, 500) + "...";
						}
						a = a.replaceAll("\\r\\n", "\\\\r\\\\n");
						a = a.replaceAll("\\n", "\\\\n");
						b = b.replaceAll("\\r\\n", "\\\\r\\\\n");
						b = b.replaceAll("\\n", "\\\\n");
						log(Level.INFO, "Original:" + (htmlAllowed ? "<p/>" : "\n") + a
								+ (htmlAllowed ? "<p/>" : "\n")
								+ "Concatenation:" + (htmlAllowed ? "<p/>" : "\n") + b);
					}
				}
				valid = false;
			}
		}
		return valid;
	}

	private boolean containsOneOf(String a, String b) {
		int counter = 0;
		Matcher m = Pattern.compile(b, Pattern.LITERAL).matcher(a);
		while (m.find()) {
			counter++;
		}
		return counter == 1 ? true : false;
	}

	private class ValidatorFormatter extends Formatter {

		private final String lineSeparator = java.security.AccessController.doPrivileged(
				new sun.security.action.GetPropertyAction("line.separator"));

		/**
		 * Format the given LogRecord.
		 * 
		 * @param record the log record to be formatted.
		 * @return a formatted log record
		 */
		@Override
		public synchronized String format(LogRecord record) {
			StringBuffer sb = new StringBuffer();

			String message = formatMessage(record);
			sb.append(record.getLevel().getLocalizedName());
			sb.append(": ");
			sb.append(message);
			sb.append(lineSeparator);
			if (record.getThrown() != null) {
				try {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					pw.close();
					sb.append(sw.toString());
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return sb.toString();
		}

	}

	/**
	 * The StringBuilder only has content for the TagHandlerInstance...
	 */
	public StringBuilder getBuilder() {
		StringBuilder temp = new StringBuilder(this.builder);
		this.builder = new StringBuilder();
		return temp;
	}

	// private static String showDiff(String x, String y) {
	// int lookahead = 50;
	// StringBuffer output = new StringBuffer();
	// int[] offset = new int[2];
	// for (int i = 0; i < x.length() + offset[0] || i < y.length() + offset[1];
	// i++) {
	// if (i == x.length() + offset[0]) {
	// output.append("<span style='background: green'>" + y.substring(i,
	// y.length()) + "</span>");
	// break;
	// } else if (i == y.length() + offset[1]) {
	// output.append("<span style='background: green'>" + x.substring(i,
	// x.length()) + "</span>");
	// break;
	// }
	// if (x.charAt(i + offset[0]) != y.charAt(i + offset[1])) {
	// //int tempLookahead = (i + lookahead < maxlength ? i + lookahead : i +
	// (maxlength - i));
	// int[] temp = findNextMatch(x.substring(i, (i + lookahead < x.length() ? i
	// + lookahead : i + (x.length() - i))),
	// y.substring(i, (i + lookahead < y.length() ? i + lookahead : i +
	// (y.length() - i))));
	// if (temp[0] == 0 && temp[1] == 0) {
	// output.append("<span style='background: red'>NO FURTHER MATCHES IN THE NEXT "
	// + lookahead + " CHARS</span>");
	// break;
	// } else {
	// offset[0] += temp[0];
	// offset[1] += temp[1];
	// if (temp[0] == temp[1]) {
	// output.append("<span style='background: purple'>" + x.substring(i, i +
	// temp[0]) + "</span>");
	// } else if (temp[0] > 0) {
	// output.append("<span style='background: orange'>" + x.substring(i, i +
	// temp[0]) + "</span>");
	// } else if (temp[1] > 0) {
	// output.append("<span style='background: yellow'>" + y.substring(i, i +
	// temp[1]) + "</span>");
	// }
	// }
	// } else {
	// output.append(x.charAt(i));
	// }
	// }
	// return output.toString();
	// }
	//	
	// private static int[] findNextMatch(String x, String y) {
	//
	// for (int i = 1; i < x.length() || i < y.length(); i++) {
	// for (int j = 0; j < x.length() - i || j < y.length() - i; j++) {
	// if (x.substring(i, x.length() - j).indexOf(y.substring(i, y.length() -
	// j)) > -1) {
	// return new int[] {i, i};
	// } else if (y.indexOf(x.substring(i, x.length() - j)) > -1) {
	// return new int[] {i, 0};
	// } else if (x.indexOf(y.substring(i, y.length() - j)) > -1) {
	// return new int[] {0, i};
	// }
	// }
	// }
	// return new int[] {0, 0};
	// }
	//	
	// public static void main(String[] args) {
	// String test1 = "asnsf";
	//		
	// String test2 = "asmsf";
	//		
	// System.out.println(showDiff(test1, test2));
	// }
}
