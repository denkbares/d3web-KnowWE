/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.quicki;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Render the quick interview -aka QuickI- in KnowWE --- HTML / JS / CSS based
 * 
 * @author Martina Freiberg
 * @created 15.07.2010
 */
public class QuickInterviewRenderer {

	private static String namespace = "";

	private static String web = "";

	private static Session session = null;

	private static KnowledgeBase kb = null;

	private static List<? extends QASet> inits = null;

	private static ResourceBundle rb;

	/**
	 * Assembles and returns the HTML representation of the interview.
	 * 
	 * @created 15.07.2010
	 * @param c the session
	 * @param web the web context
	 * @return the String representation of the interview
	 */
	public static String renderInterview(Session c, String webb, KnowWEUserContext user) {

		// Assembles the Interview
		StringBuffer buffi = new StringBuffer();

		// insert specific CSS
		// buffi.append("<link rel='stylesheet' type='text/css' href='KnowWEExtension/css/quicki.css' />");

		kb = c.getKnowledgeBase();
		session = c;
		web = webb;
		namespace = kb.getId();
		rb = D3webModule.getKwikiBundle_d3web(user);

		// Map all processed TerminologyObjects already in interview table,
		// avoids endless recursion in cyclic hierarchies
		Set<TerminologyObject> processedTOs = new HashSet<TerminologyObject>();

		// add plugin header
		getInterviewPluginHeader(buffi);

		// get all elements of InitQuestionnaire
		inits = kb.getInitQuestions();

		// call method for getting interview elements recursively
		// start with root QASet and go DFS strategy
		getInterviewElementsRenderingRecursively(kb.getRootQASet(), buffi, processedTOs, 0, true);

		// add pseudo element for correctly closing the plugin
		buffi.append("<div class='invisible'>  </div>");
		return buffi.toString();
	}

	/**
	 * Returns the Plugin Header As String
	 * 
	 * @created 15.07.2010
	 * @return the plugin header HTML String
	 */
	private static void getInterviewPluginHeader(StringBuffer html) {

		// assemble JS string
		String relAt = " rel=\"{"
				+ "web:'" + web + "', "
				+ "ns:'" + namespace + "'"
				+ "}\" ";

		html.append("<h3>");
		html.append("Quick Interview");
		html.append("<div id='quickireset' class='reset pointer' title='"
				+ rb.getString("KnowWE.quicki.reset") + "'"
				+ relAt
				+ "></div>\n");
		// html.append("<div class=''></div>");
		// html.append("<div class='qanswerunknown'></div>");
		html.append("</h3>\n");
	}

	/**
	 * Assembles the HTML representation of QContainers and Questions, starting
	 * from the root QASet of the KB, recursively, and writes them into the
	 * given StringBuffer
	 * 
	 * @created 14.07.2010
	 * @param topContainer the root container
	 * @param buffer the StringBuffer
	 * @param processedTOs already processed TerminologyObjects
	 * @param depth recursion depth; used to calculate identation
	 * @param init flag for signalling whether the processed element was in the
	 *        init questionnaire
	 */
	private static void getInterviewElementsRenderingRecursively(TerminologyObject topContainer,
			StringBuffer buffer, Set<TerminologyObject> processedTOs, int depth, boolean init) {

		// just do not display the rooty root
		if (!topContainer.getName().endsWith("Q000")) {

			// if already contained, get the already-defined rendering
			if (processedTOs.contains(topContainer)) {
				getAlreadyDefinedRendering(topContainer, buffer, depth++);
			}
			else {
				processedTOs.add(topContainer);

				// if not empty
				if (topContainer.getChildren() != null && topContainer.getChildren().length > 0) {
					getQuestionnaireRendering((QContainer) topContainer, depth, init, buffer);
				}
				// if empty, specific rendering
				else {
					getEmptyQuestionnaireRendering((QContainer) topContainer, depth, buffer, init);
				}
			}
		}

		// if init questionnaire: all contained elements are to be displayed
		String display = init ? "display: block;" : "display: none;";

		// group all following questionnaires/questions for easily hiding them
		// blockwise later
		buffer.append("\n<div id='group_" + topContainer.getId() + "' class='group' style='"
				+ display + "' >");

		depth++;

		// process all children, depending on element type branch into
		// corresponding recursion
		for (TerminologyObject qcontainerchild : topContainer.getChildren()) {

			init = inits.contains(qcontainerchild) ? true : false;

			if (qcontainerchild instanceof QContainer) {
				getInterviewElementsRenderingRecursively(
						qcontainerchild, buffer, processedTOs, depth, init);
			}
			else if (qcontainerchild instanceof Question) {
				getQuestionsRecursively((Question) qcontainerchild, buffer,
							processedTOs, depth, topContainer, init);
			}
		}
		buffer.append("</div>"); // close the grouping div
	}

