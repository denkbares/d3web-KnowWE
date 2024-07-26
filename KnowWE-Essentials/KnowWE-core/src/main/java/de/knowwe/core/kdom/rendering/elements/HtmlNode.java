/*
 * Copyright (C) 2023 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

import de.knowwe.core.kdom.rendering.RenderResult;

/**
 * A simple html node to be used similarly as inner html.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.10.2023
 */
public class HtmlNode extends HtmlElement {

	private final String innerHtml;

	public HtmlNode(String textContent) {
		this.innerHtml = textContent;
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
		result.appendHtml(innerHtml);
	}

	@Override
	public String toString() {
		return innerHtml;
	}
}
