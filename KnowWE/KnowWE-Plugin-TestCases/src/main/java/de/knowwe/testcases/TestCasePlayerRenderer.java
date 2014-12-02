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
				"display:inline-block; vertical-align:middle; text-align:center; padding-right: 5px");
		if (failureCount == 0) {
			string.appendHtmlElement("img", "", "src",
					"KnowWEExtension/images/green_bulb.png", "title", "No check failures");
		}
		else if (failureCount == 1) {
			string.appendHtmlElement("img", "", "src",
					"KnowWEExtension/images/red_bulb.png", "title", "There is one failed check failure");
		}
		else {
			string.appendHtmlElement("img", "", "src",
					"KnowWEExtension/images/red_bulb.png", "title", "There are " + failureCount + " failed checks");
		}
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

		NavigationParameters navigatorParameters = getNavigationParameters(section, user,
				chronology);

		renderTestCaseHeader(section, user, testCase, chronology, navigatorParameters, string);

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
		return new LinkedHashSet<String>(Arrays.asList(additionalQuestionsSplit));
	}

	private void renderTestCaseHeader(Section<?> section, UserContext user, TestCase testCase, Collection<Date> chronology, NavigationParameters navigatorParameters, RenderResult string) {
		string.appendHtml("<span class='fillText'> Start: </span>");
		if (testCase.getStartDate().getTime() == 0) {
			string.append("---");
		}
		else {
			string.append(dateFormat.format(testCase.getStartDate()));
		}
		string.appendHtml(PaginationRenderer.getToolSeparator());

		PaginationRenderer.setResultSize(user, section, chronology.size());
		PaginationRenderer.renderTableSizeSelector(section, user, string);
		PaginationRenderer.renderNavigation(section, user, string);
	}

	private String createFromKey(Section<?> section) {
		return FROM_KEY + "_" + section.getID();
	}

	private String createSizeKey(Section<?> section) {
		return SIZE_SELECTOR_KEY + "_" + section.getID();
	}

	private TerminologyObject renderHeader(Section<?> section, UserContext user, ProviderTriple selectedTriple, Collection<String> additionalQuestions, Collection<Question> usedQuestions, TerminologyManager manager, TableModel tableModel) {
		Section<? extends PackageCompileType> kbsection = selectedTriple.getC();
		String stopButton = renderToolbarButton("stop12.png",
				"KNOWWE.plugin.d3webbasic.actions.resetSession('" + kbsection.getID()
						+ "', TestCasePlayer.init);", user
		);
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

	private NavigationParameters getNavigationParameters(Section<?> section, UserContext user, Collection<Date> chronology) {

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
		String timeAsTimeStamp = TimeStampType.createTimeAsTimeStamp(date.getTime()
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
				Collection<String> errors = new LinkedList<String>();
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
			if (value.getValue() instanceof Date) {
				Date dateValue = (Date) value.getValue();
				if (dateValue.getTime() < TestCaseUtils.YEAR) {
					valueString = TimeStampType.createTimeAsTimeStamp(dateValue.getTime());
				}
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
			Set<String> copy = new LinkedHashSet<String>(additionalQuestions);
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
		if (status.getLastExecuted() == null
				|| status.getLastExecuted().before(date)) {
			RenderResult sb = new RenderResult(tableModel.getUserContext());
			String js = "TestCasePlayer.send("
					+ "'"
					+ selectedTriple.getB().getID()
					+ "', '" + dateString
					+ "', '" + selectedTriple.getA().getName()
					+ "', '" + selectedTriple.getC().getTitle() + "', this);";
			sb.appendHtml("<a onclick=\"" + js + "\">");
			sb.appendHtml("<img src='KnowWEExtension/testcaseplayer/icon/runto.png'>");
			sb.appendHtml("</a>");
			tableModel.addCell(row, 0, sb.toStringRaw(), 2);
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
				RenderResult done = new RenderResult(tableModel.getUserContext());
				done.appendHtml("<img src='KnowWEExtension/testcaseplayer/icon/done.png'>");
				tableModel.addCell(row, 0, done, 2);
			}
			else {
				RenderResult done = new RenderResult(tableModel.getUserContext());
				done.appendHtml("<img src='KnowWEExtension/testcaseplayer/icon/error.png'>");
				tableModel.addCell(row, 0, done, 2);
			}
		}
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
		String key = QUESTION_SELECTOR_KEY + "_" + section.getID();
		String cookie = KnowWEUtils.getCookie(key, "", user);
		String selectedQuestion = Strings.decodeURL(cookie, Encoding.ISO_8859_1);
		TerminologyObject object = null;
		RenderResult selectsb2 = new RenderResult(user);
		selectsb2.appendHtml("<form><select name=\"toAdd\" id=adder"
				+ section.getID()
				+ " onchange=\"TestCasePlayer.change('"
				+ key
				+ "', this.options[this.selectedIndex].value);\">");
		selectsb2.appendHtml("<option value='--'>--</option>");
		boolean foundone = false;
		List<TerminologyObject> objects = new LinkedList<TerminologyObject>();
		objects.addAll(manager.getQuestions());
		objects.addAll(manager.getSolutions());
		Collections.sort(objects, new NamedObjectComparator());
		int max = 0;
		for (TerminologyObject q : objects) {
			if (!alreadyAddedQuestions.contains(q.getName())) {
				max = Math.max(max, q.getName().length());
				if (q.getName().equals(selectedQuestion)) {
					selectsb2.appendHtml("<option selected='selected' value='"
							+ Strings.encodeHtml(q.getName()) + "' \n>"
							+ Strings.encodeHtml(q.getName()) + "</option>");
					object = q;
				}
				else {
					selectsb2.appendHtml("<option value='"
							+ Strings.encodeHtml(q.getName()) + "' \n>"
							+ Strings.encodeHtml(q.getName())
							+ "</option>");
				}
				foundone = true;
			}
		}
		selectsb2.appendHtml("</select>");
		// reset value because -- is selected
		if (object == null) {
			user.getSession().setAttribute(key, "");
		}
		if (object != null && !object.getName().equals(selectedQuestion)) {
			user.getSession().setAttribute(key, object.getName());
		}
		if (!alreadyAddedQuestions.isEmpty()) {
			selectsb2.appendHtml("<input "
					+
					(object == null ? "disabled='disabled'" : "")
					+ " type=\"button\" value=\"+\" onclick=\"TestCasePlayer.addCookie(&quot;"
					+ toAdditionalQuestionsCookyString(alreadyAddedQuestions)
					+ QUESTIONS_SEPARATOR
					+ "&quot;+this.form.toAdd.options[toAdd.selectedIndex].value);\"></form>");
		}
		else {
			selectsb2.appendHtml("<input "
					+ (object == null ? "disabled='disabled'" : "")
					+ " type=\"button\" value=\"+\" onclick=\"TestCasePlayer.addCookie(this.form.toAdd.options[toAdd.selectedIndex].value);\"></form>");
		}
		if (foundone) {
			tableModel.addCell(0, column, selectsb2.toStringRaw(), max + 3);
		}
		return object;
	}

	private ProviderTriple getAndRenderTestCaseSelection(Section<?> section, UserContext user, List<ProviderTriple> providers, RenderResult string) {
		String key = generateSelectedTestCaseCookieKey(section);
		String selectedID = getSelectedTestCaseId(section, user);
		RenderResult selectsb = new RenderResult(string);
		// if no pair is selected, use the first
		ProviderTriple selectedTriple = null;
		selectsb.appendHtml("<span class=fillText>Case </span>"
				+ "<select id=selector" + section.getID()
				+ " onchange=\"TestCasePlayer.change('" + key
				+ "', this.options[this.selectedIndex].value);\">");
		Set<String> ids = new HashSet<String>();
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

				List<String> attributes = new ArrayList<String>();
				attributes.add("caselink");
				attributes.add(KnowWEUtils.getURLLink(triple.getB()));
				attributes.add("value");
				attributes.add(id);
				if (id.equals(selectedID)) {
					attributes.add("selected");
					attributes.add("selected");
					selectedTriple = triple;
				}
				selectsb.appendHtmlElement("option", displayedID,
						attributes.toArray(new String[attributes.size()]));
			}
		}
		selectsb.appendHtml("</select>");
		if (selectedTriple != null) {
			string.append(selectsb);
		}
		else {
			Message notValidTestCaseError = Messages.warning(
					"There are testcase sections in the specified packages, but none of them generates a testcase.");
			DefaultMarkupRenderer.renderMessagesOfType(
					Type.WARNING, Arrays.asList(notValidTestCaseError), string);
		}
		return selectedTriple;
	}

	private String getTestCaseId(ProviderTriple triple) {
		return triple.getC().getTitle() + "/" + triple.getA().getName();
	}

	public static String getSelectedTestCaseId(Section<?> section, UserContext user) {
		return Strings.decodeURL(KnowWEUtils.getCookie(generateSelectedTestCaseCookieKey(section),
				"", user), Encoding.ISO_8859_1);
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
		return SELECTOR_KEY + "_" + Strings.encodeURL(section.getTitle()) + i;
	}

	private String renderToolbarButton(String icon, String action, UserContext user) {
		RenderResult buffer = new RenderResult(user);
		renderToolbarButton(icon, action, true, buffer);
		return buffer.toStringRaw();
	}

	private void renderToolbarButton(String icon, String action, boolean enabled, RenderResult builder) {
		int index = icon.lastIndexOf('.');
		String suffix = icon.substring(index);
		icon = icon.substring(0, index);
		if (enabled) {
			builder.appendHtml("<a onclick=\"");
			builder.appendHtml(action);
			builder.appendHtml(";\">");
		}
		builder.appendHtml("<span class='toolButton ");
		builder.appendHtml(enabled ? "enabled" : "disabled");
		builder.appendHtml("'>");
		builder.appendHtml("<img src='KnowWEExtension/testcaseplayer/icon/");
		builder.appendHtml(icon);
		if (!enabled) builder.appendHtml("_deactivated");
		builder.appendHtml(suffix).appendHtml("' /></span>");
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
