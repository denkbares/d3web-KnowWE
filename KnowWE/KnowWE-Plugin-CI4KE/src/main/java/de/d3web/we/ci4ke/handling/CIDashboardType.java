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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.Element;

import de.d3web.report.Message;
import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.diff.DiffEngine;
import de.d3web.we.ci4ke.diff.DiffFactory;
import de.d3web.we.ci4ke.handling.CITestResult.TestResultType;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.AnnotationType;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

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
		this.setCustomRenderer(new DashboardRenderer());
	}

	private class DashboardSubtreeHandler extends SubtreeHandler<CIDashboardType> {

		@Override
		public boolean isIgnoringPackageCompile() {
			return true;
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<CIDashboardType> s) {

			// TODO is this still neccessary?
			AbstractKnowWEObjectType.cleanMessages(article, s, this.getClass());

			// String monitoredArticle =
			// DefaultMarkupType.getAnnotation(s, MONITORED_ARTICLE_KEY);
			// String tests =
			// DefaultMarkupType.getAnnotation(s, TEST_KEY);

			String dashboardName = DefaultMarkupType.getAnnotation(s, NAME_KEY);

			CIBuildTriggers trigger = CIBuildTriggers.valueOf(DefaultMarkupType.
					getAnnotation(s, TRIGGER_KEY));

			// This map is used for storing tests and their parameter-list
			Map<String, List<String>> tests = new HashMap<String, List<String>>();

			List<Section<? extends AnnotationType>> annotationSections =
					DefaultMarkupType.getAnnotationSections(s, TEST_KEY);
			Pattern pattern = Pattern.compile("(?:\\w+|\".+?\")");

			// iterate over all @test-Annotations
			for(Section<?> annoSection : annotationSections) {
				String annotationText = annoSection.getOriginalText();
				Matcher matcher = pattern.matcher(annotationText);
				if (!matcher.find()) {
					// No Testname entered: Render warning!
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
					tests.put(testName, testParamters);
				}
			}

			CIConfig config = new CIConfig(dashboardName, s.getArticle().getTitle(), tests, trigger);

			// Check if monitored Article exists
			/*
			 * This is commented out, because the existence-check of the
			 * monitored article resulted in a lot of error messages on KnowWE
			 * Startup... When the startup procedure gets improved, this check
			 * can probably be activated again!
			 */
			// KnowWEArticle art = KnowWEEnvironment.getInstance().
			// getArticle(s.getWeb(), monitoredArticle);
			// if(art==null) {
			// Message message = new Message(Message.ERROR,
			// "Monitored article does not exist!", "", -1, "");
			// DefaultMarkupType.storeSingleMessage(article, s, this.getClass(),
			// message);
			// return null;
			// }

			// if (!dashboardIDisUnique(s)) {
			// // dashboardArticle+monitoredArticle combination does not
			// // uniquely identify a dashboard.
			// // check, if the ID has been overridden:
			// String overriddenID = DefaultMarkupType.getAnnotation(s,
			// NAME_KEY);
			//
			// if (overriddenID == null || overriddenID.equals("")) {
			// // the dashboard can't be uniquely identified
			// // and it's ID was not overridden. Post a warning message!
			// String message = "This dashboard can't be " +
			// "uniquely identified! Please add the 'id' annotation!";
			// // DefaultMarkupType.storeSingleMessage(article, s,
			// // this.getClass(), message);
			//
			// return Arrays.asList((KDOMReportMessage) new
			// ObjectCreationError(message,
			// this.getClass()));
			// }
			// else {
			// // the dashboard can't be uniquely identified, but a
			// // override-id annotation was set. now check if this ID
			// // itself is unique!
			// if (!dashboardIDisUnique(s, overriddenID)) {
			// // the overridden ID was not unique! Post error message
			// String message = "This dashboard can't be " +
			// "uniquely identified! Please check the id-annotation " +
			// "(must be unique in the whole wiki)";
			// // DefaultMarkupType.storeSingleMessage(article, s,
			// // this.getClass(), message);
			// return Arrays.asList((KDOMReportMessage) new
			// ObjectCreationError(message,
			// this.getClass()));
			// }
			// }
			// }

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

	private class DashboardRenderer extends KnowWEDomRenderer<CIDashboardType> {

		@Override
		public void render(KnowWEArticle article, Section<CIDashboardType> sec,
				KnowWEUserContext user, StringBuilder string) {

			boolean hasWarningOrError = false;

			Collection<Message> messages = DefaultMarkupType.getMessages(article, sec);
			for (Message m : messages) {
				if (m.getMessageType().equals(Message.ERROR) ||
						m.getMessageType().equals(Message.WARNING)) {
					hasWarningOrError = true;
					Logger.getLogger(this.getClass().getName()).log(
							Level.INFO,
							" >> DashboardRenderer >> "
									+ "Found error(s) and/or warnings in this Dashboard-Section! "
									+ "These messages will be "
									+ "rendered instead of the dashboard! ");
				}
			}

			if (hasWarningOrError) {
				// Render Error-Messages!
				DefaultMarkupRenderer.renderMessages(article, sec, string);
				// return;
			}
			else {
				CIDashboard board = new CIDashboard(sec);
				string.append(KnowWEUtils.maskHTML(board.render()));
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
			article.getSection().findSuccessorsOfType(CIDashboardType.class, sectionList);
		}

		for (Section<CIDashboardType> s : sectionList) {
			if (s.getID() != section.getID()) {
				String otherDashboardName = DefaultMarkupType.getAnnotation(section, NAME_KEY);
				if (otherDashboardName.equals(thisDashboardName)) return false;
			}
		}
		return true;
	}

	/**
	 * Renders out the wiki changes (middle column) and the test results (right
	 * column) of a selected build
	 * 
	 * @return
	 */
	public static String renderBuildDetails(String dashboardID, String dashboardArticleTitle, int selectedBuildNumber) {

		KnowWEWikiConnector conny = KnowWEEnvironment.getInstance().getWikiConnector();
		CIBuildPersistenceHandler handler = CIBuildPersistenceHandler.getHandler(dashboardID,
				dashboardArticleTitle);
		StringBuffer buffy = new StringBuffer();

		// ------------------------------------------------------------------------
		// Render the build details in the middle colum
		// (ci-column-middle)
		// ------------------------------------------------------------------------

		buffy.append("<div id='" + dashboardID + "-column-middle' class='ci-column-middle'>");

		String xPath = "builds/build[@nr=%s]/tests/test";
		List<?> tests = handler.selectNodes(String.format(xPath, selectedBuildNumber));

		buffy.append("<h3 style=\"background-color: #CCCCCC;\">" +
				"Results of Build #" + selectedBuildNumber + "</h3>");

		for (Object o : tests) {
			if (o instanceof Element) {
				Element e = (Element) o;

				buffy.append("<div class='ci-collapsible-box'><h4>");

				// Render Test Result
				String s = e.getAttributeValue("result");
				if (s != null && !s.isEmpty()) {
					TestResultType buildResult = TestResultType.valueOf(s);
					buffy.append(CIUtilities.renderResultType(buildResult, 16));
				}
				// Render Test-Name
				s = e.getAttributeValue("name");
				if (s != null && !s.isEmpty()) {
					buffy.append(s);
				}

				buffy.append("</h4>\n");

				// Render Test Message (if existent)
				buffy.append("<span style=\"display: none;\">\n");
				s = e.getAttributeValue("message");
				if (s != null && !s.isEmpty()) {
					buffy.append(s);
				}

				buffy.append("</span>\n");

				buffy.append("</div>\n");
			}
		}
		buffy.append("</table></div>");

		// ------------------------------------------------------------------------

		// -------------------------------------------------------------------------
		// Render the wiki-changes in the right column
		// (ci-column-right)
		// -------------------------------------------------------------------------

		buffy.append("<div id='" + dashboardID + "-column-right' class='ci-column-right'>");

		// the version of the article of the selected build
		int articleVersionSelected = 1;
		// the version of the article of the previous build
		int articleVersionPrevious = 1;

		// Number of build to compare to (the previous build number)
		int previousBuildNr = selectedBuildNumber > 1 ? selectedBuildNumber - 1 : 1;

		String monitoredArticleTitle = "";
		Object attrib = handler.selectSingleNode("builds/@monitoredArticle");
		if (attrib instanceof Attribute) {
			monitoredArticleTitle = ((Attribute) attrib).getValue();
		}
		attrib = null;

		// ---------------

		// xPath to select the article version of a buildNumber
		xPath = "builds/build[@nr=%s]/@articleVersion";

		// try to parse the selected build article version
		attrib = handler.selectSingleNode(String.format(xPath, selectedBuildNumber));
		if (attrib instanceof Attribute) {
			String attrValue = ((Attribute) attrib).getValue();
			if (attrValue != null && !attrValue.isEmpty()) {
				articleVersionSelected = Integer.parseInt(attrValue);
			}
		}
		attrib = null;

		// try to parse the selected build article version
		attrib = handler.selectSingleNode(String.format(xPath, previousBuildNr));
		if (attrib instanceof Attribute) {
			String attrValue = ((Attribute) attrib).getValue();
			if (attrValue != null && !attrValue.isEmpty()) {
				articleVersionPrevious = Integer.parseInt(attrValue);
			}
		}
		attrib = null;

		// buffy.append("<h4>Unterschiede zwischen <b>Build " + buildNr +
		// "</b> (Article Version " + articleVersionSelected +
		// ") und <b>Build " + previousBuildNr + "</b> (Article " +
		// "Version " + articleVersionPrevious + ")</h4>");

		// buffy.append("<h3 style=\"background-color: #CCCCCC;\">Differences between Build #"
		// +
		// selectedBuildNumber + " and Build #" + previousBuildNr + "</h3>");
		buffy.append("<h3 style=\"background-color: #CCCCCC;\">Changes in Build #" +
				selectedBuildNumber + "</h3>");

		String author = conny.getAuthor(monitoredArticleTitle, articleVersionSelected);

		if (author != null && !author.isEmpty()) {
			buffy.append("<div id=\"last-author-changed\"><b>Last change:</b> " +
					author + "</div>");
		}

		xPath = "builds/build[@nr=%s]/modifiedArticles/modifiedArticle";
		List<?> articles = handler.selectNodes(String.format(xPath, selectedBuildNumber));
		DiffEngine diff = DiffFactory.defaultDiffEngine();

		for (Object o : articles) {
			if (o instanceof Element) {
				Element e = (Element) o;
				String title = e.getAttributeValue("title");
				Integer rangeFrom = Integer.parseInt(e.getAttributeValue("rangeFrom"));
				Integer rangeTo = Integer.parseInt(e.getAttributeValue("rangeTo"));

				buffy.append("Changes in " + title + ":<br/>");
				buffy.append(diff.makeDiffHtml(
						conny.getArticleSource(title, rangeFrom),
						conny.getArticleSource(title, rangeTo)));
			}
		}

		buffy.append(diff.makeDiffHtml(
						conny.getArticleSource(monitoredArticleTitle,
								articleVersionPrevious),
						conny.getArticleSource(monitoredArticleTitle,
								articleVersionSelected)));

		buffy.append("</div>");

		return buffy.toString();
	}
}
