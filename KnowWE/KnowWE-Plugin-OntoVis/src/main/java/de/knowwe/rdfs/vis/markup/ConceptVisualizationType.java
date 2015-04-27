package de.knowwe.rdfs.vis.markup;

import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronRenderer;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdfs.vis.markup.sparql.SparqlVisualizationType;

public class ConceptVisualizationType extends DefaultMarkupType implements VisualizationType {

	public static final String ANNOTATION_CONCEPT = "concept";
	public static final String ANNOTATION_COLORS = "colors";
	public static final String ANNOTATION_SUCCESSORS = "successors";
	public static final String ANNOTATION_PREDECESSORS = "predecessors";
	public static final String ANNOTATION_EXCLUDENODES = "excludeNodes";
	public static final String ANNOTATION_EXCLUDERELATIONS = "excludeRelations";
	public static final String ANNOTATION_FILTERRELATIONS = "filterRelations";
	public static final String ANNOTATION_SIZE = "size";
	public static final String ANNOTATION_WIDTH = "width";
	public static final String ANNOTATION_HEIGHT = "height";
	public static final String ANNOTATION_FORMAT = "format";
	public static final String ANNOTATION_SHOWCLASSES = "showClasses";
	public static final String ANNOTATION_SHOWPROPERTIES = "showProperties";
	public static final String ANNOTATION_LANGUAGE = "language";
	public static final String ANNOTATION_OUTGOING_EDGES = "outgoingEdges";
	public static final String ANNOTATION_SHOWINVERSE = "showInverse";

	public static final String ANNOTATION_DOT_APP = "dotApp";
	public static final String ANNOTATION_ADD_TO_DOT = "dotAddLine";

	public static final String ANNOTATION_RENDERER = "renderer";
	public static final String ANNOTATION_VISUALIZATION = "visualization";

	public static final String ANNOTATION_CONFIG = "config";

	public static final String ANNOTATION_PRERENDER = "prerender";


	public enum DotApps {
		dot, neato
	}

	public enum Visualizations {
		wheel, force
	}

	public ConceptVisualizationType() {
		applyMarkup(createMarkup());
        this.setRenderer(new AsynchronRenderer(getPreRenderer()));
    }

	protected DefaultMarkup createMarkup() {
		DefaultMarkup MARKUP = new DefaultMarkup(getMarkupName());
		MARKUP.addAnnotation(ANNOTATION_CONCEPT, true);

		MARKUP.addAnnotation(ANNOTATION_COLORS, false);
		MARKUP.addAnnotationRenderer(ANNOTATION_COLORS, NothingRenderer.getInstance());

		MARKUP.addAnnotation(ANNOTATION_SUCCESSORS, false);
		MARKUP.addAnnotation(ANNOTATION_PREDECESSORS, false);
		MARKUP.addAnnotation(ANNOTATION_EXCLUDENODES, false);
		MARKUP.addAnnotation(ANNOTATION_EXCLUDERELATIONS, false);
		MARKUP.addAnnotation(ANNOTATION_FILTERRELATIONS, false);

		MARKUP.addAnnotation(ANNOTATION_SIZE, false);
		MARKUP.addAnnotationRenderer(ANNOTATION_SIZE, NothingRenderer.getInstance());

		MARKUP.addAnnotation(ANNOTATION_WIDTH, false);
		MARKUP.addAnnotationRenderer(ANNOTATION_WIDTH, NothingRenderer.getInstance());

		MARKUP.addAnnotation(ANNOTATION_HEIGHT, false);
		MARKUP.addAnnotationRenderer(ANNOTATION_HEIGHT, NothingRenderer.getInstance());

		MARKUP.addAnnotation(ANNOTATION_FORMAT, false);
		MARKUP.addAnnotation(ANNOTATION_SHOWCLASSES, false, "true", "false");
		MARKUP.addAnnotation(ANNOTATION_SHOWPROPERTIES, false, "true", "false");
		MARKUP.addAnnotation(ANNOTATION_LANGUAGE, false);

		MARKUP.addAnnotation(ANNOTATION_DOT_APP, false, DotApps.values());
		MARKUP.addAnnotation(ANNOTATION_ADD_TO_DOT, false);

		MARKUP.addAnnotation(ANNOTATION_OUTGOING_EDGES, false, "true", "false");
		MARKUP.addAnnotation(ANNOTATION_SHOWINVERSE, false, "true", "false");

		MARKUP.addAnnotation(Rdf2GoCore.GLOBAL, false, "true", "false");
		MARKUP.addAnnotationRenderer(Rdf2GoCore.GLOBAL, NothingRenderer.getInstance());

		//MARKUP.addAnnotation(ANNOTATION_RENDERER, false, GraphDataBuilder.Renderer.values());
		MARKUP.addAnnotation(ANNOTATION_VISUALIZATION, false, Visualizations.values());

		MARKUP.addAnnotation(SparqlVisualizationType.ANNOTATION_LINK_MODE, false, SparqlVisualizationType.LinkMode.values());
		MARKUP.addAnnotationRenderer(SparqlVisualizationType.ANNOTATION_LINK_MODE, NothingRenderer.getInstance());

		MARKUP.addAnnotation(SparqlVisualizationType.ANNOTATION_RANK_DIR, false, "LR", "RL", "TB", "BT");
		MARKUP.addAnnotationRenderer(SparqlVisualizationType.ANNOTATION_RANK_DIR, NothingRenderer.getInstance());

		MARKUP.addAnnotation(SparqlVisualizationType.ANNOTATION_LABELS, false, "true", "false");
		MARKUP.addAnnotationRenderer(SparqlVisualizationType.ANNOTATION_LABELS, NothingRenderer.getInstance());


		MARKUP.addAnnotation(ANNOTATION_CONFIG, false);
		MARKUP.addAnnotation(ANNOTATION_PRERENDER, false);
		return MARKUP;
	}

	protected String getMarkupName() {
		return "ConceptVisualization";
	}

	@Override
	public PreRenderer getPreRenderer() {
		return new OntoVisTypeRenderer();
	}

}
