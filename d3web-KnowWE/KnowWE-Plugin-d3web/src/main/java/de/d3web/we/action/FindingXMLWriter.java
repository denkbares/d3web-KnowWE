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

package de.d3web.we.action;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.session.XPSCase;
import de.d3web.core.session.values.AnswerChoice;
import de.d3web.core.session.values.AnswerNo;
import de.d3web.core.session.values.AnswerNum;
import de.d3web.core.session.values.AnswerUnknown;
import de.d3web.core.session.values.AnswerYes;
import de.d3web.we.d3webModule.D3webModule;

/**
 * Generates the XML representation of a Question Object
 * @author Michael Scharvogel
 */
public class FindingXMLWriter {
	
	public static final String ID = FindingXMLWriter.class.getName();

	private static ResourceBundle rb;
	
	
	private void appendAnswers(Question theQuestion, StringBuffer sb, XPSCase theCase) {
		sb.append("<Answers>\n");
		if (theQuestion instanceof QuestionChoice) {
			QuestionChoice theQC = (QuestionChoice) theQuestion;
			if (theQC.getAllAlternatives() != null) {
				for (AnswerChoice each : theQC.getAllAlternatives()) {
					appendAnswer(theQuestion, sb, each, theCase);
				}
				appendAnswer(theQuestion, sb, theQuestion.getUnknownAlternative(), theCase);
			}
		}
		if (theQuestion instanceof QuestionNum) {
			if(theCase != null) {
				AnswerNum answer = (AnswerNum) theQuestion.getValue(theCase);
				if(answer != null) {
					appendAnswer(theQuestion, sb, answer, theCase);
				}
			} else {
				AnswerNum an = new AnswerNum();
				an.setQuestion(theQuestion);
				an.setValue(new Double(0));
				appendAnswer(theQuestion, sb, an, theCase);
			}
		}
		sb.append("</Answers>\n");
	}

	private void appendAnswer(Question theQuestion, StringBuffer sb, Answer theAnswer, XPSCase theCase) {
		
		sb.append("<Answer ID='" + theAnswer.getId() + "'");
		if (theAnswer instanceof AnswerNo) {
			sb.append(" type='AnswerNo'");
		} else if (theAnswer instanceof AnswerYes) {
			sb.append(" type='AnswerYes'");
		} else if(theAnswer instanceof AnswerUnknown){
			sb.append(" type='AnswerUnknown'");
		} else if(theAnswer instanceof AnswerNum){
			sb.append(" type='AnswerNum'");
		} else {
			sb.append(" type='AnswerChoice'");
		} 
		if(theCase != null && theQuestion.getValue(theCase).equals(theAnswer)) {
			sb.append(" active='true'");
		}
		sb.append(">\n");
		String answerText = theAnswer.verbalizeValue(theCase);
		if(theAnswer instanceof AnswerUnknown) {
			answerText = rb.getString("KnowWE.answer.unknown");
		}
		String text = "";
		try {
			text = URLEncoder.encode(answerText, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
		}
		sb.append("<Text><![CDATA[" + text + "]]></Text>\n");
		sb.append("</Answer>\n");
	}

	private String getXMLString(Question theQuestion, String type, XPSCase theCase) {
		StringBuffer sb = new StringBuffer();
		String questionID = theQuestion.getId();
		sb.append(
			"<Question ID='"
				+ questionID
				+ "' type='"
				+ type +"'");
		sb.append( ">\n");
		String text = "";
		try {
			text = URLEncoder.encode(theQuestion.getText(), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
		}
		sb.append("<Text><![CDATA["	+ text + "]]></Text>\n");
		appendAnswers(theQuestion, sb, theCase);
		
		sb.append("</Question>\n");
		return sb.toString();
	}

	public String getXMLString(Question question, XPSCase theCase) {
		
		rb = D3webModule.getKwikiBundle_d3web();
		String retVal = null;
		if (question == null) {
			Logger.getLogger(this.getClass().getName()).warning("null is no Question");
		} else {
			if (question instanceof QuestionYN) {
				retVal = getXMLString(question, "YN", theCase);
			} else if (question instanceof QuestionOC) {
				retVal = getXMLString(question, "OC", theCase);
			} else if (question instanceof QuestionMC) {
				retVal = getXMLString(question, "MC", theCase);
			} else if (question instanceof QuestionNum) {
				retVal = getXMLString(question, "Num", theCase);
			} /*else if (question instanceof QuestionText) {
				retVal = getXMLString((Question) question, "Text", theCase);
			} else if (question instanceof QuestionDate) {
				retVal = getXMLString((Question) question, "Date", theCase);
			}*/
		}
		return retVal;
	}
}