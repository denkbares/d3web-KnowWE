/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.textParser.Utils;

public class QuestionNotInKBError extends ConceptNotInKBError {

	public static final int TYPE_OC = 1;

	public static final int TYPE_MC = 2;

	public static final int TYPE_NUM = 3;

	public static final int TYPE_YN = 4;

	private boolean typeCasted = false;
	private int type = -1;

	public QuestionNotInKBError(String messageType, String messageText,
			String file, int lineNo, int columnNo, String line) {
		super(messageType, messageText, file, lineNo, columnNo, line);
	}

	public QuestionNotInKBError(String messageType, String messageText,
			String file, int lineNo, int columnNo, String line, int type) {
		super(messageType, messageText, file, lineNo, columnNo, line);
		this.type = type;
	}

	public QuestionNotInKBError(String messageType, String file, int lineNo,
			String line) {
		super(messageType, file, lineNo, line);
	}

	public QuestionNotInKBError(String s) {
		super(s);
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
		// if(o instanceof QuestionNotInKBError) {
		// QuestionNotInKBError err = (QuestionNotInKBError)o;
		// return this.objectName.equals(err.getObjectName());
		// }
		// return false;
	}

	public QuestionNotInKBError(String s, int k) {
		super(s);
		this.type = k;
	}

	public int getType() {
		return type;
	}

	public static final String[] YES_N_NOS = { "ja", "Ja", "JA", "yes", "Yes", "YES", "nein", "Nein",
			"NEIN", "no", "No", "NO" };
	
	public static boolean isYesOrNo(String firstAnswer) {
		
		for (String string : YES_N_NOS) {
			if (firstAnswer.trim().equalsIgnoreCase(string))
				return true;
		}
		return false;
	}

	public static String deleteQTag(String name) {
		for (String tag : QTAGS) {
			name = name.replaceAll("\\[" + tag + "\\]", "").trim();
		}
		return name;
	}

	public static String cutFollowingQTag(String question) {
		String trimmed = question.trim();
		for (int i = 0; i < QTAGS.length; i++) {
			String tag = "["+QTAGS[i]+"]";
			if(trimmed.endsWith(tag)) {
				return trimmed.substring(0, trimmed.length()-(QTAGS[i].length()+2));
			}
		}
		return trimmed;
	}
	
	public static int getFollowingQTag(String question) {
		String trimmed = question.trim();
		for (int i = 0; i < QTAGS.length; i++) {
			String tag = "["+QTAGS[i]+"]";
			if(trimmed.endsWith(tag)) {
				return scanForQTypeTag(tag);
			}
		}
		return -1;
	}
	
	
	public static boolean isNum(String firstAnswer) {
		if (isQuoted(firstAnswer)) {
			return false;
		}
		//return firstAnswer.contains("<") || firstAnswer.contains(">");
		String number = firstAnswer;
		number = number.replaceAll("=", "");
		number = number.replaceAll("<", "");
		number = number.replaceAll(">", "");
		try {
			Double.parseDouble(number);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public static boolean isValidNumber(String firstAnswer) {
		if (isQuoted(firstAnswer)) {
			return false;
		}
		boolean correct = true;
		try {
			Double.parseDouble(firstAnswer);
		} catch (Exception e) {
			correct = false;
		}
		return correct;
	}
	
	public static boolean isQTag(String str) {
		for (int i = 0; i < QTAGS.length; i++) {
			if(QTAGS[i].equals(str)) {
				return true;
			}
		}
		
		return false;
	}

	private static boolean isQuoted(String firstAnswer) {
		if (firstAnswer.trim().startsWith("\"")
				&& firstAnswer.trim().endsWith("\""))
			return true;
		return false;
	}

	public void setType(int type) {
		if(!typeCasted) {
			this.type = type;
		}
	}
	
	public void setTypeCasted(int type) {
		if(type != -1) {
		typeCasted = true;
		this.type = type;
		}
	}

	public static final String OC_UPPER = "OC";

	public static final String OC_LOWER = "oc";

	public static final String MC_UPPER = "MC";

	public static final String MC_LOWER = "mc";

	public static final String NUM_UPPER = "NUM";

	public static final String NUM_LOWER = "num";

	public static final String YN_UPPER = "YN";

	public static final String YN_LOWER = "yn";

	public static final String YN_UPPER_DE = "JN";

	public static final String YN_LOWER_DE = "jn";

	public static final String[] QTAGS = { OC_UPPER, OC_LOWER, MC_UPPER,
			MC_LOWER, NUM_UPPER, NUM_LOWER, YN_UPPER, YN_LOWER, YN_UPPER_DE,
			YN_LOWER_DE };

	public static int scanForQTypeTag(String q) {
		if (q.contains(OC_LOWER) || q.contains(OC_UPPER)) {
			return QuestionNotInKBError.TYPE_OC;
		}
		if (q.contains(MC_LOWER) || q.contains(MC_UPPER)) {
			return QuestionNotInKBError.TYPE_MC;
		}
		if (q.contains(NUM_LOWER) || q.contains(NUM_UPPER)) {
			return QuestionNotInKBError.TYPE_NUM;
		}
		if (q.contains(YN_LOWER) || q.contains(YN_UPPER)
				|| q.contains(YN_LOWER_DE) || q.contains(YN_UPPER_DE)) {
			return QuestionNotInKBError.TYPE_YN;
		}
		return -1;
	}
}
