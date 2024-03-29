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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import de.knowwe.quartz.QuartzSchedulerJobServer;
import de.d3web.testing.ArgsCheckResult;
import de.d3web.testing.TestGroup;
import de.d3web.testing.TestParser;
import de.d3web.testing.TestSpecification;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardJob;
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
import de.knowwe.kdom.dashtree.LineEndComment;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageRegistrationScript;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIDashboardType extends DefaultMarkupType {

	public static final String NAME_KEY = "name";
	public static final String GROUP_KEY = "group";
	public static final String TEST_KEY = "test";

	public static final String SOFT_TEST_KEY = "softTest";
	public static final String TRIGGER_KEY = "trigger";
	public static final String TRIGGER_REGEX = "^(onDemand|(onSave|onSchedule)\\s*(\".+?\"|[^\\s]+))$";
	public static final String VERBOSE_PERSISTENCE_KEY = "persistenceVerbose";
	public static final String SCHEDULE_GROUP = CIDashboard.class.getSimpleName();

	public enum CIBuildTriggers {
		onDemand, onSave, onSchedule
	}

	protected static final DefaultMarkup MARKUP;
	private static final String CI_DASHBOARD_MARKUP_NAME = "CIDashboard";

	static {
		MARKUP = createMarkup(CI_DASHBOARD_MARKUP_NAME, true);
	}

	protected static DefaultMarkup createMarkup(String markupName, boolean testMandatory) {
		DefaultMarkup markup = new DefaultMarkup(markupName);
		markup.addAnnotation(NAME_KEY, true);
		markup.addAnnotation(TEST_KEY, testMandatory);
		markup.addAnnotation(SOFT_TEST_KEY, false);
		markup.addAnnotation(TRIGGER_KEY, true, Pattern.compile(TRIGGER_REGEX));
		markup.getAnnotation(TRIGGER_KEY).setDocumentation("Specify how to trigger the build of this dashboard." +
				"<p><b>Options:</b><br>" +
				"  <ul>" +
				"    <li>@" + TRIGGER_KEY + ": onDemand</li>" +
				"    <li>@" + TRIGGER_KEY + ": onSave \"Article Title Pattern\"</li>" +
				"    <li>@" + TRIGGER_KEY + ": onSchedule \"Cron Job Pattern\"</li>" +
				"  </ul>" +
				"</p>");
		markup.getAnnotation(SOFT_TEST_KEY).setDocumentation("Declare tests as 'soft tests'.<br>" +
				"Soft tests are only acknowledged in the build details but have no effect on the overall build result.");

		// allow grouping of tests
		markup.addAnnotation(GROUP_KEY, false);

		// add content for individual annotations
		markup.addAnnotation(VERBOSE_PERSISTENCE_KEY, false, "true", "false");
		markup.addAnnotationContentType(TEST_KEY, new TestIgnoreType());
		markup.addAnnotationContentType(SOFT_TEST_KEY, new TestIgnoreType());
		markup.addAnnotationContentType(TEST_KEY, new TestDeclarationType());
		markup.addAnnotationContentType(SOFT_TEST_KEY, new TestDeclarationType());
		return markup;
	}

	public CIDashboardType() {
		super(MARKUP);
		this.addCompileScript(new DashboardSubtreeHandler());
		// this.setCustomRenderer(new DashboardRenderer());
		this.setRenderer(new CIDashboardRenderer());
		this.removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageRegistrationScript.class);
	}

	public CIDashboardType(DefaultMarkup markup) {
		super(markup);
		//this.addCompileScript(new DashboardSubtreeHandler());
		this.setRenderer(new CIDashboardRenderer());
		this.removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageRegistrationScript.class);
	}

	public static String getDashboardName(Section<CIDashboardType> section) {
		String name = DefaultMarkupType.getAnnotation(section, NAME_KEY);
		if (name == null) name = "unnamed";
		return name;
	}

	protected static class DashboardSubtreeHandler extends DefaultGlobalScript<CIDashboardType> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<CIDashboardType> section) throws CompilerMessage {

			List<Message> msgs = new ArrayList<>();
			String triggerString = DefaultMarkupType.getAnnotation(section, TRIGGER_KEY);
			CIBuildTriggers trigger = null;
			Set<String> monitoredArticles = new HashSet<>();
			CronScheduleBuilder cronSchedule = null;

			if (triggerString != null) {
				Pattern pattern = Pattern.compile("(?:\".+?\"|[^\\s]+)");
				Matcher matcher = pattern.matcher(triggerString);
				if (matcher.find()) {
					// get the name of the test
					try {
						trigger = CIBuildTriggers.valueOf(matcher.group());
						switch (trigger) {
							case onSave -> {
								while (matcher.find()) {
									// get the monitoredArticles if onSave
									String parameter = parseParameter(matcher.group());
									if (Environment.getInstance().getWikiConnector().doesArticleExist(parameter)) {
										monitoredArticles.add(parameter);
									}
									else {
										// also resolve regex for onSave trigger articles
										Collection<Article> articles = section.getArticleManager().getArticles();
										Pattern onSaveArticleRegexPattern = Pattern.compile(parameter);
										for (Article article : articles) {
											if (onSaveArticleRegexPattern.matcher(article.getTitle()).matches()) {
												monitoredArticles.add(article.getTitle());
											}
										}
									}
									if (monitoredArticles.isEmpty()) {
										msgs.add(Messages.error("Article '" + parameter + "' for trigger does not exist"));
									}
								}
							}
							case onSchedule -> {
								if (matcher.find()) {
									String cronExpression = parseParameter(matcher.group());
									cronSchedule = CronScheduleBuilder.cronSchedule(cronExpression);
								}
							}
						}
					}
					catch (IllegalArgumentException e) {
						msgs.add(Messages.error("Invalid trigger specified: " + triggerString));
					}
					catch (RuntimeException e) {
						msgs.add(Messages.error("Invalid cron expression: " + e.getMessage()));
					}
				}
				if (CIBuildTriggers.onSave == trigger && monitoredArticles.isEmpty()) {
					msgs.add(Messages.error("Invalid trigger: " + CIBuildTriggers.onSave + " requires attached articles to monitor."));
				}
				else if (CIBuildTriggers.onSchedule == trigger && cronSchedule == null) {
					msgs.add(Messages.error("Invalid trigger: " + CIBuildTriggers.onSchedule + " requires cron expression to schedule."));
				}
			}
			List<Section<? extends AnnotationContentType>> annotationSections = getAnnotationSections(section);

			TestProcessingResult result = processTests(annotationSections, compiler, section);
			List<TestSpecification<?>> tests = result.testSpecifications();
			List<ArgsCheckResult> messages = processMessages(result.testParsers());
			convertMessages(section, messages);
			register(section, tests, trigger, monitoredArticles, cronSchedule);
			throw new CompilerMessage(msgs);
		}

		private static String parseParameter(String text) {
			if (text.startsWith("\"") && text.endsWith("\"")) {
				return text.substring(1, text.length() - 1);
			}
			return text;
		}

		protected List<Section<? extends AnnotationContentType>> getAnnotationSections(Section<CIDashboardType> section) {
			return DefaultMarkupType.getAnnotationContentSections(section, TEST_KEY, GROUP_KEY, SOFT_TEST_KEY);
		}

		protected TestProcessingResult processTests(List<Section<? extends AnnotationContentType>> annotationSections, DefaultGlobalCompiler compiler, Section<CIDashboardType> s) {
			List<TestSpecification<?>> tests = new ArrayList<>();
			List<TestParser> testParsers = new ArrayList<>();

			for (Section<? extends AnnotationContentType> annoSection : annotationSections) {
				String type = annoSection.get().getName(annoSection);
				String textWithoutComment = getTextWithoutComment(annoSection);
				if (type.equalsIgnoreCase(GROUP_KEY)) {
					TestSpecification<?> group = new TestSpecification<>(
							new TestGroup(), "void", new String[] { textWithoutComment }, new String[0][]);
					tests.add(group);
				}
				else {
					if (type.equalsIgnoreCase(SOFT_TEST_KEY)) {
						textWithoutComment += " " + type;
					}
					TestParser testParser = new TestParser(textWithoutComment);
					TestSpecification<?> executableTest = testParser.getTestSpecification();
					if (executableTest != null) {
						tests.add(executableTest);
					}
					testParsers.add(testParser);
				}
			}
			return new TestProcessingResult(tests, testParsers);
		}

		@NotNull
		protected static String getTextWithoutComment(Section<? extends AnnotationContentType> annoSection) {
			return annoSection.getChildren()
					.stream()
					.filter(c -> !(c.get() instanceof LineEndComment))
					.map(c -> {
						if (c.getChildren().isEmpty()) {
							return c.getText();
						}
						else {
							return c.getChildren().stream()
									.filter(l -> !(l.get() instanceof LineEndComment))
									.map(Section::getText).collect(Collectors.joining());
						}
					})
					.collect(Collectors.joining());
		}

		protected List<ArgsCheckResult> processMessages(List<TestParser> testParsers) {
			List<ArgsCheckResult> messages = new ArrayList<>();
			for (TestParser testParser : testParsers) {
				messages.add(testParser.getParameterCheckResult());
				messages.addAll(testParser.getIgnoreCheckResults());
			}
			return messages;
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
						msgs.add(Messages.warning(messageText));
					}
				}
				if (arguments.length == 0 && message.getMessage(0) != null) {
					msgs.add(Messages.error(message.getMessage(0)));
				}
			}
			Messages.storeMessages(section, this.getClass(), msgs);
		}

		private void register(Section<CIDashboardType> section, List<TestSpecification<?>> tests, CIBuildTriggers trigger, Set<String> monitoredArticles, CronScheduleBuilder cronSchedule) {
			CIDashboard dashboard = CIDashboardManager.generateAndRegisterDashboard(section, tests);
			if (trigger == CIBuildTriggers.onSave) {
				CIHook ciHook = new CIHook(dashboard, monitoredArticles);
				CIHookManager.registerHook(ciHook);
				// store to be able to unregister hook in destroy method
				KnowWEUtils.storeObject(section, CIHook.CI_HOOK_STORE_KEY, ciHook);
			}
			else if (trigger == CIBuildTriggers.onSchedule && cronSchedule != null) {
				String dashboardName = dashboard.getDashboardName();
				JobDataMap jobData = new JobDataMap(Collections.singletonMap(CIDashboardJob.DASHBOARD_KEY, dashboard));
				JobDetail job = JobBuilder
						.newJob(CIDashboardJob.class)
						.withIdentity(dashboardName, SCHEDULE_GROUP)
						.usingJobData(jobData)
						.build();
				Trigger jobTrigger = TriggerBuilder
						.newTrigger()
						.withIdentity(dashboardName, SCHEDULE_GROUP)
						.withSchedule(cronSchedule)
						.build();
				QuartzSchedulerJobServer.registerJob(job, jobTrigger);
				// store to be able to delete job in destroy method
				KnowWEUtils.storeObject(section, CIHook.CI_HOOK_STORE_KEY, job.getKey());
			}
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<CIDashboardType> section) {
			Object object = section.getObject(CIHook.CI_HOOK_STORE_KEY);
			if (object instanceof CIHook ciHook) {
				CIHookManager.unregisterHook(ciHook);
			}
			else if (object instanceof JobKey jobKey) {
				QuartzSchedulerJobServer.deleteJob(jobKey);
			}
			CIDashboardManager.unregisterDashboard(section);
		}
	}

	public record TestProcessingResult(List<TestSpecification<?>> testSpecifications, List<TestParser> testParsers) {
	}
}
