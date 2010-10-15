/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.taghandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * The ReportOverview TagHandler renders all {@link KDOMError} and
 * {@link KDOMWarning} messages into a wiki page. The rendered list helps to
 * identify errors/warnings in articles.
 * 
 * The Syntax for the TagHandler is:
 * 
 * <pre>
 * %%KnowWEPlugin
 * report = (both|error|warning) [this line is optional]
 * &#064;taghandlername reportoverview
 * %
 * </pre>
 * 
 * The key, value pair specifies witch reports are shown. If the pair is not
 * present all reports are shown as a default. Set to <strong>warning</strong>
 * only messages from type {@link KDOMWarning} are shown, set to
 * <strong>error</strong> only {@link KDOMError}.
 * 
 * 
 * @author smark
 * @created 12.10.2010
 */
public class ReportOverview extends AbstractTagHandler {

	/**
	 * Constructor of the ReportOverview TagHandler. If you want to change the
	 * name used in the wiki page to call this TagHandler simply change the name
	 * in the super call.
	 */
	public ReportOverview() {
		super("reportoverview");
	}

	/**
	 * Renders the result of the TagHandler into the wiki page. In this case it
	 * shows all error and warning reports of all sections of an article.
	 * 
	 * @param topic
	 * @param user
	 * @param value
	 * @param web
	 * @return
	 */
	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		
		String reportType = values.get("report");
		if( reportType == null || reportType == "") {
			reportType = "both";
		}
		
		StringBuilder html = new StringBuilder();
		StringBuilder errorHTML = new StringBuilder();
		StringBuilder warningHTML = new StringBuilder();

		KnowWEArticleManager manager = KnowWEEnvironment.getInstance().getArticleManager(web);
		Collection<KnowWEArticle> articles = manager.getArticles();
		
		for (KnowWEArticle article : articles) {
			Section<KnowWEArticle> root = article.getSection();

			Collection<KDOMReportMessage> errors = new ArrayList<KDOMReportMessage>();
			Collection<KDOMReportMessage> warnings = new ArrayList<KDOMReportMessage>();

			// search messages
			findMessages(root, article, errors, warnings);

			// render messages
			if (errors.size() > 0) {
				renderMessages(errors, errorHTML, article);
			}
			if (warnings.size() > 0) {
				renderMessages(warnings, warningHTML, article);
			}
		}

		if (errorHTML.length() > 0 && !reportType.equals("warning")) {
			html.append("<div class=\"panel\"><h3>Errors Summary</h3><dl>");
			html.append(errorHTML);
			html.append("</dl></div>");
		}
		if (warningHTML.length() > 0 && !reportType.equals("error")) {
			html.append("<div class=\"panel\"><h3>Warnings Summary</h3><dl>");
			html.append(warningHTML);
			html.append("</dl></div>");
		}
		return html.toString();
	}

	/**
	 * Creates the output HTML for a {@link KDOMReportMessage}.
	 * 
	 * @created 12.10.2010
	 * @param messages A {@link HashMap} containing the
	 *        {@link KDOMReportMessage} and the section the message occurred in.
	 * @param article The {@link KnowWEArticle} containing the erroneous
	 *        {@link Section}
	 * @param result The StringBuilder the verbalized {@link KDOMReportMessage}
	 *        should stored in.
	 */
	private void renderMessages(Collection<KDOMReportMessage> messages, StringBuilder result, KnowWEArticle article) {
		if (messages.size() > 0) {

			result.append("<dt><a href=\"Wiki.jsp?page=");
			result.append(article.getTitle()).append("\" class=\"wikipage\">");
			result.append(article.getTitle()).append("</a></dt>");

			for (KDOMReportMessage kdomReportMessage : messages) {
				if (kdomReportMessage instanceof KDOMError) {
					result.append("<dd><img src=\"templates/knowweTmps/images/error.gif\" title=\"KnowWEError\" />");
				}
				else {
					result.append("<dd><img src=\"templates/knowweTmps/images/exclamation.gif\" title=\"KnowWEError\" />");
				}
				result.append("<a href=\"Wiki.jsp?page=").append(article.getTitle()).append("#");
				result.append(kdomReportMessage.getSection().getID()).append("\" class=\"wikipage\">");
				result.append(kdomReportMessage.getVerbalization()).append("</a></dd>");
			}
		}
	}

	/**
	 * Searches for all {@link KDOMReportMessage} messages in the current
	 * article. All found {@link KDOMError} and {@link KDOMWarning} messages 
	 * are added to the according StringBuilder.
	 * 
	 * @created 12.10.2010
	 * @param section The root section of an {@link KnowWEArticle}.
	 * @param article The {@link KnowWEArticle} containing erroneous
	 *        {@link Section}'s
	 * @param errors {@link StringBuilder} containing all error messages
	 * @param warnings {@link StringBuilder} containing all warning messages
	 */
	private void findMessages(Section<?> section, KnowWEArticle article,
			Collection<KDOMReportMessage> errors, Collection<KDOMReportMessage> warnings) {

		List<Section<? extends KnowWEObjectType>> children = section.getChildren();
		for (Section<?> child : children) {

			Collection<KDOMError> e = KDOMReportMessage.getErrors(article, child);
			for (KDOMReportMessage kdomReportMessage : e) {
				errors.add(kdomReportMessage);
			}

			Collection<KDOMWarning> w = KDOMReportMessage.getWarnings(article, child);
			for (KDOMReportMessage kdomReportMessage : w) {
				warnings.add(kdomReportMessage);
			}
			findMessages(child, article, errors, warnings);
		}
	}
}
