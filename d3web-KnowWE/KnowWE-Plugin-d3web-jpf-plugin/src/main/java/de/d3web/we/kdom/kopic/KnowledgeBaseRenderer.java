package de.d3web.we.kdom.kopic;

import java.util.List;

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

}