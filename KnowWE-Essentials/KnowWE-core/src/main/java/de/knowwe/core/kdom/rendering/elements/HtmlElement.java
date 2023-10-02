/*
 * Copyright (C) 2023 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.knowwe.core.kdom.rendering.RenderResult;

/**
 * HTML element with builder pattern that can have attributes and children and can easily be used with
 * {@link RenderResult}.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.10.2023
 */
public class HtmlElement {

	private String tagName = null;
	private final List<String> attributes = new ArrayList<>();
	private final List<HtmlElement> children = new ArrayList<>();

	/**
	 * Create a new empty HTML element with default div tag name.
	 */
	public HtmlElement() {
		this.tag("div");
	}

	/**
	 * Create a new HTML element with the given tag name
	 */
	public HtmlElement(String tagName) {
		this.tag(tagName);
	}

	/**
	 * Set the tag name of this element.
	 */
	public HtmlElement tag(String tagName) {
		this.tagName = tagName;
		return this;
	}

	/**
	 * Add the given attributes to this element in pairs, where the strings will alternate between attribute name and
	 * attribute value, e.g. <tt>attributes("class", "my-class", "style", "color: red")</tt>
	 */
	public HtmlElement attributes(String... attributeNameAndValues) {
		this.attributes.addAll(Arrays.asList(attributeNameAndValues));
		return this;
	}

	public HtmlElement clazz(String className) {
		this.attributes.add("class");
		this.attributes.add(className);
		return this;
	}

	/**
	 * Add text content to this element. Will be added as a TextNode.
	 */
	public HtmlElement content(String stringContent) {
		children(new TextNode(stringContent));
		return this;
	}

	/**
	 * Add children to this HTML element
	 */

	public HtmlElement children(HtmlElement... htmlElements) {
		this.children.addAll(Arrays.asList(htmlElements));
		return this;
	}

	/**
	 * Write the HTML of this element to the given {@link RenderResult}
	 */
	public void write(RenderResult result) {
		Objects.requireNonNull(tagName);
		result.appendHtmlTag(tagName, attributes.toArray(String[]::new));
		for (HtmlElement child : children) {
			child.write(result);
		}
		result.appendHtmlTag("/" + tagName);
	}
}
