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
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerNum;
import de.d3web.kernel.domainModel.answers.AnswerUnknown;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.utils.KnowWEUtils;

/**
 * Generates the HTML representation of a Question Object
 * 
 * @author Fabian Haupt
 */
public class FindingHTMLWriter {
	private static String MC = "MC";

	private static String OC = "OC";

	private static String NUM = "NUM";

	private static String YN = "YN";

	public static final String ID = FindingXMLWriter.class.getName();

	private static ResourceBundle rb;

	@SuppressWarnings("deprecation")
	private void appendCAnswers(Question theQuestion, StringBuffer buffy,
			XPSCase theCase, String namespace, String webname, String targetUrlPrefix) {
		QuestionChoice theQC = (QuestionChoice) theQuestion;
		String timestampid = (new Date()).getTime() + "";
		if (theQC.getAllAlternatives() != null) {
			buffy.append("<form action='javascript:void()' name='semanooc'"
					+ theQuestion.getId() + timestampid + "'>");
			
			for (AnswerChoice theAnswer : theQC.getAllAlternatives()) {
				String answerText = KnowWEUtils.convertUmlaut(theAnswer.verbalizeValue(theCase));
				buffy.append("&nbsp; ");
				String rqst= targetUrlPrefix+"?action=setFinding&namespace="+java.net.URLEncoder.encode(namespace) + "&ObjectID="
								+ theQuestion.getId();
												
				buffy.append("<INPUT TYPE=radio NAME='f" + timestampid+"id"
						+ theQuestion.getId() + "' value='"
						+ theAnswer.getId() + "' id='semanooc"
						+ theQuestion.getId() + "' ");

				if (theCase != null
						&& theQuestion.getValue(theCase)
								.contains(theAnswer)) {		
					buffy.append(" checked ");
				}
				
				buffy.append("onClick='readoc(&quot;" + rqst
						+ "&quot;,&quot;" + theQuestion.getId()
						+ "&quot;,&quot;" + timestampid + "&quot;)'");

				buffy.append(">"+answerText+"<br />");
			}
			String answerText = theQuestion.getUnknownAlternative().verbalizeValue(theCase);
			
			buffy.append("&nbsp; ");
			String rqst= targetUrlPrefix+"?action=setFinding&namespace="+java.net.URLEncoder.encode(namespace) + "&ObjectID="
							+ theQuestion.getId();
											
			buffy.append("<INPUT TYPE=radio NAME='f" + timestampid+"id"
					+ theQuestion.getId() + "' value='"
					+ theQuestion.getUnknownAlternative().getId() + "' id='semanooc"
					+ theQuestion.getId() + "' ");

			if (theCase != null
					&& theQuestion.getValue(theCase)
							.contains(theQuestion.getUnknownAlternative())) {		
				buffy.append(" checked ");
			}
			
			buffy.append("onClick='readoc(&quot;" + rqst
					+ "&quot;,&quot;" + theQuestion.getId()
					+ "&quot;,&quot;" + timestampid + "&quot;)'");

			buffy.append(">"+renderAnswerText(answerText)+"<br />");
		}
			
		
		
	}

	private String renderAnswerText(String answerText) {
		//not needed right now
		return answerText;
		
	}

	@SuppressWarnings("deprecation")
	private void appendNUMAnswers(Question theQuestion, StringBuffer buffy,
			XPSCase theCase, String namespace, String webname, String targetUrlPrefix) {
		String timestampid = (new Date()).getTime() + "";
		if (theCase != null && theQuestion.getValue(theCase).size() > 0) {
			AnswerNum answer = (AnswerNum) theQuestion.getValue(theCase).get(0);			
			if (answer != null) {
				String answerText = answer.verbalizeValue(theCase);
				String prefix=KnowWEEnvironment.getInstance().getPathPrefix();
				String rqst = prefix+(prefix.length()!=0?"/":"")+"KnowWE.jsp?action=setFinding&namespace="
						+ java.net.URLEncoder.encode(namespace)
						+ "&ObjectID="
						+ theQuestion.getId()
						+ "&KWikiWeb=" + webname;
				buffy.append("<INPUT TYPE=text size=10 maxlength=10 NAME='num"
						+ timestampid + theQuestion.getId() + "' value='"
						+ answer.getValue(theCase).toString() 
						+ "' ");

				buffy.append(">");
				buffy.append("<input type='button' name='submit' value='ok' ");
				buffy.append("onClick='readformnum(&quot;" + rqst
						+ "&quot;,&quot;num" + timestampid
						+ "&quot;,&quot;" + theQuestion.getId() + "&quot;)'");
				buffy.append(answerText);

				buffy.append("<br>");
			}
		} else {
			AnswerNum an = new AnswerNum();
			an.setQuestion(theQuestion);
			an.setValue(new Double(0));
			String answerText = an.verbalizeValue(theCase);
			String rqst = targetUrlPrefix+"?action=setFinding&namespace="
					+ namespace
					+ "&ObjectID="
					+ theQuestion.getId()
					+ "&KWikiWeb=" + webname;
			buffy
					.append("<INPUT TYPE=text size=10 maxlength=10 NAME='num"
							+ timestampid + theQuestion.getId() + "' value='"
							+"' ");
			buffy.append(">");
			buffy.append("<input type='button' name='submit' value='ok' ");
			buffy.append("onClick='readformnum(&quot;" + rqst
					+ "&quot;,&quot;num" + timestampid + "&quot;,&quot;"
					+ theQuestion.getId() + "&quot;)'");
			buffy.append(answerText);

			buffy.append("<br>");

		}

	}

