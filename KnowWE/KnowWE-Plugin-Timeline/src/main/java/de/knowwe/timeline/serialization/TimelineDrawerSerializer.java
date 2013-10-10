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

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
class TimelineDrawerSerializer implements JsonSerializer<TimelineDrawer> {

	@Override
	public JsonElement serialize(TimelineDrawer src, Type srcType, JsonSerializationContext context) {
		JsonObject tlDrawer = new JsonObject();
		tlDrawer.add("sourceId", new JsonPrimitive(src.sourceID));
		tlDrawer.add("queries", context.serialize(src.queries));
		JsonArray dates = new JsonArray();
		for (Date d : src.dates) {
			JsonObject dateObject = new JsonObject();
			dateObject.add("time", new JsonPrimitive(d.getTime()));
			JsonObject valuesD = new JsonObject();
			SortedMap<Question, String> valuesX = src.data.getValues(d);
			if (valuesX != null) {
				for (Entry<Question, String> entry : valuesX.entrySet()) {
					int questionId = src.questions.getId(entry.getKey());
					valuesD.add(String.valueOf(questionId), new JsonPrimitive(entry.getValue()));
				}
			}
			dateObject.add("values", valuesD);
			dates.add(dateObject);
		}
		tlDrawer.add("dates", dates);
		tlDrawer.add("questions", context.serialize(src.questions));
		tlDrawer.add("errors", context.serialize(src.errors));
		return tlDrawer;
	}
}