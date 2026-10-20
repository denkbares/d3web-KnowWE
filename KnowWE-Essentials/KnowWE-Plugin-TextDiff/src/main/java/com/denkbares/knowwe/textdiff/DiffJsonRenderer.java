package com.denkbares.knowwe.textdiff;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Writes a {@link TextDiff} as the JSON document the browser renderer consumes. The document carries both texts
 * with normalized line endings, null for a missing side, and the added and removed line counts.
 *
 * <pre>
 * {"old": "...", "new": "...", "stats": {"added": 7, "removed": 10}}
 * </pre>
 *
 * The output never contains a {@code <} character, so it can be inlined into a script element as it is.
 */
public final class DiffJsonRenderer {

	private static final JsonFactory FACTORY = new JsonFactory();

	private DiffJsonRenderer() {
	}

	@NotNull
	public static String render(@NotNull TextDiff diff) {
		TextDiff.Stats stats = statsAsShown(diff);
		StringWriter out = new StringWriter();
		try (JsonGenerator json = FACTORY.createGenerator(out)) {
			json.writeStartObject();
			json.writeStringField("old", diff.oldText());
			json.writeStringField("new", diff.newText());
			json.writeObjectFieldStart("stats");
			json.writeNumberField("added", stats.added());
			json.writeNumberField("removed", stats.removed());
			json.writeEndObject();
			json.writeEndObject();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		// a less-than sign only occurs inside string values, where its escape is equivalent
		return out.toString().replace("<", "\\u003c");
	}

	// the browser diffs both texts with a trailing newline and a missing side as the empty text, so a newline that
	// only one side ends with is not a change there, while the line model counts the trailing empty line
	private static TextDiff.Stats statsAsShown(TextDiff diff) {
		String oldText = diff.oldText();
		String newText = diff.newText();
		if (oldText != null && newText != null && terminated(oldText) == terminated(newText)) {
			return diff.stats();
		}
		return new TextDiff(withTrailingNewline(oldText), withTrailingNewline(newText)).stats();
	}

	private static boolean terminated(String text) {
		return text.isEmpty() || text.endsWith("\n");
	}

	private static String withTrailingNewline(String text) {
		return text == null || terminated(text) ? (text == null ? "" : text) : text + "\n";
	}
}
