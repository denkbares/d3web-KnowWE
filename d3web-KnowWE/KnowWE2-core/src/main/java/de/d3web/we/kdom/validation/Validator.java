package de.d3web.we.kdom.validation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
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

public class Validator {
	
	private static Validator instance;
	
	private boolean verbose = false;
	
	private static ResourceBundle rb;
	
	private int textLength = 50;
	
	public int getTextLength() {
		return textLength;
	}

	public void setTextLength(int textLength) {
		this.textLength = textLength;
	}

	public static ResourceBundle getResourceBundle() {
		if (rb == null) {
			rb = ResourceBundle.getBundle("KnowWE_config");
		}
		return rb;
	}
	
	private Validator() {
		getResourceBundle();
		try {
			FileHandler fh = new FileHandler(KnowWEEnvironment.getInstance().getContext()
					.getRealPath("") + rb.getString("validator.logFile"));
			fh.setFormatter(new ValidatorFormatter());
			
			Logger.getLogger(Validator.class.getName()).addHandler(fh);
			Logger.getLogger(Validator.class.getName()).setUseParentHandlers(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			if (rb.getString("validator.logToConsole").contains("true")) {
				Logger.getLogger(Validator.class.getName()).addHandler(new ConsoleHandler());
			} else {
				Logger.getLogger(Validator.class.getName()).removeHandler(new ConsoleHandler());
			}
			if (rb.getString("validator.severeOnly").contains("true")) {
				Logger.getLogger(Validator.class.getName()).log(Level.INFO, 
					"Level is set to 'severe' -> check KnowWE_config to change");
				Logger.getLogger(Validator.class.getName()).setLevel(Level.SEVERE);
			} else {
				Logger.getLogger(Validator.class.getName()).setLevel(Level.ALL);
			}
			if (rb.getString("validator.verbose").contains("true")) {
				verbose = true;
			} else {
				verbose = false;
			}
		} catch (MissingResourceException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized Validator getInstance() {
		if (instance == null) {
			instance = new Validator();
		}
		return instance;
	}
	
	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
	    throw new CloneNotSupportedException(); 	   
	}
	
	public boolean validateArticle(KnowWEArticle art) {
		Level level = Logger.getLogger(Validator.class.getName()).getLevel();
		Logger.getLogger(Validator.class.getName()).setLevel(Level.INFO);
		Logger.getLogger(Validator.class.getName()).log(Level.INFO, 
			"Starting to validate Article '" + art.getTitle() + "'"  + (verbose ? "\n" : ""));
		Logger.getLogger(Validator.class.getName()).setLevel(level);
		
		boolean valid = validateSubTree(art.getSection());
		
		Logger.getLogger(Validator.class.getName()).setLevel(Level.INFO);
		Logger.getLogger(Validator.class.getName()).log(Level.INFO, 
			"Finished validating Article '" + art.getTitle() + "'"  + (verbose ? "\n\n" : ""));
		Logger.getLogger(Validator.class.getName()).setLevel(level);
		return valid;
	}
		 
	public boolean validateSubTree(Section root) {
		boolean valid = true;
		List<String> IDs = new ArrayList<String>();
		List<String> duplicateIDs = new ArrayList<String>();
		for (Section node:root.getAllNodesPreOrder()) {
			if (IDs.contains(node.getId()) && !duplicateIDs.contains(node.getId())) {
				duplicateIDs.add(node.getId());
				valid = false;
			} else {
				IDs.add(node.getId());
			}
			if (!validateNode(node)) {
				valid = false;
			}
		}
		if (!duplicateIDs.isEmpty()) {
			//Collections.sort(duplicateIDs);
			StringBuilder idString = new StringBuilder();
			int counter = 0;
			for (String id:duplicateIDs) {
				idString.append(id + " ");
				counter++;
				if (counter == 10) {
					idString.append("\n");
					counter = 0;
				}
			}
			Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, 
				"The following IDs are not unique:\n" + idString  + (verbose ? "\n" : ""));
		}
		
		return valid;
	}
	
	public boolean validateNode(Section node) {
		boolean valid = true;
		if (node == null) {
			Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, 
					"Node is null" + (verbose ? "\n" : ""));
			return false;
		}
		
		if (node.getOriginalText() == null) {
			Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, 
					"Text of node '" + node.getId() + "' is null" + (verbose ? "\n" : ""));
			valid = false;
		}
		
		if (node.getObjectType() == null) {
			Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, 
					"ObjectType of node '" + node.getId() + "' is null" + (verbose ? "\n" : ""));
			valid = false;
		} 
		
		if (node.getFather() == null
				&& !(node.getObjectType() != null 
					&& node.getObjectType() instanceof KnowWEArticle)) {
			Logger.getLogger(Validator.class.getName()).log(Level.WARNING, 
					"Father of node '" + node.getId() + "' is null" + (verbose ? "\n" : ""));
			valid = false;
		}
		
