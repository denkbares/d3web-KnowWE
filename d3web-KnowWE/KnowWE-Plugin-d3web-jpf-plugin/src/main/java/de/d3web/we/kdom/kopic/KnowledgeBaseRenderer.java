package de.d3web.we.kdom.kopic;

import java.util.List;

import de.d3web.we.action.DownloadKnowledgeBase;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.AnnotationType;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public final class KnowledgeBaseRenderer extends DefaultMarkupRenderer<KnowledgeBaseType> {

	public KnowledgeBaseRenderer() {
		super("KnowWEExtension/d3web/icon/knowledgebase24.png");
	}
	
	@Override
	protected void renderContents(KnowWEArticle article, Section<KnowledgeBaseType> section, KnowWEUserContext user, StringBuilder string) {
		String title = KnowledgeBaseType.getContent(section).trim();
		String id = KnowledgeBaseType.getAnnotation(section, KnowledgeBaseType.ANNOTATION_ID);
		String author = KnowledgeBaseType.getAnnotation(section, KnowledgeBaseType.ANNOTATION_AUTHOR);
		String comment = KnowledgeBaseType.getAnnotation(section, KnowledgeBaseType.ANNOTATION_COMMENT);
		String version = KnowledgeBaseType.getAnnotation(section, KnowledgeBaseType.ANNOTATION_VERSION);

		string.append(KnowWEUtils.maskHTML("<b>"+title+"</b>"));
		if (id != null) {
			string.append(" (").append(id).append(")");
		}
		string.append("\n");
		if (version != null) {
			string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/d3web/icon/date16.png'></img> "));
			string.append(version).append("\n");
		}
		if (author != null) {
			string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/d3web/icon/author16.png'></img> "));
			string.append(author).append("\n");
		}
		if (comment != null) {
			string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/d3web/icon/comment16.png'></img> "));
			string.append(comment).append("\n");
		}
		
		string.append(KnowWEUtils.maskHTML("<hr>\n"));
		List<Section<? extends AnnotationType>> compileSections = DefaultMarkupType.getAnnotationSections(section, KnowledgeBaseType.ANNOTATION_COMPILE);
		for (Section<? extends AnnotationType> compileSection : compileSections) {
			compileSection.getObjectType().getRenderer().render(article, compileSection, user, string);
		}
	}

//	@Override
//	public Tool[] getTools(KnowWEArticle article, Section<KnowledgeBaseType> section, KnowWEUserContext user) {
//		String kbName = DefaultMarkupType.getContent(section).trim();
//		String url = "javascript:window.location='action/DownloadKnowledgeBase" +
//				"?" + KnowWEAttributes.TOPIC + "=" + article.getTitle() +
//				"&" + KnowWEAttributes.WEB + "=" + article.getWeb() +
//				"&" + DownloadKnowledgeBase.PARAM_FILENAME + "=" + kbName + ".d3web'";
//		Tool download = new Tool(
//				"KnowWEExtension/images/save_edit.gif",
//				"Download",
//				"Download the whole knowledge base into a single file for deployment.",
//				url
//				);
//
//		String runURL = "action/InitWiki" +
//				"?user=" + user.getUsername() +
//				"&" + KnowWEAttributes.WEB + "=" + article.getWeb() +
//				"&" + KnowWEAttributes.TOPIC + "=" + article.getTitle() +
//				"&lang=de";
//		Tool run = new Tool(
//				"KnowWEExtension/images/run.gif",
//				"Run Interview",
//				"Starts a new interview with that knowledge base.",
//				"javascript:window.open('"+runURL+"');undefined;");
//		return new Tool[] {
//				download, run };
//	}
}