	private static void getEmptyQuestionnaireRendering(QContainer container, int depth, StringBuffer buffi,
			boolean show) {
		int margin = 10 + depth * 10; // calculate identation

		String clazz = show // decide class for rendering expand-icon
				? "class='questionnaire pointDown'"
				: "class='questionnaire pointRight'";

		// if init questionnaire: all contained elements are to be displayed
		String display = show ? "display: block;" : "display: none;";

		buffi.append("<div id='" + container.getId() + "' " + clazz + " style='display: block;" +
				" margin-left: " + margin + "px;' >");
		buffi.append(container.getName());
		buffi.append("</div>");

		margin = margin + 30;
		buffi.append("\n<div id='group_" + container.getId() + "' class='group' style='"
				+ display + "' >");
		buffi.append("\n<div class='emptyQuestionnaire' " +
				"style='margin-left: " + margin + "px; display: block;' "
				+ ">No elements defined!</div>");
		buffi.append("</div>");

	}

	/**
	 * Recursively walks through the questions of the hierarchy and calls
	 * methods for appending their rendering
	 * 
	 * @created 17.08.2010
	 * @param topQuestion the root question
	 * @param sb the StringBuffer
	 * @param processedTOs already processed elements
	 * @param depth the recursion depth
	 * @param parent the parent element
	 * @param init flag for displaying whether processed element is contained in
	 *        an init questionnaire
	 */
	private static void getQuestionsRecursively(Question topQuestion, StringBuffer sb,
			Set<TerminologyObject> processedTOs, int depth, TerminologyObject parent, boolean init) {

		// if already contained in interview, get already-defined rendering and
		// return for
		// avoiding endless recursion
		if (processedTOs.contains(topQuestion)) {
			getAlreadyDefinedRendering(topQuestion, sb, depth);
			return;
		}

		getQABlockRendering(topQuestion, depth, parent, sb);
		processedTOs.add(topQuestion);

		if (topQuestion.getChildren().length > 0) {

			depth++;

			// we got follow-up questions, thus call recursively for children
			for (TerminologyObject qchild : topQuestion.getChildren()) {
				getQuestionsRecursively((Question) qchild, sb, processedTOs,
						depth, parent, init);
			}
		}
	}

	/**
	 * Assembles an own div for indicating questions/questionnaires that have
	 * already been answered
	 * 
	 * @created 26.08.2010
	 * @param element the element that was already been answered
	 * @param sb StringBuffer to append the div to
	 * @param depth indicator for the indentation depth
	 */
	private static void getAlreadyDefinedRendering(TerminologyObject element, StringBuffer sb, int depth) {

		int margin = 30 + depth * 10;
		sb.append("<div id='" + element.getId() + "' " +
				"class='alreadyDefined' style='margin-left: " + margin + "px; display: block'; >");
		sb.append(element.getName() + " is already defined!");
		sb.append("</div>");
	}

