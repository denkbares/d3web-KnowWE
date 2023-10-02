/*
 * Copyright (C) 2023 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

/**
 * A simple <span>...</span> HTML element
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.10.2023
 */
public class Span extends HtmlElement {

	public Span() {
		tag("span");
	}

	public Span(String textContent) {
		this();
		content(textContent);
	}
}
