package de.d3web.we.kdom.renderer;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * A renderer that renders font size depending of its depth in the KDOM as a
 * method of tree structure visualization e.g., for logical expressions like
 * (((a and b) or (c and not(e and f)))) Note: all the nodes beeing part of this
 * tree need to get this renderer (and being delegated to).
 * 
 * @author Jochen
 * 
 */
public class KDOMDepthFontSizeRenderer extends KnowWEDomRenderer<KnowWEObjectType> {

	private double initialFontsize = 340;
	private double depthDiscountFactor = 0.88;

	public KDOMDepthFontSizeRenderer(double initial, double discount) {
		this.initialFontsize = initial;
		this.depthDiscountFactor = discount;
	}

	public KDOMDepthFontSizeRenderer() {

	}

	@Override
	public void render(KnowWEArticle article, Section<KnowWEObjectType> sec, KnowWEUserContext user, StringBuilder string) {
		// font-size:1.2em

		double font = initialFontsize;
		Section<?> father = sec.getFather();
		while (father != null) {
			font = font * depthDiscountFactor;
			father = father.getFather();
		}
		String fontString = Double.toString(font);
		// for border: border-width:1px;border-color:black;border-style:solid;
		new StyleRenderer(
				"font-size:"
						+ fontString
						+ "%;").render(
				article, sec, user,
				string);

	}

}