//		if ((node.getChildren() == null || node.getChildren().isEmpty())
//				&& !(node.getObjectType() != null 
//					&& node.getObjectType() instanceof PlainText)) {
//			Logger.getLogger(Validator.class.getName()).log(Level.WARNING, 
//					"Children of node '" + node.getId() + "' are null/empty" + (verbose ? "\n" : ""));
//			valid = false;
//		}
		
		if (node.getOffSetFromFatherText() <= -1) {
			Logger.getLogger(Validator.class.getName()).log(Level.WARNING, 
					"OffSetFromFatherText of node '" + node.getId() + "' isn't set" + (verbose ? "\n" : ""));
			if (node.getFather() != null
					&& node.getFather().getOriginalText() != null
					&& containsOneOf(node.getFather().getOriginalText(), node.getOriginalText())) {
				node.setOffSetFromFatherText(node.getFather().getOriginalText().indexOf(node.getOriginalText()));
				Logger.getLogger(Validator.class.getName()).log(Level.INFO, 
						"Set OffSetFromFatherText in node '" + node.getId() 
						+ "' to " + node.getOffSetFromFatherText()   
						+ (verbose ? "\nFather:\n<" + node.getFather().getOriginalText() 
								+ ">\nNode:\n<" + node.getOriginalText() + ">\n" : ""));
			} else {
				valid = false;
			}
		} else if (node.getFather() != null 
				&& node.getFather().getOriginalText() != null
				&& node.getOriginalText() != null
				&& node.getOffSetFromFatherText() < node.getFather().getOriginalText().length()
				&& node.getFather().getOriginalText()
					.substring(node.getOffSetFromFatherText()).length() 
					>= node.getOriginalText().length()
				&& !node.getFather().getOriginalText()
					.substring(node.getOffSetFromFatherText())
					.startsWith(node.getOriginalText())) {
			
			Logger.getLogger(Validator.class.getName()).log(Level.WARNING, 
					"OffSetFromFatherText of node '" + node.getId() + "' doesn't fit" + (verbose ? "\n" : ""));
			int old = node.getOffSetFromFatherText();
			if (containsOneOf(node.getFather().getOriginalText(), node.getOriginalText())) {
				
				int offset = node.getFather().getOriginalText().indexOf(node.getOriginalText());
				boolean isStart = offset <= textLength;
				int beginIndex = isStart ? 0 : offset - textLength;
				boolean isEnd1 = offset + textLength > node.getFather().getOriginalText().length();
				int endIndex1 = isEnd1 ? node.getFather().getOriginalText().length() : offset + textLength;
				boolean isEnd2 = textLength > node.getOriginalText().length() ;
				int endIndex2 = isEnd2 ? node.getOriginalText().length() : textLength;
				node.setOffSetFromFatherText(offset);
				Logger.getLogger(Validator.class.getName()).log(Level.INFO, 
						"Corrected OffSetFromFatherText in node '" + node.getId() + "'. "
						+ "Old: " + old + ", New: " + node.getOffSetFromFatherText()  
						+ (verbose ? "\nFather:\n" + (isStart ? "<" : "...") 
								+ node.getFather().getOriginalText().substring(beginIndex, endIndex1) 
								+ (isEnd1 ? ">" : "...") + "\nNode:\n<" 
								+ node.getOriginalText().substring(0, endIndex2) 
								+ (isEnd2 ? ">" : "...") + "\n" : ""));
			} else {
				valid = false;
			}
		}
		
		if (node.getChildren() != null && node.getChildren().size() > 0) {
			StringBuilder builder = new StringBuilder();
			for (Section child:node.getChildren()) {
				if (child != null) {
					if (child.getOriginalText() != null) {
						builder.append(child.getOriginalText());
					}
					if (child.getFather() == null || !child.getFather().equals(node)) {
						if (child.getFather() == null) {
							Logger.getLogger(Validator.class.getName()).log(Level.WARNING, 
									"Father of node '" + child.getId() + "' is null" + (verbose ? "\n" : ""));
						} else {
							Logger.getLogger(Validator.class.getName()).log(Level.WARNING, 
									"Node '" + child.getId() + "' has wrong father" + (verbose ? "\n" : ""));
						}
						if (containsOneOf(node.getOriginalText(), child.getOriginalText())) {
							//TODO: Sicher genug? Genauer?
							child.setFather(node);
							Logger.getLogger(Validator.class.getName()).log(Level.INFO, 
									"Repaired missing/wrong link to father in node '" + child.getId() + "'"  + (verbose ? "\n" : "")); 
							
							int offset = node.getOriginalText().indexOf(child.getOriginalText());
							boolean isStart = offset <= textLength;
							int beginIndex = isStart ? 0 : offset - textLength;
							boolean isEnd1 = offset + textLength > node.getOriginalText().length();
							int endIndex1 = isEnd1 ? node.getOriginalText().length() : offset + textLength;
							boolean isEnd2 = textLength > child.getOriginalText().length() ;
							int endIndex2 = isEnd2 ? child.getOriginalText().length() : textLength;
							child.setOffSetFromFatherText(offset);
							Logger.getLogger(Validator.class.getName()).log(Level.INFO, 
									"Set OffSetFromFatherText in node '" + child.getId() 
									+ "' to " + offset  
									+ (verbose ? "\nFather:\n" + (isStart ? "<" : "...") 
											+ node.getOriginalText().substring(beginIndex, endIndex1) 
											+ (isEnd1 ? ">" : "...") + "\nNode:\n<" + child.getOriginalText().substring(0, endIndex2) 
											+ (isEnd2 ? ">" : "...") + "\n" : ""));
						} else {
							valid = false;
						}
					}
				}
				
			}
			int lDist = StringUtils.getLevenshteinDistance(node.getOriginalText(), builder.toString());
			if (lDist != 0) {
				Logger.getLogger(Validator.class.getName()).log(Level.SEVERE, 
						"Text of children doesn't fit to text of node '" + node.getId() + "'" + (verbose ? 
						"\nLevenshtein distance = " + lDist 
						+ "\nOriginal:\n<" + node.getOriginalText() + ">\n"
						+ "Concatenation:\n<" + builder.toString() + ">\n"
						: ""));
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

	    private String lineSeparator = (String) java.security.AccessController.doPrivileged(
	            new sun.security.action.GetPropertyAction("line.separator"));

	    /**
	     * Format the given LogRecord.
	     * @param record the log record to be formatted.
	     * @return a formatted log record
	     */
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
			    } catch (Exception ex) {
			    	ex.printStackTrace();
			    }
			}
			return sb.toString();
	    }

	}
	
}
