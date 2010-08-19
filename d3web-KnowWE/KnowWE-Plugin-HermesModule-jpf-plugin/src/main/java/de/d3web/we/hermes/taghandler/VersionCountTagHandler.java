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

package de.d3web.we.hermes.taghandler;

import java.util.Map;
import java.util.Map.Entry;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class VersionCountTagHandler extends AbstractTagHandler {

	public VersionCountTagHandler() {
		super("versionCounts");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		KnowWEWikiConnector connector = KnowWEEnvironment.getInstance()
				.getWikiConnector();

		String result = "<div class=\"versionCounts\">";
		result += "<div class=\"sortable\">";
		result += "<table class=\"wikitable\" border=\"1\">";
		result += "<tbody>";

		result += "<tr>";
		result += "<th class=\"sort\" > Seitenname </th>";
		result += "<th class=\"sort\" > Editierungen </th>";
		result += "</tr>";

		// result += "<table>";
		// result += "<th><td>pagename</td><td>versionCount</td></th>";
		for (Entry<String, Integer> e : connector.getVersionCounts().entrySet()) {
			result += "<tr><td>" + e.getKey() + "</td><td>" + e.getValue()
					+ "</td></tr>";
		}
		// result += "</table>";

		result += "</tbody>";
		result += "</table>";
		result += "</div>";
		result += "</div>";
		return result;
	}
}
