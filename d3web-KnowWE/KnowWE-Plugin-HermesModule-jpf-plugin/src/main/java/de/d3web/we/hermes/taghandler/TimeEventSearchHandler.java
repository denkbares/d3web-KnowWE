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

import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TimeEventSearchHandler extends AbstractTagHandler {

	public TimeEventSearchHandler() {
		super("timeEventSearch");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		return generateForms(topic);
	}

	private String generateForms(String topic) {
		int startIndex = 0;
		int noEntries = 20;
		String startTimeString = "-10000";
		String endTimeString = "2008";
		String header = "Suche nach Ereignissen";
		String s = "";

		s += "<div class=\"panel\"><h3>" + header + "</h3>";
		s += "<form action=\"Wiki.jsp?page="
				+ topic
				+ "\" name=\"testform\" accept-charset=\"UTF-8\" method=\"post\" enctype=\"application/x-www-form-urlencoded\">"
				+
				"<input type=\"hidden\" name=\"formname\" value=\"testform\"/>";
		s += "<p>Start Index: <input id='startIndexTimeline' type='text' value='" + startIndex
				+ "'/> " +
				"Anzahl Einträge: <input id='hermesSearchResultCount' type='text' value='"
				+ noEntries + "'/></p>";
		s += "<p>Von: <input id='hermesSearchFrom'  type='text' value='" + startTimeString + "'/> "
				+
				"Bis: <input id='hermesSearchTo'  type='text' value='" + endTimeString + "'/></p>";
		s += "<p><input onclick='sendTimeEventSearchRequest()' type='button' value='Anzeigen'/></p>";
		s += "</form>";

		s += "</div>";
		s += "<div id='hermesSearchResult'></div>";
		return s;
	}

}
