package de.d3web.we.kdom.kopic.renderer;

import java.net.URLEncoder;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;


public class KopicRenderer extends KnowWEDomRenderer {
	
	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		String title = "Knowledge "+generateLinkIcons(user.getUsername(), topic, KnowWEEnvironment.generateDefaultID(topic), web, false, sec.getId());
		return wrapCollapsebox(title,SpecialDelegateRenderer.getInstance().render(sec, user, web, topic));
	}

	private String wrapCollapsebox(String title, String render) {
		StringBuilder result = new StringBuilder();
		result.append("%%collapsebox-closed \n");
		result.append("! " +title + " \n");
		result.append(render);
		result.append("/%\n");
		return result.toString();
	}
	
	private String generateLinkIcons(String user, String topic, String id,
			String web, boolean error, String nodeID) {
		StringBuilder result = new StringBuilder(generateReportLink(topic, web, error));
		if (!error) {
			result.append(generateDialogLink(user, topic, id));
		}
		result.append(generateDownloadLink(topic, id, web, nodeID));
		if (!error) {
			result.append(generateJarLink(topic, id, web));
		}
		return result.toString();
	}
	
	private String generateJarLink(String topic2, String id, String web2) {
		String icon = "<img src=KnowWEExtension/images/drive_disk.png title='Download jar file' /></img>";

		return KnowWEEnvironment
				.maskHTML("<a href='KnowWEExtension/KBrepository/" + web2 + "/"
						+ topic2 + ".." + id + ".jar' >" + icon + "</a>");
	}

	private String generateDownloadLink(String topic2, String id, String web2,
			String nodeID) {
		String prefix = "";
		String result = "";
		String icon = "<img src=KnowWEExtension/images/disk.png title='Txt download' /></img>";
		result += "<a href='" + prefix + "KnowWEDownload.jsp?KWiki_Topic="
				+ topic2 + "&nodeID=" + nodeID + "&filename=" + topic2
				+ "_kopic.txt' >" + icon + "</a>";

		return KnowWEEnvironment.maskHTML(result);
	}

	public String generateDialogLink(String user, String topic, String actualID) {

		String prefix = KnowWEEnvironment.getInstance().getPathPrefix();
		return KnowWEEnvironment.HTML_ST
				+ "a target=kwiki-dialog href="
				+ prefix
				+ (prefix.length() != 0 ? "/" : "")
				+ "KnowWE.jsp?renderer=KWiki_dialog&KWikisessionid="
				+ URLEncoder.encode(topic)
				+ ".."
				+ URLEncoder.encode(actualID)
				+ "&KWikiWeb=default_web&KWikiUser="
				+ user
				+ ""
				+ KnowWEEnvironment.HTML_GT
				+ KnowWEEnvironment.HTML_ST
				+ "img src=KnowWEExtension/images/run.gif title=Fall im d3web-Dialog starten /"
				+ KnowWEEnvironment.HTML_GT + KnowWEEnvironment.HTML_ST + "/a"
				+ KnowWEEnvironment.HTML_GT;
	}

	private String generateReportLink(String topicname, String web,
			boolean error) {
		String prefix = KnowWEEnvironment.getInstance().getPathPrefix();
		String pic = "statistics.gif";
		if (error) {
			pic = "statisticsError.gif";
		}
		return KnowWEEnvironment.HTML_ST + "a href=" + prefix
				+ (prefix.length() != 0 ? "/" : "")
				+ "KnowWE.jsp?action=getParseReport&topic="
				+ URLEncoder.encode(topicname) + "&KWiki_Topic="
				+ URLEncoder.encode(topicname) + "&web=" + web
				+ " target=_blank" + KnowWEEnvironment.HTML_GT
				+ KnowWEEnvironment.HTML_ST
				+ "img src='KnowWEExtension/images/" + pic
				+ "' title='Report'/" + KnowWEEnvironment.HTML_GT
				+ KnowWEEnvironment.HTML_ST + "/a" + KnowWEEnvironment.HTML_GT;
	}

}
