/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

/**
 * A simple &lt;button&gt;...&lt;/button&gt; HTML element. Defaults {@code type="button"} so a
 * button inside a form does not accidentally submit it.
 */
public class Button extends HtmlElement {

	public Button() {
		super("button");
		attributes("type", "button");
	}

	public Button(String textContent) {
		this();
		content(textContent);
	}
}
