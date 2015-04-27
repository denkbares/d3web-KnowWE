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
public class VisConfigType extends DefaultMarkupType {

	public static final String ANNOTATION_NAME = "name";

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("VisConfig");
		MARKUP.addAnnotation(ANNOTATION_NAME, true);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_COLORS, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_SUCCESSORS, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_PREDECESSORS, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_EXCLUDENODES, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_EXCLUDERELATIONS, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_FILTERRELATIONS, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_SIZE, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_HEIGHT, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_WIDTH, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_FORMAT, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_SHOWCLASSES, false, "true", "false");
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_SHOWPROPERTIES, false, "true", "false");
		MARKUP.addAnnotation(PackageManager.MASTER_ATTRIBUTE_NAME, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_LANGUAGE, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_DOT_APP, false, ConceptVisualizationType.DotApps.values());
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_ADD_TO_DOT, false);
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_OUTGOING_EDGES, false, "true", "false");
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_SHOWINVERSE, false, "true", "false");
		MARKUP.addAnnotation(Rdf2GoCore.GLOBAL, false, "true", "false");
		MARKUP.addAnnotationRenderer(Rdf2GoCore.GLOBAL, NothingRenderer.getInstance());
		//MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_RENDERER, false, GraphDataBuilder.Renderer.values());
		MARKUP.addAnnotation(ConceptVisualizationType.ANNOTATION_VISUALIZATION, false, ConceptVisualizationType.Visualizations.values());
		MARKUP.addAnnotation(SparqlVisualizationType.ANNOTATION_LINK_MODE, false, SparqlVisualizationType.LinkMode.values());
		MARKUP.addAnnotation(SparqlVisualizationType.ANNOTATION_RANK_DIR, false, "LR", "RL", "TB", "BT");
		MARKUP.addAnnotation(SparqlVisualizationType.ANNOTATION_LABELS, false, "true", "false");
		MARKUP.addAnnotation(SparqlVisualizationType.ANNOTATION_DESIGN, false);
	}

	public VisConfigType() {
		super(MARKUP);
		this.setRenderer(new DefaultMarkupRenderer());
	}

}
