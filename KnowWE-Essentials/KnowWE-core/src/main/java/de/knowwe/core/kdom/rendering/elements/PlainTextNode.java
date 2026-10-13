/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.rendering.RenderResult;

/**
 * Text that is rendered literally, interpreted neither as HTML nor as JSPWiki markup, see
 * {@link RenderResult#appendPlainText(String)}. Use it for user provided text like regular expressions,
 * which would otherwise be rendered as JSPWiki markup by a {@link TextNode} (e.g. "[a-z]" as link).
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.10.2026
 */
public class PlainTextNode extends HtmlElement {

	private final String text;

	public PlainTextNode(String text) {
		this.text = text;
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
		result.appendPlainText(text);
	}

	@Override
	public String toString() {
		return Strings.encodeHtml(text);
	}
}
