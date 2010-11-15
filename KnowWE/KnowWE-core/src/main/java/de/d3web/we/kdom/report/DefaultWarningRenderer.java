/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Default renderer for Warning messages
 * 
 * To have your own customized WarningRenderer overwrite getWarningRenderer in
 * your KnowWEObjectType and return a (custom) MessageRenderer of your choice
 * 
 * @author Jochen
 * 
 */
public class DefaultWarningRenderer implements MessageRenderer {

	private static DefaultWarningRenderer instance = null;

	public static DefaultWarningRenderer getInstance() {
		if (instance == null) {
			instance = new DefaultWarningRenderer();
		}
		return instance;
	}

	private final String cssClass = "KDDOMWarning";
	private final String cssStyle = "color:#CFCF00;text-decoration:underline;";

	@Override
	public String postRenderMessage(KDOMReportMessage m, KnowWEUserContext user) {
		return KnowWEUtils.maskHTML("</span>");
	}

	@Override
	public String preRenderMessage(KDOMReportMessage m, KnowWEUserContext user) {
		StringBuilder string = new StringBuilder();

		string.append(KnowWEUtils.maskHTML("<span"));
		if (m.getVerbalization() != null) {
			string.append(" title='").append(
					m.getVerbalization().replace('\'', '"')).append("'");
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
