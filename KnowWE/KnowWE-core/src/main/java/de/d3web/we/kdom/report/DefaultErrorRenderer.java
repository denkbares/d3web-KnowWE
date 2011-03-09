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

package de.d3web.we.kdom.report;

import de.d3web.we.user.UserContext;
import de.d3web.we.utils.KnowWEUtils;

/**
 * Default renderer for error messages
 * 
 * To have your own customized ErrorRenderer overwrite getErrorRenderer in your
 * Type and return a (custom) MessageRenderer of your choice
 * 
 * @author Jochen
 * 
 */
public class DefaultErrorRenderer implements MessageRenderer {

	public static final DefaultErrorRenderer INSTANCE_ERROR = new DefaultErrorRenderer(
			"KDDOMError", "color:red;text-decoration:underline;");

	public static final DefaultErrorRenderer INSTANCE_WARNING = new DefaultErrorRenderer(
			"KDDOMWarning", "color:#CFCF00;text-decoration:underline;");

	public static final DefaultErrorRenderer INSTANCE_NOTE = new DefaultErrorRenderer(
			"KDDOMNotice", null);

	private final String cssClass;
	private final String cssStyle;

	private DefaultErrorRenderer(String cssClass, String cssStyle) {
		this.cssClass = cssClass;
		this.cssStyle = cssStyle;
	}

	@Override
	public String postRenderMessage(KDOMReportMessage m, UserContext user) {
		return KnowWEUtils.maskHTML("</span>");
	}

	@Override
	public String preRenderMessage(KDOMReportMessage m, UserContext user) {
		StringBuilder string = new StringBuilder();

		string.append(KnowWEUtils.maskHTML("<span"));
		String tooltip = m.getVerbalization();
		if (tooltip != null) {
			string.append(" title='").append(
					tooltip.replace('\'', '"')).append("'");
		}
		if (cssClass != null) {
			string.append(" class='").append(cssClass).append("'");
		}
		if (cssStyle != null) {
			string.append(" style='").append(cssStyle).append("'");
		}

		string.append(KnowWEUtils.maskHTML(">"));

		return string.toString();
	}

}
