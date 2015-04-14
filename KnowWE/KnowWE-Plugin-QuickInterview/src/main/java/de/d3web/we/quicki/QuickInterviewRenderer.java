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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
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
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.strings.Strings;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.OutDatedSessionNotification;

/**
 * Render the quick interview -aka QuickI- in KnowWE --- HTML / JS / CSS based
 *
 * @author Martina Freiberg
 * @created 15.07.2010
 */
public class QuickInterviewRenderer {

	private final String namespace;

	private final String web;

	private final Session session;

	private final KnowledgeBase kb;

	private final ResourceBundle rb;

	private final String kbSectionId;

	private final Section<?> section;

	private Map<String, String> config = null;

	private int counter = 0;

	private final UserContext user;

	public static void renderInterview(Section<?> section, UserContext user, RenderResult result) {
		Collection<D3webCompiler> compilers = Compilers.getCompilers(section,
				D3webCompiler.class);
		if (compilers.size() > 1) {
			result.appendHtmlElement(
					"span",
					"This interview belongs to multiple knowledge bases, only " +
							"the first knowledge base (lexicographically) will be used.",
					"class", "warning");

		}
		KnowledgeBase knowledgeBase = D3webUtils.getKnowledgeBase(section);

		if (knowledgeBase == null) {
			result.appendHtmlElement(
					"span",
					"No knowledge base found for this interview.",
					"class", "error");
			return;
		}
		Session session = SessionProvider.getSession(user, knowledgeBase);
		if (SessionProvider.hasOutDatedSession(user, D3webUtils.getKnowledgeBase(section))) {
			NotificationManager.addNotification(user,
					new OutDatedSessionNotification(D3webUtils.getCompiler(section).getCompileSection().getID()));
		}
		new QuickInterviewRenderer(session, section, user).render(result);
	}

	private QuickInterviewRenderer(Session c, Section<?> section, UserContext user) {
		// insert specific CSS
		// buffi.append("<link rel='stylesheet' type='text/css' href='KnowWEExtension/css/quicki.css' />");
		this.kb = c.getKnowledgeBase();
		this.session = c;
		this.web = section.getWeb();
		this.section = section;
		this.kbSectionId = D3webUtils.getCompiler(section).getCompileSection().getID();
		this.namespace = kb.getId();
		this.rb = D3webUtils.getD3webBundle(user);
		this.user = user;
		this.config = user.getParameters();
		// this.config = new HashMap<String, String>();
		// config.put("use", "user");

	}

	private String render(RenderResult buffi) {

		// Assembles the Interview
		buffi.appendHtml("<div style='clear:both'>");

		// Map all processed TerminologyObjects already in interview table,
		// avoids endless recursion in cyclic hierarchies
		Set<TerminologyObject> processedTOs = new HashSet<TerminologyObject>();

		// add plugin header
		getInterviewPluginHeader(buffi);

		// call method for getting interview elements recursively
		// start with root QASet and go DFS strategy
		getInterviewElementsRenderingRecursively(kb.getRootQASet(), buffi,
				processedTOs, -2);

		// add pseudo element for correctly closing the plugin
		buffi.appendHtml("<div class='invisible'>  </div></div>");
		return buffi.toString();
	}

	/**
	 * Returns the Plugin Header As String
	 *
	 * @created 15.07.2010
	 */
	private void getInterviewPluginHeader(RenderResult html) {
		// assemble JS string
		html.appendHtmlTag("div", "style", "position:relative");
		html.appendHtmlElement("div", "",
				"onclick", "KNOWWE.plugin.d3webbasic.actions.resetSession('" + kbSectionId + "')",
				"class", "reset pointer", "title", rb.getString("KnowWE.quicki.reset"));
		html.appendHtmlTag("/div");
	}

