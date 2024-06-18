/*
 * Copyright (C) 2023 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

import de.knowwe.core.kdom.rendering.RenderResult;

/**
 * A text node without tag name and attributes, to be added as children to other {@link HtmlElement}s.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.10.2023
 */
public class TextNode extends HtmlElement {

	private final String textContent;

	public TextNode(String textContent) {
		this.textContent = textContent;
	}

	@Override
	public HtmlElement children(HtmlProvider... htmlElements) {
		return this; // ignore
	}

	@Override
	public HtmlElement tag(String tagName) {
		return this; // ignore
	}

	@Override
	public HtmlElement attributes(String... attributeNameAndValues) {
		return this; // ignore
	}

	@Override
	public void write(RenderResult result) {
		result.append(textContent);
	}
}
