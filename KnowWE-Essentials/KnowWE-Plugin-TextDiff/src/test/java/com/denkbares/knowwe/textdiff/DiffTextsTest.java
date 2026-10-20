package com.denkbares.knowwe.textdiff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffTextsTest {

	private static JsonNode json(String oldText, String newText) throws Exception {
		return new ObjectMapper().readTree(new DiffTexts(oldText, newText).toJson());
	}

	@Test
	void createdFileHasNullOldSide() throws Exception {
		JsonNode json = json(null, "a\nb\n");
		assertTrue(json.get("old").isNull());
		assertEquals("a\nb\n", json.get("new").asText());
	}

	@Test
	void deletedFileHasNullNewSide() throws Exception {
		JsonNode json = json("a\nb", null);
		assertEquals("a\nb", json.get("old").asText());
		assertTrue(json.get("new").isNull());
	}

	@Test
	void emptyTextsAreEmptyStrings() throws Exception {
		JsonNode json = json("", "");
		assertEquals("", json.get("old").asText());
		assertEquals("", json.get("new").asText());
	}

	@Test
	void lineEndingsAreNormalized() throws Exception {
		JsonNode json = json("a\r\nb\rc", "a\r\nB\nc");
		assertEquals("a\nb\nc", json.get("old").asText());
		assertEquals("a\nB\nc", json.get("new").asText());
	}

	@Test
	void outputIsSafeInsideAScriptElement() throws Exception {
		String json = new DiffTexts("</script><!-- x", "<b>").toJson();
		assertFalse(json.contains("<"), json);
		JsonNode parsed = new ObjectMapper().readTree(json);
		assertEquals("</script><!-- x", parsed.get("old").asText());
		assertEquals("<b>", parsed.get("new").asText());
	}
}
