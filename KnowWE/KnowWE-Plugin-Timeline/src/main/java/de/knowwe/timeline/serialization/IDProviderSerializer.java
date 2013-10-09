package de.knowwe.timeline.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
class IDProviderSerializer implements JsonSerializer<IDProvider> {

	@Override
	public JsonElement serialize(IDProvider src, Type typeOfSrc, JsonSerializationContext context) {
		return context.serialize(src.getIds());
	}

}
