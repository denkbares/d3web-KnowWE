package de.d3web.we.ci4ke.daemon;

import de.d3web.we.ci4ke.build.CIRenderer;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIDaemonRenderer implements Renderer {

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {
		String content = DefaultMarkupType.getContent(section);
		String dashboardName = DefaultMarkupType.getAnnotation(section,
				CIDashboardType.NAME_KEY);
		renderDaemonContents(section,
				dashboardName, string);
		string.append(content);
	}

	public static void renderDaemonContents(Section<?> section, String dashboardName, RenderResult string) {

		CIDashboard dashboard = CIDashboardManager.getDashboard(section.getArticleManager(),
				dashboardName);

		if (dashboard == null) {
			string.appendHtml("<span class='error'>");
			string.append("The annotation @" + CIDashboardType.NAME_KEY
					+ " has to specify an existing CI dashboard name.");
			string.appendHtml("</span>");

		}
		else {
			string.appendHtml("<a class=\"ci-daemon\" href=\""
					+ KnowWEUtils.getURLLink(dashboard.getDashboardSection())
					+ "\">");
			CIRenderer renderer = dashboard.getRenderer();
			renderer.renderCurrentBuildStatus(string);
			string.appendHtml("</a>");
		}

	}
}