	/**
	 * Assembles the div that displays icon and name of questionnaires
	 * 
	 * @created 16.08.2010
	 * @param container the qcontainer to be rendered
	 * @param depth recursion depth
	 * @param show flag that indicates whether questionnaire is expanded (show)
	 *        or not; for appropriately displaying corresponding triangles
	 * @param buffi
	 * @return the HTML of a questionnaire div
	 */
	private static void getQuestionnaireRendering(QASet container, int depth, boolean show, StringBuffer buffi) {

		int margin = 10 + depth * 10; // calculate identation

		String clazz = show // decide class for rendering expand-icon
				? "class='questionnaire pointDown'"
				: "class='questionnaire pointRight'";

		buffi.append("<div id='" + container.getId() + "' " +
				clazz + " style='margin-left: " + margin + "px; display: block'; >");
		buffi.append(" " + container.getName() + " ");
		buffi.append("</div>");
	}

	/**
	 * Assembles the HTML-string representation for one QA-Block, that is, one
	 * question first, and the answers afterwards.
	 * 
	 * @created 20.07.2010
	 * @param q the question to be rendered
	 * @param depth the depth of the recursion - for calculating identation
	 * @param parent the parent element
	 * @param sb
	 * @return HTML-String representation for one QA-Block
	 */
	private static void getQABlockRendering(Question q, int depth,
			TerminologyObject parent, StringBuffer sb) {

		// calculate indentation depth & resulting width of the question display
		// 10 for standard margin and 30 for indenting further than the triangle
		int d = 30 + depth * 10;

		sb.append("\n<div class='qablock' id='qablock' style='display: block; margin-left: " + d
				+ "px;'>");

		// width of the question front section, i.e. total width - identation
		int w = 200 - d;

		// render the first cell displaying the Question in a separate div,
		// then call method for rendering a question's answers in another div
		sb.append("\n<div id='" + q.getId() + "' " +
				"parent='" + parent.getId() + "' " +
				"class='question' " +
				"style='width: " + w + "px; display: inline-block;' >"
				+ q.getName() + "</div>");

		// TODO Maybe render abstraction questions otherwise once I know how to
		// distinguish them
		// q.getProperties().getProperty(Property.ABSTRACTION_QUESTION);
		// if (isQuestionAbstraction(q)) {
		// renderChoicesAbstract(q);
		// } else
		if (q instanceof QuestionOC) {
			List<Choice> list = ((QuestionChoice) q).getAllAlternatives();
			renderOCChoiceAnswers(q, list, sb);
		}
		else if (q instanceof QuestionMC) {
			List<Choice> list = ((QuestionMC) q).getAlternatives();
			MultipleChoiceValue mcVal = MultipleChoiceValue.fromChoices(list);
			renderMCChoiceAnswers(q, mcVal, sb);
		}
		else if (q instanceof QuestionNum) {
			renderNumAnswers(q, sb);
		}

		else if (q instanceof QuestionDate) {
			renderDateAnswers(q, sb);
		}
		else if (q instanceof QuestionText) {
			renderTextAnswers(q, sb);
		}

		sb.append("</div>");
	}

