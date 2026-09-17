/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.knowwe.d3web.property;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationNameType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Philipp Sehne (denkbares GmbH)
 * @created 11.08.26
 */
public class DescriptionRenderer extends DefaultMarkupRenderer {

	@Override
	public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult result) {
		String anchorName = KnowWEUtils.getAnchor(section);
		result.appendHtml("<a name='" + anchorName + "'></a>");
		Section<DescriptionMarkup.DescriptionTextType> first = $(section).successor(DescriptionMarkup.DescriptionTextType.class)
				.getFirst();
		if (first != null) {
			DelegateRenderer.getInstance().renderSubSection(first, user, result);
		}
		Sections<AnnotationType> annotations = $(section).successor(AnnotationType.class);

		for (Section<AnnotationType> annotation : annotations) {
			Section<AnnotationNameType> name = $(annotation).successor(AnnotationNameType.class).getFirst();
			Section<AnnotationContentType> content = $(annotation).successor(AnnotationContentType.class).getFirst();
			result.appendHtml("<div>");
			if (name != null) {
				HtmlElement nameDiv = new HtmlElement("span").content(name.getText()).clazz("style-comment");
				result.append(nameDiv);
				result.append(" ");
			}
			if (content != null) {
				DelegateRenderer.getInstance().render(content, user, result);
			}
			result.appendHtml("</div>");
		}
	}
}
