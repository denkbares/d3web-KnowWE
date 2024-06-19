/*
 * Copyright (C) 2023 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

/**
 * A simple <li>...</li> HTML element
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.10.2023
 */
public class Li extends HtmlElement {

	public Li() {
		super("li");
	}

	public Li(String textContent) {
		this();
		content(textContent);
	}
}
