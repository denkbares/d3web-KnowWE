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

package de.d3web.we.hermes;

import java.util.ArrayList;
import java.util.List;

public class TimeEvent implements Comparable<TimeEvent> {

	public void setDescription(String description) {
		this.description = description;
	}

	private Integer importance;

	private final String title;
	private String description;
	private List<String> sources = new ArrayList<String>();
	private final String textOriginNode;
	private final String topic;

	public String getTopic() {
		return topic;
	}

	private TimeStamp time;

	public TimeEvent(String title, String description, Integer imp,
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

	/**
	 * Allows to instanciate TimeEvents directly from the markup. Other data is
	 * added later.
	 * 
	 * @param title
	 * @param textOriginNode
	 * @param topic
	 */
	public TimeEvent(String title, String textOriginNode, String topic) {
		this.textOriginNode = textOriginNode;
		this.topic = topic;
		this.title = title;
	}

	public void setImportance(int importance) {
		this.importance = importance;
	}

	public void addSource(String src) {
		this.sources.add(src);
	}

	public void setTime(TimeStamp time) {
		this.time = time;
	}

	public String getDescription() {
		return description;
	}

	public Integer getImportance() {
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
