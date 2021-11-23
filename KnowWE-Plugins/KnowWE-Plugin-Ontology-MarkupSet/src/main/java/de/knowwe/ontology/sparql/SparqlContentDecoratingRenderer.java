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

import java.util.function.Consumer;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.AsyncPreviewRenderer;
import de.knowwe.kdom.renderer.PaginationRenderer;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;

import static de.knowwe.kdom.renderer.PaginationRenderer.SortingMode.multi;
import static de.knowwe.kdom.renderer.PaginationRenderer.SortingMode.off;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 10.12.14.
 */
public class SparqlContentDecoratingRenderer implements AsyncPreviewRenderer {

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		renderDecorated(section, user, renderer -> renderer.render(section, user, result));
	}

	@Override
	public void renderAsyncPreview(Section<?> section, UserContext user, RenderResult result) {
		renderDecorated(section, user, renderer -> renderer.renderAsyncPreview(section, user, result));
	}

	private void renderDecorated(Section<?> section, UserContext user, Consumer<AsyncPreviewRenderer> renderFunction) {
		Section<SparqlType> sparqlTypeSection = Sections.cast(section, SparqlType.class);
		RenderOptions opts = sparqlTypeSection.get().getRenderOptions(sparqlTypeSection, user);
		PaginationRenderer paginationRenderer = new PaginationRenderer(SparqlContentRenderer.getInstance(),
				opts.isSorting() ? multi : off, opts.isFiltering());

		AsyncPreviewRenderer renderer;
		if (opts.isNavigation() && !opts.isTree()) {
			renderer = paginationRenderer;
		}
		else {
			renderer = SparqlContentRenderer.getInstance();
		}
		renderFunction.accept(renderer);
	}
}
