/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.timeline.serialization;

import java.util.ArrayList;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.d3web.core.knowledge.terminology.Question;
import de.knowwe.timeline.IDataProvider;
import de.knowwe.timeline.Query;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public class TimelineDrawer {

	final IDataProvider dataProvider;
	final ArrayList<Query> queries;
	final String sourceID;
	final IDProvider<Date> dates;
	final IDProvider<Question> questions;
	final ValueStorage data;
	String errors;

	public TimelineDrawer(String sourceID, IDataProvider dataProvider) {
		this.sourceID = sourceID;
		this.dataProvider = dataProvider;

		this.queries = new ArrayList<Query>();
		this.dates = new IDProvider<Date>();
		this.questions = new IDProvider<Question>();
		this.data = new ValueStorage();
		errors = "";
	}

	public void addQuery(Query query) {
		queries.add(query);
	}
	
	public void addError(String error) {
		errors += error;
	}

	public String getJSON() {
		addAllDates();
		return createGson().toJson(this);
	}

	private void addAllDates() {
		for (Date d : dataProvider.getAllDates()) {
			dates.getId(d);
		}
	}

	private Gson createGson() {
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(TimelineDrawer.class, new TimelineDrawerSerializer());
		gson.registerTypeAdapter(Query.class, new QuerySerializer(this));
		gson.registerTypeHierarchyAdapter(Question.class, new QuestionSerializer());
		gson.registerTypeAdapter(Question.class, new QuestionSerializer());
		gson.registerTypeAdapter(Date.class, new DateSerializer(dates));
		gson.registerTypeAdapter(IDProvider.class, new IDProviderSerializer());
		return gson.create();
	}
}
