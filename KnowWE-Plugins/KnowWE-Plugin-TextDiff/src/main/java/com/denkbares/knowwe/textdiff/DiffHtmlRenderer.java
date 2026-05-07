package com.denkbares.knowwe.textdiff;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;

/**
 * Renders {@link TextDiff} and file/article changes to the TextDiff web components.
 * The text diff output is self-contained: with {@link DiffRenderOptions#shadowDom() shadowDom} enabled
 * (default) it ships its own stylesheet inside a declarative shadow root.
 *
 * <p>Inner elements are tagged with {@code part} attributes ({@code line}, {@code line added},
 * {@code num old}, {@code text}, ...) so host pages can override styling via
 * {@code knowwe-text-diff::part(...)}. Theme tokens are exposed as {@code --diff-*} CSS
 * custom properties (see the bundled stylesheet).
 */
public final class DiffHtmlRenderer {

	private static final int MIN_ELIDE = 2;
	private static final String TEXT_DIFF_STYLESHEET = "KnowWEExtension/css/KnowWE-Plugin-TextDiff.css";

	/**
	 * Octicons-style "unfold" icon (16×16) — vertical chevrons with a dotted divider, signalling
	 * "expand collapsed context". Rendered inline so the button stays self-contained.
	 */
	private static final String EXPAND_ICON_SVG = """
			<svg class="icon" viewBox="0 0 16 16" width="16" height="16" fill="currentColor" aria-hidden="true">\
			<path fill-rule="evenodd" d="M8.177.677l2.896 2.896a.25.25 0 0 1-.177.427H8.75v1.25a.75.75 0 0 1-1.5 0V4H5.104a.25.25 0 0 1-.177-.427L7.823.677a.25.25 0 0 1 .354 0zM7.25 10.75a.75.75 0 0 1 1.5 0V12h2.146a.25.25 0 0 1 .177.427l-2.896 2.896a.25.25 0 0 1-.354 0L4.927 12.427A.25.25 0 0 1 5.104 12H7.25v-1.25zm-5-2a.75.75 0 0 0 0-1.5h-.5a.75.75 0 0 0 0 1.5h.5zM6 8a.75.75 0 0 1-.75.75h-.5a.75.75 0 0 1 0-1.5h.5A.75.75 0 0 1 6 8zm2.25.75a.75.75 0 0 0 0-1.5h-.5a.75.75 0 0 0 0 1.5h.5zM12 8a.75.75 0 0 1-.75.75h-.5a.75.75 0 0 1 0-1.5h.5A.75.75 0 0 1 12 8zm2.25.75a.75.75 0 0 0 0-1.5h-.5a.75.75 0 0 0 0 1.5h.5z"/>\
			</svg>""";

	private DiffHtmlRenderer() {
	}

	/**
	 * Renders {@code diff} as a {@code <knowwe-text-diff>} with {@link DiffRenderOptions#defaults() default options}.
	 */
	@NotNull
	public static String renderTextDiff(@NotNull TextDiff diff) {
		return renderTextDiff(diff, DiffRenderOptions.defaults());
	}

	/**
	 * Renders {@code diff} as a {@code <knowwe-text-diff>} with the given options.
	 */
	@NotNull
	public static String renderTextDiff(@NotNull TextDiff diff, @NotNull DiffRenderOptions options) {
		if (options.view() == DiffView.SIDE_BY_SIDE) {
			throw new UnsupportedOperationException("SIDE_BY_SIDE view is not yet implemented");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<knowwe-text-diff data-view=\"unified\" data-context-lines=\"")
				.append(options.contextLines()).append("\">");
		if (options.shadowDom()) {
			sb.append("<template shadowrootmode=\"open\">");
			appendTextDiffShadowContent(sb, diff, options, true);
			sb.append("</template>");
		}
		else {
			appendTextDiffShadowContent(sb, diff, options, false);
		}
		sb.append("</knowwe-text-diff>");
		return sb.toString();
	}

	/**
	 * Renders {@code change} as a {@code <knowwe-file-change>} with an embedded
	 * {@code <knowwe-text-diff>} when {@link FileChange#diff()} is present.
	 */
	@NotNull
	public static String renderFileChange(@NotNull FileChange change) {
		return renderFileChange(change, DiffRenderOptions.defaults());
	}

