/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

/**
 * A simple &lt;th&gt;...&lt;/th&gt; HTML element.
 */
public class Th extends HtmlElement {

	public Th() {
		super("th");
	}

	public Th(String textContent) {
		this();
		content(textContent);
	}
}
