package com.denkbares.knowwe.changeannotations;

import java.time.Instant;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageAnnotationJsonTest {

	private static final Instant T1 = Instant.parse("2026-01-15T10:30:00Z");
	private static final Instant T2 = Instant.parse("2026-02-20T14:45:00Z");

	private static final PageAnnotation ANNOTATION = new PageAnnotation("Main", 2, List.of(
			new LineBlame(1, 1, "alice", T1, null),
			new LineBlame(2, 2, "bob", T2, "fix typo"),
			new LineBlame(3, 1, "alice", T1, null)));

	@Test
	void serializesEveryLineWithDistinctRevisions() {
		JSONObject json = PageAnnotationJson.toJson(ANNOTATION, "alpha\r\nbeta\ngamma\n", 1, Integer.MAX_VALUE,
				version -> version <= 1 ? null : "Diff.jsp?r1=" + version);

		assertEquals(2, json.getInt("version"));
		assertEquals(1, json.getInt("firstLine"));
		assertEquals(List.of(0, 1, 0), json.getJSONArray("lines").toList());
		assertEquals(List.of("alpha".hashCode(), "beta".hashCode(), "gamma".hashCode()),
				json.getJSONArray("hashes").toList());

		JSONArray revisions = json.getJSONArray("revisions");
		assertEquals(2, revisions.length());
		JSONObject first = revisions.getJSONObject(0);
		assertEquals(1, first.getInt("version"));
		assertEquals("alice", first.getString("author"));
		assertEquals("2026-01-15T10:30:00Z", first.getString("date"));
		assertFalse(first.has("changeNote"));
		assertFalse(first.has("diff"));
		JSONObject second = revisions.getJSONObject(1);
		assertEquals("fix typo", second.getString("changeNote"));
		assertEquals("Diff.jsp?r1=2", second.getString("diff"));
	}

	@Test
	void serializesARangeOfLines() {
		JSONObject json = PageAnnotationJson.toJson(ANNOTATION, "alpha\nbeta\ngamma", 2, 5, DiffLinkBuilder.NONE);

		assertEquals(2, json.getInt("firstLine"));
		assertEquals(List.of(0, 1), json.getJSONArray("lines").toList());
		assertEquals(2, json.getJSONArray("revisions").getJSONObject(0).getInt("version"));
	}

	@Test
	void rejectsTextOfAnotherLineCount() {
		assertThrows(IllegalArgumentException.class,
				() -> PageAnnotationJson.toJson(ANNOTATION, "alpha\nbeta", 1, 10, DiffLinkBuilder.NONE));
	}

	@Test
	void tellsTheLineOfAnOffset() {
		String text = "ab\r\ncd\ne\rf";
		assertEquals(1, PageAnnotationJson.lineAt(text, 0));
		assertEquals(1, PageAnnotationJson.lineAt(text, 3));
		assertEquals(2, PageAnnotationJson.lineAt(text, 4));
		assertEquals(3, PageAnnotationJson.lineAt(text, 7));
		assertEquals(4, PageAnnotationJson.lineAt(text, 9));
	}
}
