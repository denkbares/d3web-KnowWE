/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.testcases;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.Cookie;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyManager;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.utilities.NamedObjectComparator;
import de.d3web.core.utilities.Pair;
import de.d3web.core.utilities.Triple;
import de.d3web.testcase.TestCaseUtils;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.model.Finding;
import de.d3web.testcase.model.TestCase;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.knowledgebase.KnowledgeBaseType;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.OutDatedSessionNotification;

/**
 * Renderer for TestCasePlayerType
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 19.01.2012
 */
public class TestCasePlayerRenderer implements Renderer {

	private static final String QUESTIONS_SEPARATOR = "#####";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static String SELECTOR_KEY = "selectedValue";
	private static String QUESTION_SELECTOR_KEY = "question_selector";
	private static String SIZE_SELECTOR_KEY = "size_selector";
	private static String FROM_KEY = "from_position";

	@Override
	public void render(Section<?> section, UserContext user, StringBuilder result) {
		Section<TestCasePlayerType> playerSection =
				Sections.cast(section.getFather(), TestCasePlayerType.class);
		StringBuilder string = new StringBuilder();
		if (user == null || user.getSession() == null) {
			return;
		}
		List<Triple<TestCaseProvider, Section<?>, Article>> providers =
				getTestCaseProviders(playerSection);

		string.append(Strings.maskHTML("<div id='" + section.getID() + "'>"));

		if (providers.size() == 0) {
			StringBuilder message = new StringBuilder();
			message.append("No test cases found in the packages: ");
			boolean first = true;
			for (String s : DefaultMarkupType.getPackages(playerSection,
					KnowledgeBaseType.ANNOTATION_COMPILE)) {
				if (!first) {
					message.append(", ");
				}
				message.append(s);
				first = false;
			}
			DefaultMarkupRenderer.renderMessagesOfType(Type.WARNING,
					Arrays.asList(Messages.warning(message.toString())), string);
		}
		else {
			Triple<TestCaseProvider, Section<?>, Article> selectedTriple = renderTestCaseSelection(
					section,
					user, string, providers);
			if (selectedTriple != null) {
				String link = "<a href='"
						+ Strings.maskHTML(KnowWEUtils.getURLLink(selectedTriple.getB()))
						+ "'><img src='KnowWEExtension/testcaseplayer/icon/testcaselink.png'></a>";
				string.append(Strings.maskHTML(link));
				TestCaseProvider provider = selectedTriple.getA();
				Session session = provider.getActualSession(user);
				List<Message> messages = provider.getMessages();
				if (messages.size() > 0) {
					DefaultMarkupRenderer.renderMessagesOfType(Type.ERROR, messages,
							string);
				}
				if (session == null) {
					DefaultMarkupRenderer.renderMessagesOfType(Type.WARNING,
							Arrays.asList(Messages.warning("No knowledge base found.")), string);
				}
				else {
					TestCase testCase = provider.getTestCase();
					SessionDebugStatus status = provider.getDebugStatus(user);

					if (status.getSession() != session) {
						status.setSession(session);
					}

					if (testCase != null) {
						try {
							renderTestCase(section, user, string, selectedTriple, session,
									testCase,
									status);
						}
						catch (IllegalArgumentException e) {
							DefaultMarkupRenderer.renderMessagesOfType(
									Type.ERROR,
									Arrays.asList(Messages.error("Test case not compatible to TestCasePlayer: "
											+ e.getMessage())), string);
						}
					}
					else {
						string.append("\nNo TestCase contained!\n");
					}
				}
			}
		}
		string.append(Strings.maskHTML("</div>"));
		result.append(string.toString());
	}