	/**
	 * Renders {@code change} as a {@code <knowwe-file-change>} with the given diff options.
	 * Additions/deletions are derived from the diff model.
	 */
	@NotNull
	public static String renderFileChange(@NotNull FileChange change, @NotNull DiffRenderOptions options) {
		StringBuilder sb = new StringBuilder();
		sb.append("<knowwe-file-change");
		appendAttribute(sb, "data-change", change.changeType().htmlValue());
		appendAttribute(sb, "data-old-name", change.oldName());
		appendAttribute(sb, "data-new-name", change.newName());
		appendAttribute(sb, "data-url", change.url());
		appendAttribute(sb, "data-old-url", change.oldUrl());
		appendAttribute(sb, "data-new-url", change.newUrl());

		TextDiff diff = change.diff();
		if (diff != null) {
			ChangeStats stats = ChangeStats.from(diff);
			if (stats.additions() > 0) appendAttribute(sb, "data-additions", Integer.toString(stats.additions()));
			if (stats.deletions() > 0) appendAttribute(sb, "data-deletions", Integer.toString(stats.deletions()));
		}
		if (change.collapsed()) {
			sb.append(" data-collapsed");
		}
		sb.append(">");
		if (diff != null) {
			sb.append(renderTextDiff(diff, options));
		}
		sb.append("</knowwe-file-change>");
		return sb.toString();
	}

	/**
	 * Renders the inner shadow-root content only — stylesheet link plus the diff table — without
	 * the {@code <knowwe-text-diff>} host element or {@code <template>} wrapper. Intended for
	 * server endpoints that respond to the JS component's lazy-load fetch.
	 */
	@NotNull
	static String renderTextDiffShadowContent(@NotNull TextDiff diff, @NotNull DiffRenderOptions options) {
		if (options.view() == DiffView.SIDE_BY_SIDE) {
			throw new UnsupportedOperationException("SIDE_BY_SIDE view is not yet implemented");
		}
		StringBuilder sb = new StringBuilder();
		appendTextDiffShadowContent(sb, diff, options, true);
		return sb.toString();
	}

	private static void appendTextDiffShadowContent(StringBuilder sb, TextDiff diff, DiffRenderOptions options, boolean includeStylesheet) {
		if (includeStylesheet) {
			appendStylesheetLink(sb, TEXT_DIFF_STYLESHEET);
		}
		sb.append("<div class=\"diff-frame\" part=\"frame\">");

		List<TextDiff.Line> lines = diff.lines();
		LineNumberMode lineNumberMode = determineLineNumberMode(lines);
		sb.append("<table class=\"diff ").append(lineNumberMode.cssClass()).append("\"><tbody>");
		boolean[] visible = computeVisibility(lines, options.contextLines());

		int i = 0;
		while (i < lines.size()) {
			if (visible[i]) {
				appendDiffLineRow(sb, lines.get(i), lineNumberMode);
				i++;
			}
			else {
				int start = i;
				while (i < lines.size() && !visible[i]) {
					i++;
				}
				int end = i;
				int hidden = end - start;
				if (hidden < MIN_ELIDE) {
					for (int j = start; j < end; j++) {
						appendDiffLineRow(sb, lines.get(j), lineNumberMode);
					}
				}
				else {
					sb.append("<tr class=\"elided\" part=\"elided\" data-hidden-count=\"").append(hidden)
							.append("\"><td colspan=\"").append(lineNumberMode.columnCount())
							.append("\"><button class=\"expand\" part=\"expand-button\" type=\"button\" aria-label=\"Show ")
							.append(hidden).append(" unchanged lines\">")
							.append(EXPAND_ICON_SVG)
							.append("<span class=\"label\" part=\"expand-label\">Show ")
							.append(hidden).append(" unchanged lines</span>")
							.append("</button></td></tr>");
					for (int j = start; j < end; j++) {
						appendHiddenDiffLineRow(sb, lines.get(j), lineNumberMode);
					}
				}
			}
		}

		sb.append("</tbody></table>");
		sb.append("</div>");
	}

	private static void appendStylesheetLink(StringBuilder sb, String href) {
		sb.append("<link rel=\"stylesheet\" href=\"").append(href).append("\">");
	}

