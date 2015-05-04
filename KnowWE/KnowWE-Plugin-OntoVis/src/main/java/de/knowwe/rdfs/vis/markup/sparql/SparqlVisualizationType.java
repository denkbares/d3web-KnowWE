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

import java.util.Map;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.ontology.sparql.Rdf2GoCoreCheckRenderer;
import de.knowwe.rdfs.vis.markup.ConceptVisualizationType;
import de.knowwe.rdfs.vis.markup.PreRenderer;
import de.knowwe.rdfs.vis.markup.VisualizationType;

/**
 * 
 * @author Jochen Reutelshöfer
 * @created 23.07.2013
 */

public class SparqlVisualizationType extends DefaultMarkupType implements VisualizationType {

	public static final String ANNOTATION_CONCEPT = "concept";
	public static final String ANNOTATION_COMMENT = "comment";
	public static final String ANNOTATION_SIZE = "size";
	public static final String ANNOTATION_WIDTH = "width";
	public static final String ANNOTATION_HEIGHT = "height";
	public static final String ANNOTATION_FORMAT = "format";
	public static final String ANNOTATION_LANGUAGE = "language";
	public static final String ANNOTATION_LINK_MODE = "linkMode";

	public static final String ANNOTATION_RANK_DIR = "rankDir";

	public static final String ANNOTATION_DOT_APP = "dotApp";
	public static final String ANNOTATION_ADD_TO_DOT = "dotAddLine";

	public static final String ANNOTATION_RENDERER = "renderer";
	public static final String ANNOTATION_VISUALIZATION = "visualization";
	public static final String ANNOTATION_DESIGN = "design";
	public static final String ANNOTATION_LABELS = "labels";

	public static final String ANNOTATION_CONFIG = "config";

	public static final String ANNOTATION_PRERENDER = "prerender";


    public static void readParameterFromAnnotation(String annotationName, Section<?> section, String parameterName, Map<String, String> parameters, String defaultValue) {
        String value = SparqlVisualizationType.getAnnotation(section,
				annotationName);
        if(value != null) {
            // Set value from annotation as parameter
            parameters.put(parameterName, value);
        } else {
            // No value found in annotation, hence set default value if given
            if(defaultValue != null) {
                parameters.put(parameterName, defaultValue);
            }
        }
    }

    public static void readParameterFromAnnotation(String annotationName, Section<?> section, String parameterName, Map<String,String> parameters) {
        readParameterFromAnnotation(annotationName, section, parameterName, parameters, null);
    }

	private enum DotApps {
		dot, neato
	}

	public enum LinkMode {
		jump, browse
	}

	public enum Visualizations {
		wheel, force, tree
	}



	public SparqlVisualizationType() {
		applyMarkup(createMarkup());
		this.setRenderer(new Rdf2GoCoreCheckRenderer());
	}

	public DefaultMarkup createMarkup() {
		DefaultMarkup markup = new DefaultMarkup(getMarkupName());
		SparqlVisContentType sparqlContentType = new SparqlVisContentType();
		markup.addContentType(sparqlContentType);

		markup.addAnnotation(ANNOTATION_CONCEPT, false);
		markup.addAnnotationRenderer(ANNOTATION_CONCEPT, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_COMMENT, false);
		markup.addAnnotationRenderer(ANNOTATION_COMMENT, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_SIZE, false);
		markup.addAnnotationRenderer(ANNOTATION_SIZE, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_WIDTH, false);
		markup.addAnnotationRenderer(ANNOTATION_WIDTH, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_HEIGHT, false);
		markup.addAnnotationRenderer(ANNOTATION_HEIGHT, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_FORMAT, false);
		markup.addAnnotationRenderer(ANNOTATION_FORMAT, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_LANGUAGE, false);
		markup.addAnnotationRenderer(ANNOTATION_LANGUAGE, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_DOT_APP, false, DotApps.values());
		markup.addAnnotationRenderer(ANNOTATION_DOT_APP, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_ADD_TO_DOT, false);
		markup.addAnnotationRenderer(ANNOTATION_ADD_TO_DOT, NothingRenderer.getInstance());

		//markup.addAnnotation(ANNOTATION_RENDERER, false, GraphDataBuilder.Renderer.values());
		//markup.addAnnotationRenderer(ANNOTATION_RENDERER, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_VISUALIZATION, false, Visualizations.values());
		markup.addAnnotationRenderer(ANNOTATION_VISUALIZATION, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_LINK_MODE, false, LinkMode.values());
		markup.addAnnotationRenderer(ANNOTATION_LINK_MODE, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_DESIGN, false);
		markup.addAnnotationRenderer(ANNOTATION_DESIGN, NothingRenderer.getInstance());

		markup.addAnnotation(Rdf2GoCore.GLOBAL, false, "true", "false");
		markup.addAnnotationRenderer(Rdf2GoCore.GLOBAL, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_RANK_DIR, false, "LR", "RL", "TB", "BT");
		markup.addAnnotationRenderer(ANNOTATION_RANK_DIR, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_LABELS, false, "true", "false");
		markup.addAnnotationRenderer(ANNOTATION_LABELS, NothingRenderer.getInstance());

		//markup.addAnnotation(OntoVisType.ANNOTATION_SHOWINVERSE, false, "true", "false");
		//markup.addAnnotationRenderer(OntoVisType.ANNOTATION_SHOWINVERSE,NothingRenderer.getInstance());

		markup.addAnnotation(ConceptVisualizationType.ANNOTATION_COLORS, false);
		markup.addAnnotationRenderer(ConceptVisualizationType.ANNOTATION_COLORS, NothingRenderer.getInstance());


		markup.addAnnotation(ANNOTATION_CONFIG, false);
		markup.addAnnotationRenderer(ANNOTATION_CONFIG, NothingRenderer.getInstance());

		markup.addAnnotation(ANNOTATION_PRERENDER, false);
		markup.addAnnotationRenderer(ANNOTATION_PRERENDER, NothingRenderer.getInstance());
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
