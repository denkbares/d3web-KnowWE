package de.knowwe.rdfs.vis.markup;

import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.visualization.Config;

public class ConceptVisualizationType extends DefaultMarkupType implements VisualizationType {

	public enum DotApps {
		dot, neato
	}

	public enum Visualizations {
		wheel, force
	}

	public ConceptVisualizationType() {
		applyMarkup(createMarkup());
		this.setRenderer(new AsynchronousRenderer(getPreRenderer()));
	}

	protected DefaultMarkup createMarkup() {
		DefaultMarkup MARKUP = new DefaultMarkup(getMarkupName());
		MARKUP.addAnnotation(Config.CONCEPT, true);

		MARKUP.addAnnotation(Config.COLORS, false);
		MARKUP.addAnnotationRenderer(Config.COLORS, NothingRenderer.getInstance());

		MARKUP.addAnnotation(Config.SUCCESSORS, false);
		MARKUP.addAnnotation(Config.PREDECESSORS, false);
		MARKUP.addAnnotation(Config.EXCLUDE_NODES, false);
		MARKUP.addAnnotation(Config.EXCLUDE_RELATIONS, false);
		MARKUP.addAnnotation(Config.FILTER_RELATIONS, false);

		MARKUP.addAnnotation(Config.SIZE, false);
		MARKUP.addAnnotationRenderer(Config.SIZE, NothingRenderer.getInstance());

		MARKUP.addAnnotation(Config.WIDTH, false);
		MARKUP.addAnnotationRenderer(Config.WIDTH, NothingRenderer.getInstance());

		MARKUP.addAnnotation(Config.HEIGHT, false);
		MARKUP.addAnnotationRenderer(Config.HEIGHT, NothingRenderer.getInstance());

		MARKUP.addAnnotation(Config.FORMAT, false);
		MARKUP.addAnnotation(Config.SHOW_CLASSES, false, "true", "false");
		MARKUP.addAnnotation(Config.SHOW_PROPERTIES, false, "true", "false");
		MARKUP.addAnnotation(Config.LANGUAGE, false);

		MARKUP.addAnnotation(Config.DOT_APP, false);
		MARKUP.addAnnotation(Config.ADD_TO_DOT, false);

		MARKUP.addAnnotation(Config.SHOW_OUTGOING_EDGES, false, "true", "false");
		MARKUP.addAnnotation(Config.SHOW_INVERSE, false, "true", "false");

		MARKUP.addAnnotation(Rdf2GoCore.GLOBAL, false, "true", "false");
		MARKUP.addAnnotationRenderer(Rdf2GoCore.GLOBAL, NothingRenderer.getInstance());

		MARKUP.addAnnotation(Config.VISUALIZATION, false, Visualizations.class);

		MARKUP.addAnnotation(Config.LINK_MODE, false, Config.LinkMode.class);
		MARKUP.addAnnotationRenderer(Config.LINK_MODE, NothingRenderer.getInstance());

		MARKUP.addAnnotation(Config.RANK_DIR, false, "LR", "RL", "TB", "BT");
		MARKUP.addAnnotationRenderer(Config.RANK_DIR, NothingRenderer.getInstance());

		MARKUP.addAnnotation(Config.SHOW_LABELS, false);
		MARKUP.addAnnotationRenderer(Config.SHOW_LABELS, NothingRenderer.getInstance());

		MARKUP.addAnnotation(Config.SHOW_LITERALS, false, Config.LiteralMode.class);
		MARKUP.addAnnotationRenderer(Config.SHOW_LITERALS, NothingRenderer.getInstance());

		MARKUP.addAnnotation(Config.TIMEOUT, false);
		MARKUP.addAnnotationRenderer(Config.TIMEOUT, NothingRenderer.getInstance());

		MARKUP.addAnnotation(Config.CONFIG, false);
		MARKUP.addAnnotation(Config.PRERENDER, false);
		return MARKUP;
	}

	protected String getMarkupName() {
		return "ConceptVisualization";
	}

	@Override
	public PreRenderer getPreRenderer() {
		return new ConceptVisualizationRenderer();
	}

}
