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
import de.d3web.we.hermes.HermesUserManagement;
import de.d3web.we.hermes.TimeStamp;
import de.d3web.we.hermes.kdom.TimeEventDateType;
import de.d3web.we.hermes.kdom.TimeEventDescriptionType;
import de.d3web.we.hermes.kdom.TimeEventImportanceType;
import de.d3web.we.hermes.kdom.TimeEventSourceType;
import de.d3web.we.hermes.kdom.TimeEventTitleType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DefaultEditSectionRender;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TimeEventTypeRenderer extends DefaultEditSectionRender {

	private static TimeEventTypeRenderer instance;

	public static TimeEventTypeRenderer getInstance() {
		if (instance == null) {
			instance = new TimeEventTypeRenderer();
		}
		return instance;
	}

	@Override
	public void renderContent(Section sec, KnowWEUserContext user, StringBuilder result) {

		// check filter Level
		int filterLevel = getFilterLevel(user);
		int eventImportanceLevel = getImportanceOfEvent(sec);

		if (eventImportanceLevel > filterLevel) {
			// do NOT render TimeEvent at all
			return;
		}

		Section titleSection = sec.findChildOfType(TimeEventTitleType.class);
		String title = "no title found";
		if (titleSection != null)
			title = titleSection.getOriginalText();

		String date = TimeStamp.decode(getDateString(sec));

		Section descriptionSection = sec
				.findChildOfType(TimeEventDescriptionType.class);
		// String description = "no description found";
		// if (descriptionSection != null)
		// description = descriptionSection.getOriginalText();

		List<Section> sources = new ArrayList<Section>();
		sec.findSuccessorsOfType(TimeEventSourceType.class, sources);

		// StringBuilder result = new StringBuilder();

		String titleHeader = "";
		String style = "color:rgb(20, 200, 102)";
		if (eventImportanceLevel == 1) {
			style = "color:rgb(255, 0, 102)";
		}
		if (eventImportanceLevel == 2) {
			style = "color:rgb(235, 235, 20)";
		}

		title = KnowWEEnvironment.maskHTML("<span style='" + style + "'>") + title + KnowWEEnvironment.maskHTML("</span>");
		titleHeader = title + "   :   ";
		titleHeader += date;
		// titleHeader += " " + importance;
		result.append("%%collapsebox-closed \n");

		result.append("! " + titleHeader + " \n");

		DelegateRenderer.getInstance().render(descriptionSection, user, result);

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

		// return result.toString();
	}

	private String getDateString(Section sec) {
		Section dateSection = sec.findChildOfType(TimeEventDateType.class);
		String date = "no date found";
		if (dateSection != null)
			date = dateSection.getOriginalText();
		if (date.startsWith("\r\n"))
			date = date.substring(2);
		return date;
	}

	private int getFilterLevel(KnowWEUserContext user) {
		Integer impFilterLevel = HermesUserManagement.getInstance()
				.getEventFilterLevelForUser(user.getUsername());
		int filterLevel = 3;
		if (impFilterLevel != null) {
			filterLevel = impFilterLevel.intValue();
		}
		return filterLevel;
	}

	private int getImportanceOfEvent(Section sec) {
		Section importanceSection = sec
				.findChildOfType(TimeEventImportanceType.class);
		String importance = "no importance found";
		if (importanceSection != null) {
			importance = importanceSection.getOriginalText();
		}

		try {
			String digit = importance.substring(importance.indexOf('(') + 1,
					importance.indexOf(')')).trim();
			int eventLevel = Integer.parseInt(digit);
			return eventLevel;
		} catch (Exception e) {
			return -1;
		}
	}



}