	private static void renderTextAnswers(Question q, StringBuffer sb) {
		String value = "";

		// if answer has already been answered write value into the field
		if (UndefinedValue.isNotUndefinedValue(session.getBlackboard().getValue(q))) {
			Value answer = session.getBlackboard().getValue(q);

			if (answer != null && answer instanceof Unknown) {
				value = "";
			}
			else if (answer != null && answer instanceof TextValue) {
				value = ((TextValue) answer).toString();
			}
		}

		String id = q.getId();
		String jscall = "";
		// assemble the JS call
		try {
			jscall = " rel=\"{oid: '" + id + "', "
					+ "web:'" + web + "',"
					+ "ns:'" + namespace + "',"
					+ "type:'num', "
					+ "qtext:'" + URLEncoder.encode(q.getName(), "UTF-8") + "', "
					+ "}\" ";
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// assemble the input field
		sb.append("\n<input class='inputdate'  style='display: inline;' id='input_" + id
				+ "' type='text' "
				+ "value='" + value + "' "
				+ "size='18' "
				+ jscall + " />");
		// TODO: Really a date-ok class?
		sb.append("<input type='button' value='ok' class='date-ok' /> ");
		// "<div class='dateformatdesc'>()</div>");

		sb.append("\n<div class='answerseparator'></div>");
		renderAnswerUnknown(q, "num", sb);
	}

	/**
	 * Assembles the HTML for rendering a one choice question
	 * 
	 * @created 21.08.2010
	 * @param q the question
	 * @param list the list of possible choices
	 * @return the HTML representation of one choice questions
	 */
	private static void renderOCChoiceAnswers(Question q, List<Choice> list, StringBuffer sb) {

		// go through all choices = answer alternatives
		for (Choice choice : list) {

			String cssclass = "answer";

			// assemble JS string
			String jscall = " rel=\"{oid:'" + choice.getId() + "', "
					+ "web:'" + web + "', "
					+ "ns:'" + namespace + "', "
					+ "qid:'" + q.getId() + "', "
					+ "type:'oc'"
					+ "}\" ";

			String spanid = q.getId() + "_" + choice.getId();

			// if a value was already set, get the value and set corresponding
			// css class
			Value value = session.getBlackboard().getValue(q);

			if (value != null && UndefinedValue.isNotUndefinedValue(value)
					&& isAnsweredinCase(value, new ChoiceValue(choice))) {

				cssclass = "answerClicked";
			}

			sb.append(getEnclosingTagOnClick("div", "" + choice.getName() + " ",
					cssclass, jscall, null, spanid, ""));

			// System.out.println(getEnclosingTagOnClick("div", "" +
			// choice.getName() + " ",
			// cssclass, jscall, null, spanid));

			// for having a separator between answer alternatives (img, text...)
			sb.append("\n<div class='answerseparator'></div>");
		}

		renderAnswerUnknown(q, "oc", sb);
	}

	/**
	 * Assembles the HTML needed for displaying the (numerical) answer field
	 * 
	 * @created 20.07.2010
	 * @param q the question to which numerical answers are attached
	 * @return the String for rendering numerical answer field
	 */
	private static void renderNumAnswers(Question q, StringBuffer sb) {

		String value = "";

		// if answer has already been answered write value into the field
		if (UndefinedValue.isNotUndefinedValue(session.getBlackboard().getValue(q))) {
			Value answer = session.getBlackboard().getValue(q);

			if (answer != null && answer instanceof Unknown) {
				value = "";
			}
			else if (answer != null && answer instanceof NumValue) {
				value = answer.getValue().toString();
			}
		}

		String id = q.getId();
		String unit = "";
		Double rangeMax = Double.MAX_VALUE;
		Double rangeMin = Double.MIN_VALUE;

		Object rangeValue = q.getInfoStore().getValue(BasicProperties.QUESTION_NUM_RANGE);
		if (rangeValue != null) {
			NumericalInterval range = (NumericalInterval) rangeValue;
			rangeMax = range.getRight();
			rangeMin = range.getLeft();
		}
		Object questionUnit = q.getInfoStore().getValue(BasicProperties.UNIT);
		if (questionUnit != null) {
			unit = questionUnit.toString();
		}

		// assemble the JS call
		String jscall = "";
		try {
			if (rangeMin != Double.MIN_VALUE && rangeMax != Double.MAX_VALUE) {
				jscall = " rel=\"{oid: '" + id + "', "
						+ "web:'" + web + "',"
						+ "ns:'" + namespace + "',"
						+ "type:'num', "
						+ "rangeMin:'" + rangeMin + "', "
						+ "rangeMax:'" + rangeMax + "', "
						+ "qtext:'" + URLEncoder.encode(q.getName(), "UTF-8") + "', "
						+ "}\" ";
			}
			else {
				jscall = " rel=\"{oid: '" + id + "', "
						+ "web:'" + web + "',"
						+ "ns:'" + namespace + "',"
						+ "type:'num', "
						+ "rangeMin:'NaN', "
						+ "rangeMax:'NaN', "
						+ "qtext:'" + URLEncoder.encode(q.getName(), "UTF-8") + "', "
						+ "}\" ";
			}
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// assemble the input field
		sb.append("<input class='numinput'  style='display: inline;' id='input_" + id
				+ "' type='text' "
				+ "value='" + value + "' "
				+ "size='7' "
				+ jscall + " />");

		// print the units
		sb.append("<div class='unit'>" + unit + "</div>");

		// TODO: do not insert a newline here, breaks functionality
		sb.append("<input type='button' value='ok' class='num-ok' />");

		sb.append("<div class='answerseparator'></div>");

		renderAnswerUnknown(q, "num", sb);

		String errmsgid = q.getId() + "_errormsg";
		sb.append("<div id='" + errmsgid + "' class='invisible' ></div>");
	}

	// TODO: check Date input format
	/**
	 * Assembles the HTML representation of a date answer input
	 * 
	 * @created 01.09.2010
	 * @param q the date-question
	 * @param sb the String Buffer, the HTML is attached to
	 */
	private static void renderDateAnswers(Question q, StringBuffer sb) {

		String value = "";

		// if answer has already been answered write value into the field
		if (UndefinedValue.isNotUndefinedValue(session.getBlackboard().getValue(q))) {
			Value answer = session.getBlackboard().getValue(q);

			if (answer != null && answer instanceof Unknown) {
				value = "";
			}
			else if (answer != null && answer instanceof DateValue) {
				value = ((DateValue) answer).getDateString();
			}
		}

		if (value.equals("")) {
			value = "yyyy-mm-dd-hh-mm-ss";
		}

		String id = q.getId();

		// assemble the JS call
		String jscall = "";
		try {
			jscall = " rel=\"{oid: '" + id + "', "
					+ "web:'" + web + "',"
					+ "ns:'" + namespace + "',"
					+ "type:'num', "
					+ "qtext:'" + URLEncoder.encode(q.getName(), "UTF-8") + "', "
					+ "}\" ";
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// assemble the input field
		sb.append("<input class='inputdate'  style='display: inline;' id='input_" + id
				+ "' type='text' "
				+ "value='" + value + "' "
				+ "size='18' "
				+ jscall + " />");
		sb.append("<input type='button' value='ok' class='date-ok' /> ");
		// "<div class='dateformatdesc'>()</div>");

		sb.append("<div class='answerseparator'></div>");
		renderAnswerUnknown(q, "num", sb);
	}

	/**
	 * Creates the HTML needed for displaying the answer alternatives of mc
	 * choice answers.
	 * 
	 * @created 01.09.2010
	 * @param session
	 * @param q
	 * @param sb
	 * @param choices
	 * @param web
	 * @param namespace
	 * @return
	 */
	private static void renderMCChoiceAnswers(Question q, MultipleChoiceValue mcval, StringBuffer sb) {

		sb.append("\n<div class='answers' style='display: inline;'>");
		for (Choice choice : mcval.asChoiceList()) {

			String cssclass = "answerMC";
			String jscall = " rel=\"{oid:'" + choice.getId() + "', "
					+ "web:'" + web + "', "
					+ "ns:'" + namespace + "', "
					+ "qid:'" + q.getId() + "', "
					+ "type:'mc', "
					+ "}\" ";

			Value value = session.getBlackboard().getValue(q);
			if (value != null && UndefinedValue.isNotUndefinedValue(value)
					&& isAnsweredinCase(value, new ChoiceValue(choice))) {
				cssclass = "answerMCClicked";
			}
			String spanid = q.getId() + "_" + choice.getId();
			sb.append(getEnclosingTagOnClick("div", "" + choice.getName() + " ", cssclass,
					jscall, null, spanid, ""));
			sb.append("\n<div class='answerseparator'></div>");
		}

		// also render the unknown alternative for choice questions
		renderAnswerUnknown(q, "mc", sb);

		String jscall = " rel=\"{web:'" + web + "', "
				+ "ns:'" + namespace + "', "
				+ "qid:'" + q.getId() + "', "
				+ "type:'mc', "
				+ "}\" ";
		sb.append("<button type='button' class='MCButton' " + jscall + " >OK</button>");

		sb.append("</div>");
	}

	/**
	 * Assembles the HTML representation for rendering answer unknown
	 * 
	 * @created 22.07.2010
	 * @param web the web context
	 * @param namespace the namespace
	 * @param q the question, to which unknown is added
	 * @return the HTML representation
	 */
	private static void renderAnswerUnknown(Question q, String type, StringBuffer sb) {

		String jscall = " rel=\"{oid: '" + Unknown.getInstance().getId() + "', "
				+ "web:'" + web + "', "
				+ "ns:'" + namespace + "', "
				+ "type:'" + type + "', "
				+ "qid:'" + q.getId() + "'"
				+ "}\" ";
		String cssclass = "answerunknown";
		String spanid = q.getId() + "_" + Unknown.getInstance().getId();
		String title = "title=' " + rb.getString("KnowWE.quicki.unknown") + " '";
		sb.append(getEnclosingTagOnClick("div", "", cssclass, jscall, null, spanid, title));
	}

	/**
	 * Assembles the HTML representation for a given Tag
	 * 
	 * @created 22.07.2010
	 * @param tag The String representation of the tag
	 * @param text The text to be placed inside the tag
	 * @param cssclass The css class to style the resulting tag
	 * @param onclick The String representation of the onclick action, i.e., a
	 *        JS call
	 * @param onmouseover Something to happen regarding the onmouseover
	 * @param id The id of the object represented , i.e., answer alternative,
	 *        here
	 * @return String representation of the final tag. An example: <span rel=
	 *         "{oid:'MaU',web:'default_web',ns:'FAQ Devel..FAQ Devel_KB',qid:'Q2'}"
	 *         class="answercell" id="span_Q2_MaU" > MaU </span>
	 */
	private static String getEnclosingTagOnClick(String tag, String text,
			String cssclass, String onclick, String onmouseover, String id, String title) {
		StringBuffer sub = new StringBuffer();
		sub.append("<" + tag);
		if (id != null && id.length() > 0) {
			sub.append(" id='" + id + "' ");
		}
		if (cssclass != null && cssclass.length() > 0) {
			sub.append(" class='" + cssclass + "'");
		}
		if (onclick != null && onclick.length() > 0) {
			sub.append(" " + onclick + " ");
		}
		if (onmouseover != null && onmouseover.length() > 0) {
			sub.append(" " + onmouseover + " ");
		}
		if (title != null && title.length() > 0) {
			sub.append(" " + title + " ");
		}
		sub.append(">");
		sub.append(text);
		sub.append("</" + tag + ">");
		return sub.toString();
	}

	/**
	 * Checks, whether an answer value was already processed in the current
	 * session
	 * 
	 * @created 27.08.2010
	 * @param sessionValue the sessionValue
	 * @param value the value to be checked
	 * @return true if the given session value contains the checked value (MC
	 *         Questions) or if the session value equals the value
	 */
	private static boolean isAnsweredinCase(Value sessionValue, Value value) {
		// test for MC values separately
		if (sessionValue instanceof MultipleChoiceValue) {
			return ((MultipleChoiceValue) sessionValue).contains(value);
		}
		else {
			return sessionValue.equals(value);
		}
	}
}
