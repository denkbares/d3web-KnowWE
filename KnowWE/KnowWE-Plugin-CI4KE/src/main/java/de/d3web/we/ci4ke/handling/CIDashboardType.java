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

package de.d3web.we.ci4ke.handling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.ci4ke.util.Pair;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIDashboardType extends DefaultMarkupType {

	public static final String NAME_KEY = "name";
	public static final String TEST_KEY = "test";
	public static final String TRIGGER_KEY = "trigger";

	public static enum CIBuildTriggers {
		onDemand, onSave
	}

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("CIDashboard");
		MARKUP.addAnnotation(NAME_KEY, true);
		MARKUP.addAnnotation(TEST_KEY, true);
		MARKUP.addAnnotation(TRIGGER_KEY, true);
	}

	public CIDashboardType() {
		super(MARKUP);
		this.addSubtreeHandler(new DashboardSubtreeHandler(readIgnoreFlag()));
		// this.setCustomRenderer(new DashboardRenderer());
		this.setRenderer(new CIDashboardRenderer());
	}

	private boolean readIgnoreFlag() {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("KnowWE_config");
		String ignoreFlag = "packaging.ignorePackages";
		boolean ignorePackageCompile = false;
		if (resourceBundle.containsKey(ignoreFlag)) {
			if (resourceBundle.getString(ignoreFlag).contains("true")) {
				ignorePackageCompile = true;
			}
			if (resourceBundle.getString(ignoreFlag).contains("false")) {
				ignorePackageCompile = false;
			}
		}
		return ignorePackageCompile;
	}

	private class DashboardSubtreeHandler extends SubtreeHandler<CIDashboardType> {

		public DashboardSubtreeHandler(boolean ignorePackageCompile) {
			super(ignorePackageCompile);
		}

		@Override
		public boolean isIgnoringPackageCompile() {
			return true;
		}

		@Override
		public Collection<Message> create(KnowWEArticle article, Section<CIDashboardType> s) {

			List<Message> msgs = new ArrayList<Message>();

			String dashboardName = DefaultMarkupType.getAnnotation(s, NAME_KEY);

			if (dashboardName == null) return msgs;

			String triggerString = DefaultMarkupType.getAnnotation(s, TRIGGER_KEY);

			if (triggerString == null) return msgs;

			CIBuildTriggers trigger = null;

			Pattern pattern = Pattern.compile("(?:\\w+|\".+?\")");

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
							monitoredArticles.add(parameter);
						}
					}
				}
				catch (IllegalArgumentException e) {
					msgs.add(Messages.error("Invalid trigger specified: " + triggerString));
					return msgs;
				}
			}

			if (trigger.equals(CIBuildTriggers.onSave) && monitoredArticles.isEmpty()) {
				msgs.add(Messages.error("Invalid trigger: " + CIBuildTriggers.onSave
						+ " requires attached articles to monitor."));
				return msgs;
			}

			// This map is used for storing tests and their parameter-list
			// Map<String, List<String>> tests = new HashMap<String,
			// List<String>>();
			List<Pair<String, List<String>>> tests = new ArrayList<Pair<String, List<String>>>();

			List<Section<? extends AnnotationContentType>> annotationSections =
					DefaultMarkupType.getAnnotationContentSections(s, TEST_KEY);

			// iterate over all @test-Annotations
			for (Section<?> annoSection : annotationSections) {
				String annotationText = annoSection.getText();
				matcher = pattern.matcher(annotationText);
				if (matcher.find()) {
					// get the name of the test
					String testName = matcher.group();
					// get the parameters of the test
					List<String> testParamters = new ArrayList<String>();
					while (matcher.find()) {
						String parameter = matcher.group();
						if (parameter.startsWith("\"") && parameter.endsWith("\"")) {
							parameter = parameter.substring(1, parameter.length() - 1);
						}
						testParamters.add(parameter);
					}
					tests.add(new Pair<String, List<String>>(testName, testParamters));
				}
			}

			CIConfig config = new CIConfig(dashboardName, s.getArticle().getTitle(), tests, trigger);

			// Parse the trigger-parameter and (eventually) register
			// a CIHook
			if (trigger.equals(CIBuildTriggers.onSave)) {
				// Hook registrieren
				CIHook ciHook = new CIHook(article.getTitle(), dashboardName, monitoredArticles);
				CIHookManager.getInstance().registerHook(ciHook);
				// Store to be able to unregister in destroy method
				KnowWEUtils.storeObject(article, s,
						CIHook.CIHOOK_STORE_KEY, ciHook);
			}

			// Alright, everything seems to be ok. Let's store the CIConfig in
			// the store

			KnowWEUtils.storeObject(article, s, CIConfig.CICONFIG_STORE_KEY, config);

			return new ArrayList<Message>(0);
		}

		@Override
		public void destroy(KnowWEArticle article, Section<CIDashboardType> s) {
			CIHook ciHook = (CIHook) s.getSectionStore().getObject(article,
					CIHook.CIHOOK_STORE_KEY);
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
		for (KnowWEArticle article : KnowWEEnvironment.getInstance().
				getArticleManager(section.getWeb()).getArticles()) {
			Sections.findSuccessorsOfType(article.getSection(), CIDashboardType.class, sectionList);
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