	private void renderTestCase(Section<?> section, UserContext user, StringBuilder string, Triple<TestCaseProvider, Section<?>, Article> selectedTriple, Session session, TestCase testCase, SessionDebugStatus status) {
		string.append(Strings.maskHTML("<span class='fillText'> from </span>"));
		if (testCase.getStartDate().getTime() != 0) {
			string.append(dateFormat.format(testCase.getStartDate()));
		}
		else {
			string.append("---");
		}
		string.append(Strings.maskHTML("<div class='toolSeparator'></div>"));

		String additionalQuestions = getAdditionalQuestionsCookie(section, user);
		String[] questionStrings = new String[0];
		if (additionalQuestions != null && !additionalQuestions.isEmpty()) {
			questionStrings = additionalQuestions.split(QUESTIONS_SEPARATOR);
		}
		Collection<Question> usedQuestions = TestCaseUtils.getUsedQuestions(testCase,
				session.getKnowledgeBase());
		Collection<Date> chronology = testCase.chronology();
		String sizeKey = SIZE_SELECTOR_KEY + "_" + section.getID();
		String fromKey = FROM_KEY + "_" + section.getID();
		TableParameters tableParameters = getTableParameters(user, chronology, sizeKey,
				fromKey);

		TerminologyManager manager = session.getKnowledgeBase().getManager();
		TableModel tableModel = new TableModel();
		String kbArticle = selectedTriple.getC().getTitle();
		KnowledgeBase base = D3webUtils.getKnowledgeBase(user.getWeb(), kbArticle);

		// check if the latest knowledge base is used
		if (base != null) {
			if (SessionProvider.hasOutDatedSession(user, base)) {
				NotificationManager.addNotification(user,
						new OutDatedSessionNotification(kbArticle));
			}
		}

		TerminologyObject selectedObject = renderHeader(section, user, kbArticle, status,
				additionalQuestions, questionStrings, usedQuestions, manager,
				tableModel);
		int row = 1;
		for (Date date : chronology) {
			if (row < tableParameters.from) {
				row++;
				continue;
			}
			if (row > tableParameters.to) break;
			renderTableLine(selectedTriple, testCase, status, questionStrings,
					usedQuestions,
					manager, selectedObject, date, row - tableParameters.from + 1,
					tableModel);
			row++;
		}
		string.append(
				renderTableSizeSelector(section, user, sizeKey, tableParameters.size,
						chronology.size()));
		string.append(renderNavigation(section, tableParameters.from,
				tableParameters.size, fromKey, chronology.size()));
		string.append(tableModel.toHtml(section, user));
	}

	private TerminologyObject renderHeader(Section<?> section, UserContext user, String kbArticle, SessionDebugStatus status, String additionalQuestions, String[] questionStrings, Collection<Question> usedQuestions, TerminologyManager manager, TableModel tableModel) {
		tableModel.addCell(
				0,
				0,
				Strings.maskHTML(renderToolbarButton("stop12.png",
						"KNOWWE.plugin.d3webbasic.actions.resetSession('" + kbArticle
								+ "')")), 1);
		tableModel.addCell(0, 1, "Time", "Time".length());
		int column = 2;
		for (Question q : usedQuestions) {
			tableModel.addCell(0, column++, q.getName(), q.getName().length());
		}
		tableModel.addCell(0, column++, "Checks", "Checks".length());
		renderObservationQuestionsHeader(status, additionalQuestions, questionStrings,
				manager, tableModel, column);
		column += questionStrings.length;
		TerminologyObject selectedObject = renderObservationQuestionAdder(section,
				user,
				questionStrings, manager, additionalQuestions,
				tableModel, column++);
		return selectedObject;
	}

	private TableParameters getTableParameters(UserContext user, Collection<Date> chronology, String sizeKey, String fromKey) {
		TableParameters tableParameters = new TableParameters();
		String selectedSizeString = "10";
		String fromString = "1";
		for (Cookie cookie : user.getRequest().getCookies()) {
			if (cookie.getName().equals(sizeKey)) {
				selectedSizeString = cookie.getValue();
			}
			else if (cookie.getName().equals(fromKey)) {
				fromString = cookie.getValue();
			}
		}
		tableParameters.size = Integer.parseInt(selectedSizeString);
		tableParameters.from = 1;
		if (fromString != null) {
			try {
				tableParameters.from = Integer.parseInt(fromString);
				if (tableParameters.from < 1) {
					tableParameters.from = 1;
				}
			}
			catch (NumberFormatException e) {
				tableParameters.from = 1;
			}
		}
		tableParameters.to = tableParameters.from + tableParameters.size - 1;
		if (tableParameters.to > chronology.size()) {
			int tempfrom = tableParameters.from - (tableParameters.to - chronology.size());
			if (tempfrom > 0) {
				tableParameters.from = tempfrom;
			}
			else {
				tableParameters.from = 1;
			}
			// user.getSession().setAttribute(fromKey,
			// String.valueOf(tableParameters.from));
			tableParameters.to = chronology.size();
		}
		return tableParameters;
	}

