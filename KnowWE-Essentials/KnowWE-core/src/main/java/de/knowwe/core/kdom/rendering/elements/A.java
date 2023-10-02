/*
 * Copyright (C) 2023 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.10.2023
 */
public class A extends HtmlElement {

	public A() {
		tag("a");
	}

	public A(String textContent) {
		this();
		content(textContent);
	}

	public A(String label, String href) {
		this();
		content(label);
		attributes("href", href);
	}
}
