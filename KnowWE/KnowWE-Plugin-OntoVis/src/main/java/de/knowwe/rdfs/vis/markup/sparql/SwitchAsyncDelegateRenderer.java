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

package de.knowwe.rdfs.vis.markup.sparql;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronRenderer;
import de.knowwe.visualization.Config;

/**
 * A renderer that renders d3 visualizations synchronously and dot visualization asynchronously.
 *
 * @author Jochen Reutelshöfer
 * @created 11.08.2014
 */
public class SwitchAsyncDelegateRenderer implements Renderer {


    private final SparqlVisualizationTypeRenderer visRenderer;
    private final AsynchronRenderer asynchronRenderer;

    public SwitchAsyncDelegateRenderer() {
        visRenderer = new SparqlVisualizationTypeRenderer();
        asynchronRenderer = new AsynchronRenderer(visRenderer);
    }

    @Override
    public void render(Section<?> content, UserContext user, RenderResult result) {
        // switch according to renderer annotation (d3/dot)
        Section<SparqlVisualizationType> section = Sections.ancestor(content,
				SparqlVisualizationType.class);
        Section<DefaultMarkupType> defMarkupSection = Sections.cast(section,
                DefaultMarkupType.class);

        // set renderer
        String rendererType = DefaultMarkupType.getAnnotation(defMarkupSection,
                Config.RENDERER);

        if(rendererType != null && rendererType.equalsIgnoreCase("d3")) {
            visRenderer.render(content, user, result);
        } else {
            asynchronRenderer.render(content, user, result);
        }

    }
}
