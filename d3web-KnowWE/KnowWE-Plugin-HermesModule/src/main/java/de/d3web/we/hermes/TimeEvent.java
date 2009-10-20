package de.d3web.we.hermes;

import java.util.List;

public class TimeEvent implements Comparable<TimeEvent> {

	private int importance;

	private String title;
	private String description;
	private List<String> sources;
	private String textOriginNode;
	private String topic;

	public String getTopic() {
		return topic;
	}

	private TimeStamp time;

	public TimeEvent(String title, String description, int imp,
			List<String> sources, String time, String textOriginNode, String topic) {
		this.title = title;
		this.description = description;
		this.sources = sources;
		this.importance = imp;
		this.textOriginNode = textOriginNode;
		this.topic = topic;
		if (time != null) {
			this.time = new TimeStamp(time);
		}
	}

	public String getDescription() {
		return description;
	}

	public int getImportance() {
		return importance;
	}

	public List<String> getSources() {
		return sources;
	}

	public String getTextOriginNode() {
		return textOriginNode;
	}

	public TimeStamp getTime() {
		return time;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public int compareTo(TimeEvent o) {
		return this.getTime().compareTo(o.getTime());
	}

}
