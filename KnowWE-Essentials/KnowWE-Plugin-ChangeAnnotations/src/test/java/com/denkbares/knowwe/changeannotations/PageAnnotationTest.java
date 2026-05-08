package com.denkbares.knowwe.changeannotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageAnnotationTest {

	@Test
	void copiesLinesDefensively() {
		List<LineBlame> source = new ArrayList<>();
		source.add(new LineBlame(1, 1, "a", Instant.EPOCH, null));

		PageAnnotation annotation = new PageAnnotation("Main", 1, source);
		source.add(new LineBlame(2, 1, "a", Instant.EPOCH, null));

		assertEquals(1, annotation.lines().size(), "external mutation must not leak into the record");
	}

	@Test
	void linesAreImmutable() {
		PageAnnotation annotation = new PageAnnotation("Main", 1,
				List.of(new LineBlame(1, 1, "a", Instant.EPOCH, null)));

		assertThrows(UnsupportedOperationException.class,
				() -> annotation.lines().add(new LineBlame(2, 1, "a", Instant.EPOCH, null)));
	}

	@Test
	void rejectsCurrentVersionBelowOne() {
		assertThrows(IllegalArgumentException.class,
				() -> new PageAnnotation("Main", 0, List.of()));
	}
}
