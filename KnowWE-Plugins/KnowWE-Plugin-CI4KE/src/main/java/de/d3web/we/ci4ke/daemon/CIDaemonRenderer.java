package de.d3web.we.ci4ke.daemon;

import de.d3web.we.ci4ke.build.CIRenderer;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.util.Icon;

public class CIDaemonRenderer implements Renderer {

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {
		String content = DefaultMarkupType.getContent(section);
		String dashboardName = DefaultMarkupType.getAnnotation(section, CIDashboardType.NAME_KEY);
		renderDaemonContents(user, section, dashboardName, string);
		string.append(content);
	}

	public static void renderDaemonContents(UserContext user, Section<?> section, String dashboardName, RenderResult string) {

		ArticleManager articleManager = section.getArticleManager();
		CIDashboard dashboard = CIDashboardManager.getDashboard(articleManager, dashboardName);

		if (dashboard == null) {
			string.appendHtml("<span class='ci-dashboard-not-found tooltipster' title='CI dashboard " + dashboardName + " not found. Please specify an existing dashboard via annotation @" + CIDashboardType.NAME_KEY + ".'>");
			string.append(Icon.ERROR.toHtmlElement());
			string.appendHtml("</span>");
		}
		else {
			dashboard.updateDefaultDashboardOfUser(user);
			string.appendHtml("<a class=\"ci-daemon\" href=\""
					+ KnowWEUtils.getURLLink(dashboard.getDashboardSection())
					+ "\">");
			CIRenderer renderer = dashboard.getRenderer();
			renderer.renderCurrentBuildStatus(string);
			string.appendHtml("</a> ");
		}
	}
}
