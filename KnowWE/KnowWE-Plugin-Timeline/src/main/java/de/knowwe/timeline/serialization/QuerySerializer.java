package de.knowwe.timeline.serialization;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map.Entry;
import java.util.SortedMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.Value;
import de.knowwe.timeline.Query;
import de.knowwe.timeline.Timeset;
import de.knowwe.timeline.Timespan;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
class QuerySerializer implements JsonSerializer<Query> {

	private final TimelineDrawer timelineDrawer;

	QuerySerializer(TimelineDrawer timelineDrawer) {
		this.timelineDrawer = timelineDrawer;
	}

	@Override
	public JsonElement serialize(Query src, Type srcType, JsonSerializationContext context) {
		JsonObject query = new JsonObject();
		query.add("text", new JsonPrimitive(src.getQuery().trim()));
		Timeset ts = src.execute(this.timelineDrawer.dataProvider);
		query.add("events", context.serialize(ts.spans));
		for (Timespan span : ts) {

			for (Question qu : src.getQuestions(this.timelineDrawer.dataProvider)) {
				if (qu == null) {
					//
				}
				SortedMap<Date, Value> values = this.timelineDrawer.dataProvider.getValues(qu);
				for (Entry<Date, Value> d : values.entrySet()) {
					if (span.contains(d.getKey())) {
						this.timelineDrawer.data.addValue(d.getKey(), qu, d.getValue().toString());
					}
				}
			}
		}
		JsonArray questions = new JsonArray();
		for(Question q : src.getQuestions(this.timelineDrawer.dataProvider)) {
			questions.add(new JsonPrimitive(this.timelineDrawer.questions.getId(q)));
		}
		query.add("questions", questions);
		return query;
	}

}