	private static LineNumberMode determineLineNumberMode(List<TextDiff.Line> lines) {
		if (lines.isEmpty()) return LineNumberMode.BOTH;
		boolean hasOld = false;
		boolean hasNew = false;
		for (TextDiff.Line line : lines) {
			hasOld |= line.oldLineNumber() > 0;
			hasNew |= line.newLineNumber() > 0;
			if (hasOld && hasNew) return LineNumberMode.BOTH;
		}
		if (hasOld) return LineNumberMode.OLD_ONLY;
		if (hasNew) return LineNumberMode.NEW_ONLY;
		return LineNumberMode.BOTH;
	}

	private static boolean[] computeVisibility(List<TextDiff.Line> lines, int contextLines) {
		boolean[] visible = new boolean[lines.size()];
		if (contextLines < 0) {
			Arrays.fill(visible, true);
			return visible;
		}
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).status() != TextDiff.Line.Status.COMMON) {
				int from = Math.max(0, i - contextLines);
				int to = Math.min(lines.size() - 1, i + contextLines);
				for (int j = from; j <= to; j++) {
					visible[j] = true;
				}
			}
		}
		return visible;
	}

	private static void appendDiffLineRow(StringBuilder sb, TextDiff.Line line, LineNumberMode lineNumberMode) {
		appendRow(sb, line, false, lineNumberMode);
	}

	private static void appendHiddenDiffLineRow(StringBuilder sb, TextDiff.Line line, LineNumberMode lineNumberMode) {
		appendRow(sb, line, true, lineNumberMode);
	}

	private static void appendRow(StringBuilder sb, TextDiff.Line line, boolean hidden, LineNumberMode lineNumberMode) {
		String statusClass = switch (line.status()) {
			case COMMON -> "common";
			case ADDED -> "added";
			case REMOVED -> "removed";
		};
		String sign = switch (line.status()) {
			case COMMON -> "";
			case ADDED -> "+";
			case REMOVED -> "−";
		};
		sb.append("<tr class=\"line ").append(statusClass);
		if (hidden) sb.append(" hidden");
		sb.append("\" part=\"line ").append(statusClass);
		if (hidden) sb.append(" hidden");
		sb.append("\">");
		if (lineNumberMode != LineNumberMode.NEW_ONLY) {
			sb.append("<td class=\"num old\" part=\"num old\">")
					.append(line.oldLineNumber() > 0 ? line.oldLineNumber() : "")
					.append("</td>");
		}
		if (lineNumberMode != LineNumberMode.OLD_ONLY) {
			sb.append("<td class=\"num new\" part=\"num new\">")
					.append(line.newLineNumber() > 0 ? line.newLineNumber() : "")
					.append("</td>");
		}
		sb.append("<td class=\"sign\" part=\"sign\">").append(sign).append("</td>");
		sb.append("<td class=\"text\" part=\"text\">").append(Strings.encodeHtml(line.text())).append("</td>");
		sb.append("</tr>");
	}

	private static void appendAttribute(StringBuilder sb, String name, @Nullable String value) {
		if (value == null) return;
		sb.append(' ').append(name).append("=\"").append(Strings.encodeHtml(value)).append('"');
	}

	private record ChangeStats(int additions, int deletions) {
		private static ChangeStats from(TextDiff diff) {
			int additions = 0;
			int deletions = 0;
			for (TextDiff.Line line : diff.lines()) {
				if (line.status() == TextDiff.Line.Status.ADDED) {
					additions++;
				}
				else if (line.status() == TextDiff.Line.Status.REMOVED) deletions++;
			}
			return new ChangeStats(additions, deletions);
		}
	}

	private enum LineNumberMode {
		BOTH("two-line-numbers", 4),
		OLD_ONLY("single-line-number old-only", 3),
		NEW_ONLY("single-line-number new-only", 3);

		private final String cssClass;
		private final int columnCount;

		LineNumberMode(String cssClass, int columnCount) {
			this.cssClass = cssClass;
			this.columnCount = columnCount;
		}

		private String cssClass() {
			return cssClass;
		}

		private int columnCount() {
			return columnCount;
		}
	}
}
