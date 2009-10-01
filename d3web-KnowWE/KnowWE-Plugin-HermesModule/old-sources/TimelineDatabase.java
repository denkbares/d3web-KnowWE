package de.d3web.we.hermes.timeline;
import java.util.ArrayList;
import java.util.Collections;

// TODO: Auto-generated Javadoc
/**
 * The Class TimelineDatabase.
 */
public class TimelineDatabase {

	
	/** The singleton instance. */
	private static TimelineDatabase instance;
	
	/**
	 * Gets the single instance of TimelineDatabase.
	 * 
	 * @return single instance of TimelineDatabase
	 */
	public static TimelineDatabase getInstance() {
		if (instance == null) {
			instance = new TimelineDatabase();
		}
		return instance;
	}
	
	/** The Timeline events saved in this database. */
	private ArrayList<TimelineEvent> events;
	
	public ArrayList<TimelineEvent> getAllEvents() {
		Collections.sort(events);
		return events;
	}

	/**
	 * Instantiates the singleton instance.
	 */
	private TimelineDatabase () {
		events = new ArrayList<TimelineEvent>();
	}
	
	/**
	 * Adds the timeline event to the database.
	 * 
	 * @param te the te
	 * @param topic the topic
	 */
	public synchronized void addTimelineEvent (TimelineEvent te) {
		events.add(te);
	}
	
	/**
	 * Clear this database (remove all events from it).
	 */
	public void clear() {
		events.clear();
	}
	
	public ArrayList<TimelineEvent> getEventsInTimeframe(double startTimedouble, double stopTime) {
		return this.getEventsInTimeframe(startTimedouble, stopTime, 3);
	}
	
	/**
	 * Gets the events with the specified parameters.
	 * 
	 * @param startIndex the start index
	 * @param stopIndex the stop index
	 * @param startTimedouble the lower bound of our timefilter
	 * @param stopTime the upper bound of our time filter
	 * 
	 * @return the events
	 */
	public ArrayList<TimelineEvent> getEventsInTimeframe(double startTimedouble, double stopTime, int relevance) {
		Collections.sort(events);
		
		if (relevance > 3 && relevance < 1) {
			return new ArrayList<TimelineEvent> ();
		}
		//filter the events in the specified timeframe;
		ArrayList<TimelineEvent> eventsInTimeFrame = new ArrayList<TimelineEvent>();
		for (TimelineEvent te : events) {
			if (te.getStartTime() > startTimedouble && te.getStopTime() < stopTime && te.getRelevance() <= relevance) {
				eventsInTimeFrame.add(te);
			}
		}
		return eventsInTimeFrame;
	}
	
	public ArrayList<TimelineEvent> getEventsInTopicWithAtExactRelevance(String topic, int relevance) {
		Collections.sort(events);
		
		//filter the events in the specified timeframe;
		ArrayList<TimelineEvent> result = new ArrayList<TimelineEvent>();
		for (TimelineEvent te : events) {
			if (te.getTopicName().equals(topic) && te.getRelevance() == relevance) {
				result.add(te);
			}
		}
		return result;
	}
	
	public ArrayList<TimelineEvent> getEventsInTopicWithAtLeastRelevance(String topic, int relevance) {
		Collections.sort(events);
		
		//filter the events in the specified timeframe;
		ArrayList<TimelineEvent> result = new ArrayList<TimelineEvent>();
		for (TimelineEvent te : events) {
			if (te.getTopicName().equals(topic) && te.getRelevance() <= relevance) {
				result.add(te);
			}
		}
		return result;
	}
	
	/**
	 * Gets the number of events in this Database.
	 * 
	 * @return the number of events
	 */
	public int getNumberOfEvents () {
		return events.size();
	}
	
	/**
	 * Gets the topic of event.
	 * 
	 * @param te the te
	 * 
	 * @return the topic of event
	 */
	public String getTopicOfEvent (TimelineEvent te) {
		return te.getTopicName();
	}

	//improve this on perfomance bottleneck!
	/**
	 * Removes the all events from topic.
	 * @param topic the topic
	 */
	public void removeAllEventsFromTopic (String topic) {
		ArrayList<TimelineEvent> allEvents = (ArrayList<TimelineEvent>) events.clone();
		for (TimelineEvent te : allEvents) {
			if (te.getTopicName().equals(topic)) {
				events.remove(te);
			}
		}
	}
}
