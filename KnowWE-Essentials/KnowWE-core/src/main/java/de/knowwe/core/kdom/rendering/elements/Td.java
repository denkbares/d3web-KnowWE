/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

/**
 * A simple &lt;td&gt;...&lt;/td&gt; HTML element.
 */
public class Td extends HtmlElement {

	public Td() {
		super("td");
	}

	public Td(String textContent) {
		this();
		content(textContent);
	}
}