	private String getAdditionalQuestionsCookie(Section<?> section, UserContext user) {
		String additionalQuestions = null;
		String cookiename = "additionalQuestions" + section.getTitle();
		Cookie[] cookies = user.getRequest().getCookies();
		if (cookies != null) for (Cookie cookie : cookies) {
			if (Strings.decodeURL(cookie.getName()).equals(cookiename)) {
				additionalQuestions = Strings.decodeURL(cookie.getValue());
				break;
			}
		}
		return additionalQuestions;
	}

	public static List<Triple<TestCaseProvider, Section<?>, Article>> getTestCaseProviders(Section<TestCasePlayerType> section) {
		String[] kbpackages = DefaultMarkupType.getPackages(section,
				KnowledgeBaseType.ANNOTATION_COMPILE);
		String web = section.getWeb();
		return de.knowwe.testcases.TestCaseUtils.getTestCaseProviders(kbpackages, web);
	}

	private void renderTableLine(Triple<TestCaseProvider, Section<?>, Article> selectedTriple, TestCase testCase, SessionDebugStatus status, String[] questionStrings, Collection<Question> usedQuestions, TerminologyManager manager, TerminologyObject selectedObject, Date date, int row, TableModel tableModel) {
		String dateString = String.valueOf(date.getTime());
		renderRunTo(selectedTriple, status, date, dateString, tableModel, row);
		int column = 1;
		// render date cell
		String timeAsTimeStamp = TimeStampType.createTimeAsTimeStamp(date.getTime()
				- testCase.getStartDate().getTime());
		tableModel.addCell(row, column++, timeAsTimeStamp, timeAsTimeStamp.length());
		// render values of questions
		for (Question q : usedQuestions) {
			Finding finding = testCase.getFinding(date, q);
			if (finding != null) {
				String findingString = finding.getValue().toString();
				Collection<String> errors = new LinkedList<String>();
				TestCaseUtils.checkValues(errors, q, finding.getValue());
				if (!errors.isEmpty()) {
					String errorstring = Strings.maskHTML("<div style='background-color:"
							+ StyleRenderer.CONDITION_FALSE + "'>");
					errorstring += findingString;
					errorstring += Strings.maskHTML("</div>");
					findingString = errorstring;
				}
				tableModel.addCell(row, column, findingString,
						finding.getValue().toString().length());
			}
			column++;
		}
		renderCheckResults(testCase, status, date, tableModel, row, column++);
		// render observations
		for (String s : questionStrings) {
			TerminologyObject object = manager.search(s);
			if (object != null) {
				appendValueCell(status, object, date, tableModel, row, column);
			}
			column++;
		}
		if (selectedObject != null) {
			appendValueCell(status, selectedObject, date, tableModel, row, column++);
		}
	}

	private void appendValueCell(SessionDebugStatus status, TerminologyObject object, Date date, TableModel tableModel, int row, int column) {
		Value value = status.getValue(object, date);
		if (value != null) {
			tableModel.addCell(row, column, value.toString(), value.toString().length());
		}
	}

	private void renderObservationQuestionsHeader(SessionDebugStatus status, String additionalQuestions, String[] questionStrings, TerminologyManager manager, TableModel tableModel, int column) {
		for (String s : questionStrings) {
			TerminologyObject object = manager.search(s);
			StringBuilder sb = new StringBuilder();
			if (object instanceof Question || object instanceof Solution) {
				sb.append(s);
			}
			else {
				sb.append(Strings.maskHTML("<span style='color:silver'>" + s + "</span>"));
			}
			String newQuestionsString = additionalQuestions;
			newQuestionsString = newQuestionsString.replace(s, "");
			newQuestionsString = newQuestionsString.replace(QUESTIONS_SEPARATOR
					+ QUESTIONS_SEPARATOR,
					QUESTIONS_SEPARATOR);
			if (newQuestionsString.startsWith(QUESTIONS_SEPARATOR)) {
				newQuestionsString = newQuestionsString.replaceFirst(
						QUESTIONS_SEPARATOR, "");
			}
			if (newQuestionsString.endsWith(QUESTIONS_SEPARATOR)) {
				newQuestionsString = newQuestionsString.substring(0,
						newQuestionsString.length() - QUESTIONS_SEPARATOR.length());
			}
			sb.append(Strings.maskHTML(" <input type=\"button\" value=\"-\" onclick=\"TestCasePlayer.addCookie(&quot;"
					+ newQuestionsString
					+ "&quot;);\">"));
			tableModel.addCell(0, column++, sb.toString(), s.length() + 2);
		}
	}

