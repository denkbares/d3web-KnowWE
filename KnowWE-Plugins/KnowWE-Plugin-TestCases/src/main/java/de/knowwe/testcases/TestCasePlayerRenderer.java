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
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

import com.denkbares.strings.Strings;
import com.denkbares.strings.Strings.Encoding;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyManager;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.utilities.NamedObjectComparator;
import de.d3web.testcase.TestCaseUtils;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.model.DescribedTestCase;
import de.d3web.testcase.model.Finding;
import de.d3web.testcase.model.TestCase;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.renderer.PaginationRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.OutDatedSessionNotification;
import de.knowwe.testcases.table.KnowWEConditionCheck;
import de.knowwe.util.Color;
import de.knowwe.util.Icon;

import static java.util.stream.Collectors.groupingBy;

/**
 * Renderer for TestCasePlayerType
 *
 * @author Markus Friedrich (denkbares GmbH)
 * @created 19.01.2012
 */
public class TestCasePlayerRenderer implements Renderer {

	private static final String OBJECTS_SEPARATOR = "#####";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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

		if (providers.isEmpty()) {
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
		string.appendHtmlTag("div", "style",
				"display:inline-block; vertical-align:middle; text-align:center; padding: 0 12px 0 8px");
		string.appendHtmlTag("span", "style", "position:relative; top:-1px;");
		int failureCount = selectedTriple == null ? -1
				: selectedTriple.getProvider().getDebugStatus(user).getFailureCount();
		if (failureCount == -1) {
			string.appendHtml(Icon.BULB.addColor(Color.DISABLED).addTitle("No selection").toHtml());
		}
		else if (failureCount == 0) {
			string.appendHtml(Icon.BULB.addColor(Color.OK).addTitle("No check failures").toHtml());
		}
		else if (failureCount == 1) {
			string.appendHtml(Icon.BULB.addColor(Color.ERROR)
					.addTitle("There is one failed check failure")
					.toHtml());
		}
		else {
			string.appendHtml(Icon.BULB.addColor(Color.ERROR)
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
			DefaultMarkupRenderer.renderMessageOfType(string, Type.WARNING, "No knowledge base found.");
		}
		else {
			TestCase testCase = provider.getTestCase();
			SessionDebugStatus status = provider.getDebugStatus(user);
			if (status.getSession() != session) {
				status.setSession(session);
			}

			if (testCase != null) {
				try {
					renderTestCase(section, user, selectedTriple, string);
				}
				catch (IllegalArgumentException e) {
					DefaultMarkupRenderer.renderMessageOfType(string, Type.ERROR,
							"Test case not compatible to TestCasePlayer: " + e.getMessage());
				}
			}
			else {
				string.append("\nNo TestCase contained!\n");
			}
		}
	}

	private void renderProviderMessages(TestCaseProvider provider, RenderResult string) {
		List<Message> messages = provider.getMessages();
		if (!messages.isEmpty()) {
			DefaultMarkupRenderer.renderMessagesOfType(Type.ERROR, messages, string);
		}
	}

	private void renderNoProviderWarning(Section<TestCasePlayerType> playerSection, RenderResult string) {
		DefaultMarkupRenderer.renderMessageOfType(string, Type.WARNING, "No test cases found in the packages: " +
				Strings.concat(", ", de.knowwe.testcases.TestCaseUtils.getTestCasePackages(playerSection)));
	}

	private void renderTestCase(Section<?> section, UserContext user, ProviderTriple selectedTriple, RenderResult string) {

		renderTestCaseHeader(section, user, selectedTriple, string);

		TableModel tableModel = getTableModel(section, user, selectedTriple);

		string.append(tableModel.toHtml(section, user));
	}

	private TableModel getTableModel(Section<?> section, UserContext user, ProviderTriple selectedTriple) {

		NavigationParameters navigatorParameters = getNavigationParameters(section, user);

		KnowledgeBase knowledgeBase = selectedTriple.getProvider().getDebugStatus(user).getSession().getKnowledgeBase();
		TestCase testCase = selectedTriple.getProvider().getTestCase();
		Collection<Date> chronology = testCase.chronology();

		Collection<TerminologyObject> usedObjects = getUsedObjects(testCase, knowledgeBase);
		Collection<String> additionalObjects = getAdditionalObjects(section, user);

		TableModel tableModel = new TableModel(user);
		tableModel.setName(getTestCaseId(selectedTriple));
		KnowledgeBase base = D3webUtils.getKnowledgeBase(user, selectedTriple.getKbSection());

		// check if the latest knowledge base is used
		if (base != null) {
			if (SessionProvider.hasOutDatedSession(user, base)) {
				NotificationManager.addNotification(user,
						new OutDatedSessionNotification(selectedTriple.getKbSection(), KnowledgeBaseUtils.getBaseName(knowledgeBase)));
			}
		}

		TerminologyObject selectedObject = renderHeader(section, user, selectedTriple, tableModel);
		if (selectedObject != null) additionalObjects.add(selectedObject.getName());

		int row = 1;
		for (Date date : chronology) {
			if (row < navigatorParameters.from) {
				row++;
				continue;
			}
			if (row > navigatorParameters.to) break;
			renderRow(user, date, selectedTriple, usedObjects, additionalObjects, tableModel);
			row++;
		}
		return tableModel;
	}

	public static Collection<TerminologyObject> getUsedObjects(TestCase testCase, KnowledgeBase kb) {
		Collection<TerminologyObject> questions = new LinkedHashSet<>();
		for (Date date : testCase.chronology()) {
			for (Finding finding : testCase.getFindings(date, kb)) {
				questions.add(finding.getTerminologyObject());
			}
		}
		return questions;
	}

	private Collection<String> getAdditionalObjects(Section<?> section, UserContext user) {
		String additionalObjects = getAdditionalQuestionsCookie(section, user);
		String[] additionalObjectsSplit = new String[0];
		if (additionalObjects != null && !additionalObjects.isEmpty()) {
			additionalObjectsSplit = additionalObjects.split(OBJECTS_SEPARATOR);
		}
		return new LinkedHashSet<>(Arrays.asList(additionalObjectsSplit));
	}

	private void renderTestCaseHeader(Section<?> section, UserContext user, ProviderTriple selectedTriple, RenderResult string) {
		TestCase testCase = selectedTriple.getProvider().getTestCase();
		Collection<Date> chronology = testCase.chronology();

		string.appendHtml("<span class='fillText'> Start: </span>");
		if (testCase.getStartDate().getTime() == 0) {
			string.append("---");
		}
		else {
			synchronized (DATE_FORMAT) {
				string.append(DATE_FORMAT.format(testCase.getStartDate()));
			}
		}
		string.appendHtml(PaginationRenderer.getToolSeparator());

		PaginationRenderer.setResultSize(user, chronology.size());
		PaginationRenderer.renderPagination(section, user, string);
	}

	private TerminologyObject renderHeader(Section<?> section, UserContext user, ProviderTriple selectedTriple, TableModel tableModel) {
		Section<? extends PackageCompileType> kbsection = selectedTriple.getC();
		String stopButton = renderToolbarButton(Icon.STOP.addClasses("knowwe-red"),
				"KNOWWE.plugin.d3webbasic.actions.resetSession('" + kbsection.getID()
						+ "', TestCasePlayer.init);", user);
		RenderResult stopButtonResult = new RenderResult(tableModel.getUserContext());
		stopButtonResult.appendHtml(stopButton);
		int column = 0;
		tableModel.addCell(0, column++, stopButtonResult, 1);
		TestCase testCase = selectedTriple.getProvider().getTestCase();
		if (testCase instanceof DescribedTestCase && ((DescribedTestCase) testCase).hasDescriptions()) {
			tableModel.addCell(0, column++, "Name", "Name".length());
		}
		tableModel.addCell(0, column++, "Time", "Time".length());
		tableModel.addCell(0, column++, "Checks", "Checks".length());
		tableModel.setFirstFinding(column);
		Collection<TerminologyObject> usedObjects = getUsedObjects(testCase,
				selectedTriple.getProvider().getDebugStatus(user).getSession().getKnowledgeBase());
		for (TerminologyObject object : usedObjects) {
			tableModel.addCell(0, column++, object.getName(), object.getName().length());
		}
		TerminologyManager manager = selectedTriple.getProvider().getDebugStatus(user)
				.getSession().getKnowledgeBase().getManager();
		Collection<String> additionalQuestions = getAdditionalObjects(section, user);

		tableModel.setLastFinding(column - 1);
		renderObservationQuestionsHeader(additionalQuestions, manager, tableModel, column);
		column += additionalQuestions.size();
		TerminologyObject terminologyObject = renderObservationQuestionAdder(section,
				user, manager, additionalQuestions, tableModel, column);
		tableModel.nextRow();
		return terminologyObject;
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

	protected void renderRow(UserContext user, Date date, ProviderTriple selectedTriple, Collection<TerminologyObject> usedQuestions, Collection<String> additionalQuestions, TableModel tableModel) {
		TestCase testCase = selectedTriple.getProvider().getTestCase();
		SessionDebugStatus status = selectedTriple.getProvider().getDebugStatus(user);
		KnowledgeBase knowledgeBase = status.getSession().getKnowledgeBase();

		renderRunTo(selectedTriple, status, date, tableModel);
		if (testCase instanceof DescribedTestCase) {
			DescribedTestCase describedTestCase = (DescribedTestCase) testCase;
			if (describedTestCase.hasDescriptions()) {
				String description = describedTestCase.getDescription(date);
				if (Strings.isBlank(description)) {
					tableModel.skipColumn();
				}
				else {
					renderDescription(description, tableModel);
				}
			}
		}
		renderDate(testCase.getStartDate(), date, tableModel);
		renderCheckResults(user, testCase.getChecks(date, knowledgeBase), status.getCheckResults(date), tableModel);
		renderFindings(testCase.getFindings(date, knowledgeBase), usedQuestions, tableModel);
		renderObservations(date, knowledgeBase, status, additionalQuestions, tableModel);
		tableModel.nextRow();
	}

	protected void renderObservations(Date date, KnowledgeBase knowledgeBase, SessionDebugStatus status, Collection<String> additionalQuestions, TableModel tableModel) {
		for (String objectName : additionalQuestions) {
			appendValueCell(status, knowledgeBase.getManager().search(objectName), date, tableModel);
		}
	}

	protected void renderFindings(Collection<Finding> findings, Collection<TerminologyObject> usedQuestions, TableModel tableModel) {
		Map<TerminologyObject, List<Finding>> mappedFindings = findings.stream()
				.collect(groupingBy(Finding::getTerminologyObject));
		for (TerminologyObject question : usedQuestions) {
			List<Finding> findingList = mappedFindings.get(question);
			if (findingList != null) {
				renderFinding(findingList.get(0), tableModel);
			}
			else {
				tableModel.skipColumn();
			}
		}
	}

	protected void renderDate(Date startDate, Date date, TableModel tableModel) {
		String timeAsTimeStamp = Strings.getDurationVerbalization(date.getTime()
				- startDate.getTime());
		tableModel.addCell(timeAsTimeStamp, timeAsTimeStamp.length());
	}

	protected void renderDescription(String description, TableModel tableModel) {
		RenderResult sb = new RenderResult(tableModel.getUserContext());
		sb.appendHtml("<br />");
		if (description == null) description = "";
		description = description.replace("\n", sb.toStringRaw());
		tableModel.addCell(description, description.length());
	}

	private void renderFinding(Finding finding, TableModel tableModel) {
		Question question = (Question) finding.getTerminologyObject();
		Value value = finding.getValue();
		String findingString;
		if (question instanceof QuestionDate && value instanceof DateValue) {
			findingString = ValueUtils.getDateVerbalization((QuestionDate) question, (DateValue) value, ValueUtils.TimeZoneDisplayMode.IF_NOT_DEFAULT);
		}
		else {
			findingString = value.toString();
		}
		Collection<String> errors = new ArrayList<>();
		TestCaseUtils.checkValues(errors, question, value);
		if (!errors.isEmpty()) {
			RenderResult errorResult = new RenderResult(tableModel.getUserContext());
			errorResult.appendHtml("<div class='"
					+ StyleRenderer.CONDITION_FALSE.getCssClass() + "'>");
			errorResult.append(findingString);
			errorResult.appendHtml("</div>");
			findingString = errorResult.toStringRaw();
		}
		tableModel.addCell(findingString, value.toString().length());
	}

	private void appendValueCell(SessionDebugStatus status, TerminologyObject object, Date date, TableModel tableModel) {
		Value value = status.getValue(object, date);
		if (value == null) {
			tableModel.skipColumn();
		}
		else {
			String valueString = value.toString();
			if (value instanceof DateValue) {
				valueString = ValueUtils.getDateOrDurationVerbalization((QuestionDate) object, ((DateValue) value).getDate());
			}
			tableModel.addCell(valueString, valueString.length());
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
				builder.append(OBJECTS_SEPARATOR);
			}
			builder.append(Strings.encodeHtml(question.replace("\\", "\\\\")));
		}
		return builder.toString();
	}

	protected void renderRunTo(ProviderTriple selectedTriple, SessionDebugStatus status, Date date, TableModel tableModel) {

		RenderResult result = new RenderResult(tableModel.getUserContext());
		String js = "TestCasePlayer.send("
				+ "'"
				+ selectedTriple.getB().getID()
				+ "', '" + date.getTime()
				+ "', '" + selectedTriple.getA().getName()
				+ "', '" + selectedTriple.getC().getTitle() + "', this);";
		result.appendHtml("<a onclick=\"" + js + "\" class='tooltipster'");
		if (status.getLastExecuted() == null
				|| status.getLastExecuted().before(date)) {
			result.appendHtml(" title='Run to'>");
			result.appendHtml(Icon.RUN.toHtml());
		}
		else {
			Map<Check, Boolean> checkResults = status.getCheckResults(date);
			boolean ok = true;
			if (checkResults != null) {
				for (Map.Entry<Check, Boolean> pair : checkResults.entrySet()) {
					ok &= pair.getValue();
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
		// write to first column of current row
		tableModel.addCell(result.toStringRaw(), 2);
	}

	protected void renderCheckResults(UserContext user, Collection<Check> checks, Map<Check, Boolean> checkResults, TableModel tableModel) {
		if (checks.isEmpty()) {
			tableModel.skipColumn();
			return;
		}
		int max = 0;
		RenderResult sb = new RenderResult(tableModel.getUserContext());
		sb.appendHtmlTag("div", "style", "white-space: nowrap");
		if (checkResults == null) {
			boolean first = true;
			for (Check check : checks) {
				if (!first) sb.appendHtml("<br />");
				first = false;
				renderCheck(check, user, sb);
				max = Math.max(max, check.getCondition().length());
			}
		}
		else {
			boolean first = true;
			for (Check check : checks) {
				boolean success = checkResults.get(check);
				max = Math.max(max, check.getCondition().length());
				if (!first) sb.appendHtml("<br />");
				first = false;
				String cssClass;
				if (success) {
					cssClass = StyleRenderer.CONDITION_FULFILLED.getCssClass();
				}
				else {
					cssClass = StyleRenderer.CONDITION_FALSE.getCssClass();
				}
				sb.appendHtml("<span class='" + cssClass + "'>");
				// render the condition appropriately
				renderCheck(check, user, sb);
				sb.appendHtml("</span>");
			}
		}
		sb.appendHtmlTag("/div");
		tableModel.addCell(sb.toStringRaw(), max);
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
		objects.sort(new NamedObjectComparator());
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
					+ OBJECTS_SEPARATOR
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
				selectString.appendHtmlElement("option", displayedID, attributes.toArray(new String[0]));
			}
		}
		selectString.appendHtml("</select>");
		if (selectedTriple != null) {
			string.append(selectString);
		}
		else {
			DefaultMarkupRenderer.renderMessageOfType(string, Type.WARNING,
					"There are testcase sections in the specified packages, but none of them generates a testcase.");
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
				.anyMatch(triple -> getTestCaseId(triple).equals(selectedId));
		if (Strings.isBlank(selectedId) || !caseExists) {
			for (ProviderTriple provider : providers) {
				if (Objects.equals(provider.getProviderSection().getTitle(), section.getTitle())) {
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
			if (Objects.equals(testCasePlayerTypeSection, s)) {
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
			builder.appendHtml(icon.addColor(Color.DISABLED).toHtml());
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
