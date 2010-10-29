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

package de.d3web.we.hermes.kdom.event.renderer;

import de.d3web.we.hermes.HermesUserManagement;
import de.d3web.we.hermes.kdom.event.TimeEventNew;
import de.d3web.we.hermes.kdom.event.TimeEventNew.ImportanceType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TimeEventRenderer extends KnowWEDomRenderer<TimeEventNew> {

	private static TimeEventRenderer instance;

	public static TimeEventRenderer getInstance() {
		if (instance == null) {
			instance = new TimeEventRenderer();
		}
		return instance;
	}

	@Override
	public void render(KnowWEArticle article, Section<TimeEventNew> sec,
			KnowWEUserContext user, StringBuilder result) {

		int eventImportanceLevel = -1;
		Section<ImportanceType> childOfType = sec.findChildOfType(ImportanceType.class);
		if (childOfType != null) {
			eventImportanceLevel = ImportanceType.getImportance(childOfType);
		}

		// check filter Level
		int filterLevel = getFilterLevel(user);
		if (eventImportanceLevel > filterLevel) {
			// do NOT render TimeEvent at all
			return;
		}

		result.append("%%collapsebox-closed \n! ");

		// change color of title depending on importance
		String style = "color:black";
		if (eventImportanceLevel == 1) {
			style = "color:rgb(255, 0, 102)";
		}
		if (eventImportanceLevel == 2) {
			style = "color:rgb(235, 235, 20)";
		}
		if (eventImportanceLevel == 3) {
			style = "color:rgb(20, 200, 102)";
		}
		String imp = KnowWEUtils.maskHTML("<span style='" + style + "'>");// Span-Tag
		// closes
		// in
		// TimeEventTitleRenderer

		result.append(imp);
		DelegateRenderer.getInstance().render(article, sec, user, result);
		result.append("/%\\\\");
	}

	private int getFilterLevel(KnowWEUserContext user) {
		Integer impFilterLevel = HermesUserManagement.getInstance()
				.getEventFilterLevelForUser(user.getUserName());
		int filterLevel = 3;
		if (impFilterLevel != null) {
			filterLevel = impFilterLevel.intValue();
		}
		return filterLevel;
	}

}
