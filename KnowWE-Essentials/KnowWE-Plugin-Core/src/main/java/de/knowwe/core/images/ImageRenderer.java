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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationNameType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;

import static de.knowwe.core.images.ImageMarkup.ANNOTATION_CAPTION;

public class ImageRenderer implements Renderer {
	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		Set<String> validImageAttributes = Set.of("src", "width", "height", "alt", "class", "style", "id");
		Map<String, String> attributes = new HashMap<>();
		List<Section<AnnotationType>> annotations = Sections.successors(section, AnnotationType.class);

		for (Section<AnnotationType> annotation : annotations) {
			Section<AnnotationNameType> annotationName = Sections.successor(annotation, AnnotationNameType.class);
			Section<AnnotationContentType> annotationContent = Sections.successor(annotation, AnnotationContentType.class);
			assert annotationContent != null;
			String annotationString = annotationContent.getText().split("\n")[0];
			assert annotationName != null;
			String attrib = annotationName.getText().replace("@", "");
			attrib = attrib.replace(":", "");
			attributes.put(attrib, annotationString);
		}
		setMaxSize(attributes);

		result.appendHtml("<figure>");
		result.appendHtml("<img ");
		for (String attrib : attributes.keySet()) {
			String attribContent = attributes.get(attrib);

			if (validImageAttributes.contains(attrib)) {
				result.appendHtml(attrib + "='" + attribContent + "' ");
			}
		}
		result.appendHtml("/>");
		if (attributes.containsKey(ANNOTATION_CAPTION)) {
			String caption = attributes.get("caption");
			result.appendHtml("<figcaption>" + caption + "</figcaption>");
		}
		result.appendHtml("</figure>");
	}

	private static void setMaxSize(Map<String, String> attributes) {
		int maxWidth = 600;
		int maxHeight = 600;
		if (!attributes.containsKey("style")) {
			attributes.put("style", "max-width:" + maxWidth + "px;max-height:" + maxHeight + "px;");
		}
		else {
			String styleAttrib = attributes.get("style");
			styleAttrib += ";max-width:" + maxWidth + "px;max-height:" + maxHeight + "px;";
			attributes.put("style", styleAttrib);
		}
	}
}