	/**
	 * Assembles the HTML representation of QContainers and Questions, starting
	 * from the root QASet of the KB, recursively, and writes them into the
	 * given StringBuffer
	 *
	 * @param questionnaire the root container
	 * @param buffer        the StringBuffer
	 * @param processedTOs  already processed TerminologyObjects
	 * @param depth         recursion depth; used to calculate identation
	 * @created 14.07.2010
	 */
	private void getInterviewElementsRenderingRecursively(
			TerminologyObject questionnaire, RenderResult buffer,
			Set<TerminologyObject> processedTOs, int depth) {

		boolean visible = isVisible(questionnaire);

		String id = getID();
		// just do not display the rooty root
		if (questionnaire != questionnaire.getKnowledgeBase().getRootQASet()) {

			// if already contained, get the already-defined rendering
			if (processedTOs.contains(questionnaire)) {
				getAlreadyDefinedRendering(questionnaire, buffer, depth++);
			}
			else {
				processedTOs.add(questionnaire);
				getQuestionnaireRendering((QContainer) questionnaire, depth,
						buffer, id);
			}
		}

		// group all following questionnaires/questions for easily hiding them
		// blockwise later
		buffer.appendHtml("<div id='group_" + id + "' style='display: "
				+ (visible ? "block" : "none") + "' >");

		depth++;

		// process all children, depending on element type branch into
		// corresponding recursion
		for (TerminologyObject child : questionnaire.getChildren()) {

			if (child instanceof QContainer) {
				if (!processedTOs.contains(child)) {
					getInterviewElementsRenderingRecursively(child, buffer,
							processedTOs, depth);
				}
			}
			else if (child instanceof Question) {
				getQuestionsRecursively((Question) child, buffer, processedTOs,
						depth, questionnaire);
			}
		}
		buffer.appendHtml("</div>"); // close the grouping div
	}

	/**
	 * Recursively walks through the questions of the hierarchy and calls
	 * methods for appending their rendering
	 *
	 * @param topQuestion  the root question
	 * @param sb           the StringBuffer
	 * @param processedTOs already processed elements
	 * @param depth        the recursion depth
	 * @param parent       the parent element
	 * @created 17.08.2010
	 */
	private void getQuestionsRecursively(Question topQuestion, RenderResult sb,
										 Set<TerminologyObject> processedTOs, int depth,
										 TerminologyObject parent) {

		// if already contained in interview, get already-defined rendering and
		// return for
		// avoiding endless recursion
		if (processedTOs.contains(topQuestion)) {
			getQABlockRendering(topQuestion, depth, parent, sb);
			return;
		}

		getQABlockRendering(topQuestion, depth, parent, sb);
		processedTOs.add(topQuestion);

		if (topQuestion.getChildren().length > 0) {

			depth++;

			// we got follow-up questions, thus call recursively for children
			for (TerminologyObject qchild : topQuestion.getChildren()) {
				getQuestionsRecursively((Question) qchild, sb, processedTOs,
						depth, parent);
			}
		}
	}

	/**
	 * Assembles an own div for indicating questions/questionnaires that have
	 * already been answered
	 *
	 * @param element the element that was already been answered
	 * @param sb      StringBuffer to append the div to
	 * @param depth   indicator for the indentation depth
	 * @created 26.08.2010
	 */
	private void getAlreadyDefinedRendering(TerminologyObject element,
											RenderResult sb, int depth) {

		int margin = 30 + depth * 20;
		sb.appendHtml("<div "
				+ "class='alreadyDefined' style='margin-left: " + margin
				+ "px; display: block;' >");
		sb.append(Strings.encodeHtml(getLabel(element)) + " is already defined!");
		sb.appendHtml("</div>");
	}

