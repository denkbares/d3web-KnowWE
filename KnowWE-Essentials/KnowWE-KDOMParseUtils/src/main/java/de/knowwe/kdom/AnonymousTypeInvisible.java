package de.knowwe.kdom;

import de.knowwe.core.kdom.rendering.NothingRenderer;

public class AnonymousTypeInvisible extends AnonymousType {

	public AnonymousTypeInvisible(String name) {
		super(name);
		this.setRenderer(NothingRenderer.getInstance());
	}
}
