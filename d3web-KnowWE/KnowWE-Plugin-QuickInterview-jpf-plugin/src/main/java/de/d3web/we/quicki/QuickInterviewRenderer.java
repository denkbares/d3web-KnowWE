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

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.core.session.values.Unknown;

/**
 * Render the quick interview -aka QuickI- in KnowWE --- HTML / JS / CSS based
 * 
 * @author Martina Freiberg
 * @created 15.07.2010
 */
public class QuickInterviewRenderer {

	/**
	 * Assembles and returns the HTML representation of the interview.
	 * 
	 * @created 15.07.2010
	 * @param c
	 * @param web
	 * @return the String representation of the interview
	 */
	public static String renderInterview(Session c, String web) {

		// Assembles the Interview
		StringBuffer buffi = new StringBuffer();
		KnowledgeBase b = c.getKnowledgeBase();

		// Get the qcontainers for the current session / knowledge base
		List<QContainer> containers = setupKBAndGetInterviewElements(b, c);

		// Map all processed TerminologyObjects already in interview table,
		// avoids endless recursion in cyclic hierarchies
		Set<TerminologyObject> processedTOs = new HashSet<TerminologyObject>();

		// add plugin header
		buffi.append(getInterviewPluginHeader());

		buffi.append(getInterviewElmentsHTML(containers, processedTOs, c, web, b));

		return buffi.toString();
	}

	/**
	 * Gets the knowledge base of the current session and fills the QContainer
	 * list with the QContainers of that session, thereby orders them according
	 * to DFS strategy of the KnowledgeBaseManagement.
	 * 
	 * @created 15.07.2010
	 */
	private static List<QContainer> setupKBAndGetInterviewElements(KnowledgeBase b, Session c) {

		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(b);

		// get all qcontainers of kb into a list
		List<QContainer> containers = b.getQContainers();

		// sort according to DFS as implemented in KnowledgeBaseManagement
		kbm.sortQContainers(containers);

		return containers;
	}

	/**
	 * Returns the Plugin Header As String
	 * 
	 * @created 15.07.2010
	 * @return
	 */
	private static String getInterviewPluginHeader() {
		StringBuffer html = new StringBuffer();
		html.append("<h3>");
		html.append("Quick Interview");
		html.append("</h3>");
		return html.toString();
	}

	/**
	 * Create the HTML that appends all interview elements to the HTML displayed
	 * in the plugin underneath the header for the actual interview
	 * 
	 * @param web, b
	 * @param c
	 * 
	 * @created 20.07.2010
	 * @return the HTML String of all interview elements
	 */
	private static String getInterviewElmentsHTML(
			List<QContainer> containers, Set<TerminologyObject> processedTOs, Session c, String web, KnowledgeBase b) {
		StringBuffer html = new StringBuffer();
		boolean even = true;
		boolean first = true;
		boolean empty = false;

		// go through all qcontainers of the knowledge base
		for (QContainer container : containers) {

			processedTOs.add(container); // add to set of processed TOs

			// skip the topmost (sometimes default) root element "Q000"
			if (container.getName().endsWith("Q000")) continue;

			String displayClass = first ? "class='visible'" : "class='hidden'";
			first = false;
			empty = (container.getNumberOfChildren() == 0) ? true : false;

			// in case first questionnaire is empty --> do not display expandend
			if (first && empty) displayClass = "class='hidden'";

			// get appropriate header for the questionnaire within the interview
			html.append(getQContainerTableHeader(displayClass, container));

			// for each question within questionnaire get one table row that
			// displays its text and the answer alternatives
			for (TerminologyObject to : container.getChildren()) {

				// only continue if found Terminology is a Question
				// TODO: what if child is a questionnaire as well?
				Question q = null;
				if (!(to instanceof Question)) {
					continue;
				}
				q = (Question) to;

				// assigns css classes according to whether line is
				// even/odd, afterwards toggle value
				String trClass = even ? "class='trEven'" : "class='trOdd'";
				even = even ? false : true;

				// if question has further children (follow-up questions)
				// add corresponding classifier for classname
				if (q.getChildren().length != 0) {
					trClass = trClass.replace("'", "");
					trClass = trClass.concat("Follow'");
				}
				html.append(getTableRowString(c, q, web, b.getId(), trClass));

			}

			// close the QContainerTable
			html.append(getQContainerTableFooter());
		}
		return html.toString();
	}

	/**
	 * Get the header (opening and heading elements) for the interview tables
	 * 
	 * @created 20.07.2010
	 * @return the interview table header
	 */
	private static String getQContainerTableHeader(String displayClass, QContainer container) {
		StringBuffer html = new StringBuffer();
		html.append("<div id='containerHeader_" + container.getId() + "' class='containerHeader'>");
		html.append(" " + container.getName() + ": ");
		html.append("</div>");
		html.append("<table id='containerTable_" + container.getId() + "' " + displayClass + ">");
		return html.toString();
	}

	/**
	 * Get the footer (closing elements) for the interview tables
	 * 
	 * @created 20.07.2010
	 * @return the interview table footer
	 */
	private static String getQContainerTableFooter() {
		return "</table>";
	}

	/**
	 * Returns the string representation for one row of a Questionnaire table.
	 * 
	 * @created 20.07.2010
	 * @param c
	 * @param q
	 * @param web
	 * @param namespace
	 * @param trClass
	 * @return String representation for one row, i.e., a question and its set
	 *         of answers
	 */
	private static String getTableRowString(Session c, Question q, String web,
			String namespace, String trClass) {

		StringBuffer html = new StringBuffer();
		html.append("<tr " + trClass + ">");

		if (!trClass.contains("Follow")) {

			// render the first cell displaying the Question, afterwards
			// call method for rendering a question's answers
			html.append("<td><div id='question_" + q.getId() + "' class='questioncell'>"
					+ q.getName()
					+ " ");

			if (q instanceof QuestionChoice) {
				List<Choice> list = ((QuestionChoice) q).getAllAlternatives();
				html.append(renderChoiceAnswers(c, q, list, web, namespace));
			}

			else {
				html.append(renderNumAnswers(c, q, web, namespace, trClass));
			}
		}
		else {

		}

		html.append("</div></td></tr> \n");
		return html.toString();
	}

