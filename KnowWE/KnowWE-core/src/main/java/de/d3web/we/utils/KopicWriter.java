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

package de.d3web.we.utils;

public class KopicWriter {

	private StringBuffer textBuff = new StringBuffer();
	private static final String newLine = "\n";

	public KopicWriter() {
		textBuff.append(newLine);
		textBuff.append("<Kopic>");
		textBuff.append(newLine);
		textBuff.append(newLine);
	}

	public void appendSolutions(String text) {
		// TODO factor String out
		String tag = "Solutions-section";

		addContent(text, tag);

	}

	private void addContent(String text, String tag) {
		if (text == null || text.length() == 0) return;
		appendStartTag(tag);
		textBuff.append(text);
		appendEndTag(tag);
	}

	public void appendDecisionTable(String text) {
		// TODO factor String out
		String tag = "DecisionTableClassic-section";

		addContent(text, tag);
	}

	public void appendScoreTable(String text) {
		// TODO factor String out
		String tag = "DiagnosisScoreTable-section";

		addContent(text, tag);
	}

	public void appendCoveringTable(String text) {
		// TODO factor String out
		String tag = "SetCoveringTable-section";

		addContent(text, tag);
	}

	public void appendQuestions(String text) {
		// TODO factor String out
		String tag = "Questions-section";

		addContent(text, tag);
	}

	public void appendQuestionnaires(String text) {
		// TODO factor String out
		String tag = "Questionnaires-section";

		addContent(text, tag);
	}

	public void appendCoveringLists(String text) {
		// TODO factor String out
		String tag = "SetCoveringList-section";

		addContent(text, tag);
	}

	public void appendRules(String text) {
		// TODO factor String out
		String tag = "Rules-section";

		addContent(text, tag);
	}

	public void appendConfig(String text) {
		// TODO factor String out
		String tag = "KBconfig-section";

		addContent(text, tag);
	}

	private void appendStartTag(String tagName) {
		textBuff.append("<");
		textBuff.append(tagName);
		textBuff.append(">");
		textBuff.append(newLine);
		textBuff.append(newLine);
	}

	private void appendEndTag(String tagName) {
		textBuff.append(newLine);
		textBuff.append("</");
		textBuff.append(tagName);
		textBuff.append(">");
		textBuff.append(newLine);
		textBuff.append(newLine);
	}

	public String getKopicText() {
		textBuff.append(newLine);
		textBuff.append(newLine);
		textBuff.append("</Kopic>");
		textBuff.append(newLine);
		textBuff.append(newLine);

		return textBuff.toString();
	}

}