	private void renderRunTo(Triple<TestCaseProvider, Section<?>, Article> selectedTriple, SessionDebugStatus status, Date date, String dateString, TableModel tableModel, int row) {
		if (status.getLastExecuted() == null
				|| status.getLastExecuted().before(date)) {
			StringBuffer sb = new StringBuffer();
			String js = "TestCasePlayer.send("
					+ "'"
					+ selectedTriple.getB().getID()
					+ "', '" + dateString
					+ "', '" + selectedTriple.getA().getName()
					+ "', '" + selectedTriple.getC().getTitle() + "');";
			sb.append("<a href=\"javascript:" + js + ";undefined;\">");
			sb.append("<img src='KnowWEExtension/testcaseplayer/icon/runto.png'>");
			sb.append("</a>");
			tableModel.addCell(row, 0, Strings.maskHTML(sb.toString()), 2);
		}
		else {
			Collection<Pair<Check, Boolean>> checkResults = status.getCheckResults(date);
			boolean ok = true;
			if (checkResults != null) {
				for (Pair<Check, Boolean> pair : checkResults) {
					ok &= pair.getB();
				}
			}
			if (ok) {
				tableModel.addCell(
						row,
						0,
						Strings.maskHTML("<img src='KnowWEExtension/testcaseplayer/icon/done.png'>"),
						2);
			}
			else {
				tableModel.addCell(
						row,
						0,
						Strings.maskHTML("<img src='KnowWEExtension/testcaseplayer/icon/error.png'>"),
						2);
			}
		}
	}

	private void renderCheckResults(TestCase testCase, SessionDebugStatus status, Date date, TableModel tableModel, int row, int column) {
		Collection<Pair<Check, Boolean>> checkResults = status.getCheckResults(date);
		int max = 0;
		StringBuilder sb = new StringBuilder();
		if (checkResults == null) {
			boolean first = true;
			for (Check c : testCase.getChecks(date, status.getSession().getKnowledgeBase())) {
				if (!first) sb.append(Strings.maskHTML("<br />"));
				first = false;
				sb.append(c.getCondition());
				max = Math.max(max, c.getCondition().toString().length());
			}
		}
		else {
			if (!checkResults.isEmpty()) {
				boolean first = true;
				for (Pair<Check, Boolean> p : checkResults) {
					max = Math.max(max, p.getA().getCondition().toString().length());
					if (!first) sb.append(Strings.maskHTML("<br />"));
					first = false;
					String color;
					if (p.getB()) {
						color = StyleRenderer.CONDITION_FULLFILLED;
					}
					else {
						color = StyleRenderer.CONDITION_FALSE;
					}
					sb.append(Strings.maskHTML("<span style='background-color:" + color + "'>"));
					sb.append(p.getA().getCondition());
					sb.append(Strings.maskHTML("</span>"));
				}
			}
		}
		tableModel.addCell(row, column, sb.toString(), max);
	}

