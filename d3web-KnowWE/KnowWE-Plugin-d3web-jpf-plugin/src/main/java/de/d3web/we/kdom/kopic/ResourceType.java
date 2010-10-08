package de.d3web.we.kdom.kopic;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Defines a resource that should be added to the knowledge base as a binary
 * resource (e.g. multimedia file).
 * <p>
 * The markup allows to specify a pathname. The file content will be stored
 * inside the knowledge bases resources under that specified pathname. Using
 * this pathname it can be accessed from the knowledge base using
 * {@link KnowledgeBase#getResource(String)}. If no pathname is used, the
 * attachment name is used for the filename and the article name as the parent
 * folder.
 * <p>
 * This markup allows to specify the content of the knowledge base resource
 * either as the content block of the markup (useful for texual content) as well
 * as to specify an attachment to take the content from. To user this latter
 * functionality use the "src" annotation to specify the attachments as <br>
 * <code>@src = &lt;article&gt;/&lt;filename&gt;</code>. <br>
 * or <br>
 * <code>@src = &lt;filename&gt;</code> <br>
 * for local attachments of this article.
 * 
 * @author volker_belli
 * @created 07.10.2010
 */
public class ResourceType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	public static final String MARKUP_NAME = "Resource";
	public static final String ANNOTATION_PATH = "path";
	public static final String ANNOTATION_SRC = "src";

	static {
		MARKUP = new DefaultMarkup(MARKUP_NAME);
		MARKUP.addAnnotation(ANNOTATION_PATH, false);
		MARKUP.addAnnotation(ANNOTATION_SRC, false);
	}

	public ResourceType() {
		super(MARKUP);
		this.setCustomRenderer(new DefaultMarkupRenderer<ResourceType>(
				"KnowWEExtension/d3web/icon/resource24.png"));
		this.addSubtreeHandler(new ResourceHandler());
	}

}
