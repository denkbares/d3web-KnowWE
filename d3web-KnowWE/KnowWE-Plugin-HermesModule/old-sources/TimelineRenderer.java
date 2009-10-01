package de.d3web.we.hermes.timeline;

import java.util.ArrayList;
import java.util.List;

public class TimelineRenderer {

	public static String renderEventsToTabbedPane(String topic) {
		String s = "<div class=\"tabbedSection\">\n";
		
		//add tab for important events;
		List<TimelineEvent> actEvents = TimelineDatabase.getInstance().getEventsInTopicWithAtLeastRelevance(topic, 1);
		s += "<div class=\"tab-Important\">";
		for (TimelineEvent te : actEvents) {
			s += TimelineEventRenderer.renderToHTML(te, false);
		}
		s += "</div>";
		
		//add tab for mediocre events;
		List<TimelineEvent> actEvents2 = TimelineDatabase.getInstance().getEventsInTopicWithAtLeastRelevance(topic, 2);
		if (actEvents2.size() > actEvents.size()) {
			s += "<div class=\"tab-More...\">";
			for (TimelineEvent te : actEvents2) {
				s += TimelineEventRenderer.renderToHTML(te, false);
			}
			s += "</div>";
		}

		//add tab for m events;
		List<TimelineEvent> actEvents3 = TimelineDatabase.getInstance().getEventsInTopicWithAtLeastRelevance(topic, 3);
		if (actEvents3.size() > actEvents2.size()) {
			s += "<div class=\"tab-EvenMore...\">";
			for (TimelineEvent te : actEvents3) {
				s += TimelineEventRenderer.renderToHTML(te, false);
			}
			s += "</div>";
		}
		
		//end tabbedSection
		s += "</div>";
		return s;
	}

	public static String renderImportantEventsToList(String topic, String relevanceString) {
		//set default
		int relevance = 1;
		try {
			relevance = Integer.parseInt(relevanceString);
		} catch (NumberFormatException e) {
			//do nothing, just use default
		}
		return TimelineEventRenderer.renderToSimpleList(TimelineDatabase.getInstance().getEventsInTopicWithAtLeastRelevance(topic, relevance));
	}

	public static String generateTimeline(String startIndexString, String numberOfEntriesString,
			String startTimeString, String endTimeString) {
		//set defaults
		int startIndex = 0;
		int noEntries = 20;
		double startTime = Double.NEGATIVE_INFINITY;
		double endTime = Double.POSITIVE_INFINITY;
		
		// get startIndex
		try {
			startIndex = Integer.parseInt(startIndexString);
		} catch (NumberFormatException nfe) {
			// do nothing,use default
		}
	
		// get stopIndex
		try {
			noEntries = Integer.parseInt(numberOfEntriesString);
		} catch (NumberFormatException nfe) {
			// do nothing,use default
		}
	
		// get startTime
		startTime = new TimeStringInterpreter(startTimeString).getStartTime();
		endTime = new TimeStringInterpreter(endTimeString).getStartTime();
	
		// get the TimelineEvents for these indices
		ArrayList<TimelineEvent> eventsInTimeFrame = TimelineDatabase.getInstance().getEventsInTimeframe(startTime, endTime);
		
		// ... and render them
		String result = "Anzahl der gefundenen Zeitleisteneinträge: "
				+ eventsInTimeFrame.size() + "\n\n";
	
		startIndex = Math.max (startIndex, 0);
		int stopIndex = Math.min (startIndex + noEntries, eventsInTimeFrame.size() - 1);
		
		for (; startIndex <= stopIndex; startIndex++) {
			result += TimelineEventRenderer.renderToHTML(eventsInTimeFrame.get(startIndex), false);
		}
	
		return result;
	}

}
