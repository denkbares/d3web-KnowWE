/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

/**
 * A simple &lt;p&gt;...&lt;/p&gt; HTML element.
 */
public class P extends HtmlElement {

	public P() {
		super("p");
	}

	public P(String textContent) {
		this();
		content(textContent);
	}
}
