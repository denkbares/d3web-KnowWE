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

package de.d3web.we.ci4ke.dashboard.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.testing.ArgsCheckResult;
import de.d3web.testing.TestParser;
import de.d3web.testing.TestSpecification;
import de.d3web.we.ci4ke.build.CIConfig;
import de.d3web.we.ci4ke.dashboard.rendering.CIDashboardRenderer;
import de.d3web.we.ci4ke.hook.CIHook;
import de.d3web.we.ci4ke.hook.CIHookManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIDashboardType extends DefaultMarkupType {

	public static final String NAME_KEY = "name";
	public static final String TEST_KEY = "test";
	public static final String TRIGGER_KEY = "trigger";
	public static final String VERBOSE_PERSISTENCE_KEY = "persistenceVerbose";

	public static enum CIBuildTriggers {
		onDemand, onSave
	}

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("CIDashboard");
		MARKUP.addAnnotation(NAME_KEY, true);
		MARKUP.addAnnotation(TEST_KEY, true);
		MARKUP.addAnnotation(TRIGGER_KEY, true);
		MARKUP.addAnnotation(VERBOSE_PERSISTENCE_KEY, false, Pattern.compile("^(true|false)$"));
		MARKUP.addAnnotationContentType(TEST_KEY, new TestIgnoreType());
		MARKUP.addAnnotationContentType(TEST_KEY, new TestDeclarationType());
	}

	public CIDashboardType() {
		super(MARKUP);
		this.addCompileScript(new DashboardSubtreeHandler());
		// this.setCustomRenderer(new DashboardRenderer());
		this.setRenderer(new CIDashboardRenderer());
	}

	public static String getDashboardName(Section<CIDashboardType> section) {
		return DefaultMarkupType.getAnnotation(section, NAME_KEY);
	}

	private class DashboardSubtreeHandler extends DefaultGlobalScript<CIDashboardType> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<CIDashboardType> s) {

			// List<Message> msgs = new ArrayList<Message>();

			String dashboardName = DefaultMarkupType.getAnnotation(s, NAME_KEY);

			if (dashboardName == null) return;

			String triggerString = DefaultMarkupType.getAnnotation(s, TRIGGER_KEY);

			if (triggerString == null) return;

			CIBuildTriggers trigger = null;

			Pattern pattern = Pattern.compile("(?:\".+?\"|[^\\s]+)");

			Set<String> monitoredArticles = new HashSet<String>();
			Matcher matcher = pattern.matcher(triggerString);
			if (matcher.find()) {
				// get the name of the test
				try {
					trigger = CIBuildTriggers.valueOf(matcher.group());
					// get the monitoredArticles if onSave
					if (trigger.equals(CIDashboardType.CIBuildTriggers.onSave)) {
						while (matcher.find()) {
							String parameter = matcher.group();
							if (parameter.startsWith("\"") && parameter.endsWith("\"")) {
								parameter = parameter.substring(1, parameter.length() - 1);
							}
							if (Environment.getInstance().getWikiConnector().doesArticleExist(
									parameter)) {
								monitoredArticles.add(parameter);
							}
							else {
								Message msg = Messages.error("Article '" + parameter
										+ "' for trigger does not exist");
								Messages.storeMessage(s, this.getClass(), msg);
								return;
							}
						}
					}
				}
				catch (IllegalArgumentException e) {
					Message msg = Messages.error("Invalid trigger specified: " + triggerString);
					Messages.storeMessage(s, this.getClass(), msg);
					return;
				}
			}

			if (trigger.equals(CIBuildTriggers.onSave) && monitoredArticles.isEmpty()) {
				Message msg = Messages.error("Invalid trigger: " + CIBuildTriggers.onSave
						+ " requires attached articles to monitor.");
				Messages.storeMessage(s, this.getClass(), msg);
				return;
			}

			// This map is used for storing tests and their parameter-list
			// Map<String, List<String>> tests = new HashMap<String,
			// List<String>>();
			List<TestSpecification<?>> tests = new ArrayList<TestSpecification<?>>();

			List<Section<? extends AnnotationContentType>> annotationSections =
					DefaultMarkupType.getAnnotationContentSections(s, TEST_KEY);

			// iterate over all @test-Annotations
			List<ArgsCheckResult> messages = new ArrayList<ArgsCheckResult>();
			for (Section<?> annoSection : annotationSections) {
				// Section<TestDeclarationType> testSection =
				// Sections.findChildOfType(annoSection,
				// TestDeclarationType.class);
				// List<Section<TestIgnoreType>> ignoreSections =
				// Sections.findChildrenOfType(annoSection,
				// TestIgnoreType.class);

				// parse test
				TestParser testParser = new TestParser(annoSection.getText());
				TestSpecification<?> executableTest = testParser.getTestSpecification();
				messages.add(testParser.getParameterCheckResult());
				messages.addAll(testParser.getIgnoreCheckResults());
				if (executableTest != null) {
					tests.add(executableTest);
				}
			}
			convertMessages(compiler, s, messages);

			CIConfig config = new CIConfig(s.getWeb(), s.getArticle().getTitle(),
					dashboardName, tests, trigger);

			// Parse the trigger-parameter and (eventually) register
			// a CIHook
			if (trigger.equals(CIBuildTriggers.onSave)) {
				// Hook registrieren
				CIHook ciHook = new CIHook(s.getWeb(), s.getTitle(), dashboardName,
						monitoredArticles);
				CIHookManager.getInstance().registerHook(ciHook);
				// Store to be able to unregister in destroy method
				Compilers.storeObject(s,
						CIHook.CIHOOK_STORE_KEY, ciHook);
			}

			// Alright, everything seems to be ok. Let's store the CIConfig in
			// the store

			Compilers.storeObject(s, CIConfig.CICONFIG_STORE_KEY, config);

		}

		/**
		 * 
		 * @created 11.06.2012
		 * @param messages
		 * @param msgs
		 */
		private void convertMessages(DefaultGlobalCompiler compiler, Section<?> section, List<ArgsCheckResult> messages) {
			Collection<Message> msgs = new ArrayList<Message>();
			for (ArgsCheckResult message : messages) {
				if (message == null) continue;
				String[] arguments = message.getArguments();
				for (int i = 0; i < arguments.length; i++) {
					String messageText = message.getMessage(i);
					if (message.hasError(i)) {
						msgs.add(new Message(Message.Type.ERROR, messageText));
					}
					else if (message.hasWarning(i)) {
						msgs.add(new Message(Message.Type.WARNING, messageText));
					}

				}
				if (arguments.length == 0 && message.getMessage(0) != null) {
					msgs.add(new Message(Message.Type.ERROR, message.getMessage(0)));
				}
			}
			Messages.storeMessages(section, this.getClass(), msgs);
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<CIDashboardType> section) {
			CIHook ciHook = (CIHook) section.getSectionStore().getObject(CIHook.CIHOOK_STORE_KEY);
			if (ciHook != null) {
				CIHookManager.getInstance().unregisterHook(ciHook);
			}
		}

	}

	/**
	 * Checks if the name of the given CIDashboard-Section is not taken by any
	 * other CIDashboard-Section in the wiki.
	 * 
	 * @created 12.11.2010
	 * @param section the name of this CIDashboard-section is checked for
	 *        uniqueness
	 * @return true if the name of the section is unique in the wiki
	 */
	public static boolean dashboardNameIsUnique(Section<CIDashboardType> section) {

		String thisDashboardName = CIDashboardType.getAnnotation(section, NAME_KEY);

		List<Section<CIDashboardType>> sectionList = new ArrayList<Section<CIDashboardType>>();
		for (Article article : Environment.getInstance().
				getDefaultArticleManager(section.getWeb()).getArticles()) {
			Sections.findSuccessorsOfType(article.getRootSection(), CIDashboardType.class,
					sectionList);
		}

		for (Section<CIDashboardType> s : sectionList) {
			if (s.getID() != section.getID()) {
				String otherDashboardName = DefaultMarkupType.getAnnotation(section, NAME_KEY);
				if (otherDashboardName.equals(thisDashboardName)) {
					return false;
				}
			}
		}
		return true;
	}

}
