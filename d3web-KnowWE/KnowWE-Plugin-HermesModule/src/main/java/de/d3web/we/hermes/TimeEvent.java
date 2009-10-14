package de.d3web.we.hermes;

import java.util.List;

import de.d3web.we.hermes.util.TimeStringInterpreter;

public class TimeEvent {

	public String getTextOrigin() {
		return textOrigin;
	}

	private int importance;
	private String title;
	private String description;
	private List<String> sources;

	private String textOrigin;

	private TimeStamp time;

	public TimeEvent(String title, String description, int imp,
			List<String> sources, String time, String textOrigin) {
		this.title = title;
		this.description = description;
		this.sources = sources;
		this.importance = imp;
		this.textOrigin = textOrigin;
		if (time != null) {
			this.time = new TimeStringInterpreter(time);
		}
	}

	public int getImportance() {
		return importance;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getSources() {
		return sources;
	}

	public TimeStamp getTime() {
		return time;
	}

}
