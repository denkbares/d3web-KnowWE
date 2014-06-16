/*
 * Copyright (C) ${year} denkbares GmbH, Germany
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.d3web.resource;

import de.d3web.core.knowledge.KnowledgeBase;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

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
 * either as the content block of the markup (useful for textual content) as
 * well as to specify an attachment to take the content from. To user this
 * latter functionality use the "src" annotation to specify the attachments as <br>
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
	public static final String ANNOTATION_XMLSCHEMA = "xmlschema";

	static {
		MARKUP = new DefaultMarkup(MARKUP_NAME);
		MARKUP.addAnnotation(ANNOTATION_PATH, false);
		MARKUP.addAnnotation(ANNOTATION_SRC, false);
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(ANNOTATION_XMLSCHEMA);
		AttachmentType schemaAttachment = new AttachmentType();
		schemaAttachment.addCompileScript(new XMLValidationHandler());
		MARKUP.addAnnotationContentType(ANNOTATION_XMLSCHEMA, schemaAttachment);
	}

	public ResourceType() {
		super(MARKUP);
		this.setRenderer(new DefaultMarkupRenderer(
				"KnowWEExtension/d3web/icon/resource24.png"));
		this.addCompileScript(new ResourceHandler());
	}

}
