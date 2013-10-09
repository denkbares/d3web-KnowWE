package de.knowwe.timeline.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.d3web.core.knowledge.terminology.Question;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
class QuestionSerializer implements JsonSerializer<Question> {

	@Override
	public JsonElement serialize(Question src, Type srcType, JsonSerializationContext context) {
		return new JsonPrimitive(src.getName());
	}
}