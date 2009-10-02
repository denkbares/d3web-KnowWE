/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.hermes.kdom.renderer;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.hermes.kdom.TimeEventDateType;
import de.d3web.we.hermes.kdom.TimeEventDescriptionType;
import de.d3web.we.hermes.kdom.TimeEventImportanceType;
import de.d3web.we.hermes.kdom.TimeEventSourceType;
import de.d3web.we.hermes.kdom.TimeEventTitleType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TimeEventRenderer extends KnowWEDomRenderer {

	private static TimeEventRenderer instance;

	public static TimeEventRenderer getInstance() {
		if (instance == null) {
			instance = new TimeEventRenderer();
		}
		return instance;
	}

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder result) {

		Section titleSection = sec.findChildOfType(TimeEventTitleType.class);
		String title = "no title found";
		if (titleSection != null)
			title = titleSection.getOriginalText();

		Section dateSection = sec.findChildOfType(TimeEventDateType.class);
		String date = "no date found";
		if (dateSection != null)
			date = dateSection.getOriginalText();
		if (date.startsWith("\r\n"))
			date = date.substring(2);

		Section descriptionSection = sec
				.findChildOfType(TimeEventDescriptionType.class);
		String description = "no description found";
		if (descriptionSection != null)
			description = descriptionSection.getOriginalText();

		Section importanceSection = sec
				.findChildOfType(TimeEventImportanceType.class);
		String importance = "no importance found";
		if (importanceSection != null)
			importance = importanceSection.getOriginalText();

		List<Section> sources = new ArrayList<Section>();
		sec.findSuccessorsOfType(TimeEventSourceType.class, sources);

		//StringBuilder result = new StringBuilder();
		result.append("%%collapsebox-closed \n");
		String titleHeader = "";
		String style = "color:rgb(20, 200, 102)";
		if (importance.contains("1")) {
			style = "color:rgb(255, 0, 102)";
		}
		if (importance.contains("2")) {
			style = "color:rgb(235, 235, 20)";
		}
		title = KnowWEEnvironment.maskHTML("<span style='" + style + "'>"
				+ title + "</span>");
		titleHeader = title + "   :   ";
		titleHeader += date;
		// titleHeader += " " + importance;
		result.append("! " + titleHeader + " \n");

		result.append(description);

		if (sources.size() > 0) {
			result.append("\\\\__Quellen:__\n\n");
			for (Section section : sources) {
				String text = section.getOriginalText();
				String key = "QUELLE:";
				if (text.startsWith(key)) {
					text = text.substring(key.length());
				}
				result.append(text + "\\\\");
			}
		}
		result.append("/%\n");

		//return result.toString();
	}

}
