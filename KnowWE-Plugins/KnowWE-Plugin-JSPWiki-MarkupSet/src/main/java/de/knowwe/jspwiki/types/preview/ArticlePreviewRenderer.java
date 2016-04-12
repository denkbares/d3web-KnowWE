/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.jspwiki.types.preview;

import java.util.Collection;

import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.preview.PreviewRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Renders the preview of a complete article.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 21.02.2014
 */
public class ArticlePreviewRenderer implements PreviewRenderer {

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		result.appendHtmlElement("a", "Go to article '" + section.getTitle() + "'", "href", KnowWEUtils.getURLLink(section
				.getArticle()));
//		result.appendHtml(KnowWEUtils.getLinkHTMLToArticle(section.getTitle()));
// 		HeaderPreviewRenderer.render(section.getChildren(), user, result);
	}

	@Override
	public boolean matches(Section<?> section) {
		// only want to use that preview for whole articles
		return section.get() instanceof RootType;
	}
}
