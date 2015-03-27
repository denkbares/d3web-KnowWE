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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyManager;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.utilities.NamedObjectComparator;
import de.d3web.strings.Strings;
import de.d3web.strings.Strings.Encoding;
import de.d3web.testcase.TestCaseUtils;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.model.Finding;
import de.d3web.testcase.model.TestCase;
import de.d3web.testcase.stc.CommentedTestCase;
import de.d3web.utils.Pair;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.renderer.PaginationRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.OutDatedSessionNotification;
import de.knowwe.testcases.table.KnowWEConditionCheck;
import de.knowwe.util.Icon;
import de.knowwe.util.IconColor;

/**
 * Renderer for TestCasePlayerType
 *
 * @author Markus Friedrich (denkbares GmbH)
 * @created 19.01.2012
 */
public class TestCasePlayerRenderer implements Renderer {

	private static final String QUESTIONS_SEPARATOR = "#####";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static final Pattern[] cookiePatterns = { Pattern.compile("^columnstatus_([^_]+)_.*"),
			Pattern.compile("^question_selector_([^_]+)_.*") };

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		RenderResult string = new RenderResult(result);
		if (user == null || user.getSession() == null) {
			return;
		}
		for (Pattern cookiePattern : cookiePatterns) {
			KnowWEUtils.cleanupSectionCookies(user, cookiePattern, 1);
		}
		Section<TestCasePlayerType> playerSection =
				Sections.cast(section.getParent(), TestCasePlayerType.class);
		List<ProviderTriple> providers =
				de.knowwe.testcases.TestCaseUtils.getTestCaseProviders(user, playerSection);

		string.appendHtml("<div class='TestCasePlayerContent' id='" + section.getID() + "'>");

