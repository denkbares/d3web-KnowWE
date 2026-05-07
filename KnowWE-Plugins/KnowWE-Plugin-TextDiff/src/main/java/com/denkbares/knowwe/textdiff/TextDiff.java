package com.denkbares.knowwe.textdiff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.Patch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Line-by-line diff between two texts. Line endings are normalized ({@code \r\n} and {@code \r} → {@code \n}),
 * a {@code null} side is treated as if the entire other side was added/removed.
 */
public final class TextDiff implements Iterable<TextDiff.Line> {

	private final List<Line> lines;

	/**
	 * One line in the diff. Line numbers are 1-based; {@code -1} means the line is absent on that side.
	 */
	public record Line(
			int oldLineNumber,
			int newLineNumber,
			@NotNull Status status,
			@NotNull String text
	) {
		public enum Status {
			/** Present unchanged on both sides. */
			COMMON,
			/** Present only on the new side. */
			ADDED,
			/** Present only on the old side. */
			REMOVED
		}
	}

	/** Computes the diff between two texts. Either side may be {@code null}. */
	public TextDiff(@Nullable String oldText, @Nullable String newText) {
		this.lines = diff(oldText, newText);
	}

	private TextDiff(@NotNull List<Line> lines) {
		this.lines = lines;
	}

	/** A diff with no lines — convenient for "no input" branches. */
	public static TextDiff empty() {
		return new TextDiff(Collections.emptyList());
	}

	/** The diff lines in display order (old-side common lines, then deltas, in source order). */
	@NotNull
	public List<Line> lines() {
		return lines;
	}

	@Override
	@NotNull
	public Iterator<Line> iterator() {
		return lines.iterator();
	}

	@NotNull
	private List<Line> diff(@Nullable String oldText, @Nullable String newText) {
		if (oldText == null && newText == null) {
			return Collections.emptyList();
		}
		if (oldText == null) {
			return allOnSide(newText, Line.Status.ADDED);
		}
		if (newText == null) {
			return allOnSide(oldText, Line.Status.REMOVED);
		}

		List<String> a = splitLines(oldText);
		List<String> b = splitLines(newText);

		Patch<String> patch = DiffUtils.diff(a, b);

		List<Line> result = new ArrayList<>();
		int ai = 0, bi = 0;
		for (AbstractDelta<String> delta : patch.getDeltas()) {
			Chunk<String> source = delta.getSource();
			Chunk<String> target = delta.getTarget();
			int aPos = source.getPosition();
			int bPos = target.getPosition();

			while (ai < aPos && bi < bPos) {
				result.add(new Line(ai + 1, bi + 1, Line.Status.COMMON, b.get(bi)));
				ai++;
				bi++;
			}

			switch (delta.getType()) {
				case DELETE -> {
					for (String line : source.getLines()) {
						result.add(new Line(ai + 1, -1, Line.Status.REMOVED, line));
						ai++;
					}
				}
				case INSERT -> {
					for (String line : target.getLines()) {
						result.add(new Line(-1, bi + 1, Line.Status.ADDED, line));
						bi++;
					}
				}
				case CHANGE -> {
					for (String line : source.getLines()) {
						result.add(new Line(ai + 1, -1, Line.Status.REMOVED, line));
						ai++;
					}
					for (String line : target.getLines()) {
						result.add(new Line(-1, bi + 1, Line.Status.ADDED, line));
						bi++;
					}
				}
				case EQUAL -> {
					for (String line : source.getLines()) {
						result.add(new Line(ai + 1, bi + 1, Line.Status.COMMON, line));
						ai++;
						bi++;
					}
				}
			}
		}

		while (ai < a.size() && bi < b.size()) {
			result.add(new Line(ai + 1, bi + 1, Line.Status.COMMON, b.get(bi)));
			ai++;
			bi++;
		}

		return result;
	}

	private static List<Line> allOnSide(String text, Line.Status status) {
		List<String> lines = splitLines(text);
		List<Line> out = new ArrayList<>(lines.size());
		for (int i = 0; i < lines.size(); i++) {
			int oldNo = status == Line.Status.REMOVED ? i + 1 : -1;
			int newNo = status == Line.Status.ADDED ? i + 1 : -1;
			out.add(new Line(oldNo, newNo, status, lines.get(i)));
		}
		return out;
	}

	private static List<String> splitLines(String text) {
		String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
		return new ArrayList<>(Arrays.asList(normalized.split("\n", -1)));
	}
}
