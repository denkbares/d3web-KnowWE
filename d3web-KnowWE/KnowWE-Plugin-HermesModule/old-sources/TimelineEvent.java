package de.d3web.we.hermes.timeline;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class TimelineEvent.
 */
public class TimelineEvent implements Comparable<TimelineEvent> {
	
	/** The Constant SOURCE_INDICATOR. */
	public static final String SOURCE_INDICATOR = "QUELLE:";

	/** The string that specifies the time, when this event occurred */
	private final String timeString;
	
	/** A rendering of the time this event occurred. Caches the renderer's output. */
	private String timeOutputString;
	
	/** The header of this event. */
	private final String header;
	
	/** The abstract text. short description of this event */
	private final  String abstractText;
	
	//the topic, in which this event was defined;
	/** The name of the wiki article, in which TimelineEvent is specified */
	private String topicName;

	/** The relevance of this event. An integer from (1-4) */
	private int relevance;

	/** The historical sources describing this event. */
	private ArrayList<String> sources;
//
//	// caches the rendered output
//	/** The rendered output. */
//	private String renderedOutput;

	/** The time interpreter. */
	private TimeStringInterpreter timeInterpreter;
	
	/**
	 * Instantiates a new timeline event.
	 * 
	 * @param topicName the topic name
	 * @param header the header
	 * @param timeString the time string
	 * @param abstractText the abstract text
	 * @param fullText the full text
	 * @param relevance the relevance
	 * @param sourcesStrings the sources strings
	 */
	public TimelineEvent(String topicName, String header, String timeString, String abstractText, int relevance, ArrayList<String> sourcesStrings) {
		super();
		this.topicName = topicName;
		this.header = header;
		this.relevance = relevance;
		this.timeString = timeString;
		this.timeInterpreter = new TimeStringInterpreter(timeString);

		this.abstractText = abstractText;
		this.sources = sourcesStrings;
//		this.renderedOutput = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TimelineEvent o) {
		return (int) Math.round(10000 * (this.getStartTime() - (o.getStartTime())));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimelineEvent other = (TimelineEvent) obj;
		if (abstractText == null) {
			if (other.abstractText != null)
				return false;
		} else if (!abstractText.equals(other.abstractText))
			return false;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		if (relevance != other.relevance) {
			return false;	
		}
		if (sources == null) {
			if (other.sources != null)
				return false;
		} else if (!sources.equals(other.sources))
			return false;
		if (timeInterpreter == null) {
			if (other.timeInterpreter != null)
				return false;
		} else if (!timeInterpreter.equals(other.timeInterpreter))
			return false;
		if (timeOutputString == null) {
			if (other.timeOutputString != null)
				return false;
		} else if (!timeOutputString.equals(other.timeOutputString))
			return false;
		if (timeString == null) {
			if (other.timeString != null)
				return false;
		} else if (!timeString.equals(other.timeString))
			return false;
		if (topicName == null) {
			if (other.topicName != null)
				return false;
		} else if (!topicName.equals(other.topicName))
			return false;
		return true;
	}

	/**
	 * Gets the abstract text.
	 * 
	 * @return the abstract text
	 */
	public String getAbstractText() {
		return abstractText;
	}

	/**
	 * Gets the header.
	 * 
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Gets the relevance.
	 * 
	 * @return the relevance
	 */
	public int getRelevance() {
		return relevance;
	}


	/**
	 * Gets the sources.
	 * 
	 * @return the sources
	 */
	public ArrayList<String> getSources() {
		return sources;
	}

	/**
	 * Gets the start time.
	 * 
	 * @return the start time
	 */
	public double getStartTime() {
		return timeInterpreter.getStartTime();
	}

	/**
	 * Gets the stop time.
	 * 
	 * @return the stop time
	 */
	public double getStopTime() {
		// no end time given => return StartTime
		if (timeInterpreter.getEndTime() == Double.NEGATIVE_INFINITY)
			return getStartTime();
		return timeInterpreter.getEndTime();
	}

	/**
	 * Gets the time output string.
	 * 
	 * @return the time output string
	 */
	public String getTimeOutputString() {
		if (timeOutputString == null) {
			timeOutputString = new TimeStringInterpreter(timeString).getOutputString();
		}
		return timeOutputString;
	}

	/**
	 * Gets the time string.
	 * 
	 * @return the time string
	 */
	public String getTimeString() {
		return timeString;
	}

	/**
	 * Gets the topic name.
	 * 
	 * @return the topic name
	 */
	public String getTopicName() {
		return topicName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abstractText == null) ? 0 : abstractText.hashCode());
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result + relevance;
		result = prime * result + ((sources == null) ? 0 : sources.hashCode());
		result = prime * result + ((timeInterpreter == null) ? 0 : timeInterpreter.hashCode());
		result = prime * result + ((timeOutputString == null) ? 0 : timeOutputString.hashCode());
		result = prime * result + ((timeString == null) ? 0 : timeString.hashCode());
		result = prime * result + ((topicName == null) ? 0 : topicName.hashCode());
		return result;
	}
	
	/**
	 * Sets the relevance.
	 * 
	 * @param relevance the new relevance
	 */
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}
}
