package de.knowwe.timeline.serialization;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
class DateSerializer implements JsonSerializer<Date> {

	private final IDProvider<Date> dates;

	DateSerializer(IDProvider<Date> dates) {
		this.dates = dates;
	}

	@Override
	public JsonElement serialize(Date src, Type srcType, JsonSerializationContext context) {
		return new JsonPrimitive(this.dates.getId(src));
	}
}