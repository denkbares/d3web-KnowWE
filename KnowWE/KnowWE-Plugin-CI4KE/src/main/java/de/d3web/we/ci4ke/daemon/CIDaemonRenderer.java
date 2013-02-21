package de.d3web.we.ci4ke.daemon;

import de.d3web.testing.Message.Type;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIDaemonRenderer implements Renderer {

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {
		String content = DefaultMarkupType.getContent(section);
		String dashboardName = DefaultMarkupType.getAnnotation(section,
				CIDashboardType.NAME_KEY);
		String dashboardArticle = DefaultMarkupType.getAnnotation(section,
				CIDaemonType.DASHBOARD_ARTICLE);
		renderDaemonContents(content, section.getWeb(),
				dashboardName, dashboardArticle, string);

	}

	public static String renderDaemonContents(String content, String web, String dashboardName, String dashboardArticleTitle, RenderResult string) {

		if (!Environment.getInstance().getArticleManager(web).getTitles().contains(
				dashboardArticleTitle)) {
			string.appendHTML("<span class='error'>");
			string.append("The annotation @" + CIDaemonType.DASHBOARD_ARTICLE
					+ " has to specify an existing article name.");
			string.appendHTML("</span>");
		}
		boolean hasDashboard = CIDashboard.hasDashboard(web, dashboardArticleTitle, dashboardName);

		if (!hasDashboard) {
			string.appendHTML("<span class='error'>");
			string.append("The annotation @" + CIDashboardType.NAME_KEY
					+ " has to specify an existing CI dashboard name on the specified article.");
			string.appendHTML("</span>");
		}

		String baseURL =
				Environment.getInstance().getWikiConnector().getBaseUrl();
		String srclink = "<a class=\"ci-daemon\" href=\"" + baseURL + (baseURL.endsWith("/") ? ""
				: "/")
				+ "Wiki.jsp?page="
				+ dashboardArticleTitle
				+ "\">";
		string.appendHTML(srclink);

		CIDashboard dashboard = CIDashboard.getDashboard(web, dashboardArticleTitle, dashboardName);
		CIRenderer renderer = dashboard.getRenderer();
		if (hasDashboard) {
			renderer.renderCurrentBuildStatus(string);
		}
		else {
			renderer.renderBuildStatus(Type.ERROR, true, "", string);
		}
		string.appendHTML("</a>");

		string.append(Environment.getInstance().getWikiConnector().renderWikiSyntax(content));

		return string.toString();
	}
}
