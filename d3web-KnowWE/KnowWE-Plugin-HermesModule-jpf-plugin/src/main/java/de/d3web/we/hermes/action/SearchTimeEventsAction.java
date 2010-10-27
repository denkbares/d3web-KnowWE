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

package de.d3web.we.hermes.action;

import java.util.Collections;
import java.util.List;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.hermes.TimeEvent;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;
import de.d3web.we.hermes.util.TimeLineEventRenderer;

public class SearchTimeEventsAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		String from = null;
		String to = null;
		String count = null;
		String startIndex = null;

		if (parameterMap.containsKey("from") && parameterMap.containsKey("to")) {
			from = parameterMap.get("from");
			to = parameterMap.get("to");
			count = parameterMap.get("count");
			startIndex = parameterMap.get("startIndex");
			
			int startIndexNum = 0;
			int countNum = 20;

			//startIndex 1 is first element
			try {
				startIndexNum = Integer.parseInt(startIndex)-1;
			}
			catch (NumberFormatException e) {
				// TODO
			}
			
			try {
				countNum = Integer.parseInt(count)+startIndexNum;
			}
			catch (NumberFormatException e) {
				// TODO
			}
			


			List<TimeEvent> events = TimeEventSPARQLUtils.findTimeEventsFromTo(
					Integer.parseInt(from), Integer.parseInt(to));

			Collections.sort(events);
			StringBuffer result = new StringBuffer();
			if (events != null) {
				int cnt = 0;
				for (TimeEvent timeEvent : events) {
					cnt++;
					if (cnt > startIndexNum) { 
						if (cnt > (countNum)) break;
						result.append(TimeLineEventRenderer.renderToHTML(timeEvent,
								false));
					}
				}
			}

			return result.toString();

		}

		return "nicht erfolgreich";
	}

}
