/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.core.report;

import de.knowwe.core.compile.Compiler;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

/**
 * Default renderer for error messages
 * <p/>
 * To have your own customized ErrorRenderer overwrite getErrorRenderer in your
 * Type and return a (custom) MessageRenderer of your choice
 *
 * @author Jochen
 */
public class DefaultMessageRenderer implements MessageRenderer {

	public static final DefaultMessageRenderer ERROR_RENDERER = new DefaultMessageRenderer(
			"KDDOMError", "color:red;text-decoration:underline;");

	public static final DefaultMessageRenderer WARNING_RENDERER = new DefaultMessageRenderer(
			"KDDOMWarning", "color:#CFCF00;text-decoration:underline;");

	public static final DefaultMessageRenderer NOTE_RENDERER = new DefaultMessageRenderer(
			"KDDOMNotice", null);

	private final String cssClass;
	private final String cssStyle;

	private DefaultMessageRenderer(String cssClass, String cssStyle) {
		this.cssClass = cssClass;
		this.cssStyle = cssStyle;
	}

	@Override
	public void postRenderMessage(Message m, UserContext user, Compiler source, RenderResult result) {
		result.appendHtml("</span>");
	}

	@Override
	public void preRenderMessage(Message m, UserContext user, Compiler source, RenderResult result) {

		result.appendHtml("<span");
		String tooltip = m.getVerbalization();
		if (tooltip != null) {
			if (source != null) {
				tooltip = source + ": " + tooltip;
			}
			result.append(" title='").appendEntityEncoded(
					tooltip.replace('\'', '"')).append("'");
		}
		String cssClass = "tooltipster " + this.cssClass;
		if (cssClass != null) {
			result.append(" class='").append(cssClass).append("'");
		}
		if (cssStyle != null) {
			result.append(" style='").append(cssStyle).append("'");
		}

		result.appendHtml(">");

	}

}
