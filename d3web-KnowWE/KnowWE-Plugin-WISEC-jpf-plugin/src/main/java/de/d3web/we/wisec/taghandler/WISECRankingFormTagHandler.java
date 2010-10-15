/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.wisec.taghandler;

import java.util.Map;

import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.taghandler.AbstractHTMLTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Renders a form that offers basic options for the specification of WISEC
 * rankings.
 * 
 * @see Uki.js, wisec.js
 * 
 * @author Sebastian Furth
 * @created 14/09/2010
 */
public class WISECRankingFormTagHandler extends AbstractHTMLTagHandler {

	/**
	 * @param name
	 */
	public WISECRankingFormTagHandler() {
		super("wisec-form");
		KnowWERessourceLoader.getInstance().add("uki.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);
		KnowWERessourceLoader.getInstance().add("wisec.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);
		KnowWERessourceLoader.getInstance().add("wisec.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
	}

	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin wisec-form}]";
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		// TODO: This should probably moved to a resource bundle
		return "Displays a form that offers basic options for the specification of WISEC rankings.";
	}

	@Override
	public String renderHTML(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		StringBuilder html = new StringBuilder();

		// All we need is a div, everything else is done in wisec.js
		html.append("<div id=\"wisec-ranking-form\"></div>");
		html.append("<div id=\"wisec-ranking-result\"></div>");
		html.append("<div id=\"wisec-ranking-back-button\"></div>");

		return html.toString();
	}

}
