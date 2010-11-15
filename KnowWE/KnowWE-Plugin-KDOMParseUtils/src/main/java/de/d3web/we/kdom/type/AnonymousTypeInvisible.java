package de.d3web.we.kdom.type;

import de.d3web.we.kdom.rendering.NothingRenderer;

public class AnonymousTypeInvisible extends AnonymousType {

	public AnonymousTypeInvisible(String name) {
		super(name);
		this.setCustomRenderer(NothingRenderer.getInstance());
	}
}
