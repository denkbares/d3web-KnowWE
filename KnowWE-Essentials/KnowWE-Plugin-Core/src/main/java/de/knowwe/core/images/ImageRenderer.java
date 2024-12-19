/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

package de.knowwe.core.images;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationNameType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

import static de.knowwe.core.images.ImageMarkup.*;
import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Renders images by processing the annotations given in the markup.
 * It handles the extraction of image metadata from annotations, validates the presence of a mandatory source for the
 * image
 * and constructs an HTML representation of the figure element (image and optional a caption) with the specified
 * attributes.
 *
 * @author Antonia Heyder (denkbares GmbH)
 * @created 08.11.2023
 */
public class ImageRenderer implements Renderer {
	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {

		boolean belongsToVariant = DefaultMarkupRenderer.isInCurrentDefaultCompiler(section,user);

		Set<String> validImageAttributes = Set.of("src", "width", "height", "alt", "class", "style", "id", "align");
		Map<String, String> attributes = new HashMap<>();
		List<Section<AnnotationType>> annotations = Sections.successors(section, AnnotationType.class);

		for (Section<AnnotationType> annotation : annotations) {
			Section<AnnotationNameType> annotationName = Sections.successor(annotation, AnnotationNameType.class);
			assert annotationName != null;
			String annotationAttribName = annotationName.getText().replace("@", "").replace(":", "");
			Section<AnnotationContentType> annotationContent = Sections.successor(annotation, AnnotationContentType.class);
			if (annotationContent == null && "src".equals(annotationAttribName)) {
				appendErrorMsg(result, "Missing image source in markup. Please reference attachment with @src annotation.");
				return;
			}
			else {
				String annotationString = null;
				if (annotationContent != null) {
					annotationString = annotationContent.getText().split("\n")[0];
				}
				attributes.put(annotationAttribName, annotationString);
			}
		}
		Section<AttachmentType> attachmentSrc = $(section).successor(AttachmentType.class).getFirst();
		assert attachmentSrc != null;
		try {
			WikiAttachment wikiAttachment = AttachmentType.getAttachment(attachmentSrc);
			if (wikiAttachment == null) {
				appendErrorMsg(result, "Wrong attachment definition in markup. Please reference each attachment with @annotation.");
				return;
			}
			String path = KnowWEUtils.getURLLink(Objects.requireNonNull(wikiAttachment));
			attributes.put("src", path);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		buildFigureHtml(result, validImageAttributes, attributes, belongsToVariant);
	}

	private static void appendErrorMsg(RenderResult result, String message) {
		result.append(new HtmlElement("span").attributes("class", "error").content(message));
	}

	private static void buildFigureHtml(RenderResult result, Set<String> validImageAttributes, Map<String, String> attributes, boolean belongsToVariant) {
		HtmlElement figureHtml = new HtmlElement("figure");
		if (!belongsToVariant) {
			result.appendHtmlTag("div", "class", "image-markup-wrapper");
			result.append("%%accordion-close\n!\n");
		}
		String figureStyle = "";
		String imgStyle = "";
		HtmlElement imageHtml = new HtmlElement("img");
		for (String attrib : attributes.keySet()) {
			String attribContent = attributes.get(attrib);

			if (validImageAttributes.contains(attrib)) {
				//align as in JSPWiki-Markup used but has to be converted to functional CSS
				if (attrib.equals(ANNOTATION_ALIGN)) {
					figureStyle = "display: flex; justify-content: " + attribContent + "; ";
				}
				else if (attrib.equals(ANNOTATION_STYLE)) {
					imgStyle = attribContent + "; ";
				}
				else {
					imageHtml.attributes(attrib, attribContent);
				}
			}
		}
		figureHtml.children(imageHtml);
		if (attributes.containsKey(ANNOTATION_CAPTION)) {
			String caption = attributes.get("caption");
			figureHtml.children(new HtmlElement("figcaption").content(caption));
		}
		figureStyle += setFigureSize(attributes);
		imgStyle += setImgSize(attributes);
		figureHtml.attributes("style", figureStyle);
		imageHtml.attributes("style", imgStyle);
		result.append(figureHtml);
		if (!belongsToVariant) {
			result.append("/%\n");
			result.appendHtmlTag("div");
		}
	}

	private static String setFigureSize(Map<String, String> attributes) {
		int maxWidth = 100;
		int maxHeight = 80;
		String maxAttribs = "";
		if (!attributes.containsKey("width") && !attributes.containsKey("height")) {
			maxAttribs = "max-width: " + maxWidth + "%; max-height: " + maxHeight + "%; ";
		}
		if (attributes.containsKey("style")) {
			String styleAttrib = attributes.get("style");
			maxAttribs += styleAttrib + ";";
		}

		return maxAttribs;
	}

	private static String setImgSize(Map<String, String> attributes) {
		int maxWidth = 80;
		int maxHeight = 100;
		String maxAttribs = "";
		if (!attributes.containsKey("width") && !attributes.containsKey("height")) {
			maxAttribs = "max-width: " + maxWidth + "%; max-height: " + maxHeight + "%; ";
		}
		return maxAttribs;
	}
}
