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
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.preview.AbstractPreviewRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.jspwiki.JSPWikiMarkupUtils;
import de.knowwe.jspwiki.types.HeaderType;

/**
 * Renders the preview of a header, including the subsequent sections that
 * naturally belongs to that header.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 21.02.2014
 */
public class HeaderPreviewRenderer extends AbstractPreviewRenderer {

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		Section<HeaderType> header = Sections.cast(section, HeaderType.class);
		List<Section<? extends Type>> contents = JSPWikiMarkupUtils.getContent(header, true);
		render(contents, user, result);
	}

	public static void render(List<Section<? extends Type>> contentSections, UserContext user, RenderResult result) {
		result.appendHtml("<div style='max-height:250px; overflow:scroll;'>\n");
		for (Section<?> content : contentSections) {
			result.append(content, user);
		}
		result.appendHtml("</div");
	}
}