	@SuppressWarnings("deprecation")
	private void appendMCAnswers(Question theQuestion, StringBuffer buffy,
			XPSCase theCase, String namespace, String webname, String targetUrlPrefix) {
		QuestionMC theMC = (QuestionMC) theQuestion;
		String timestampid = (new Date()).getTime() + "";

		
		
		if (theMC.getAllAlternatives() != null) {
	
			buffy.append("<form action='javascript:void()' name='semanomc'"
					+ theQuestion.getId() + timestampid + "'>");
			for (Answer theAnswer : theMC.getAllAlternatives()) {
				String answerText = theAnswer.verbalizeValue(theCase);
				if (theAnswer instanceof AnswerUnknown) {
					answerText = KnowWEUtils.convertUmlaut(rb.getString("KnowWE.answer.unknown"));
				} else {
					String rqst = targetUrlPrefix+"?action=setFinding&namespace="
							+ java.net.URLEncoder.encode(namespace)
							+ "&ObjectID="
							+ theQuestion.getId()
							+ "&ValueID="
							+ theAnswer.getId()
							+ "&KWikiWeb="+ webname;
					buffy.append("<INPUT TYPE=CHECKBOX NAME='f" + timestampid+"id"
							+ theQuestion.getId() + "' value='"
							+ theAnswer.getId() + "' id='semanomc"
							+ theQuestion.getId() + "' ");
					buffy.append("onClick='readform(&quot;" + rqst
							+ "&quot;,&quot;" + theQuestion.getId()
							+ "&quot;,&quot;" + timestampid + "&quot;)'");

					if (theCase != null
							&& theQuestion.getValue(theCase)
									.contains(theAnswer)) {		
						buffy.append(" checked");
					}
					buffy.append(">");
					buffy.append(renderAnswerText(KnowWEUtils.convertUmlaut(answerText)));
					buffy.append("<br>");
				}
			}
			buffy.append("</form>");
		}
	}
	private String getHTMLString(Question theQuestion, String type,
			XPSCase theCase, String namespace, String webname, String targetUrlPrefix) {
		StringBuffer buffy = new StringBuffer();

		buffy.append("<div class='semContents' >");
		buffy
				.append("<div align=left style='color:black;font-size:140%;padding:0.2em;margin-left:2px;"
						+ "margin-top:5px;margin-bottom:5px;' >");
		if (type.equalsIgnoreCase(MC)) {
			appendMCAnswers(theQuestion, buffy, theCase, namespace, webname, targetUrlPrefix);
		} else if (type.equalsIgnoreCase(OC) || type.equalsIgnoreCase(YN)) {
			appendCAnswers(theQuestion, buffy, theCase, namespace, webname, targetUrlPrefix);
		} else if (type.equalsIgnoreCase(NUM)) {
			appendNUMAnswers(theQuestion, buffy, theCase, namespace, webname, targetUrlPrefix);
		} else {
			Logger.getLogger(ID).warning("invalid question type");
		}
		buffy.append("</div>");
		buffy.append("</div >");
		return buffy.toString();
	}
	public String getHTMLString(Question question, XPSCase theCase,
			String namespace, String webname, String targetUrlPrefix) {
		
		rb = D3webModule.getKwikiBundle_d3web();
		String retVal = null;		
		if (question == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"null is no Question");	
		} else {
			retVal= KnowWEUtils.convertUmlaut(question.getText());
			if (question instanceof QuestionYN) {
				retVal += getHTMLString((QuestionChoice) question, "YN",
						theCase, namespace, webname,targetUrlPrefix);
			} else if (question instanceof QuestionOC) {
				retVal += getHTMLString((QuestionChoice) question, "OC",
						theCase, namespace, webname,targetUrlPrefix);
			} else if (question instanceof QuestionMC) {
				retVal += getHTMLString((QuestionChoice) question, "MC",
						theCase, namespace, webname,targetUrlPrefix);
			} else if (question instanceof QuestionNum) {
				retVal += getHTMLString((Question) question, "Num", theCase,
						namespace, webname,targetUrlPrefix);
			} /*
				 * else if (question instanceof QuestionText) { retVal =
				 * getXMLString((Question) question, "Text", theCase); } else if
				 * (question instanceof QuestionDate) { retVal =
				 * getXMLString((Question) question, "Date", theCase); }
				 */
		}
		String reencode=retVal;
		//TODO: reencoding shouldn't be necessary -> everything utf8
		if (reencode!=null){
		try {
			reencode =new String(retVal.getBytes("UTF-8"),"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		return reencode;
	}
}