	/**
	 * Assembles the div that displays icon and name of questionnaires
	 *
	 * @param container the qcontainer to be rendered
	 * @param depth     recursion depth
	 * @created 16.08.2010
	 */
	private void getQuestionnaireRendering(QASet container, int depth,
										   RenderResult buffi, String id) {

		int margin = 10 + depth * 20; // calculate identation

		boolean visible = isVisible(container);
		boolean indicated = isThisOrFollowUpIndicated(container);
		buffi.appendHtml("<div id='" + Strings.encodeHtml(id)
				+ "' " + "class='questionnaire point"
				+ (visible ? "Down" : "Right")
				+ (indicated ? " indicated" : "") + "' "
				+ "style='margin-left: " + margin + "px;' >");

		buffi.append(Strings.encodeHtml(getLabel(container)));
		buffi.appendHtml("</div>\n");

		if (container.getChildren().length == 0) {
			margin = margin + 30;
			buffi.appendHtml("<div id='group_" + Strings.encodeHtml(container.getName())
					+ "' class='group' style='display: "
					+ (visible ? "block" : "none") + ";' >");
			buffi.appendHtml("\n<div class='emptyQuestionnaire' "
					+ "style='margin-left: " + margin
					+ "px'>No elements defined!</div>");
			buffi.appendHtml("</div>");
		}
	}

	/**
	 * Assembles the HTML-string representation for one QA-Block, that is, one
	 * question first, and the answers afterwards.
	 *
	 * @param question the question to be rendered
	 * @param depth    the depth of the recursion - for calculating identation
	 * @param parent   the parent element
	 * @created 20.07.2010
	 */
	private void getQABlockRendering(Question question, int depth,
									 TerminologyObject parent, RenderResult sb) {

		// calculate indentation depth & resulting width of the question display
		// 10 for standard margin and 30 for indenting further than the triangle
		int d = 30 + depth * 20;

		String qablockCSS = "qablock";
		if (isAbstract(question) || !isVisible(question)) {
			qablockCSS = "qablockHidden";
			if (hideAbstractions()) {
				// do not render anything in this case
				return;
			}
		}

		sb.appendHtml("<div class='" + qablockCSS
				+ "' style='display: block; margin-left: " + d
				+ "px;'\n>");

		sb.appendHtml("<table><tr><td class='tdquestion'\n>");
		// width of the question front section, i.e. total width - identation
		int w = 320 - d;
		String divText = Strings.encodeHtml(getLabel(question));
		String cssClass = "question";
		String title = question.getInfoStore().getValue(MMInfo.DESCRIPTION);
		if (title == null) {
			title = "";
		}
		else {
			cssClass = cssClass + " tooltipster";
			title = "title='" + title + "' ";
		}

		sb.appendHtml("<div " + "parent='"
				+ Strings.encodeHtml(parent.getName()) + "' " + "class='" + cssClass + "' "
				+ title + "style='width: " + w
				+ "px; display: inline-block;' >").append(divText).appendHtml("</div>");
		// }
		sb.appendHtml("</td><td>");

		if (question instanceof QuestionOC) {
			List<Choice> list = ((QuestionChoice) question)
					.getAllAlternatives();
			renderOCChoiceAnswers(question, list, sb);
		}
		else if (question instanceof QuestionMC) {
			QuestionMC questionMC = (QuestionMC) question;
			List<Choice> list = questionMC.getAllAlternatives();
			MultipleChoiceValue mcVal = MultipleChoiceValue.fromChoices(list);
			renderMCChoiceAnswers(questionMC, mcVal, sb);
		}
		else if (question instanceof QuestionNum) {
			renderNumAnswers(question, sb);
		}

		else if (question instanceof QuestionDate) {
			renderDateAnswers(question, sb);
		}
		else if (question instanceof QuestionText) {
			renderTextAnswers(question, sb);
		}
		sb.appendHtml("</td></tr></table>");
		sb.appendHtml("</div>");
	}

	private boolean hideAbstractions() {
		return this.config.containsKey("abstractions")
				&& this.config.get("abstractions").equals("false");
	}

	private boolean isAbstract(Question question) {
		return question.getInfoStore().getValue(
				BasicProperties.ABSTRACTION_QUESTION) != null
				&& question.getInfoStore().getValue(
				BasicProperties.ABSTRACTION_QUESTION);
	}

