package de.d3web.we.kdom.kopic;

import de.d3web.we.action.DownloadKnowledgeBase;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.tools.DefaultTool;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolProvider;
import de.d3web.we.wikiConnector.KnowWEUserContext;


public class KnowledgeBaseDownloadProvider implements ToolProvider {

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext) {
		String kbName = DefaultMarkupType.getContent(section).trim();
		String jsAction = "window.location='action/DownloadKnowledgeBase" +
				"?" + KnowWEAttributes.TOPIC + "=" + article.getTitle() +
				"&" + KnowWEAttributes.WEB + "=" + article.getWeb() +
				"&" + DownloadKnowledgeBase.PARAM_FILENAME + "=" + kbName + ".d3web'";
		Tool download = new DefaultTool(
				"KnowWEExtension/images/save_edit.gif",
				"Download",
				"Download the whole knowledge base into a single file for deployment.",
				jsAction
				);
		return new Tool[] {download};
	}

}
