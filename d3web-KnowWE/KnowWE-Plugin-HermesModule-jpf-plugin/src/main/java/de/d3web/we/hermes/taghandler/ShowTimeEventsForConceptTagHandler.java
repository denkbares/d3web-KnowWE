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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.d3web.we.hermes.TimeEvent;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;
import de.d3web.we.hermes.util.TimeLineEventRenderer;
import de.d3web.we.taghandler.AbstractHTMLTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ShowTimeEventsForConceptTagHandler extends AbstractHTMLTagHandler {

	public ShowTimeEventsForConceptTagHandler() {
		super("eventsForConcept");
	}

	@Override
	public String renderHTML(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		String concept = topic;
		String givenConcept = values.get("concept");
		if (givenConcept != null) {
			concept = givenConcept;
		}

		List<TimeEvent> events = TimeEventSPARQLUtils
				.findTimeEventsInvolvingConcept(concept);
		Collections.sort(events);

		StringBuffer result = new StringBuffer();
		result.append("<div class=\"panel\">");
		result.append("<h3> Ereignisse für \"" + concept + "\":</h3><div>");

		if (events != null) {
			for (TimeEvent timeEvent : events) {
				result.append(TimeLineEventRenderer.renderToHTML(timeEvent,
						false));
			}
		}
		result.append("</div></div>");
		return result.toString();
	}
}
