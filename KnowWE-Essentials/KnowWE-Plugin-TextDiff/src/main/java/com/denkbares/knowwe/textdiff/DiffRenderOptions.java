package com.denkbares.knowwe.textdiff;

import org.jetbrains.annotations.NotNull;

/**
 * Render options for {@link DiffHtmlRenderer}.
 *
 * @param renderer     who builds the body, the server as a table or the browser as a CodeMirror view
 * @param view         layout to use, the table renderer supports {@link DiffView#UNIFIED} only
 * @param contextLines number of unchanged lines to keep around each change; {@code -1} disables elision
 * @param shadowDom    if {@code true}, wrap output in a declarative shadow root that loads the bundled
 *                     stylesheet; strongly recommended for style isolation
 */
public record DiffRenderOptions(
		@NotNull DiffRenderer renderer,
		@NotNull DiffView view,
		int contextLines,
		boolean shadowDom
) {

	private static final DiffRenderOptions DEFAULT = new DiffRenderOptions(
			DiffRenderer.TABLE,
			DiffView.UNIFIED,
			3,
			true
	);

	/** Sensible defaults: server rendered table, unified view, 3 context lines, shadow DOM enabled. */
	public static DiffRenderOptions defaults() {
		return DEFAULT;
	}

	public DiffRenderOptions withRenderer(DiffRenderer renderer) {
		return new DiffRenderOptions(renderer, view, contextLines, shadowDom);
	}

	public DiffRenderOptions withView(DiffView view) {
		return new DiffRenderOptions(renderer, view, contextLines, shadowDom);
	}

	public DiffRenderOptions withContextLines(int contextLines) {
		return new DiffRenderOptions(renderer, view, contextLines, shadowDom);
	}

	public DiffRenderOptions withShadowDom(boolean shadowDom) {
		return new DiffRenderOptions(renderer, view, contextLines, shadowDom);
	}
}
