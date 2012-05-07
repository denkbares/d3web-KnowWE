package de.d3web.we.ci4ke.daemon;

import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.d3web.we.ci4ke.testing.CITestResult.Type;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIDaemonRenderer implements Renderer {

	private static final int PIXEL_SIZE = 16;

	@Override
	public void render(Section<?> section, UserContext user, StringBuilder string) {

		String dashboardName = DefaultMarkupType.getAnnotation(section,
				CIDashboardType.NAME_KEY);
		String dashboardArticle = DefaultMarkupType.getAnnotation(section,
				CIDaemonType.DASHBOARD_ARTICLE);
		string.append(Strings.maskHTML(renderDaemonContents(section.getWeb(), dashboardName,
				dashboardArticle)));

	}

	public static String renderDaemonContents(String web, String dashboardName, String dashboardArticleTitle) {

		StringBuilder string = new StringBuilder();

		if (!Environment.getInstance().getArticleManager(web).getTitles().contains(
				dashboardArticleTitle)) {
			string.append("<span class='error'>");
			string.append("The annotation @" + CIDaemonType.DASHBOARD_ARTICLE
					+ " has to specify an existing article name.");
			string.append("</span>");
		}
		CIBuildPersistenceHandler handler = CIBuildPersistenceHandler.
				getExistingHandler(dashboardName, dashboardArticleTitle);

		if (handler == null) {
			string.append("<span class='error'>");
			string.append("The annotation @" + CIDashboardType.NAME_KEY
					+ " has to specify an existing CI dashboard name on the specified article.");
			string.append("</span>");
		}

		String baseURL = Environment.getInstance().getWikiConnector().getBaseUrl();
		String srclink = "<a href=\"" + baseURL + (baseURL.endsWith("/") ? "" : "/")
				+ "Wiki.jsp?page="
				+ dashboardArticleTitle
				+ "\">";
		string.append(srclink);

		if (handler == null) {
			string.append(CIUtilities.renderResultType(Type.ERROR, PIXEL_SIZE));
		}
		else {
			string.append(handler.renderCurrentBuildStatus(PIXEL_SIZE));
		}
		string.append("</a>");

		return string.toString();
	}

}
