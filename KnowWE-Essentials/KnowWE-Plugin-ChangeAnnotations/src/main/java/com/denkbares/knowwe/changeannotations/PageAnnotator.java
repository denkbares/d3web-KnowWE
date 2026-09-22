package com.denkbares.knowwe.changeannotations;

import java.util.ArrayList;
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
		// only the normalized form of a version is ever compared, and it carries from step to step because the
		// current version of a step was the next version of the step before
		List<String> currentNormalized = normalizeAll(PageLines.split(first.text()));
		List<LineBlame> currentBlames = initialBlames(first, currentNormalized.size());

		for (int idx = 1; idx < sorted.size(); idx++) {
			VersionEntry next = sorted.get(idx);
			List<String> nextNormalized = normalizeAll(PageLines.split(next.text()));
			currentBlames = stepForward(currentBlames, currentNormalized, nextNormalized, next);
			currentNormalized = nextNormalized;
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
			List<String> currentNormalized,
			List<String> nextNormalized,
			VersionEntry nextVersion) {
		Patch<String> patch = DiffUtils.diff(currentNormalized, nextNormalized);
		List<LineBlame> result = new ArrayList<>(nextNormalized.size());

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
		while (ai < currentNormalized.size() && bi < nextNormalized.size()) {
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
		StringBuilder collapsed = new StringBuilder(line.length());
		boolean inWhitespace = false;
		for (int i = 0; i < line.length(); i++) {
			char character = line.charAt(i);
			if (isWhitespace(character)) {
				if (!inWhitespace) {
					collapsed.append(' ');
					inWhitespace = true;
				}
			}
			else {
				collapsed.append(character);
				inWhitespace = false;
			}
		}
		return collapsed.toString().trim();
	}

	/**
	 * The characters a regular expression counts as whitespace. Every line of every version of a page passes through
	 * here, and doing it with a regular expression cost more than the diff the comparison feeds.
	 */
	private static boolean isWhitespace(char character) {
		return character == ' ' || character == '\t' || character == '\n'
				|| character == '\u000B' || character == '\f' || character == '\r';
	}

}