		if (providers.size() == 0) {
			renderNoProviderWarning(playerSection, string);
		}
		else {
			RenderResult caseSelectionResult = new RenderResult(string);
			ProviderTriple selectedTriple = getAndRenderTestCaseSelection(
					section, user, providers, caseSelectionResult);
			renderOverallStatus(selectedTriple, user, string);
			string.append(caseSelectionResult);
			if (selectedTriple != null) {
				renderSelectedTestCase(section, user, selectedTriple, string);
			}
		}
		string.appendHtml("</div>");
		result.append(string.toStringRaw());
	}

	private void renderOverallStatus(ProviderTriple selectedTriple, UserContext user, RenderResult string) {
		int failureCount = selectedTriple.getProvider().getDebugStatus(user).getFailureCount();
		string.appendHtmlTag("div", "style",
				"display:inline-block; vertical-align:middle; text-align:center; padding: 0 12px 0 8px");
		string.appendHtmlTag("span", "style", "position:relative; top:-1px;");
		if (failureCount == 0) {
			string.appendHtml(Icon.BULB.addColor(IconColor.OK).addTitle("No check failures").toHtml());
		}
		else if (failureCount == 1) {
			string.appendHtml(Icon.BULB.addColor(IconColor.ERROR)
					.addTitle("There is one failed check failure")
					.toHtml());
		}
		else {
			string.appendHtml(Icon.BULB.addColor(IconColor.ERROR)
					.addTitle("There are " + failureCount + " failed checks")
					.toHtml());
		}
		string.appendHtml("</span>");
		string.appendHtmlTag("/div");
	}

	private void renderSelectedTestCase(Section<?> section, UserContext user, ProviderTriple selectedTriple, RenderResult string) {
		// renderLinkToTestCase(selectedTriple, string);
		// renderDownloadLink(section, string);
		// string.appendHTML(renderToolSeparator());
		TestCaseProvider provider = selectedTriple.getA();
		renderProviderMessages(provider, string);
		Session session = provider.getActualSession(user);
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
					renderTestCase(section, user, selectedTriple, session, testCase,
							status, string);
				}
				catch (IllegalArgumentException e) {
					Message error = Messages.error("Test case not compatible to TestCasePlayer: "
							+ e.getMessage());
					DefaultMarkupRenderer.renderMessagesOfType(
							Type.ERROR, Arrays.asList(error), string);
				}
			}
			else {
				string.append("\nNo TestCase contained!\n");
			}
		}
	}

	private void renderProviderMessages(TestCaseProvider provider, RenderResult string) {
		List<Message> messages = provider.getMessages();
		if (messages.size() > 0) {
			DefaultMarkupRenderer.renderMessagesOfType(Type.ERROR, messages,
					string);
		}
	}

	private void renderNoProviderWarning(Section<TestCasePlayerType> playerSection, RenderResult string) {
		String message = "No test cases found in the packages: " + Strings.concat(", ", de.knowwe.testcases.TestCaseUtils
				.getTestCasePackages(playerSection));
		DefaultMarkupRenderer.renderMessagesOfType(Type.WARNING, Arrays.asList(Messages.warning(message)), string);
	}

	private void renderTestCase(Section<?> section, UserContext user, ProviderTriple selectedTriple, Session session, TestCase testCase, SessionDebugStatus status, RenderResult string) {
		Collection<Date> chronology = testCase.chronology();

		NavigationParameters navigatorParameters = getNavigationParameters(section, user);

		renderTestCaseHeader(section, user, testCase, chronology, string);

		TableModel tableModel = getTableModel(section, user, selectedTriple, session, testCase,
				status, chronology, navigatorParameters);

		string.append(tableModel.toHtml(section, user));
	}

	private TableModel getTableModel(Section<?> section, UserContext user, ProviderTriple selectedTriple, Session session, TestCase testCase, SessionDebugStatus status, Collection<Date> chronology, NavigationParameters navigatorParameters) {
		TerminologyManager manager = session.getKnowledgeBase().getManager();
		TableModel tableModel = new TableModel(user);
		tableModel.setName(getTestCaseId(selectedTriple));
		KnowledgeBase base = D3webUtils.getKnowledgeBase(section);

		// check if the latest knowledge base is used
		if (base != null) {
			if (SessionProvider.hasOutDatedSession(user, base)) {
				NotificationManager.addNotification(user,
						new OutDatedSessionNotification(selectedTriple.getKbSection().getID()));
			}
		}

		Collection<String> additionalQuestions = getAdditionalQuestions(section, user);

		Collection<Question> usedQuestions = TestCaseUtils.getUsedQuestions(testCase,
				session.getKnowledgeBase());

		TerminologyObject selectedObject = renderHeader(section, user, selectedTriple,
				additionalQuestions, usedQuestions, manager, tableModel);
		int row = 1;
		for (Date date : chronology) {
			if (row < navigatorParameters.from) {
				row++;
				continue;
			}
			if (row > navigatorParameters.to) break;
			renderRow(user, selectedTriple, status, additionalQuestions,
					usedQuestions, manager, selectedObject, date,
					row - navigatorParameters.from + 1, tableModel);
			row++;
		}
		return tableModel;
	}

	private Collection<String> getAdditionalQuestions(Section<?> section, UserContext user) {
		String additionalQuestions = getAdditionalQuestionsCookie(section, user);
		String[] additionalQuestionsSplit = new String[0];
		if (additionalQuestions != null && !additionalQuestions.isEmpty()) {
			additionalQuestionsSplit = additionalQuestions.split(QUESTIONS_SEPARATOR);
		}
		return new LinkedHashSet<>(Arrays.asList(additionalQuestionsSplit));
	}

	private void renderTestCaseHeader(Section<?> section, UserContext user, TestCase testCase, Collection<Date> chronology, RenderResult string) {
		string.appendHtml("<span class='fillText'> Start: </span>");
		if (testCase.getStartDate().getTime() == 0) {
			string.append("---");
		}
		else {
			string.append(dateFormat.format(testCase.getStartDate()));
		}
		string.appendHtml(PaginationRenderer.getToolSeparator());

		PaginationRenderer.setResultSize(user, chronology.size());
		boolean show = chronology.size() > 10;
		PaginationRenderer.renderPagination(section, user, string, show);

	}

	private TerminologyObject renderHeader(Section<?> section, UserContext user, ProviderTriple selectedTriple, Collection<String> additionalQuestions, Collection<Question> usedQuestions, TerminologyManager manager, TableModel tableModel) {
		Section<? extends PackageCompileType> kbsection = selectedTriple.getC();
		String stopButton = renderToolbarButton(Icon.STOP.addClasses("knowwe-red"),
				"KNOWWE.plugin.d3webbasic.actions.resetSession('" + kbsection.getID()
						+ "', TestCasePlayer.init);", user);
		RenderResult stopButtonResult = new RenderResult(tableModel.getUserContext());
		stopButtonResult.appendHtml(stopButton);
		int column = 0;
		tableModel.addCell(0, column++, stopButtonResult, 1);
		if (selectedTriple.getProvider().getTestCase() instanceof CommentedTestCase) {
			tableModel.addCell(0, column++, "Name", "Name".length());
		}
		tableModel.addCell(0, column++, "Time", "Time".length());
		tableModel.addCell(0, column++, "Checks", "Checks".length());
		tableModel.setFirstFinding(column);
		for (Question q : usedQuestions) {
			tableModel.addCell(0, column++, q.getName(), q.getName().length());
		}
		tableModel.setLastFinding(column - 1);
		renderObservationQuestionsHeader(additionalQuestions,
				manager, tableModel, column);
		column += additionalQuestions.size();
		return renderObservationQuestionAdder(section,
				user, manager, additionalQuestions, tableModel, column);
	}

	private NavigationParameters getNavigationParameters(Section<?> section, UserContext user) {

		NavigationParameters tableParameters = new NavigationParameters();
		int selectedSizeString = PaginationRenderer.getCount(section, user);
		int fromString = PaginationRenderer.getStartRow(section, user);

		tableParameters.size = selectedSizeString;
		tableParameters.from = fromString;
		tableParameters.to = tableParameters.from + tableParameters.size - 1;

		return tableParameters;
	}

	private String getAdditionalQuestionsCookie(Section<?> section, UserContext user) {
		String additionalQuestions = null;
		String cookiename = "additionalQuestions" + section.getTitle();
		Cookie[] cookies = user.getRequest().getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (Strings.decodeURL(cookie.getName(), Encoding.ISO_8859_1).equals(cookiename)) {
					additionalQuestions = Strings.decodeURL(cookie.getValue(), Encoding.ISO_8859_1);
					break;
				}
			}
		}
		return additionalQuestions;
	}

	private void renderRow(UserContext user, ProviderTriple selectedTriple, SessionDebugStatus status, Collection<String> additionalQuestions, Collection<Question> usedQuestions, TerminologyManager manager, TerminologyObject selectedObject, Date date, int row, TableModel tableModel) {
		TestCase testCase = selectedTriple.getProvider().getTestCase();
		String dateString = String.valueOf(date.getTime());
		renderRunTo(selectedTriple, status, date, dateString, tableModel, row);
		int column = 1;
		// render date cell
		String timeAsTimeStamp = Strings.getDurationVerbalization(date.getTime()
				- testCase.getStartDate().getTime());
		if (testCase instanceof CommentedTestCase) {
			RenderResult sb = new RenderResult(tableModel.getUserContext());
			sb.appendHtml("<br />");
			String comment = ((CommentedTestCase) testCase).getComment(date).replace("\n", sb.toStringRaw());
			tableModel.addCell(row, column++, comment, comment.length());
		}
		tableModel.addCell(row, column++, timeAsTimeStamp, timeAsTimeStamp.length());
		renderCheckResults(user, testCase, status, date, tableModel, row, column++);
		// render values of questions
		for (Question q : usedQuestions) {
			Finding finding = testCase.getFinding(date, q);
			if (finding != null) {
				Value value = finding.getValue();
				String findingString;
				if (value instanceof DateValue) {
					findingString = ((DateValue) value).getDateString();
				}
				else {
					findingString = value.toString();
				}
				Collection<String> errors = new ArrayList<>();
				TestCaseUtils.checkValues(errors, q, value);
				if (!errors.isEmpty()) {
					RenderResult errorResult = new RenderResult(tableModel.getUserContext());
					errorResult.appendHtml("<div style='background-color:"
							+ StyleRenderer.CONDITION_FALSE + "'>");
					errorResult.append(findingString);
					errorResult.appendHtml("</div>");
					findingString = errorResult.toStringRaw();
				}
				tableModel.addCell(row, column, findingString,
						value.toString().length());
			}
			column++;
		}
		// render observations
		for (String s : additionalQuestions) {
			TerminologyObject object = manager.search(s);
			if (object != null) {
				appendValueCell(status, object, date, tableModel, row, column);
			}
			column++;
		}
		if (selectedObject != null) {
			appendValueCell(status, selectedObject, date, tableModel, row, column);
		}
	}

	private void appendValueCell(SessionDebugStatus status, TerminologyObject object, Date date, TableModel tableModel, int row, int column) {
		Value value = status.getValue(object, date);
		if (value != null) {
			String valueString = value.toString();
			if (value instanceof DateValue) {
				valueString = ((DateValue) value).getDateOrDurationString();
			}
			tableModel.addCell(row, column, valueString, valueString.length());
		}
	}

	private void renderObservationQuestionsHeader(Collection<String> additionalQuestions, TerminologyManager manager, TableModel tableModel, int column) {
		for (String questionString : additionalQuestions) {
			TerminologyObject object = manager.search(questionString);
			RenderResult sb = new RenderResult(tableModel.getUserContext());
			if (object instanceof Question || object instanceof Solution) {
				sb.append(questionString);
			}
			else {
				sb.appendHtml("<span style='color:silver'>");
				sb.append(questionString);
				sb.appendHtml("</span>");
			}
			Set<String> copy = new LinkedHashSet<>(additionalQuestions);
			copy.remove(questionString);
			String input = " <input type=\"button\" value=\"-\" onclick=\"TestCasePlayer.addCookie('"
					+ toAdditionalQuestionsCookyString(copy) + "');\">";
			sb.appendHtml(input);
			tableModel.addCell(0, column++, sb.toStringRaw(), questionString.length() + 2);
		}
	}

	private String toAdditionalQuestionsCookyString(Collection<String> additionalQuestions) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (String question : additionalQuestions) {
			if (first) {
				first = false;
			}
			else {
				builder.append(QUESTIONS_SEPARATOR);
			}
			builder.append(Strings.encodeHtml(question.replace("\\", "\\\\")));
		}
		return builder.toString();
	}

	private void renderRunTo(ProviderTriple selectedTriple, SessionDebugStatus status, Date date, String dateString, TableModel tableModel, int row) {

		RenderResult result = new RenderResult(tableModel.getUserContext());
		String js = "TestCasePlayer.send("
				+ "'"
				+ selectedTriple.getB().getID()
				+ "', '" + dateString
				+ "', '" + selectedTriple.getA().getName()
				+ "', '" + selectedTriple.getC().getTitle() + "', this);";
		result.appendHtml("<a onclick=\"" + js + "\" class='tooltipster'");
		if (status.getLastExecuted() == null
				|| status.getLastExecuted().before(date)) {
			result.appendHtml(" title='Run to'>");
			result.appendHtml(Icon.RUN.toHtml());
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
				result.appendHtml(" title='Checks successful</br>Click to rerun'>");
				result.appendHtml(Icon.CHECKED.addClasses("knowwe-ok").toHtml());
			}
			else {
				result.appendHtml(" title='Checks failed</br>Click to rerun'>");
				result.appendHtml(Icon.ERROR.toHtml());
			}
		}
		result.appendHtml("</a>");
		tableModel.addCell(row, 0, result.toStringRaw(), 2);

	}

	private void renderCheckResults(UserContext user, TestCase testCase, SessionDebugStatus status, Date date, TableModel tableModel, int row, int column) {
		Collection<Pair<Check, Boolean>> checkResults = status.getCheckResults(date);
		int max = 0;
		RenderResult sb = new RenderResult(tableModel.getUserContext());
		sb.appendHtmlTag("div", "style", "white-space: nowrap");
		if (checkResults == null) {
			boolean first = true;
			for (Check c : testCase.getChecks(date, status.getSession().getKnowledgeBase())) {
				if (!first) sb.appendHtml("<br />");
				first = false;
				renderCheck(c, user, sb);
				max = Math.max(max, c.getCondition().length());
			}
		}
		else {
			if (!checkResults.isEmpty()) {
				boolean first = true;
				for (Pair<Check, Boolean> p : checkResults) {
					Check check = p.getA();
					boolean success = p.getB();
					max = Math.max(max, check.getCondition().length());
					if (!first) sb.appendHtml("<br />");
					first = false;
					String color;
					if (success) {
						color = StyleRenderer.CONDITION_FULLFILLED;
					}
					else {
						color = StyleRenderer.CONDITION_FALSE;
					}
					sb.appendHtml("<span style='background-color:" + color + "'>");
					// render the condition appropriately
					renderCheck(check, user, sb);
					sb.appendHtml("</span>");
				}
			}
		}
		sb.appendHtmlTag("/div");
		tableModel.addCell(row, column, sb.toStringRaw(), max);
	}

	private void renderCheck(Check check, UserContext user, RenderResult sb) {
		if (check instanceof KnowWEConditionCheck) {
			((KnowWEConditionCheck) check).render(user, sb);
		}
		else {
			sb.append(check.getCondition());
		}
	}

	private TerminologyObject renderObservationQuestionAdder(Section<?> section, UserContext user, TerminologyManager manager, Collection<String> alreadyAddedQuestions, TableModel tableModel, int column) {
		String key = "question_selector_" + section.getID();
		String cookie = KnowWEUtils.getCookie(key, "", user);
		String selectedQuestion = Strings.decodeURL(cookie, Encoding.ISO_8859_1);
		TerminologyObject object = null;
		RenderResult result = new RenderResult(user);
		result.appendHtml("<form><select name=\"toAdd\" id=adder"
				+ section.getID()
				+ " onchange=\"TestCasePlayer.change('"
				+ key
				+ "', this.options[this.selectedIndex].value);\">");
		result.appendHtml("<option value='--'>--</option>");
		boolean foundOne = false;
		List<TerminologyObject> objects = new LinkedList<>();
		objects.addAll(manager.getQuestions());
		objects.addAll(manager.getSolutions());
		Collections.sort(objects, new NamedObjectComparator());
		int max = 0;
		for (TerminologyObject q : objects) {
			if (!alreadyAddedQuestions.contains(q.getName())) {
				max = Math.max(max, q.getName().length());
				if (q.getName().equals(selectedQuestion)) {
					result.appendHtml("<option selected='selected' value='"
							+ Strings.encodeHtml(q.getName()) + "' \n>"
							+ Strings.encodeHtml(q.getName()) + "</option>");
					object = q;
				}
				else {
					result.appendHtml("<option value='"
							+ Strings.encodeHtml(q.getName()) + "' \n>"
							+ Strings.encodeHtml(q.getName())
							+ "</option>");
				}
				foundOne = true;
			}
		}
		result.appendHtml("</select>");
		// reset value because -- is selected
		if (object == null) {
			user.getSession().setAttribute(key, "");
		}
		if (object != null && !object.getName().equals(selectedQuestion)) {
			user.getSession().setAttribute(key, object.getName());
		}
		if (!alreadyAddedQuestions.isEmpty()) {
			result.appendHtml("<input "
					+
					(object == null ? "disabled='disabled'" : "")
					+ " type=\"button\" value=\"+\" onclick=\"TestCasePlayer.addCookie(&quot;"
					+ toAdditionalQuestionsCookyString(alreadyAddedQuestions)
					+ QUESTIONS_SEPARATOR
					+ "&quot;+this.form.toAdd.options[toAdd.selectedIndex].value);\"></form>");
		}
		else {
			result.appendHtml("<input "
					+ (object == null ? "disabled='disabled'" : "")
					+ " type=\"button\" value=\"+\" onclick=\"TestCasePlayer.addCookie(this.form.toAdd.options[toAdd.selectedIndex].value);\"></form>");
		}
		if (foundOne) {
			tableModel.addCell(0, column, result.toStringRaw(), max + 3);
		}
		return object;
	}

	private ProviderTriple getAndRenderTestCaseSelection(Section<?> section, UserContext user, List<ProviderTriple> providers, RenderResult string) {
		String key = generateSelectedTestCaseCookieKey(section);
		String selectedID = getSelectedTestCaseId(section, providers, user);
		RenderResult selectString = new RenderResult(string);
		// if no pair is selected, use the first
		ProviderTriple selectedTriple = null;
		selectString.appendHtml("<span class=fillText>Case </span>"
				+ "<select id=selector" + section.getID()
				+ " onchange=\"TestCasePlayer.change('" + key
				+ "', this.options[this.selectedIndex].value, '" + section.getID() + "');\">");
		Set<String> ids = new HashSet<>();
		boolean unique = true;
		for (ProviderTriple triple : providers) {
			unique &= ids.add(triple.getA().getName());
		}
		for (ProviderTriple triple : providers) {
			if (triple.getA().getTestCase() != null) {
				if (selectedTriple == null) {
					selectedTriple = triple;
				}
				String id = getTestCaseId(triple);
				String displayedID = (unique) ? triple.getA().getName() : id;

				List<String> attributes = new ArrayList<>();
				attributes.add("caselink");
				attributes.add(KnowWEUtils.getURLLink(triple.getB()));
				attributes.add("value");
				attributes.add(id);
				if (id.equals(selectedID)) {
					attributes.add("selected");
					attributes.add("selected");
					selectedTriple = triple;
				}
				selectString.appendHtmlElement("option", displayedID,
						attributes.toArray(new String[attributes.size()]));
			}
		}
		selectString.appendHtml("</select>");
		if (selectedTriple != null) {
			string.append(selectString);
		}
		else {
			Message notValidTestCaseError = Messages.warning(
					"There are testcase sections in the specified packages, but none of them generates a testcase.");
			DefaultMarkupRenderer.renderMessagesOfType(
					Type.WARNING, Arrays.asList(notValidTestCaseError), string);
		}
		return selectedTriple;
	}

	private static String getTestCaseId(ProviderTriple triple) {
		return triple.getC().getTitle() + "/" + triple.getA().getName();
	}

	public static String getSelectedTestCaseId(Section<?> section, List<ProviderTriple> providers, UserContext user) {
		final String selectedId = Strings.decodeURL(KnowWEUtils.getCookie(generateSelectedTestCaseCookieKey(section),
				"", user), Encoding.ISO_8859_1);
		boolean caseExists = providers.stream()
				.filter(triple -> getTestCaseId(triple).equals(selectedId))
				.findFirst()
				.isPresent();
		if (Strings.isBlank(selectedId) || !caseExists) {
			for (ProviderTriple provider : providers) {
				if (provider.getProviderSection().getTitle().equals(section.getTitle())) {
					return getTestCaseId(provider);
				}
			}
		}
		return selectedId;
	}

	public static String generateSelectedTestCaseCookieKey(Section<?> section) {
		int i = 1;
		List<Section<TestCasePlayerType>> sections = Sections.successors(
				section.getArticle().getRootSection(), TestCasePlayerType.class);
		Section<TestCasePlayerType> testCasePlayerTypeSection = Sections.ancestor(
				section, TestCasePlayerType.class);
		for (Section<TestCasePlayerType> s : new TreeSet<>(sections)) {
			if (testCasePlayerTypeSection.equals(s)) {
				break;
			}
			else {
				i++;
			}
		}
		return "selectedValue_" + Strings.encodeURL(section.getTitle()) + i;
	}

	private String renderToolbarButton(Icon icon, String action, UserContext user) {
		RenderResult buffer = new RenderResult(user);
		renderToolbarButton(icon, action, true, buffer);
		return buffer.toStringRaw();
	}

	private void renderToolbarButton(Icon icon, String action, boolean enabled, RenderResult builder) {
		if (enabled) {
			builder.appendHtml("<a onclick=\"");
			builder.appendHtml(action);
			builder.appendHtml(";\">");
		}
		if (enabled) {
			builder.appendHtml(icon.toHtml());
		}
		else {
			builder.appendHtml(icon.addColor(IconColor.DISABLED).toHtml());

		}
		if (enabled) {
			builder.appendHtml("</a>");
		}
	}

	/**
	 * Encapsules parameters for the table
	 *
	 * @author Markus Friedrich (denkbares GmbH)
	 * @created 29.02.2012
	 */
	private static class NavigationParameters {

		private int from;
		private int to;
		private int size;
	}
}
