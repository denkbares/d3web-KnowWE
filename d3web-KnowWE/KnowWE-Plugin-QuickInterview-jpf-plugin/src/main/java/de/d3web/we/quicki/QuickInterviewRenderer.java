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
import java.util.ArrayList;
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
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionOC;
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

	private static String namespace = "";

	private static String web = "";

	private static Session session = null;

	private static KnowledgeBase kb = null;

	private static List<? extends QASet> inits = null;

	/**
	 * Assembles and returns the HTML representation of the interview.
	 *
	 *
	 * @created 15.07.2010
	 * @param c
	 * @param web
	 * @return the String representation of the interview
	 */

	/*
	 * First generate all objects: (1) get all questionnaires, in hierarchical
	 * order, and corresponding questions (2) while traversing those objects add
	 * css related information: needed for every question/questionnaire is its
	 * ID for finding it later (e.g., highlighting indicated questions),
	 * indicate abstraction questions with a special additional class
	 */
	public static String renderInterview(Session c, String webb) {

		// Assembles the Interview
		StringBuffer buffi = new StringBuffer();
		buffi.append("<link rel='stylesheet' type='text/css' href='KnowWEExtension/css/quicki.css'></link>");

		KnowledgeBase b = c.getKnowledgeBase();
		kb = b;
		session = c;
		web = webb;
		namespace = b.getId();

		// Get the qcontainers for the current session / knowledge base
		List<QContainer> containers = setupKBAndGetInterviewElements();

		// Map all processed TerminologyObjects already in interview table,
		// avoids endless recursion in cyclic hierarchies
		Set<TerminologyObject> processedTOs = new HashSet<TerminologyObject>();

		// add plugin header
		buffi.append(getInterviewPluginHeader());

		inits = kb.getInitQuestions();

		// go through all top-level qcontainers of the knowledge base
		for (QContainer container : containers) {

			// skip the topmost (sometimes default) root element "Q000"
			if (container.getName().endsWith("Q000")) continue;

			StringBuffer qcon = new StringBuffer();
			// call recursive method for getting QContainer HTML

			int recCounter = -1;

			boolean init = false;
			if (inits.contains(container)) {
				init = true;
			}
			getInterviewElementsRenderingRecursively(container, qcon, processedTOs, recCounter,
					init);
			buffi.append(qcon.toString());
		}

		return buffi.toString();
	}

	/**
	 * Gets the knowledge base of the current session and fills the QContainer
	 * list with the QContainers of that session, thereby orders them according
	 * to DFS strategy of the KnowledgeBaseManagement.
	 *
	 * @created 15.07.2010
	 */
	private static List<QContainer> setupKBAndGetInterviewElements() {

		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(kb);

		// get all qcontainers of kb into a list
		List<QContainer> containers = kb.getQContainers();

		// sort according to DFS as implemented in KnowledgeBaseManagement
		kbm.sortQContainers(containers);

		return containers;
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
	 * from a given top-level container, recursively and writes them into the
	 * given StringBuffer
	 *
	 * @created 17.08.2010
	 * @param topContainer the starting top-level container
	 * @param qcon the StringBuffer, taking in the HTML
	 */
	private static void getInterviewElementsRenderingRecursively(QContainer topContainer,
			StringBuffer qcon, Set<TerminologyObject> processedTOs, int depth, boolean init) {

		depth++;
		boolean show = false;
		if (depth == 0) {
			show = depth == 0 ? true : false;
		}
		else {
			show = init ? true : false;
		}

		if (!processedTOs.contains(topContainer)) {
			processedTOs.add(topContainer);
			qcon.append(getQuestionnaireRendering(topContainer, depth, show));
		}

		String display = init ? "display: block" : "display: none";
		qcon.append("<div id='group_" + topContainer.getId() + "' class='group' style='"
				+ display + "'");
		for (TerminologyObject qcontainerchild : topContainer.getChildren()) {

			if (qcontainerchild instanceof Question) {
				getQuestionsRecursively((Question) qcontainerchild, qcon, processedTOs, depth,
						topContainer, init);
			}
			else {
				getInterviewElementsRenderingRecursively((QContainer) qcontainerchild, qcon, processedTOs,
						depth, init);
			}
		}
		qcon.append("</div>");
	}

	/**
	 *
	 *
	 * @created 17.08.2010
	 * @param topQuestion
	 * @param sb
	 * @param processedTOs
	 */
	private static void getQuestionsRecursively(Question topQuestion, StringBuffer sb,
			Set<TerminologyObject> processedTOs, int depth, TerminologyObject parent, boolean init) {

		depth++;
		if (topQuestion.getChildren().length == 0) {
			// no further follow-up questions, thus append question rendering
			// but only if not yet done (if so it is contained in processedTOs
			if (!processedTOs.contains(topQuestion)) {
				processedTOs.add(topQuestion);
				sb.append(getQABlockRendering(topQuestion, depth, parent));
			}
		}
		else {

			if (!processedTOs.contains(topQuestion)) {
				processedTOs.add(topQuestion);
				sb.append(getQABlockRendering(topQuestion, depth, parent));
			}
			// we got follow-up questions, thus call method again
			for (TerminologyObject qchild : topQuestion.getChildren()) {
				getQuestionsRecursively((Question) qchild, sb, processedTOs, depth, parent,
						init);
			}
		}

	}


	/**
	 * Assembles the div that displays the name and an icon for questionnaires
	 *
	 * @created 16.08.2010
	 * @param container the QContainer to be rendered
	 * @return the HTML for the div
	 */
	private static String getQuestionnaireRendering(QContainer container, int depth, boolean show) {
		StringBuffer div = new StringBuffer();
		int margin = 10 + depth * 10;

		String clazz = show
				? "class='questionnaire pointDown'"
				: "class='questionnaire pointRight'";

		div.append("<div id='" + container.getId() + "' " +
				clazz + " style='margin-left: " + margin + "px; display: block'>");
		div.append(" " + container.getName() + ": ");
		div.append("</div>");

		return div.toString();
	}

	/**
	 * Returns the HTML-string representation for one QA-Block, that is, one
	 * question first, and the answers afterwards.
	 *
	 * @created 20.07.2010
	 * @param q the question to be rendered
	 * @param depth the depth of the recursion - for calculating identation
	 * @return HTML-String representation for one QA-Block
	 */
	private static String getQABlockRendering(Question q, int depth,
			TerminologyObject parent) {

		StringBuffer html = new StringBuffer();
		// String id = "qa_" + parent.getId();
		// html.append("<div id='" + id + "' class='qa' style='" + display +
		// "' >");

		// calculate indentation depth & resulting width of the question display
		int d = 10 + depth * 10;
		int w = 300 - d;

		// render the first cell displaying the Question in a separate div,
		// then call method for rendering a question's answers in another div
		html.append("<div id='" + q.getId() + "' " +
				"parent='" + parent.getId() + "' " +
				"class='question' " +
				"style='margin-left: " + d + "px; width: " + w + "px; display: inline-block;' >"
				+ q.getName() + "</div>");

		// MultChoiceValue --> setValue TODO
		if (q instanceof QuestionOC) {
			List<Choice> list = ((QuestionChoice) q).getAllAlternatives();
			html.append(renderOCChoiceAnswers(q, list));
		}
		else if (q instanceof QuestionMC) {
			List<Choice> list = ((QuestionMC) q).getAlternatives();
			List<ChoiceValue> cvlist = new ArrayList<ChoiceValue>();
			for (Choice c : list) {
				cvlist.add(new ChoiceValue(c));
			}
			MultipleChoiceValue mcVal = new MultipleChoiceValue(cvlist);
			System.out.println(mcVal);

			html.append(renderChoiceAnswers(q, mcVal));
		}
		else {
			html.append(renderNumAnswers(q));
		}
		// html.append("</div>");
		return html.toString();
	}

	private static String renderOCChoiceAnswers(Question q, List<Choice> list) {
		StringBuffer html = new StringBuffer();
		html.append("<div class='answers' style='display: inline;'>");
		for (Choice choice : list) {

			String cssclass = "answer";
			String jscall = " rel=\"{oid:'" + choice.getId() + "', "
					+ "web:'" + web + "', "
					+ "ns:'" + namespace + "', "
					+ "qid:'" + q.getId() + "'"
					+ "}\" ";

			// Value value = session.getBlackboard().getValue(q);
			// if (value != null && UndefinedValue.isNotUndefinedValue(value)
			// && isAnsweredinCase(value, new ChoiceValue(choice))) {
			// cssclass = "answer answerActive";
			// }
			String spanid = q.getId() + "_" + choice.getId();
			html.append(getEnclosingTagOnClick("div", "" + choice.getName() + " ", cssclass,
					jscall, null, spanid));
			html.append("<div class='answerseparator'>&nbsp;-&nbsp;</div>");
		}

		// also render the unknown alternative for choice questions
		html.append(renderAnswerUnknown(q));

		html.append("</div>");
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
	private static String renderNumAnswers(Question q) {

		StringBuffer html = new StringBuffer();
		String value = "";

		// if answer has already been answered
		if (UndefinedValue.isNotUndefinedValue(session.getBlackboard().getValue(q))) {
			Value answer = session.getBlackboard().getValue(q);
			if (answer != null && answer instanceof NumValue) {
				value = answer.getValue().toString();
			}
		}

		String id = q.getId();

		// append the JS call
		String jscall = "";
		try {
			jscall = " rel=\"{oid: '" + id + "', "
					+ "web:'" + web + "',"
					+ "ns:'" + namespace + "',"
					+ "qtext:'" + URLEncoder.encode(q.getName(), "UTF-8") + "', "
					+ "inputid:'" + id + "'"
					+ "}\" ";
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// append the input, if available with already provided input
		html.append("<div class='answer' style='display: inline;'>"
				+ "<input id='" + id + "' type='text' "
				+ "value='" + value + "' "
				+ "size='7' "
				+ jscall + ">");
		html.append("<input type='button' value='ok' class='num-ok'>");
		html.append("</div>");
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
			html.append("<div class='answerseparator'>&nbsp;-&nbsp;</div>");
		}

		// also render the unknown alternative for choice questions
		html.append(renderAnswerUnknown(q));

		html.append("</div>");
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
