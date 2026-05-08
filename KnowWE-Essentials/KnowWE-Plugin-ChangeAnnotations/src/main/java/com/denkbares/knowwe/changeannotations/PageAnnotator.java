package com.denkbares.knowwe.changeannotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

/**
 * Computes a per-line {@link PageAnnotation} ("blame") for a wiki page from a list of
 * {@link VersionEntry}s — pure logic, no JSPWiki dependency, so it can be exercised with
 * synthetic histories in unit tests.
 *
 * <p>The algorithm walks the versions in ascending order, line-diffs each step
 * {@code vi → vi+1} via {@code java-diff-utils}, and forwards the existing
 * {@link LineBlame} for unchanged lines. Inserted (or changed-into) lines receive a fresh
 * blame that points at {@code vi+1}'s author, date, and change note.
 *
 * <p>Whitespace-only changes are ignored: the diff input is built from
 * {@link #normalizeForBlameComparison(String) whitespace-normalized} line texts, so two
 * lines that differ only in spacing are treated as equal and keep their existing blame.
 * That mirrors {@code git blame -w} and is the default per the project plan.
 */
public final class PageAnnotator {

	private PageAnnotator() {
	}

	/**
	 * Annotates {@code pageName} from the given history. Versions are processed in ascending
	 * version order; the resulting {@link PageAnnotation} describes the latest version.
	 *
	 * @throws IllegalArgumentException if {@code versions} is empty
	 */
	public static PageAnnotation annotate(String pageName, List<VersionEntry> versions) {
		if (pageName == null) throw new NullPointerException("pageName");
		if (versions == null) throw new NullPointerException("versions");
		if (versions.isEmpty()) {
			throw new IllegalArgumentException("versions must not be empty");
		}

		List<VersionEntry> sorted = new ArrayList<>(versions);
		sorted.sort(Comparator.comparingInt(VersionEntry::version));

		VersionEntry first = sorted.get(0);
		List<String> currentLines = splitLines(first.text());
		List<LineBlame> currentBlames = initialBlames(first, currentLines.size());

		for (int idx = 1; idx < sorted.size(); idx++) {
			VersionEntry next = sorted.get(idx);
			List<String> nextLines = splitLines(next.text());
			currentBlames = stepForward(currentBlames, currentLines, nextLines, next);
			currentLines = nextLines;
		}

		VersionEntry latest = sorted.get(sorted.size() - 1);
		return new PageAnnotation(pageName, latest.version(), currentBlames);
	}

	private static List<LineBlame> initialBlames(VersionEntry version, int lineCount) {
		List<LineBlame> blames = new ArrayList<>(lineCount);
		for (int i = 0; i < lineCount; i++) {
			blames.add(new LineBlame(i + 1, version.version(), version.author(),
					version.date(), version.changeNote()));
		}
		return blames;
	}

	private static List<LineBlame> stepForward(
			List<LineBlame> currentBlames,
			List<String> currentLines,
			List<String> nextLines,
			VersionEntry nextVersion) {
		Patch<String> patch = DiffUtils.diff(normalizeAll(currentLines), normalizeAll(nextLines));
		List<LineBlame> result = new ArrayList<>(nextLines.size());

		int ai = 0;
		int bi = 0;
		for (AbstractDelta<String> delta : patch.getDeltas()) {
			int aPos = delta.getSource().getPosition();
			int bPos = delta.getTarget().getPosition();
			// Common prefix between previous delta (or start) and this delta — carry blame forward.
			while (ai < aPos && bi < bPos) {
				result.add(reblame(currentBlames.get(ai), bi + 1));
				ai++;
				bi++;
			}
			switch (delta.getType()) {
				case DELETE -> ai += delta.getSource().getLines().size();
				case INSERT -> {
					int inserted = delta.getTarget().getLines().size();
					for (int i = 0; i < inserted; i++) {
						result.add(blameFor(nextVersion, bi + 1));
						bi++;
					}
				}
				case CHANGE -> {
					ai += delta.getSource().getLines().size();
					int inserted = delta.getTarget().getLines().size();
					for (int i = 0; i < inserted; i++) {
						result.add(blameFor(nextVersion, bi + 1));
						bi++;
					}
				}
				case EQUAL -> {
					// java-diff-utils treats an EQUAL delta the same as a non-delta gap, but we
					// still handle it defensively in case the upstream behavior changes.
					int equal = delta.getSource().getLines().size();
					for (int i = 0; i < equal; i++) {
						result.add(reblame(currentBlames.get(ai), bi + 1));
						ai++;
						bi++;
					}
				}
			}
		}
		// Common suffix after the last delta.
		while (ai < currentLines.size() && bi < nextLines.size()) {
			result.add(reblame(currentBlames.get(ai), bi + 1));
			ai++;
			bi++;
		}
		return result;
	}

	private static LineBlame reblame(LineBlame existing, int newLineNumber) {
		return new LineBlame(newLineNumber, existing.introducedInVersion(),
				existing.author(), existing.date(), existing.changeNote());
	}

	private static LineBlame blameFor(VersionEntry version, int lineNumber) {
		return new LineBlame(lineNumber, version.version(), version.author(),
				version.date(), version.changeNote());
	}

	private static List<String> normalizeAll(List<String> lines) {
		List<String> normalized = new ArrayList<>(lines.size());
		for (String line : lines) {
			normalized.add(normalizeForBlameComparison(line));
		}
		return normalized;
	}

	/**
	 * Whitespace-folding key used to decide whether two lines are "the same" for blame
	 * purposes. Collapses any run of whitespace to a single space and trims the ends — so a
	 * pure indentation change does not invalidate the existing blame.
	 */
	static String normalizeForBlameComparison(String line) {
		return line.replaceAll("\\s+", " ").trim();
	}

	/**
	 * Splits {@code text} into its visible lines. A single trailing newline is treated as a
	 * line terminator (not a blank line), so {@code "foo\nbar\n"} yields two lines, matching
	 * how editors typically render the same content.
	 */
	private static List<String> splitLines(String text) {
		if (text.isEmpty()) return List.of();
		String[] parts = text.split("\\R", -1);
		int len = parts.length;
		if (len > 0 && parts[len - 1].isEmpty()) len--;
		if (len == 0) return List.of();
		return new ArrayList<>(Arrays.asList(parts).subList(0, len));
	}
}