	private void renderTextAnswers(Question q, RenderResult sb) {

		// if answer has already been answered write value into the field
		Value value = D3webUtils.getValueNonBlocking(session, q);
		String valueString = "";
		if (value instanceof TextValue) {
			valueString = value.toString();
		}

		String id = getID();
		String jscall = " rel=\"{oid: '" + id + "', " + "web:'" + web + "',"
				+ "ns:'" + namespace + "'," + "type:'text', " + "qtext:'"
				+ Strings.encodeURL(q.getName()) + "', " + "}\" ";

		String inputSize = getInputSize();
		if (inputSize == null) inputSize = "18";
		// assemble the input field
		sb.appendHtml("<input qid='\" + id + \"' class='inputtextvalue'  style='display: inline;' type='text' "
				+ "value='"
				+ Strings.encodeHtml(valueString)
				+ "' "
				+ "size='" + inputSize + "' " + jscall + " \n/>");
		// "<div class='dateformatdesc'>()</div>");

		sb.appendHtml("<div class='separator'></div>");
		renderAnswerUnknown(q, "num", sb);
	}

	private String getInputSize() {
		if (section.get() instanceof DefaultMarkupType) {
			return DefaultMarkupType.getAnnotation(section, QuickInterviewMarkup.INPUT_SIZE_KEY);
		}
		else {
			return null;
		}

	}

	/**
	 * Assembles the HTML for rendering a one choice question
	 *
	 * @param q    the question
	 * @param list the list of possible choices
	 * @created 21.08.2010
	 */
	private void renderOCChoiceAnswers(Question q, List<Choice> list,
									   RenderResult sb) {

		// go through all choices = answer alternatives
		boolean first = true;
		for (Choice choice : list) {
			if (first) {
				first = false;
			}
			else {
				renderChoiceSeparator(sb);
			}

			String cssclass = "answer";

			// assemble JS string
			String jscall = " rel=\"{oid:'"
					+ Strings.encodeHtml(choice.getName().replace("'", "\\'")) + "', "
					+ "web:'" + web + "', " + "ns:'" + namespace + "', "
					+ "qid:'" + Strings.encodeURL(q.getName()) + "', "
					+ "choice:'" + Strings.encodeURL(choice.getName()) + "', "
					+ "type:'oc', " + "}\" ";

			// if a value was already set, get the value and set corresponding
			// css class
			Value value = D3webUtils.getValueNonBlocking(session, q);
			if (value != null && UndefinedValue.isNotUndefinedValue(value)
					&& isAnsweredinCase(value, new ChoiceValue(choice))) {
				cssclass = "answerClicked";
			}

			String label = getLabel(choice);
			appendEnclosingTagOnClick("span", label, cssclass,
					jscall, null, null,
					choice.getInfoStore().getValue(MMInfo.DESCRIPTION), sb);

			// System.out.println(getEnclosingTagOnClick("div", "" +
			// choice.getName() + " ",
			// cssclass, jscall, null, spanid));

			// for having a separator between answer alternatives (img, text...)
			// sb.append("\n<div class='separator'></div>");
		}

		renderAnswerUnknown(q, "oc", sb);
	}

