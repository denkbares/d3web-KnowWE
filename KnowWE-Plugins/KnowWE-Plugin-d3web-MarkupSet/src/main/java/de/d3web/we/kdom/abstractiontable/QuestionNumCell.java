package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.kdom.renderer.StyleRenderer;

public class QuestionNumCell extends AbstractType {


	public QuestionNumCell() {
		StyleRenderer renderer = StyleRenderer.NUMBER.withMaskMode(StyleRenderer.MaskMode.htmlEntities, StyleRenderer.MaskMode.jspwikiMarkup);
		this.setRenderer(renderer);
	}



}
