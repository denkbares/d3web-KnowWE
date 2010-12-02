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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.core.utilities.Pair;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.AnnotationType;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public class CIDashboardType extends DefaultMarkupType {

	// public static final String WEB_KEY = "web";
	// public static final String MONITORED_ARTICLE_KEY = "monitoredArticle";
	// public static final String DASHBOARD_ARTICLE_KEY = "dashboardArticle";

	public static final String NAME_KEY = "name";
	public static final String TEST_KEY = "test";
	public static final String TRIGGER_KEY = "trigger";

	public static enum CIBuildTriggers {
		onDemand
		// , onSave
	}

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("CIDashboard");
		// MARKUP.addAnnotation(MONITORED_ARTICLE_KEY, true);
		MARKUP.addAnnotation(NAME_KEY, true);
		MARKUP.addAnnotation(TEST_KEY, true);
		MARKUP.addAnnotation(TRIGGER_KEY, true, CIBuildTriggers.values());
	}

	public CIDashboardType() {
		super(MARKUP);
		this.addSubtreeHandler(new DashboardSubtreeHandler());
		// this.setCustomRenderer(new DashboardRenderer());
		this.setCustomRenderer(new CIDashboardRenderer());
	}

	private class DashboardSubtreeHandler extends SubtreeHandler<CIDashboardType> {

		@Override
		public boolean isIgnoringPackageCompile() {
			return true;
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<CIDashboardType> s) {

			// TODO is this still neccessary?
			// AbstractKnowWEObjectType.cleanMessages(article, s,
			// this.getClass());

			String dashboardName = DefaultMarkupType.getAnnotation(s, NAME_KEY);

			CIBuildTriggers trigger = CIBuildTriggers.valueOf(DefaultMarkupType.
					getAnnotation(s, TRIGGER_KEY));

			// This map is used for storing tests and their parameter-list
			// Map<String, List<String>> tests = new HashMap<String,
			// List<String>>();
			List<Pair<String, List<String>>> tests = new ArrayList<Pair<String, List<String>>>();

			List<Section<? extends AnnotationType>> annotationSections =
					DefaultMarkupType.getAnnotationSections(s, TEST_KEY);
			Pattern pattern = Pattern.compile("(?:\\w+|\".+?\")");

			// iterate over all @test-Annotations
			for(Section<?> annoSection : annotationSections) {
				String annotationText = annoSection.getOriginalText();
				Matcher matcher = pattern.matcher(annotationText);
				if (!matcher.find()) {
					// No Testname entered: TODO: Render warning!
				}
				else {
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

			// String id = getDashboardID(s);

			// Parse the trigger-parameter and (eventually) register
			// or deregister a CIHook
			// if (trigger.equals(CIDashboardType.CIBuildTriggers.onDemand)) {
			// // the tests of this dashboard should (currently!) be build
			// // only on Demand. Lets check, if there is a hook registered
			// // for this dashboard, then deregister it.
			// CIHookManager.getInstance().deRegisterHook(monitoredArticle,
			// s.getArticle().getTitle(), id);
			// }
			// else if (trigger.equals(CIDashboardType.CIBuildTriggers.onSave))
			// {
			// Logger.getLogger(this.getClass().getName()).log(
			// Level.INFO, ">> CI >> Setting Hook on " + monitoredArticle);
			//
			// // Hook registrieren, TODO: Wenn er nicht schon registriert ist!
			// CIHookManager.getInstance().registerHook(monitoredArticle,
			// s.getArticle().getTitle(), id);
			// }

			// Alright, everything seems to be ok. Let's store the CIConfig in
			// the store

			// KnowWEUtils.storeSectionInfo(s, CIConfig.CICONFIG_STORE_KEY,
			// config);

			KnowWEUtils.storeObject(s.getArticle().getWeb(), s.getTitle(), s.getID(),
					CIConfig.CICONFIG_STORE_KEY, config);

			return new ArrayList<KDOMReportMessage>(0);
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
			article.getSection().findSuccessorsOfType(CIDashboardType.class, sectionList);
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
