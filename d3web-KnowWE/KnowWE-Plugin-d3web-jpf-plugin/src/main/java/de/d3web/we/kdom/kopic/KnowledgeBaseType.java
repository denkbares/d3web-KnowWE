package de.d3web.we.kdom.kopic;

import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.packaging.SinglePackageReference;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;


public class KnowledgeBaseType extends DefaultMarkupType {

	public static final String ANNOTATION_ID = "id";
	public static final String ANNOTATION_VERSION = "version";
	public static final String ANNOTATION_AUTHOR = "author";
	public static final String ANNOTATION_COMMENT = "comment";
	public static final String ANNOTATION_COMPILE = "compile";
	
	private static final DefaultMarkup MARKUP;
	
	static {
		SinglePackageReference compileType = new SinglePackageReference();
//		compileType.setCustomRenderer(null);
		
		MARKUP = new DefaultMarkup("KnowledgeBase");
		MARKUP.addAnnotation(ANNOTATION_COMPILE, true);
		MARKUP.addAnnotationType(ANNOTATION_COMPILE, compileType);
		MARKUP.addAnnotation(ANNOTATION_AUTHOR, false);
		MARKUP.addAnnotation(ANNOTATION_COMMENT, false);
		MARKUP.addAnnotation(ANNOTATION_ID, false);
		MARKUP.addAnnotation(ANNOTATION_VERSION, false);
	}
	
	public KnowledgeBaseType() {
		super(MARKUP);
		this.setCustomRenderer(new KnowledgeBaseRenderer());
		this.addSubtreeHandler(new D3webSubtreeHandler<KnowledgeBaseType>() {

			@Override
			public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<KnowledgeBaseType> section) {
				// get required information
				KnowledgeBaseManagement kbm = getKBM(article);
				KnowledgeBase kb = kbm.getKnowledgeBase();
				
				// prepare the items to be set into the knowledge base
				String title = getContent(section).trim();
				String id = getAnnotation(section, ANNOTATION_ID);
				String author = getAnnotation(section, ANNOTATION_AUTHOR);
				String comment = getAnnotation(section, ANNOTATION_COMMENT);
				String version = getAnnotation(section, ANNOTATION_VERSION);
				
				// and write it to the knowledge base
				if (id != null) kb.setId(id);

				DCMarkup dcm = kb.getDCMarkup();
				dcm.setContent(DCElement.TITLE, title);
				if (author != null) dcm.setContent(DCElement.CREATOR, author);
				if (comment != null) dcm.setContent(DCElement.DESCRIPTION, comment);
				if (version != null) dcm.setContent(DCElement.DATE, version);
				
				return null;
			}
		});
	}
	
}
