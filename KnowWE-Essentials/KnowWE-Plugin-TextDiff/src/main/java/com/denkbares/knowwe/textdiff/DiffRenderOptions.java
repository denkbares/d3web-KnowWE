package com.denkbares.knowwe.textdiff;

import org.jetbrains.annotations.NotNull;

/**
 * Render options for {@link DiffHtmlRenderer}.
 *
 * @param view         layout to use; only {@link DiffView#UNIFIED} is supported today
 * @param contextLines number of unchanged lines to keep around each change; {@code -1} disables elision
 * @param shadowDom    if {@code true}, wrap output in a declarative shadow root that loads the bundled
 *                     stylesheet — strongly recommended for style isolation
 */
public record DiffRenderOptions(
		@NotNull DiffView view,
		int contextLines,
		boolean shadowDom
) {

	private static final DiffRenderOptions DEFAULT = new DiffRenderOptions(
			DiffView.UNIFIED,
			3,
			true
	);

	/** Sensible defaults: unified view, 3 context lines, shadow DOM enabled. */
	public static DiffRenderOptions defaults() {
		return DEFAULT;
	}

	public DiffRenderOptions withView(DiffView view) {
		return new DiffRenderOptions(view, contextLines, shadowDom);
	}

	public DiffRenderOptions withContextLines(int contextLines) {
		return new DiffRenderOptions(view, contextLines, shadowDom);
	}

	public DiffRenderOptions withShadowDom(boolean shadowDom) {
		return new DiffRenderOptions(view, contextLines, shadowDom);
	}
}