	/**
	 * Generates the HTML needed for displaying the (numerical) answers and for
	 * integrating necessary JS calls for interactivity.
	 * 
	 * @created 20.07.2010
	 * @param c
	 * @param q
	 * @param web
	 * @param namespace
	 * @return the String for rendering numerical answers and corresponding JS
	 */
	private static String renderNumAnswers(Session c, Question q, String web, String namespace, String visib) {

		StringBuffer html = new StringBuffer();
		String value = "";

		// if answer has already been answered
		if (UndefinedValue.isNotUndefinedValue(c.getBlackboard().getValue(q))) {
			Value answer = c.getBlackboard().getValue(q);
			if (answer != null && answer instanceof NumValue) {
				value = answer.getValue().toString();
			}
		}

		String id = "answerNum_" + q.getId();

		// append the JS call
		String jscall = " rel=\"{oid: '" + q.getId() + "', "
				+ "web:'" + web + "',"
				+ "ns:'" + namespace + "',"
				+ "qtext:'" + URLEncoder.encode(q.getName()) + "', "
				+ "inputid:'" + id + "'"
				+ "}\" ";

		// append the input, if available with already provided input
		html.append("<input id='" + id + "' type='text' "
				+ "value='" + value + "' "
				+ "class='numInput num-cell-down " + visib + "' "
				+ "size='7' "
				+ jscall + ">");
		html.append("<input type='button' value='ok' class=\"num-cell-ok\">");

		return html.toString();
	}

	/**
	 * Creates the HTML needed for displaying the answer alternatives of choice
	 * answers.
	 * 
	 * @created 22.07.2010
	 * @param session
	 * @param q
	 * @param choices
	 * @param web
	 * @param namespace
	 * @return
	 */
	private static String renderChoiceAnswers(Session session, Question q, List<Choice> choices, String web, String namespace) {

		StringBuffer html = new StringBuffer();

		// int i = 0;
		// to space before and after commas evenly
		// buffi.delete(buffi.length() - 1, buffi.length());
		for (Choice choice : choices) {
			String cssclass = "answercell";

			// For BIOLOG2
			String jscall = " rel=\"{oid:'" + choice.getId() + "', "
					+ "web:'" + web + "', "
					+ "ns:'" + namespace + "', "
					+ "qid:'" + q.getId() + "'"
					+ "}\" ";

			Value value = session.getBlackboard().getValue(q);
			if (value != null && UndefinedValue.isNotUndefinedValue(value)
					&& isAnsweredinCase(value, new ChoiceValue(choice))) {
				cssclass = "answercell answerTextActive";
			}
			String spanid = "answerChoice" + q.getId() + "_" + choice.getId();
			html.append(getEnclosingTagOnClick("span", ""
					+ choice.getName() + " ", cssclass, jscall, null,
					spanid));

			// if (i < choices.size()) { html.append(" . ");} i++;
		}

		// also render the unknown alternative for choice questions
		html.append(renderAnswerUnknown(web, namespace, q));

		return html.toString();
	}

	/**
	 * 
	 * @created 22.07.2010
	 * @param sessionValue
	 * @param value
	 * @return
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

	/**
	 * Assembles the HTML representation for rendering answer unknown
	 * 
	 * @created 22.07.2010
	 * @param web
	 * @param namespace
	 * @param q
	 * @return
	 */
	private static String renderAnswerUnknown(String web, String namespace, Question q) {
		StringBuffer html = new StringBuffer();
		String jscall = " rel=\"{oid: '" + Unknown.getInstance().getId() + "', "
				+ "web:'" + web + "', "
				+ "ns:'" + namespace + "', "
				+ "qid:'" + q.getId() + "'"
				+ "}\" ";
		String cssclass = "answercell";
		String spanid = "answerunknown_" + q.getId() + "_" + Unknown.getInstance().getId();
		html.append(getEnclosingTagOnClick("span", "&gt;?&lt;", cssclass, jscall, null,
				spanid));
		return html.toString();
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
			String cssclass, String onclick, String onmouseover, String id) {
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
		sub.append(">");
		sub.append(text);
		sub.append("</" + tag + ">");
		return sub.toString();

	}
	/*
	 * private static StringBuffer getFollowUpChildrenRek(Question q, Session c,
	 * String web, String namespace, int indent, Question parent,
	 * Set<TerminologyObject> processedTOs) { // increase the indentation with
	 * every hierarchical descendance = each // recursion level indent += 15;
	 * 
	 * // add the table-row HTML, assign the id of the clicked root element //
	 * that initiates the // follow-up questions and set each element hidden for
	 * the first // rendering children.append("<tr id='" + parent.getId() +
	 * "' class='trf hidden'"); children.append(renderFollowUpQuestion(c, set,
	 * web, namespace, indent)); children.append("</tr> \n");
	 * 
	 * // as long as the follow-up question has further child elements, call //
	 * the method // recursively for each of them for (TerminologyObject cset :
	 * set.getChildren()) { if (!processedTOs.contains(cset)) {
	 * processedTOs.add(cset); getFollowUpChildrenRekur(children, (Question)
	 * cset, c, web, namespace, indent, parent, processedTOs);
	 * 
	 * }
	 * 
	 * } return children; }
	 */
}
