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
import de.d3web.testing.TestGroup;
import de.d3web.testing.TestParser;
import de.d3web.testing.TestSpecification;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.d3web.we.ci4ke.dashboard.rendering.CIDashboardRenderer;
import de.d3web.we.ci4ke.hook.CIHook;
import de.d3web.we.ci4ke.hook.CIHookManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageRegistrationScript;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIDashboardType extends DefaultMarkupType {

	public static final String NAME_KEY = "name";
	public static final String GROUP_KEY = "group";
	public static final String TEST_KEY = "test";
	public static final String TRIGGER_KEY = "trigger";
	public static final String VERBOSE_PERSISTENCE_KEY = "persistenceVerbose";

	public enum CIBuildTriggers {
		onDemand, onSave
	}

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("CIDashboard");
		MARKUP.addAnnotation(NAME_KEY, true);
		MARKUP.addAnnotation(TEST_KEY, true);
		MARKUP.addAnnotation(TRIGGER_KEY, true, Pattern.compile(
				"^(onDemand|onSave\\s*(\".+?\"|[^\\s]+))$"));

		// allow grouping of tests
		MARKUP.addAnnotation(GROUP_KEY, false);

		// add content for individual annotations
		MARKUP.addAnnotation(VERBOSE_PERSISTENCE_KEY, false, "true", "false");
		MARKUP.addAnnotationContentType(TEST_KEY, new TestIgnoreType());
		MARKUP.addAnnotationContentType(TEST_KEY, new TestDeclarationType());
	}

	public CIDashboardType() {
		super(MARKUP);
		this.addCompileScript(new DashboardSubtreeHandler());
		// this.setCustomRenderer(new DashboardRenderer());
		this.setRenderer(new CIDashboardRenderer());
		this.removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageRegistrationScript.class);
	}

	public static String getDashboardName(Section<CIDashboardType> section) {
		String name = DefaultMarkupType.getAnnotation(section, NAME_KEY);
		if (name == null) name = "unnamed";
		return name;
	}

	private static class DashboardSubtreeHandler extends DefaultGlobalScript<CIDashboardType> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<CIDashboardType> s) throws CompilerMessage {

			List<Message> msgs = new ArrayList<>();
			String triggerString = DefaultMarkupType.getAnnotation(s, TRIGGER_KEY);
			CIBuildTriggers trigger = null;
			Set<String> monitoredArticles = new HashSet<>();

			if (triggerString != null) {
				Pattern pattern = Pattern.compile("(?:\".+?\"|[^\\s]+)");
				Matcher matcher = pattern.matcher(triggerString);
				if (matcher.find()) {
					// get the name of the test
					try {
						trigger = CIBuildTriggers.valueOf(matcher.group());
						// get the monitoredArticles if onSave
						if (trigger == CIBuildTriggers.onSave) {
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
									// also resolve regex for onSave trigger articles
									Collection<Article> articles = s.getArticleManager().getArticles();
									Pattern onSaveArticleRegexPattern = Pattern.compile(parameter);
									for (Article article : articles) {
										if(onSaveArticleRegexPattern.matcher(article.getTitle()).matches()) {
											monitoredArticles.add(article.getTitle());
										}
									}

								}
								if(monitoredArticles.isEmpty()) {
									msgs.add(Messages.error("Article '" + parameter
											+ "' for trigger does not exist"));
								}
							}
						}
					}
					catch (IllegalArgumentException e) {
						msgs.add(Messages.error("Invalid trigger specified: " + triggerString));
					}
				}

				if (CIBuildTriggers.onSave == trigger && monitoredArticles.isEmpty()) {
					msgs.add(Messages.error("Invalid trigger: " + CIBuildTriggers.onSave
							+ " requires attached articles to monitor."));
				}
			}

			// This map is used for storing tests and their parameter-list
			// Map<String, List<String>> tests = new HashMap<String,
			// List<String>>();
			List<TestSpecification<?>> tests = new ArrayList<>();

			List<Section<? extends AnnotationContentType>> annotationSections =
					DefaultMarkupType.getAnnotationContentSections(s, TEST_KEY, GROUP_KEY);

			// iterate over all @test-Annotations
			List<ArgsCheckResult> messages = new ArrayList<>();
			for (Section<? extends AnnotationContentType> annoSection : annotationSections) {
				String type = annoSection.get().getName(annoSection);
				if (type.equalsIgnoreCase(GROUP_KEY)) {
					// parse group
					String text = annoSection.getText();
					TestSpecification<?> group = new TestSpecification<>(
							new TestGroup(), "void", new String[] { text }, new String[0][]);
					tests.add(group);
				}
				else {
					// parse test
					TestParser testParser = new TestParser(annoSection.getText());
					TestSpecification<?> executableTest = testParser.getTestSpecification();
					messages.add(testParser.getParameterCheckResult());
					messages.addAll(testParser.getIgnoreCheckResults());
					if (executableTest != null) {
						tests.add(executableTest);
					}
				}
			}
			convertMessages(s, messages);

			CIDashboard dashboard = CIDashboardManager.generateAndRegisterDashboard(s, tests);

			if (trigger == CIBuildTriggers.onSave) {
				CIHook ciHook = new CIHook(dashboard, monitoredArticles);
				CIHookManager.registerHook(ciHook);
				// Store to be able to unregister in destroy method
				KnowWEUtils.storeObject(s,
						CIHook.CIHOOK_STORE_KEY, ciHook);
			}
			throw new CompilerMessage(msgs);
		}

		private void convertMessages(Section<?> section, List<ArgsCheckResult> messages) {
			Collection<Message> msgs = new ArrayList<>();
			for (ArgsCheckResult message : messages) {
				if (message == null) continue;
				String[] arguments = message.getArguments();
				for (int i = 0; i < arguments.length; i++) {
					String messageText = message.getMessage(i);
					if (message.hasError(i)) {
						msgs.add(Messages.error(messageText));
					}
					else if (message.hasWarning(i)) {
						msgs.add(Messages.warning( messageText));
					}

				}
				if (arguments.length == 0 && message.getMessage(0) != null) {
					msgs.add(Messages.error( message.getMessage(0)));
				}
			}
			Messages.storeMessages(section, this.getClass(), msgs);
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<CIDashboardType> section) {
			CIHook ciHook = (CIHook) section.getObject(CIHook.CIHOOK_STORE_KEY);
			if (ciHook != null) {
				CIHookManager.unregisterHook(ciHook);
			}
			CIDashboardManager.unregisterDashboard(section);
		}

	}

}
