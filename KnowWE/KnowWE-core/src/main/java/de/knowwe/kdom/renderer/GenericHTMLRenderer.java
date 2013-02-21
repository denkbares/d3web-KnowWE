/*
 * Copyright (C) 2011 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.kdom.renderer;

import java.util.Arrays;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * A renderer that encapsulates the content in arbitrary HTML content. Useful
 * for testing.
 * 
 * @author Alex Legler
 */
public class GenericHTMLRenderer implements Renderer {

	/**
	 * The HTML tag to wrap the content in
	 */
	protected String tagName;

	/**
	 * The Attributes to set
	 */
	protected String[] attributes;

	/**
	 * Creates a new GenericHTMLRenderer Object
	 * 
	 * The attributes are expected in an array with key-value pairs Example:
	 * {"title", "The Title tag", "href", "http://the.link.com/"}
	 * 
	 * @param tagName The HTML tag name
	 * @param attributes The Attributes, as described above
	 */
	public GenericHTMLRenderer(String tagName, String[] attributes) {
		this.tagName = tagName;
		this.attributes = Arrays.copyOf(attributes, attributes.length);
	}

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {
		string.appendHtml("<").append(tagName);

		if (attributes != null && attributes.length > 0) {
			string.append(" ");
			for (int i = 0; i < attributes.length; i += 2) {
				string.append(attributes[i]).appendHtml("=\"");

				if (i < attributes.length && attributes[i + 1] != null) {
					string.append(attributes[i + 1].replace("\"", "&quot;"));
				}

				string.appendHtml("\"");

				if ((i + 2) < attributes.length) {
					string.append(" ");
				}
			}
		}

		string.appendHtml(">");
		renderContent(sec, user, string);
		string.appendHtml("</span>");
	}

	protected void renderContent(Section<?> section, UserContext user, RenderResult string) {
		RenderResult builder = new RenderResult(user);
		DelegateRenderer.getInstance().render(section, user, builder);
		string.appendJSPWikiMarkup(builder);
	}
}