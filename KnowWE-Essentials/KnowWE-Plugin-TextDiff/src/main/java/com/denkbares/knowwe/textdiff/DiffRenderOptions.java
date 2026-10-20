package com.denkbares.knowwe.textdiff;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Render options for {@link DiffHtmlRenderer}.
 *
 * @param view         the layout the diff opens in, unless the user chose one for the context
 * @param contextLines number of unchanged lines to keep around each change, {@code -1} shows all lines
 * @param context      a name under which the choices of the user, such as the layout and the whitespace mode, are
 *                     remembered and shared by all diffs of the same context, {@code null} offers no choices
 */
public record DiffRenderOptions(
		@NotNull DiffView view,
		int contextLines,
		@Nullable String context
) {

	public static final int DEFAULT_CONTEXT_LINES = 3;

	private static final DiffRenderOptions DEFAULT = new DiffRenderOptions(DiffView.UNIFIED, DEFAULT_CONTEXT_LINES, null);

	/** Unified view, 3 context lines, no context. */
	public static DiffRenderOptions defaults() {
		return DEFAULT;
	}

	public DiffRenderOptions withView(DiffView view) {
		return new DiffRenderOptions(view, contextLines, context);
	}

	public DiffRenderOptions withContextLines(int contextLines) {
		return new DiffRenderOptions(view, contextLines, context);
	}

	public DiffRenderOptions withContext(@Nullable String context) {
		return new DiffRenderOptions(view, contextLines, context);
	}
}
