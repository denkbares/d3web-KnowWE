package de.d3web.we.action;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.utils.KnowWEUtils;

public class ParseWebOfflineRenderer implements KnowWEAction {

	private static ResourceBundle kwikiBundle = ResourceBundle
			.getBundle("KnowWE_messages");

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String webname = parameterMap.get(KnowWEAttributes.WEB);

		Map<String, String> articles = KnowWEEnvironment.getInstance()
				.getWikiConnector().getAllArticles(webname);
		Set<String> articleNames = articles.keySet();
		StringBuffer reports = new StringBuffer();
		int problems = 0;
		for (String name : articleNames) {
			KnowWEDomParseReport object = KnowWEEnvironment.getInstance()
					.getArticleManager(webname).saveUpdatedArticle(new KnowWEArticle(articles.get(name),
							name, KnowWEEnvironment.getInstance().getRootTypes(),webname));
			
			if (object.hasErrors()) {
				reports.append("<p class=\"box error\">");
			} else {
				reports.append("<p class=\"box ok\">");
			}
			reports.append(kwikiBundle.getString("webparser.info.parsing")
					+ createLink(name, webname) + ": "
					+ object.getShortStatus() + "<br />");
			if (object.hasErrors()) {
				problems++;
				reports.append("<br />\n");
			} else {

				reports.append(kwikiBundle.getString("webparser.info.saved")
						+ " </p>");
			}
		}
		
		String converted = KnowWEUtils.convertUmlaut(reports.toString());
		reports.delete(0, reports.length());
		reports.append(converted);
		
		reports.insert(0, "<a href=\"#\" onclick=\"clearInnerHTML('parseWeb');\">" + kwikiBundle.getString("KnowWE.buttons.close") + "</a><br />");
		
		return reports.toString();

	}

	private String createLink(String topicName, String webname) {

		return "<a href='Wiki.jsp?page=" + topicName + "' target='_blank'>"
				+ topicName + "</a>";
	}

}