	private TerminologyObject renderObservationQuestionAdder(Section<?> section, UserContext user, String[] questionStrings, TerminologyManager manager, String questionString, TableModel tableModel, int column) {
		String key = QUESTION_SELECTOR_KEY + "_" + section.getID();
		String selectedQuestion = "";
		for (Cookie cookie : user.getRequest().getCookies()) {
			if (cookie.getName().equals(key)) {
				selectedQuestion = Strings.decodeURL(cookie.getValue());
			}
		}
		TerminologyObject object = null;
		StringBuffer selectsb2 = new StringBuffer();
		selectsb2.append("<form><select name=\"toAdd\" id=adder"
				+ section.getID()
				+ " onchange=\"TestCasePlayer.change('"
				+ key
				+ "', this.options[this.selectedIndex].value);\">");
		HashSet<String> alreadyAddedQuestions = new HashSet<String>(Arrays.asList(questionStrings));
		selectsb2.append("<option value='--'>--</option>");
		boolean foundone = false;
		List<TerminologyObject> objects = new LinkedList<TerminologyObject>();
		objects.addAll(manager.getQuestions());
		objects.addAll(manager.getSolutions());
		Collections.sort(objects, new NamedObjectComparator());
		int max = 0;
		for (TerminologyObject q : objects) {
			if (!alreadyAddedQuestions.contains(q.getName())) {
				max = Math.max(max, q.getName().toString().length());
				if (q.getName().equals(selectedQuestion)) {
					selectsb2.append("<option selected='selected' value='" + q.getName() + "'>"
							+ q.getName() + "</option>");
					object = q;
				}
				else {
					selectsb2.append("<option value='" + q.getName() + "'>" + q.getName()
							+ "</option>");
				}
				foundone = true;
			}
		}
		selectsb2.append("</select>");
		// reset value because -- is selected
		if (object == null) {
			user.getSession().setAttribute(key, "");
		}
		if (object != null && !object.getName().equals(selectedQuestion)) {
			user.getSession().setAttribute(key, object.getName());
		}
		if (questionString != null && !questionString.isEmpty()) {
			selectsb2.append("<input "
					+
					(object == null ? "disabled='disabled'" : "")
					+ " type=\"button\" value=\"+\" onclick=\"TestCasePlayer.addCookie(&quot;"
					+ questionString
					+ QUESTIONS_SEPARATOR
					+ "&quot;+this.form.toAdd.options[toAdd.selectedIndex].value);TestCasePlayer.change('"
					+ key
					+ "','');\"></form>");
		}
		else {
			selectsb2.append("<input "
					+
					(object == null ? "disabled='disabled'" : "")
					+ "type=\"button\" value=\"+\" onclick=\"TestCasePlayer.addCookie(this.form.toAdd.options[toAdd.selectedIndex].value);TestCasePlayer.change('"
					+ key
					+ "','');\"></form>");
		}
		if (foundone) {
			tableModel.addCell(0, column, Strings.maskHTML(selectsb2.toString()), max + 3);
		}
		return object;
	}

	private Triple<TestCaseProvider, Section<?>, Article> renderTestCaseSelection(Section<?> section, UserContext user, StringBuilder string, List<Triple<TestCaseProvider, Section<?>, Article>> providers) {
		String key = generateSelectedTestCaseCookieKey(section);
		String selectedID = "";
		for (Cookie cookie : user.getRequest().getCookies()) {
			if (cookie.getName().equals(key)) {
				selectedID = Strings.decodeURL(cookie.getValue());
			}
		}
		StringBuffer selectsb = new StringBuffer();
		// if no pair is selected, use the first
		Triple<TestCaseProvider, Section<?>, Article> selectedTriple = null;
		selectsb.append("<span class=fillText>Case </span>"
				+ "<select id=selector" + section.getID()
				+ " onchange=\"TestCasePlayer.change('" + key
				+ "', this.options[this.selectedIndex].value);\">");
		Set<String> ids = new HashSet<String>();
		boolean unique = true;
		for (Triple<TestCaseProvider, Section<?>, Article> triple : providers) {
			unique &= ids.add(triple.getA().getName());
		}
		for (Triple<TestCaseProvider, Section<?>, Article> triple : providers) {
			if (triple.getA().getTestCase() != null) {
				if (selectedTriple == null) {
					selectedTriple = triple;
				}
				String id = triple.getC().getTitle() + "/" + triple.getA().getName();
				String displayedID = (unique) ? triple.getA().getName() : id;
				if (id.equals(selectedID)) {
					selectsb.append("<option value='" + id + "' selected='selected'>"
							+ displayedID + "</option>");
					selectedTriple = triple;
				}
				else {
					selectsb.append("<option value='" + id + "'>"
							+ displayedID + "</option>");
				}
			}
		}
		selectsb.append("</select>");
		if (selectedTriple != null) {
			string.append(Strings.maskHTML(selectsb.toString()));
		}
		else {
			DefaultMarkupRenderer.renderMessagesOfType(
					Type.WARNING,
					Arrays.asList(Messages.warning("There are testcase sections in the specified packages, but none of them generates a testcase.")),
					string);
		}
		return selectedTriple;
	}