	/**
	 * Assembles the HTML needed for displaying the (numerical) answer field
	 *
	 * @param q the question to which numerical answers are attached
	 * @created 20.07.2010
	 */
	private void renderNumAnswers(Question q, RenderResult sb) {

		// if answer has already been answered write value into the field
		Value value = D3webUtils.getValueNonBlocking(session, q);
		String valueString = "";
		if (value instanceof NumValue) {
			valueString = trimPZ(((NumValue) value).getDouble());
		}

		String id = getID();
		Double rangeMax = Double.MAX_VALUE;
		Double rangeMin = Double.MIN_VALUE;

		Object rangeValue = q.getInfoStore().getValue(
				BasicProperties.QUESTION_NUM_RANGE);
		if (rangeValue != null) {
			NumericalInterval range = (NumericalInterval) rangeValue;
			rangeMax = range.getRight();
			rangeMin = range.getLeft();
		}

		// assemble the JS call
		String jscall;
		if (rangeMin != Double.MIN_VALUE && rangeMax != Double.MAX_VALUE) {
			jscall = " rel=\"{oid: '" + id + "', " + "web:'" + web + "',"
					+ "ns:'" + namespace + "'," + "type:'num', " + "rangeMin:'"
					+ rangeMin + "', " + "rangeMax:'" + rangeMax + "', "
					+ "qtext:'" + Strings.encodeURL(q.getName()) + "', "
					+ "}\" ";
		}
		else {
			jscall = " rel=\"{oid: '" + id + "', " + "web:'" + web + "',"
					+ "ns:'" + namespace + "'," + "type:'num', "
					+ "rangeMin:'NaN', " + "rangeMax:'NaN', " + "qtext:'"
					+ Strings.encodeURL(q.getName()) + "', " + "}\" ";
		}

		NumericalInterval range = q.getInfoStore().getValue(
				BasicProperties.QUESTION_NUM_RANGE);
		String rangeString = " ";
		if (range != null) {
			rangeString = "placeholder='" + trimPZ(range.getLeft()) + " - "
					+ trimPZ(range.getRight()) + "' ";
		}

		String inputSize = getInputSize();
		if (inputSize == null) inputSize = "7";
		// assemble the input field
		sb.appendHtml("<input qid='" + id + "' class='numinput' type='text' "
				+ rangeString + "value='" + valueString + "' " + "size='" + inputSize + "' " + jscall + " />");

		// print the units
		appendUnit(q, sb);

		if (Unknown.assignedTo(value) || !suppressUnknown(q)) {
			sb.appendHtml("<div class='separator'></div>");
		}

		renderAnswerUnknown(q, "num", sb);

		String errmsgid = id + "_errormsg";
		sb.appendHtml("<div id='" + errmsgid + "' class='invisible' ></div>");
	}

	private void appendUnit(Question q, RenderResult sb) {
		String unit = getUnit(q);
		sb.appendHtml("<div class='unit'>").append(Strings.encodeHtml(unit)).appendHtml("</div>");
	}

	private String getUnit(Question q) {
		String unit = "";
		Object questionUnit = q.getInfoStore().getValue(MMInfo.UNIT);
		if (questionUnit != null) {
			unit = questionUnit.toString();
		}
		return unit;
	}

	private String trimPZ(double d) {
		return trimPZ(String.valueOf(d));
	}

	private String trimPZ(String dString) {
		if (dString.endsWith(".0")) {
			dString = dString.substring(0, dString.length() - 2);
		}
		return dString;
	}

	private boolean suppressUnknown(Question question) {
		return !BasicProperties.isUnknownVisible(question)
				|| (this.config.containsKey("unknown") && this.config.get("unknown").equals("false"));
	}

	/**
	 * Assembles the HTML representation of a date answer input
	 *
	 * @param q  the date-question
	 * @param sb the String Buffer, the HTML is attached to
	 * @created 01.09.2010
	 */
	private void renderDateAnswers(Question q, RenderResult sb) {

		// if answer has already been answered write value into the field
		Value value = D3webUtils.getValueNonBlocking(session, q);
		String valueString = "";
		if (value instanceof DateValue) {
			valueString = ValueUtils.getDateVerbalization((QuestionDate) q, (DateValue) value, ValueUtils.TimeZoneDisplayMode.NEVER);
		}

		String id = getID();

		// assemble the JS call
		String jscall = " rel=\"{oid: '" + id + "', " + "web:'" + web + "',"
				+ "ns:'" + namespace + "'," + "type:'num', " + "qtext:'"
				+ Strings.encodeURL(q.getName()) + "', " + "}\" ";

		String placeHolder = DateValue.getDefaultDateFormat().toPattern();

		// assemble the input field
		String title = "Use the following date format:\n"
				+ placeHolder + "\nTime is optional, "
				+ "if you use time, seconds and milliseconds are optional.";
		sb.appendHtml("<input qid='" + id + "' class='inputdate'  style='display: inline;' type='dateValue' " + "value='" + valueString + "' placeholder='"
				+ placeHolder + "' title='" + title + "' " + jscall + " />");

		// print the units
		appendUnit(q, sb);

		// sb.append("<input type='button' value='OK' class='date-ok' /> ");
		if (Unknown.assignedTo(value) || !suppressUnknown(q)) {
			sb.appendHtml("<div class='separator'> | </div>");
		}
		renderAnswerUnknown(q, "num", sb);

		String errmsgid = id + "_errormsg";
		sb.appendHtml("<div id='" + errmsgid + "' class='invisible' ></div>");
	}

