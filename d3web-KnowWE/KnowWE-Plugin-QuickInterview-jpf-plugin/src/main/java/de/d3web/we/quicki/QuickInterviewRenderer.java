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
import java.util.Set;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
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

	private static String namespace = "";

	private static String web = "";

	private static Session session = null;

	private static KnowledgeBase kb = null;

	private static List<? extends QASet> inits = null;

	/**
	 * Assembles and returns the HTML representation of the interview.
	 *
	 * @created 15.07.2010
	 * @param c the session
	 * @param web the web context
	 * @return the String representation of the interview
	 */
	public static String renderInterview(Session c, String webb) {

		// Assembles the Interview
		StringBuffer buffi = new StringBuffer();

		// insert specific CSS
		buffi.append("<link rel='stylesheet' type='text/css' href='KnowWEExtension/css/quicki.css' />");

		kb = c.getKnowledgeBase();
		session = c;
		web = webb;
		namespace = kb.getId();

		// TODO Map all processed TerminologyObjects already in interview table,
		// avoids endless recursion in cyclic hierarchies
		Set<TerminologyObject> processedTOs = new HashSet<TerminologyObject>();

		// add plugin header
		buffi.append(getInterviewPluginHeader());

		inits = kb.getInitQuestions();

		StringBuffer qcon = new StringBuffer();

		// call method for getting interview elements recursively
		// start with root QASet and go DFS strategy
		getInterviewElementsRenderingRecursively(kb.getRootQASet(), qcon, processedTOs, 0, true);

		buffi.append(qcon.toString());

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
	private static String getInterviewPluginHeader() {
		StringBuffer html = new StringBuffer();
		html.append("<h3>");
		html.append("Quick Interview");
		html.append("</h3>");
		return html.toString();
	}

	/**
	 * Assembles the HTML representation of QContainers and Questions, starting
	 * from the root QASet of the KB, recursively, and writes them into the
	 * given StringBuffer
	 *
	 * @created 14.07.2010
	 * @param topContainer the root container
	 * @param qcon the StringBuffer
	 * @param processedTOs already processed TerminologyObjects
	 * @param depth recursion depth; used to calculate identation
	 * @param init flag for signalling whether the processed element was in the
	 *        init questionnaire
	 */
	private static void getInterviewElementsRenderingRecursively(QASet topContainer,
			StringBuffer qcon, Set<TerminologyObject> processedTOs, int depth, boolean init) {

		// just do not display the rooty root
		if (!topContainer.getName().endsWith("Q000")) {
			if (!processedTOs.contains(topContainer)) {
				processedTOs.add(topContainer);
				qcon.append(getQuestionnaireRendering(topContainer, depth, init));
			}
		}

		// if init questionnaire: all contained elements are to be displayed
		String display = init ? "display: block;" : "display: none;";

		// group all following questionnairs/questions for easily hiding them
		// blockwise later
		qcon.append("<div id='group_" + topContainer.getId() + "' class='group' style='"
				+ display + "' >");

		depth++;

		// process all children, depending on element type branch into
		// corresponding recursion
		for (TerminologyObject qcontainerchild : topContainer.getChildren()) {

			init = inits.contains(qcontainerchild) ? true : false;

			if (qcontainerchild instanceof QContainer) {
				getInterviewElementsRenderingRecursively((QContainer)
						qcontainerchild, qcon, processedTOs, depth, init);
			}
			else if (qcontainerchild instanceof Question) {
				getQuestionsRecursively((Question) qcontainerchild, qcon,
							processedTOs, depth, topContainer, init);
			}
		}
		qcon.append("</div>"); // close the grouping div
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

		// no follow-ups --> append question rendering if not already rendered
		if (!processedTOs.contains(topQuestion)) {
			processedTOs.add(topQuestion);
			sb.append(getQABlockRendering(topQuestion, depth++, parent));
		}

		if (topQuestion.getChildren().length > 0) {

			// we got follow-up questions, thus call recursively for children
			for (TerminologyObject qchild : topQuestion.getChildren()) {

				getQuestionsRecursively((Question) qchild, sb, processedTOs,
						depth++, parent, init);
			}
		}
	}

	/**
	 * Assembles the div that displays icon and name of questionnaires
	 *
	 * @created 16.08.2010
	 * @param container the qcontainer to be rendered
	 * @param depth recursion depth
	 * @param show flag that indicates whether questionnaire is expanded (show)
	 *        or not; for appropriately displaying corresponding triangles
	 * @return the HTML of a questionnaire div
	 */
	private static String getQuestionnaireRendering(QASet container, int depth, boolean show) {

		StringBuffer div = new StringBuffer();
		int margin = 10 + depth * 10; // calculate identation

		String clazz = show // decide class for rendering expand-icon
				? "class='questionnaire pointDown'"
				: "class='questionnaire pointRight'";

		div.append("<div id='" + container.getId() + "' " +
				clazz + " style='margin-left: " + margin + "px; display: block'; >");
		div.append(" " + container.getName() + ": ");
		div.append("</div>");

		return div.toString();
	}

	/**
	 * Assembles the HTML-string representation for one QA-Block, that is, one
	 * question first, and the answers afterwards.
	 *
	 * @created 20.07.2010
	 * @param q the question to be rendered
	 * @param depth the depth of the recursion - for calculating identation
	 * @param parent the parent element
	 * @return HTML-String representation for one QA-Block
	 */
	private static String getQABlockRendering(Question q, int depth,
			TerminologyObject parent) {

		StringBuffer html = new StringBuffer();
		html.append("<div id='qablock' style='display: block;'>");

		// calculate indentation depth & resulting width of the question display
		// 10 for standard margin and 30 for indenting further than the triangle
		int d = 10 + 30 + depth * 10;

		// width of the question front section, i.e. total width - identation
		int w = 300 - d;

		// render the first cell displaying the Question in a separate div,
		// then call method for rendering a question's answers in another div
		html.append("<div id='" + q.getId() + "' " +
				"parent='" + parent.getId() + "' " +
				"class='question' " +
				"style='margin-left: " + d + "px; width: " + w + "px; display: inline-block;' >"
				+ q.getName() + "</div>");

		// TODO MultChoiceValue --> setValue
		if (q instanceof QuestionOC) {
			List<Choice> list = ((QuestionChoice) q).getAllAlternatives();
			html.append(renderOCChoiceAnswers(q, list));
		}
		/*
		 * else if (q instanceof QuestionMC) { List<Choice> list = ((QuestionMC)
		 * q).getAlternatives(); List<ChoiceValue> cvlist = new
		 * ArrayList<ChoiceValue>(); for (Choice c : list) { cvlist.add(new
		 * ChoiceValue(c)); } MultipleChoiceValue mcVal = new
		 * MultipleChoiceValue(cvlist);
		 *
		 * html.append(renderChoiceAnswers(q, mcVal)); }
		 */
		else if (q instanceof QuestionNum) {
			html.append(renderNumAnswers(q));
		}
		html.append("</div>");
		return html.toString();
	}

	/**
	 * Assembles the HTML for rendering a one choice question
	 *
	 * @created 21.08.2010
	 * @param q the question
	 * @param list the list of possible choices
	 * @return the HTML representation of one choice questions
	 */
	private static String renderOCChoiceAnswers(Question q, List<Choice> list) {

		StringBuffer html = new StringBuffer();

		for (Choice choice : list) {

			String cssclass = "answer";
			String jscall = " rel=\"{oid:'" + choice.getId() + "', "
					+ "web:'" + web + "', "
					+ "ns:'" + namespace + "', "
					+ "qid:'" + q.getId() + "'"
					+ "}\" ";

			// TODO: activate?
			// Value value = session.getBlackboard().getValue(q);
			// if (value != null && UndefinedValue.isNotUndefinedValue(value)
			// && isAnsweredinCase(value, new ChoiceValue(choice))) {
			// cssclass = "answer answerActive";
			// }
			String spanid = q.getId() + "_" + choice.getId();
			html.append(getEnclosingTagOnClick("div", "" + choice.getName() + " ", cssclass,
					jscall, null, spanid));

			// for having a separator between answer alternatives (img, text...)
			html.append("<div class='answerseparator'></div>");
		}

		// TODO also render the unknown alternative for choice questions?!
		html.append(renderAnswerUnknown(q));

		return html.toString();
	}

	/**
	 * Assembles the HTML needed for displaying the (numerical) answer field
	 *
	 * @created 20.07.2010
	 * @param q the question to which numerical answers are attached
	 * @return the String for rendering numerical answer field
	 */
	private static String renderNumAnswers(Question q) {

		StringBuffer html = new StringBuffer();
		String value = "";

		// if answer has already been answered write value into the field
		if (UndefinedValue.isNotUndefinedValue(session.getBlackboard().getValue(q))) {
			Value answer = session.getBlackboard().getValue(q);
			if (answer != null && answer instanceof NumValue) {
				value = answer.getValue().toString();
			}
		}

		String id = q.getId();

		// assemble the JS call
		String jscall = "";
		try {
			jscall = " rel=\"{oid: '" + id + "', "
					+ "web:'" + web + "',"
					+ "ns:'" + namespace + "',"
					+ "qtext:'" + URLEncoder.encode(q.getName(), "UTF-8") + "', "
					+ "}\" ";
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// assemble the input field
		html.append("<input class='input'  style='display: inline;' id='input_" + id
				+ "' type='text' "
				+ "value='" + value + "' "
				+ "size='7' "
				+ jscall + " />");
		html.append("<input type='button' value='ok' class='num-ok' />");

		return html.toString();
	}

	/**
	 * TODO Creates the HTML needed for displaying the answer alternatives of
	 * choice answers.
	 *
	 * @created 22.07.2010
	 * @param session
	 * @param q
	 * @param choices
	 * @param web
	 * @param namespace
	 * @return
	 */
	private static String renderChoiceAnswers(Question q, MultipleChoiceValue mcval) {

		StringBuffer html = new StringBuffer();
		html.append("<div class='answers' style='display: inline;'>");
		for (Choice choice : mcval.asChoiceList()) {

			String cssclass = "answer";
			String jscall = " rel=\"{oid:'" + choice.getId() + "', "
					+ "web:'" + web + "', "
					+ "ns:'" + namespace + "', "
					+ "qid:'" + q.getId() + "', "
					+ "mcid:'" + mcval.getAnswerChoicesID() + "'"
					+ "}\" ";

			// Value value = session.getBlackboard().getValue(q);
			// if (value != null && UndefinedValue.isNotUndefinedValue(value)
			// && isAnsweredinCase(value, new ChoiceValue(choice))) {
			// cssclass = "answer answerActive";
			// }
			String spanid = q.getId() + "_" + choice.getId();
			html.append(getEnclosingTagOnClick("div", "" + choice.getName() + " ", cssclass,
					jscall, null, spanid));
			html.append("<div class='answerseparator'></div>");
		}

		// also render the unknown alternative for choice questions
		html.append(renderAnswerUnknown(q));

		html.append("</div>");
		return html.toString();
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
	private static String renderAnswerUnknown(Question q) {
		StringBuffer html = new StringBuffer();
		String jscall = " rel=\"{oid: '" + Unknown.getInstance().getId() + "', "
				+ "web:'" + web + "', "
				+ "ns:'" + namespace + "', "
				+ "qid:'" + q.getId() + "'"
				+ "}\" ";
		String cssclass = "answerunknown";
		String spanid = q.getId() + "_" + Unknown.getInstance().getId();
		html.append(getEnclosingTagOnClick("div", "", cssclass, jscall, null, spanid));

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
}
