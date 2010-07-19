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

package de.d3web.we.action;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.MMInfoSubject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.we.core.KnowWEAttributes;
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
	private void appendOCAnswers(Question theQuestion, StringBuffer buffy,
			Session session, String namespace, String webname, String topic, String targetUrlPrefix) {
		QuestionChoice theQC = (QuestionChoice) theQuestion;
		String timestampid = (new Date()).getTime() + "";

		String rel = "url : '" + targetUrlPrefix + "',"
				+ "namespace : '" + java.net.URLEncoder.encode(namespace) + "',"
				+ KnowWEAttributes.WEB + ": '" + webname + "',"
				+ KnowWEAttributes.TOPIC + ": '" + topic + "',";

		if (theQC.getAllAlternatives() != null) {
			buffy.append("<form action='#' name='semanooc'>");

			for (Choice choice : theQC.getAllAlternatives()) {
				String choiceText = KnowWEUtils.convertUmlaut(choice.getName().toString());
				Value choiceValue = new ChoiceValue(choice);
				Value sessionValue = session.getBlackboard().getValue(theQuestion);

				buffy.append("<INPUT TYPE='radio' NAME='f" + timestampid + "id"
						+ theQuestion.getId() + "' "
						+ "value='" + choice.getId() + "'"
						+ "id='semanooc" + theQuestion.getId() + "' "
						+ "rel=\"{" + rel
						+ "ObjectID : '" + theQuestion.getId() + "',"
						+ "ValueID : '" + choice.getId() + "'"
						+ "}\" ");

				if (session != null && sessionValue != UndefinedValue.getInstance()
						&& sessionValue.equals(choiceValue)) {
					buffy.append(" checked=\"checked\" ");
				}
				buffy.append("class='semano_oc'");
				buffy.append(">" + choiceText + "<br />");
			}

			// TODO: 04.2010 joba: add Unknown alternative
			// String answerText =
			// theQuestion.getUnknownAlternative().verbalizeValue(
			// session);
			//
			// buffy.append("<INPUT TYPE=radio NAME='f" + timestampid + "id"
			// + theQuestion.getId() + "'"
			// + " value='" + theQuestion.getUnknownAlternative().getId() + "'"
			// + " id='semanooc" + theQuestion.getId() + "' "
			// + "rel=\"{" + rel
			// + "ObjectID : '" + theQuestion.getId() + "',"
			// + "ValueID : '" + theQuestion.getUnknownAlternative().getId() +
			// "'"
			// + "}\" ");

			// if (session != null
			// && session.getValue(theQuestion) != UndefinedValue.getInstance()
			// && session.getValue(theQuestion).equals(
			// theQuestion.getUnknownAlternative())) {
			// buffy.append(" checked=\"checked\" ");
			// }
			// buffy.append("class='semano_oc'");

			// buffy.append(">" + renderAnswerText(answerText) + "<br />");
		}
	}

	private String renderAnswerText(String answerText) {
		// not needed right now
		return answerText;

	}

	@SuppressWarnings("deprecation")
	private void appendNUMAnswers(Question theQuestion, StringBuffer buffy,
			Session session, String namespace, String webname, String topic, String targetUrlPrefix) {
		String timestampid = (new Date()).getTime() + "";

		String rel = "url : '" + targetUrlPrefix + "',"
				+ "namespace : '" + java.net.URLEncoder.encode(namespace) + "',"
				+ KnowWEAttributes.WEB + ": '" + webname + "',"
				+ KnowWEAttributes.TOPIC + ": '" + topic + "',";

		Value sessionValue = session.getBlackboard().getValue(theQuestion);

		if (session != null && sessionValue != UndefinedValue.getInstance()) {
			if (sessionValue != UndefinedValue.getInstance()) {
				String valueText = sessionValue.getValue().toString();

				buffy.append("<INPUT TYPE=text size=10 maxlength=10 "
						+ "NAME='num" + timestampid + theQuestion.getId() + "' "
						+ "value='" + valueText + "' "
						+ "class=\"semano_num\""
						+ "rel=\"{" + rel
						+ "ObjectID : '" + theQuestion.getId() + "'"
						+ "}\" ");

				buffy.append(">");
				buffy.append("<input type='button' name='submit' value='ok' class=\"semano_ok\" "
						+ "rel=\"{" + rel
						+ "ObjectID : '" + theQuestion.getId() + "'"
						+ "}\" ");
				buffy.append(valueText);
				buffy.append("<br />");
			}
		}
		else {
			NumValue numValue = new NumValue(0.0);
			String valueText = numValue.getValue().toString();

			buffy.append("<INPUT TYPE=text size=10 maxlength=10 "
					+ "NAME='num" + timestampid + theQuestion.getId() + "' "
					+ "value='' "
					+ "class=\"semano_num\" "
					+ "rel=\"{" + rel
					+ "ObjectID : '" + theQuestion.getId() + "'"
					+ "}\" ");
			buffy.append(">");
			buffy.append("<input type='button' name='submit' value='ok' class=\"semano_ok\" "
					+ "rel=\"{" + rel
					+ "ObjectID : '" + theQuestion.getId() + "'"
					+ "}\" ");

			buffy.append(valueText);
			buffy.append("<br />");
		}
	}

	@SuppressWarnings("deprecation")
	private void appendMCAnswers(Question theQuestion, StringBuffer buffy,
			Session session, String namespace, String webname, String topic, String targetUrlPrefix) {
		QuestionMC theMC = (QuestionMC) theQuestion;
		String timestampid = (new Date()).getTime() + "";

		String rel = "url : '" + targetUrlPrefix + "',"
				+ "namespace : '" + java.net.URLEncoder.encode(namespace) + "',"
				+ KnowWEAttributes.WEB + ": '" + webname + "',"
				+ KnowWEAttributes.TOPIC + ": '" + topic + "',";

		if (theMC.getAllAlternatives() != null) {
			buffy.append("<form action='#' name='semanomc' id='semanomc'>");
			for (Choice choice : theMC.getAllAlternatives()) {
				ChoiceValue choiceValue = new ChoiceValue(choice);
				Value sessionValue = session.getBlackboard().getValue(theQuestion);
				// TODO: 04.2010 joba: insert Unknown alternative
				// if (theAnswer instanceof AnswerUnknown) {
				// answerText =
				// KnowWEUtils.convertUmlaut(rb.getString("KnowWE.answer.unknown"));
				// }
				// else {

				buffy.append("<INPUT TYPE=CHECKBOX NAME='f" + timestampid + "id"
						+ theQuestion.getId() + "' "
						+ "value='" + choice.getId() + "' "
						+ "id='semanomc" + theQuestion.getId() + "' "
						+ "class=\"semano_mc\" "
						+ " rel=\"{" + rel
						+ "ObjectID : '" + theQuestion.getId() + "',"
						+ "ValueIDS : '" + choice.getId() + "'"
						+ "}\" ");

				if (session != null && sessionValue != UndefinedValue.getInstance()
						&& containsValue((MultipleChoiceValue) sessionValue, choiceValue)) {
					buffy.append(" checked=\"checked\" ");
				}
				buffy.append(">");
				buffy.append(renderAnswerText(KnowWEUtils.convertUmlaut(choice.getName())));
				buffy.append("<br />");
				// }
			}
			buffy.append("</form>");
		}
	}

	private boolean containsValue(MultipleChoiceValue sessionValue, ChoiceValue choiceValue) {
		return sessionValue.contains(choiceValue);
	}

	private String getHTMLString(Question theQuestion, String type,
			Session session, String namespace, String webname, String topic, String targetUrlPrefix) {
		StringBuffer buffy = new StringBuffer();

		buffy.append("<div class=\"semContents\" >");
		buffy.append("<div class=\"questionsheet-layer\">");
		if (type.equalsIgnoreCase(MC)) {
			appendMCAnswers(theQuestion, buffy, session, namespace, webname, topic,
					targetUrlPrefix);
		}
		else if (type.equalsIgnoreCase(OC) || type.equalsIgnoreCase(YN)) {
			appendOCAnswers(theQuestion, buffy, session, namespace, webname, topic,
					targetUrlPrefix);
		}
		else if (type.equalsIgnoreCase(NUM)) {
			appendNUMAnswers(theQuestion, buffy, session, namespace, webname, topic,
					targetUrlPrefix);
		}
		else {
			Logger.getLogger(ID).warning("invalid question type");
		}
		buffy.append("</div>");
		buffy.append("</div>");
		return buffy.toString();
	}

	public static String getPrompt(Question q) {
		MMInfoStorage storage = (MMInfoStorage) q.getProperties()
				.getProperty(Property.MMINFO);
		if (storage != null) {
			DCMarkup dcMarkup = new DCMarkup();
			dcMarkup.setContent(DCElement.SOURCE, q.getId());
			dcMarkup.setContent(DCElement.SUBJECT, MMInfoSubject.PROMPT.getName());
			Set<MMInfoObject> info = storage.getMMInfo(dcMarkup);

			if (info != null) {
				Iterator<MMInfoObject> iter = info.iterator();
				while (iter.hasNext()) {
					return iter.next().getContent();
				}
			}
		}
		return null;
	}

	public String getHTMLString(Question question, Session session,
			String namespace, String webname, String topic, String targetUrlPrefix) {

		rb = D3webModule.getKwikiBundle_d3web();
		String retVal = null;
		if (question == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"null is no Question");
		}
		else {
			String questionText = question.getName();
			if (getPrompt(question) != null) {
				questionText = getPrompt(question);
			}
			retVal = "<h3>" + KnowWEUtils.convertUmlaut(questionText) + "</h3>";
			if (question instanceof QuestionYN) {
				retVal += getHTMLString(question, "YN",
						session, namespace, webname, topic, targetUrlPrefix);
			}
			else if (question instanceof QuestionOC) {
				retVal += getHTMLString(question, "OC",
						session, namespace, webname, topic, targetUrlPrefix);
			}
			else if (question instanceof QuestionMC) {
				retVal += getHTMLString(question, "MC",
						session, namespace, webname, topic, targetUrlPrefix);
			}
			else if (question instanceof QuestionNum) {
				retVal += getHTMLString(question, "Num", session,
						namespace, webname, topic, targetUrlPrefix);
			} /*
			 * else if (question instanceof QuestionText) { retVal =
			 * getXMLString((Question) question, "Text", session); } else if
			 * (question instanceof QuestionDate) { retVal =
			 * getXMLString((Question) question, "Date", session); }
			 */
		}
		String reencode = retVal;
		// TODO: reencoding shouldn't be necessary -> everything utf8
		if (reencode != null) {
			try {
				reencode = new String(retVal.getBytes("UTF-8"), "ISO-8859-1");
			}
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return reencode;
	}
}