	/**
	 * Creates the HTML needed for displaying the answer alternatives of mc
	 * choice answers.
	 *
	 * @created 01.09.2010
	 */
	private void renderMCChoiceAnswers(QuestionChoice q,
									   MultipleChoiceValue mcval, RenderResult sb) {

		sb.appendHtml("<div class='answers'>");
		boolean first = true;
		for (Choice choice : mcval.asChoiceList(q)) {
			if (first) {
				first = false;
			}
			else {
				renderChoiceSeparator(sb);
			}

			String cssclass = "answerMC";
			String jscall = " rel=\"{oid:'"
					+ Strings.encodeHtml(choice.getName().replace("'", "\\'")) + "', "
					+ "web:'" + web + "', " + "ns:'" + namespace + "', "
					+ "qid:'" + Strings.encodeURL(q.getName()) + "', "
					+ "type:'mc', " + "choice:'"
					+ Strings.encodeURL(choice.getName()) + "', " + "}\" ";

			Value value = D3webUtils.getValueNonBlocking(session, q);
			if (value != null && UndefinedValue.isNotUndefinedValue(value)
					&& isAnsweredinCase(value, new ChoiceValue(choice))) {
				cssclass = "answerMCClicked";
			}

			String label = getLabel(choice);
			appendEnclosingTagOnClick("span", "" + label + "", cssclass,
					jscall, null, null,
					choice.getInfoStore().getValue(MMInfo.DESCRIPTION), sb);

		}

		// also render the unknown alternative for choice questions
		renderAnswerUnknown(q, "mc", sb);
		sb.appendHtml("</div>");
	}

	private void renderChoiceSeparator(RenderResult sb) {
		if (renderChoiceAnswerAsList()) {
			sb.appendHtml("<br>");
		}
		else {
			sb.appendHtml("<div class='separator'> | </div>");
		}
	}

	private boolean renderChoiceAnswerAsList() {
		return this.config.containsKey("answers")
				&& this.config.get("answers").equals("list");
	}

	/**
	 * Assembles the HTML representation for rendering answer unknown
	 *
	 * @created 22.07.2010
	 */
	private void renderAnswerUnknown(Question q, String type, RenderResult sb) {

		// if unknown should neither be displayed, not is selected
		// render no answer unknown
		Value value = D3webUtils.getValueNonBlocking(session, q);
		boolean isUnknown = Unknown.assignedTo(value);
		if (!isUnknown && suppressUnknown(q)) {
			return;
		}

		String jscall = " rel=\"{oid: '" + Unknown.getInstance().getId()
				+ "', " + "web:'" + web + "', " + "ns:'" + namespace + "', "
				+ "type:'" + type + "', " + "qid:'"
				+ Strings.encodeURL(q.getName()) + "', " + "}\" ";
		String cssclass = "answerunknown";

		if (isUnknown) {
			cssclass = "answerunknownClicked";
		}
		else if (value != null) {
			cssclass = "answerunknown";
		}
		String prompt = MMInfo.getUnknownPrompt(q, null);

		if (!(q instanceof QuestionNum)) {
			// separator already rendered in renderNumAnswers
			renderChoiceSeparator(sb);
		}
		appendEnclosingTagOnClick("div", prompt, cssclass, jscall, null,
				null, null, sb);
	}

