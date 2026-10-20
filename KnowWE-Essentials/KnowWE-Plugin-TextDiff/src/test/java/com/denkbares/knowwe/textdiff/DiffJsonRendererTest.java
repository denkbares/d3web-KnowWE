package com.denkbares.knowwe.textdiff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffJsonRendererTest {

	private static JsonNode render(String oldText, String newText) throws Exception {
		return new ObjectMapper().readTree(DiffJsonRenderer.render(new TextDiff(oldText, newText)));
	}

	private static void assertStats(JsonNode json, int added, int removed) {
		assertEquals(added, json.get("stats").get("added").asInt(), json.toString());
		assertEquals(removed, json.get("stats").get("removed").asInt(), json.toString());
	}

	@Test
	void createdFileHasNullOldSide() throws Exception {
		JsonNode json = render(null, "a\nb\n");
		assertTrue(json.get("old").isNull());
		assertEquals("a\nb\n", json.get("new").asText());
		assertStats(json, 2, 0);
	}

	@Test
	void deletedFileHasNullNewSide() throws Exception {
		JsonNode json = render("a\nb", null);
		assertEquals("a\nb", json.get("old").asText());
		assertTrue(json.get("new").isNull());
		assertStats(json, 0, 2);
	}

	@Test
	void emptyDiffHasBothSidesNullAndNoChanges() throws Exception {
		JsonNode json = render(null, null);
		assertTrue(json.get("old").isNull());
		assertTrue(json.get("new").isNull());
		assertStats(json, 0, 0);
	}

	@Test
	void emptyTextsAreEmptyStrings() throws Exception {
		JsonNode json = render("", "");
		assertEquals("", json.get("old").asText());
		assertEquals("", json.get("new").asText());
		assertStats(json, 0, 0);
	}

	@Test
	void trailingNewlineOnOneSideOnlyIsNoChange() throws Exception {
		assertStats(render("a\nb", "a\nb\n"), 0, 0);
		assertStats(render("a\nb\n", "a\nb"), 0, 0);
		assertStats(render("a\nb", "a\nc\n"), 1, 1);
	}

	@Test
	void lineEndingsAreNormalized() throws Exception {
		JsonNode json = render("a\r\nb\rc", "a\r\nB\nc");
		assertEquals("a\nb\nc", json.get("old").asText());
		assertEquals("a\nB\nc", json.get("new").asText());
		assertStats(json, 1, 1);
	}

	@Test
	void outputIsSafeInsideAScriptElement() throws Exception {
		String json = DiffJsonRenderer.render(new TextDiff("</script><!-- x", "<b>"));
		assertFalse(json.contains("<"), json);
		JsonNode parsed = new ObjectMapper().readTree(json);
		assertEquals("</script><!-- x", parsed.get("old").asText());
		assertEquals("<b>", parsed.get("new").asText());
	}
}
