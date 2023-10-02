/*
 * Copyright (C) 2023 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

/**
 * A simple <div>...</div> HTML element
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.10.2023
 */
public class Div extends HtmlElement {

	public Div() {
		super("div");
	}

	public Div(String tagName) {
		throw new UnsupportedOperationException();
	}
}