	/**
	 * Assembles the HTML representation for a given Tag
	 *
	 * @param tag         The String representation of the tag
	 * @param text        The text to be placed inside the tag
	 * @param cssclass    The css class to style the resulting tag
	 * @param onclick     The String representation of the onclick action, i.e., a
	 *                    JS call
	 * @param onmouseover Something to happen regarding the onmouseover
	 * @param id          The id of the object represented , i.e., answer alternative,
	 *                    here
	 * @created 22.07.2010
	 */
	private void appendEnclosingTagOnClick(String tag, String text,
										   String cssclass, String onclick, String onmouseover, String id,
										   String title, RenderResult result) {
		result.appendHtml("<" + tag);
		if (id != null && id.length() > 0) {
			result.appendHtml(" id='" + Strings.encodeHtml(id) + "' ");
		}
		if (onclick != null && onclick.length() > 0) {
			result.appendHtml(" " + onclick + " ");
		}
		if (onmouseover != null && onmouseover.length() > 0) {
			result.appendHtml(" " + onmouseover + " ");
		}
		if (title != null && title.length() > 0) {
			cssclass = cssclass + " tooltipster";
			result.appendHtml(" title='" + Strings.encodeHtml(title) + "' ");
		}
		if (cssclass != null && cssclass.length() > 0) {
			result.appendHtml(" class='" + cssclass + "'");
		}
		result.appendHtml(">");
		result.append(Strings.encodeHtml(text));
		result.appendHtml("</" + tag + ">");
	}

	/**
	 * Checks, whether an answer value was already processed in the current
	 * session
	 *
	 * @param sessionValue the sessionValue
	 * @param value        the value to be checked
	 * @return true if the given session value contains the checked value (MC
	 * Questions) or if the session value equals the value
	 * @created 27.08.2010
	 */
	private boolean isAnsweredinCase(Value sessionValue, Value value) {
		// test for MC values separately
		if (sessionValue instanceof MultipleChoiceValue) {
			return ((MultipleChoiceValue) sessionValue).contains(value);
		}
		else {
			return sessionValue.equals(value);
		}
	}

	/**
	 * Checks, whether the given TerminologyObject is currently visible or not.
	 *
	 * @param to the TerminologyObject to be checked.
	 * @return true, if the given TerminologyObject is indicated.
	 * @created 30.10.2010
	 */
	private boolean isVisible(TerminologyObject to) {

		if (to == to.getKnowledgeBase().getRootQASet()) return true;
		if (isThisOrFollowUpIndicated(to)) return true;
		if (to instanceof Question) {
			for (TerminologyObject parent : to.getParents()) {
				if (parent instanceof QContainer) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isThisOrFollowUpIndicated(TerminologyObject to) {
		Set<TerminologyObject> visited = new HashSet<TerminologyObject>();
		return isThisOrFollowUpIndicated(visited, to);
	}

	private boolean isThisOrFollowUpIndicated(Set<TerminologyObject> visited, TerminologyObject to) {
		if (visited.contains(to)) return false;
		visited.add(to);
		if (session.getBlackboard().getIndication((InterviewObject) to)
				.isRelevant()) {
			return true;
		}
		if (to.getChildren().length > 0) {
			for (TerminologyObject child : to.getChildren()) {
				if (isThisOrFollowUpIndicated(visited, child)) return true;
			}
		}
		return false;
	}

	private String getLabel(NamedObject to) {
		String prompt = to.getInfoStore().getValue(
				MMInfo.PROMPT,
				Environment.getInstance().getWikiConnector()
						.getLocale(user.getRequest()));
		if (prompt != null) return prompt;
		return to.getName();
	}

	private String getID() {
		return "quicki" + counter++;
	}

}
