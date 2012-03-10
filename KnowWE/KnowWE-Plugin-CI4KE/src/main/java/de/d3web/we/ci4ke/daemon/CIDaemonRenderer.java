package de.d3web.we.ci4ke.daemon;

import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIDaemonRenderer implements Renderer {

	@Override
	public void render(Section<?> section, UserContext user, StringBuilder string) {

		String dashboardName = DefaultMarkupType.getAnnotation(section,
				CIDashboardType.NAME_KEY);
		String dashboardArticle = DefaultMarkupType.getAnnotation(section,
				CIDaemonType.DASHBOARD_ARTICLE);
		string.append(KnowWEUtils.maskHTML(renderDaemonContents(section.getWeb(), dashboardName,
				dashboardArticle)));

	}

	public static String renderDaemonContents(String web, String dashboardName, String dashboardArticleTitle) {

		StringBuilder string = new StringBuilder();
		CIBuildPersistenceHandler handler = CIBuildPersistenceHandler.
				getHandler(dashboardName, dashboardArticleTitle);
		if (handler == null) {
			return "";
		}

		if (!Environment.getInstance().getArticleManager(web).getTitles().contains(
				dashboardArticleTitle)) {
			string.append("<span class='error'>");
			string.append("The annotation @" + CIDaemonType.DASHBOARD_ARTICLE
					+ " has to specify an existing article name.");
			string.append("</span>");
			return string.toString();
		}

		String baseURL = Environment.getInstance().getWikiConnector().getBaseUrl();
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
