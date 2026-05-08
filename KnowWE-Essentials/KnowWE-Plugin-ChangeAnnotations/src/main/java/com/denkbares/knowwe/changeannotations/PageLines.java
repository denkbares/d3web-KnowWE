package com.denkbares.knowwe.changeannotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shared line-tokenization for page texts. Both {@link PageAnnotator} and
 * {@link PageAnnotationRenderer} need to agree on what counts as "a line" so the per-line
 * blame array stays index-aligned with what the renderer emits.
 */
final class PageLines {

	private PageLines() {
	}

	/**
	 * Splits {@code text} into visible lines. A single trailing newline is treated as a
	 * line terminator (not a blank line), so {@code "foo\nbar\n"} yields two lines, matching
	 * how editors typically render the same content.
	 */
	static List<String> split(String text) {
		if (text.isEmpty()) return List.of();
		String[] parts = text.split("\\R", -1);
		int len = parts.length;
		if (len > 0 && parts[len - 1].isEmpty()) len--;
		if (len == 0) return List.of();
		return new ArrayList<>(Arrays.asList(parts).subList(0, len));
	}
}
