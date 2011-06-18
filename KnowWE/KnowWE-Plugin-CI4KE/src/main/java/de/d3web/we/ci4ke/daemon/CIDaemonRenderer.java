package de.d3web.we.ci4ke.daemon;

import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.user.UserContext;
import de.d3web.we.utils.KnowWEUtils;

public class CIDaemonRenderer extends KnowWEDomRenderer<CIDaemonType> {

	@Override
	public void render(KnowWEArticle article, Section<CIDaemonType> section, UserContext user, StringBuilder string) {

		String dashboardName = DefaultMarkupType.getAnnotation(section,
				CIDashboardType.NAME_KEY);
		String dashboardArticle = DefaultMarkupType.getAnnotation(section,
				CIDaemonType.DASHBOARD_ARTICLE);
		string.append(KnowWEUtils.maskHTML(renderDaemonContents(article.getWeb(), dashboardName,
				dashboardArticle)));

	}

	public static String renderDaemonContents(String web, String dashboardName, String dashboardArticleTitle) {

		StringBuilder string = new StringBuilder();
		CIBuildPersistenceHandler handler = CIBuildPersistenceHandler.
				getHandler(dashboardName, dashboardArticleTitle);
		if (handler == null) {
			return "";
		}

		if (!KnowWEEnvironment.getInstance().getArticleManager(web).getTitles().contains(
				dashboardArticleTitle)) {
			string.append("<span class='error'>");
			string.append("The annotation @" + CIDaemonType.DASHBOARD_ARTICLE
					+ " has to specify an existing article name.");
			string.append("</span>");
			return string.toString();
		}

		String baseURL = KnowWEEnvironment.getInstance().getWikiConnector().getBaseUrl();
		String srclink = "<a href=\"" + baseURL + (baseURL.endsWith("/") ? "" : "/")
				+ "Wiki.jsp?page="
				+ dashboardArticleTitle
				+ "\">";
		string.append(srclink);

		string.append(handler.renderCurrentBuildStatus(16));
		string.append("</a>");

		return string.toString();
	}

}