	public static String generateSelectedTestCaseCookieKey(Section<?> section) {
		int i = 1;
		List<Section<TestCasePlayerType>> sections = Sections.findSuccessorsOfType(
				section.getArticle().getRootSection(), TestCasePlayerType.class);
		Section<TestCasePlayerType> testCasePlayerTypeSection = Sections.findAncestorOfExactType(
				section, TestCasePlayerType.class);
		for (Section<TestCasePlayerType> s : new TreeSet<Section<TestCasePlayerType>>(sections)) {
			if (testCasePlayerTypeSection.equals(s)) {
				break;
			}
			else {
				i++;
			}
		}
		return SELECTOR_KEY + "_" + Strings.encodeURL(section.getTitle()) + i;
	}

	private String renderTableSizeSelector(Section<?> section, UserContext user, String key, int selectedSize, int maxSize) {
		StringBuilder builder = new StringBuilder();

		int[] sizeArray = new int[] {
				1, 2, 5, 10, 20, 50, 100 };
		builder.append("<div class='toolBar'>");
		builder.append("<span class=fillText>Show </span>"
				+ "<select id=sizeSelector"
				+ section.getID()
				+ " onchange=\"TestCasePlayer.change('"
				+ key
				+ "', this.options[this.selectedIndex].value);\">");
		for (int size : sizeArray) {
			if (size == selectedSize) {
				builder.append("<option selected='selected' value='" + size + "'>"
						+ size + "</option>");
			}
			else {
				builder.append("<option value='" + size + "'>" + size
						+ "</option>");
			}
		}
		builder.append("</select><span class=fillText> lines of </span>" + maxSize);
		builder.append("<div class='toolSeparator'></div>");
		builder.append("</div>");
		return Strings.maskHTML(builder.toString());
	}

	private Object renderNavigation(Section<?> section, int from, int selectedSize, String key, int maxsize) {
		StringBuilder builder = new StringBuilder();
		int previous = Math.max(1, from - selectedSize);
		int next = from + selectedSize;

		builder.append("<div class='toolBar avoidMenu'>");
		renderToolbarButton(
				"begin.png", "TestCasePlayer.change('" + key + "', " + 1 + ")",
				(from > 1), builder);
		renderToolbarButton(
				"back.png", "TestCasePlayer.change('" + key + "', " + previous + ")",
				(from > 1), builder);
		builder.append("<span class=fillText> Lines </span>");
		builder.append("<input size=3 type=\"field\" onchange=\"TestCasePlayer.change('"
				+ key
				+ "', " + "this.value);\" value='" + from + "'>");
		builder.append("<span class=fillText> to </span>" + (from + selectedSize - 1));
		renderToolbarButton(
				"forward.png", "TestCasePlayer.change('" + key + "', " + next + ")",
				(from + selectedSize <= maxsize), builder);
		renderToolbarButton(
				"end.png", "TestCasePlayer.change('" + key + "', " + maxsize + ")",
				(from + selectedSize <= maxsize), builder);
		builder.append("</div>");
		return Strings.maskHTML(builder.toString());
	}

	private String renderToolbarButton(String icon, String action) {
		StringBuilder buffer = new StringBuilder();
		renderToolbarButton(icon, action, true, buffer);
		String buttonHTML = buffer.toString();
		return buttonHTML;
	}

	private void renderToolbarButton(String icon, String action, boolean enabled, StringBuilder builder) {
		int index = icon.lastIndexOf('.');
		String suffix = icon.substring(index);
		icon = icon.substring(0, index);
		if (enabled) {
			builder.append("<a onclick=\"");
			builder.append(action);
			builder.append(";\">");
		}
		builder.append("<span class='toolButton ");
		builder.append(enabled ? "enabled" : "disabled");
		builder.append("'>");
		builder.append("<img src='KnowWEExtension/testcaseplayer/icon/");
		builder.append(icon);
		if (!enabled) builder.append("_deactivated");
		builder.append(suffix).append("'></img></span>");
		if (enabled) {
			builder.append("</a>");
		}
	}

	/**
	 * Encapsules parameters for the table
	 * 
	 * @author Markus Friedrich (denkbares GmbH)
	 * @created 29.02.2012
	 */
	private static class TableParameters {

		private int from;
		private int to;
		private int size;
	}
}
