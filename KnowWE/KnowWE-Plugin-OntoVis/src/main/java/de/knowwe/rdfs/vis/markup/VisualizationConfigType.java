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

package de.knowwe.rdfs.vis.markup;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdfs.vis.markup.sparql.SparqlVisualizationType;

/**
 * @author: Johanna Latt
 * @created 13.07.2014.
 */
public class VisualizationConfigType extends DefaultMarkupType {

	public static final String ANNOTATION_NAME = "name";



	protected String getMarkupName() {
		return "VisualizationConfig";
	}

	public VisualizationConfigType() {
		applyMarkup(createMarkup());
		this.setRenderer(new DefaultMarkupRenderer());
	}

	public  DefaultMarkup createMarkup() {
		DefaultMarkup markup = new DefaultMarkup(getMarkupName());
		markup.addAnnotation(ANNOTATION_NAME, true);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_COLORS, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_SUCCESSORS, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_PREDECESSORS, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_EXCLUDENODES, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_EXCLUDERELATIONS, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_FILTERRELATIONS, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_SIZE, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_HEIGHT, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_WIDTH, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_FORMAT, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_SHOWCLASSES, false, "true", "false");
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_SHOWPROPERTIES, false, "true", "false");
		markup.addAnnotation(PackageManager.MASTER_ATTRIBUTE_NAME, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_LANGUAGE, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_DOT_APP, false, ConceptVisualizationType.DotApps.values());
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_ADD_TO_DOT, false);
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_OUTGOING_EDGES, false, "true", "false");
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_SHOWINVERSE, false, "true", "false");
		markup.addAnnotation(Rdf2GoCore.GLOBAL, false, "true", "false");
		markup.addAnnotationRenderer(Rdf2GoCore.GLOBAL, NothingRenderer.getInstance());
		//MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_RENDERER, false, GraphDataBuilder.Renderer.values());
		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_VISUALIZATION, false, ConceptVisualizationType.Visualizations.values());
		markup.addAnnotation(SparqlVisualizationType.ANNOTATION_LINK_MODE, false, SparqlVisualizationType.LinkMode.values());
		markup.addAnnotation(SparqlVisualizationType.ANNOTATION_RANK_DIR, false, "LR", "RL", "TB", "BT");
		markup.addAnnotation(SparqlVisualizationType.ANNOTATION_LABELS, false, "true", "false");
		markup.addAnnotation(SparqlVisualizationType.ANNOTATION_DESIGN, false);
		return markup;
	}
}
