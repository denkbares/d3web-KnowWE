/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.ontology.sparql;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.PaginationRenderer;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 10.12.14.
 */
public class SparqlContentDecoratingRenderer implements Renderer {

	private SparqlContentRenderer contentRenderer = new SparqlContentRenderer();
	private PaginationRenderer paginationRenderer = new PaginationRenderer(contentRenderer);

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		boolean wantsNavigation = checkForNavigation(section);
		if (wantsNavigation && !checkForTree(section)) {
			paginationRenderer.render(section, user, result);
		}
		else {
			contentRenderer.render(section, user, result);
		}
	}

	private boolean checkForNavigation(Section<?> section) {
		Section<DefaultMarkupType> defaultMarkupSection = getDefaultMarkupSection(section);
		return SparqlContentType.checkAnnotation(defaultMarkupSection, SparqlMarkupType.NAVIGATION, true);
	}

	private boolean checkForTree(Section<?> section) {
		Section<DefaultMarkupType> defaultMarkupSection = getDefaultMarkupSection(section);
		return SparqlContentType.checkAnnotation(defaultMarkupSection, SparqlMarkupType.TREE, false);
	}

	private Section<DefaultMarkupType> getDefaultMarkupSection(Section<?> section) {
		if (section.get() instanceof DefaultMarkupType) {
			return Sections.cast(section, DefaultMarkupType.class);
		}
		else {
			return Sections.ancestor(section, DefaultMarkupType.class);
		}
	}
}
