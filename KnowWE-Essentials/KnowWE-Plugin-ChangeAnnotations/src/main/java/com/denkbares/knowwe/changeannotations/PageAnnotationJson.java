/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package com.denkbares.knowwe.changeannotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import com.denkbares.strings.Strings;

/**
 * Serializes a {@link PageAnnotation} as JSON for editors that show the annotation next to the text they edit, e.g.
 * as a gutter of the CodeMirror page editor. Unlike {@link PageAnnotationRenderer}, the result carries no page text,
 * only the blame of a range of lines and a hash of each of these lines, so the editor can tell which of its lines
 * still have the text the annotation describes.
 *
 * <p>The result has the version the annotation describes as {@code version}, the 1-based number of the first
 * serialized line of the page as {@code firstLine}, the distinct introducing versions as {@code revisions} (each with
 * {@code version}, {@code author}, {@code date} as ISO instant, the optional {@code changeNote} and the optional
 * {@code diff} url), for every serialized line the index of its revision as {@code lines} and the {@link
 * String#hashCode()} of its text without line terminator as {@code hashes}.
 */
public final class PageAnnotationJson {

	private PageAnnotationJson() {
	}

	/**
	 * Serializes the blame of at most {@code maxLines} lines of the annotated page, starting at line {@code
	 * firstLine}.
	 *
	 * @param annotation  the annotation of the page
	 * @param pageText    the page text the annotation was computed for
	 * @param firstLine   the 1-based page line to start with
	 * @param maxLines    the maximum number of lines to serialize, the lines after the last page line are left out
	 * @param linkBuilder produces the diff url of a version, see {@link DiffLinkBuilder#NONE}
	 * @throws IllegalArgumentException if annotation and page text disagree on the line count
	 */
	@NotNull
	public static JSONObject toJson(@NotNull PageAnnotation annotation, @NotNull String pageText, int firstLine,
									int maxLines, @NotNull DiffLinkBuilder linkBuilder) {
		List<String> texts = PageLines.split(pageText);
		List<LineBlame> blames = annotation.lines();
		if (texts.size() != blames.size()) {
			throw new IllegalArgumentException("Line count mismatch: annotation has " + blames.size()
					+ " entries but the page text has " + texts.size() + " lines.");
		}
		int from = Math.max(0, firstLine - 1);
		int to = (int) Math.min(texts.size(), (long) from + Math.max(0, maxLines));

		JSONArray revisions = new JSONArray();
		JSONArray lines = new JSONArray();
		JSONArray hashes = new JSONArray();
		Map<Integer, Integer> indexByVersion = new HashMap<>();
		for (int i = from; i < to; i++) {
			LineBlame blame = blames.get(i);
			Integer index = indexByVersion.get(blame.introducedInVersion());
			if (index == null) {
				index = revisions.length();
				indexByVersion.put(blame.introducedInVersion(), index);
				revisions.put(revision(blame, linkBuilder));
			}
			lines.put(index);
			hashes.put(texts.get(i).hashCode());
		}

		return new JSONObject()
				.put("version", annotation.currentVersion())
				.put("firstLine", from + 1)
				.put("revisions", revisions)
				.put("lines", lines)
				.put("hashes", hashes);
	}

	/**
	 * Returns the 1-based number of the line that contains {@code offset} of {@code text}, counting line terminators
	 * the way the annotation splits lines.
	 */
	public static int lineAt(@NotNull String text, int offset) {
		int line = 1;
		int end = Math.min(offset, text.length());
		for (int i = 0; i < end; i++) {
			char c = text.charAt(i);
			if (c == '\n' || (c == '\r' && (i + 1 >= text.length() || text.charAt(i + 1) != '\n'))) line++;
		}
		return line;
	}

	private static JSONObject revision(LineBlame blame, DiffLinkBuilder linkBuilder) {
		JSONObject revision = new JSONObject()
				.put("version", blame.introducedInVersion())
				.put("author", blame.author())
				.put("date", blame.date().toString());
		if (Strings.nonBlank(blame.changeNote())) revision.put("changeNote", blame.changeNote());
		String diff = linkBuilder.diffLink(blame.introducedInVersion());
		if (diff != null) revision.put("diff", diff);
		return revision;
	}
}
