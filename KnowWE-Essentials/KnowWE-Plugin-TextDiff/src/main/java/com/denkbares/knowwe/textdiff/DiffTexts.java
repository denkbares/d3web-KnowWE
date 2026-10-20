package com.denkbares.knowwe.textdiff;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Both texts of a diff with normalized line endings, as the browser diffs them. A missing side is null and means a
 * created or a deleted file.
 */
public record DiffTexts(@Nullable String oldText, @Nullable String newText) {

	private static final JsonFactory FACTORY = new JsonFactory();

	public DiffTexts {
		oldText = TextDiff.normalize(oldText);
		newText = TextDiff.normalize(newText);
	}

	/**
	 * The JSON document a {@code <knowwe-text-diff>} consumes, {@code {"old": "...", "new": "..."}}. It never
	 * contains a {@code <} character, so it can be inlined into a script element as it is.
	 */
	@NotNull
	public String toJson() {
		StringWriter out = new StringWriter();
		try (JsonGenerator json = FACTORY.createGenerator(out)) {
			json.writeStartObject();
			json.writeStringField("old", oldText);
			json.writeStringField("new", newText);
			json.writeEndObject();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		// a less-than sign only occurs inside string values, where its escape is equivalent
		return out.toString().replace("<", "\\u003c");
	}
}
