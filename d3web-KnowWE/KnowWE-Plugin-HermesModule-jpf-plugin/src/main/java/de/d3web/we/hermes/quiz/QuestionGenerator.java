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

package de.d3web.we.hermes.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.we.hermes.TimeEvent;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;

public class QuestionGenerator {

	private Integer to = null;
	private Integer from = null;

	private List<TimeEvent> events = new ArrayList<TimeEvent>();

	public QuestionGenerator(Integer from, Integer to) {
		this.from = from;
		this.to = to;
		events = TimeEventSPARQLUtils.findTimeEventsFromTo(-1000, 1000);
	}

	public Question generateNewQuestion() {

		if (events.size() == 0) {
			events = TimeEventSPARQLUtils.findTimeEventsFromTo(-1000, 1000);
		}

		if (events.size() == 0) {
			return null;
		}

		List<TimeEvent> acutalEventsToAsk = new ArrayList<TimeEvent>();

		if (from != null || to != null) {
			Collections.sort(events);
			for (TimeEvent timeEvent : events) {
				if (from != null
						&& timeEvent.getTime().getStartPoint().getInterpretableTime() < from) {
					// is before 'from' -> not collected
					continue;
				}
				if (to != null
						&& timeEvent.getTime().getStartPoint().getInterpretableTime() > to) {
					// is after 'to' -> not collected
					continue;
				}

				acutalEventsToAsk.add(timeEvent);

			}
		}
		else {
			acutalEventsToAsk = events;
		}

		int numQ0 = (int) (Math.random() * acutalEventsToAsk.size());
		int numQ1 = (int) (Math.random() * acutalEventsToAsk.size());
		int numQ2 = (int) (Math.random() * acutalEventsToAsk.size());

		int correct = (int) (Math.random() * 3);

		TimeEvent[] eventArray = new TimeEvent[3];

		eventArray[0] = acutalEventsToAsk.get(numQ0);
		eventArray[1] = acutalEventsToAsk.get(numQ1);
		eventArray[2] = acutalEventsToAsk.get(numQ2);

		String[] answers = {
				eventArray[0].getTime().getDescription(),
				eventArray[1].getTime().getDescription(),
				eventArray[2].getTime().getDescription() };

		Question q = new Question(eventArray[correct].getTitle(), answers, correct);

		return q;
	}

}
