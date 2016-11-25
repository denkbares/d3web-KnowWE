/*
 * Copyright (C) 2013 denkbares GmbH
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

import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.ontology.sparql.Rdf2GoCoreCheckRenderer;
import de.knowwe.rdfs.vis.markup.PreRenderer;
import de.knowwe.rdfs.vis.markup.VisualizationType;
import de.knowwe.visualization.Config;

/**
 * 
 * @author Jochen Reutelshöfer
 * @created 23.07.2013
 */

public class SparqlVisualizationType extends DefaultMarkupType implements VisualizationType {

	public static final String VIS_TEMPLATE_CLASS = "isVisTemplateForClass";
	public static final String VIS_TEMPLATE_EXAMPLE = "templateDefaultConcept";

	public SparqlVisualizationType() {
		applyMarkup(createMarkup());
		this.setRenderer(new Rdf2GoCoreCheckRenderer());
	}

	public DefaultMarkup createMarkup() {
		DefaultMarkup markup = new DefaultMarkup(getMarkupName());
		SparqlVisContentType sparqlContentType = new SparqlVisContentType();
		markup.addContentType(sparqlContentType);

		markup.addAnnotation(Config.SIZE, false);
		markup.addAnnotationRenderer(Config.SIZE, NothingRenderer.getInstance());

		markup.addAnnotation(Config.WIDTH, false);
		markup.addAnnotationRenderer(Config.WIDTH, NothingRenderer.getInstance());

		markup.addAnnotation(Config.HEIGHT, false);
		markup.addAnnotationRenderer(Config.HEIGHT, NothingRenderer.getInstance());

		markup.addAnnotation(Config.FORMAT, false);
		markup.addAnnotationRenderer(Config.FORMAT, NothingRenderer.getInstance());

		markup.addAnnotation(Config.LANGUAGE, false);
		markup.addAnnotationRenderer(Config.LANGUAGE, NothingRenderer.getInstance());

		markup.addAnnotation(Config.ADD_TO_DOT, false);
		markup.addAnnotationRenderer(Config.ADD_TO_DOT, NothingRenderer.getInstance());


		markup.addAnnotation(Config.VISUALIZATION, false, Config.Visualization.class);
		markup.addAnnotationRenderer(Config.VISUALIZATION, NothingRenderer.getInstance());

		markup.addAnnotation(Config.DESIGN, false);
		markup.addAnnotationRenderer(Config.DESIGN, NothingRenderer.getInstance());

		markup.addAnnotation(Config.RANK_DIR, false, Config.RankDir.class);
		markup.addAnnotationRenderer(Config.RANK_DIR, NothingRenderer.getInstance());

		markup.addAnnotation(Config.SHOW_LABELS, false);
		markup.addAnnotationRenderer(Config.SHOW_LABELS, NothingRenderer.getInstance());

		markup.addAnnotation(Config.SHOW_INVERSE, false, "true", "false");
		markup.addAnnotationRenderer(Config.SHOW_INVERSE, NothingRenderer.getInstance());

		markup.addAnnotation(Config.SHOW_REDUNDANT, false, "true", "false");
		markup.addAnnotationRenderer(Config.SHOW_REDUNDANT, NothingRenderer.getInstance());

		markup.addAnnotation(Config.COLORS, false);
		markup.addAnnotationRenderer(Config.COLORS, NothingRenderer.getInstance());

		markup.addAnnotation(Config.CONFIG, false);
		markup.addAnnotationRenderer(Config.CONFIG, NothingRenderer.getInstance());

		markup.addAnnotation(Config.LINK_MODE, false, Config.LinkMode.class);
		markup.addAnnotationRenderer(Config.LINK_MODE, NothingRenderer.getInstance());
		markup.setAnnotationDeprecated("linkMode");

		markup.addAnnotation(Config.TITLE, false);
		markup.addAnnotationRenderer(Config.TITLE, NothingRenderer.getInstance());

		markup.addAnnotation(Config.RANK_SAME, false);
		markup.addAnnotationRenderer(Config.RANK_SAME, NothingRenderer.getInstance());

		markup.addAnnotation(Config.OVERLAP, false, Config.Overlap.class);
		markup.addAnnotationRenderer(Config.OVERLAP, NothingRenderer.getInstance());

		markup.addAnnotation(Config.LAYOUT, false, Config.Layout.class);
		markup.addAnnotationRenderer(Config.LAYOUT, NothingRenderer.getInstance());

		markup.addAnnotation(VIS_TEMPLATE_CLASS, false);
		markup.addAnnotationRenderer(VIS_TEMPLATE_CLASS, NothingRenderer.getInstance());

		markup.addAnnotation(VIS_TEMPLATE_EXAMPLE, false);
		markup.addAnnotationRenderer(VIS_TEMPLATE_EXAMPLE, NothingRenderer.getInstance());

		markup.addAnnotation(Config.TIMEOUT, false);
		markup.addAnnotationRenderer(Config.TIMEOUT, NothingRenderer.getInstance());

		markup.addAnnotation(Config.SHOW_LITERALS, false, Config.LiteralMode.class);
		markup.addAnnotationRenderer(Config.SHOW_LITERALS, NothingRenderer.getInstance());

		markup.addAnnotation(AsynchronousRenderer.ASYNCHRONOUS, false);
		return markup;
	}

	protected String getMarkupName() {
		return "SparqlVisualization";
	}

	@Override
	public PreRenderer getPreRenderer() {
		return new SparqlVisualizationTypeRenderer();
	}

}
