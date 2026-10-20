package com.denkbares.knowwe.textdiff;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Checks the fixtures in {@code fixtures/}, which the browser tests of the diff module use as well. Each fixture
 * holds both texts as given, the JSON of {@link DiffTexts}, whose texts are the given ones with normalized
 * line endings, the line counts the browser diff reports, and the chunks {@code [oldPos, oldLen, newPos, newLen]} of
 * the line diff as the browser computes it: over the lines before the final newline of each text, a missing or
 * empty side as no lines, and a missing side diffed as one chunk of all lines of the other side.
 */
class DiffFixturesTest {

	private static final String[] FIXTURES = {
			"blank-lines", "coom-moved-block", "created", "crlf", "deleted", "empty", "identical", "long-line",
			"repeated-lines", "start-and-end", "trailing-newline-added", "trailing-newline-removed", "unicode",
			"whitespace-only", "wiki-table"
	};

	@Test
	void jsonAndChunksMatchEveryFixture() {
		List<Executable> checks = new ArrayList<>();
		for (String name : FIXTURES) checks.add(() -> check(name));
		assertAll(checks);
	}

	private static void check(String name) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode fixture;
		try (InputStream in = DiffFixturesTest.class.getResourceAsStream("fixtures/" + name + ".json")) {
			assertNotNull(in, name);
			fixture = mapper.readTree(in);
		}
		String oldText = text(fixture.get("old"));
		String newText = text(fixture.get("new"));
		TextDiff diff = new TextDiff(oldText, newText);

		JsonNode json = mapper.readTree(diff.texts().toJson());
		assertEquals(normalize(oldText), text(json.get("old")), name);
		assertEquals(normalize(newText), text(json.get("new")), name);
		assertEquals(fixture.get("chunks"), mapper.valueToTree(chunksAsShown(diff)), name);
	}

	private static String normalize(String text) {
		return text == null ? null : text.replace("\r\n", "\n").replace("\r", "\n");
	}

	private static String text(JsonNode node) {
		return node.isNull() ? null : node.asText();
	}

	private static List<int[]> chunksAsShown(TextDiff diff) {
		List<int[]> chunks = new ArrayList<>();
		if (diff.oldText() == null && diff.newText() == null) return chunks;
		if (diff.oldText() == null) {
			chunks.add(new int[] { 0, 0, 0, lineCount(diff.newText()) });
			return chunks;
		}
		if (diff.newText() == null) {
			chunks.add(new int[] { 0, lineCount(diff.oldText()), 0, 0 });
			return chunks;
		}
		if (diff.oldText().isEmpty() || diff.newText().isEmpty()) {
			chunks.add(new int[] { 0, lineCount(diff.oldText()), 0, lineCount(diff.newText()) });
			chunks.removeIf(chunk -> chunk[1] == 0 && chunk[3] == 0);
			return chunks;
		}
		TextDiff shown = new TextDiff(withoutFinalNewline(diff.oldText()), withoutFinalNewline(diff.newText()));
		int oldPos = 0;
		int newPos = 0;
		int[] current = null;
		for (TextDiff.Line line : shown.lines()) {
			switch (line.status()) {
				case COMMON -> {
					oldPos++;
					newPos++;
					current = null;
				}
				case REMOVED -> {
					if (current == null) chunks.add(current = new int[] { oldPos, 0, newPos, 0 });
					current[1]++;
					oldPos++;
				}
				case ADDED -> {
					if (current == null) chunks.add(current = new int[] { oldPos, 0, newPos, 0 });
					current[3]++;
					newPos++;
				}
			}
		}
		return chunks;
	}

	// lines of a text as the browser counts them, without the empty line after a final newline
	private static int lineCount(String text) {
		if (text.isEmpty()) return 0;
		return withTrailingNewline(text).split("\n", -1).length - 1;
	}

	private static String withTrailingNewline(String text) {
		return text.isEmpty() || text.endsWith("\n") ? text : text + "\n";
	}

	private static String withoutFinalNewline(String text) {
		return text.endsWith("\n") ? text.substring(0, text.length() - 1) : text;
	}
}
