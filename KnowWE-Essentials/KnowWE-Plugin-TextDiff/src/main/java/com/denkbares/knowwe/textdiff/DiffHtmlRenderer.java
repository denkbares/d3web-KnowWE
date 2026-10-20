package com.denkbares.knowwe.textdiff;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;

/**
 * Renders a {@link TextDiff} as a {@code <knowwe-text-diff>} and a {@link FileChange} as a {@code <knowwe-file-change>}.
 * The host carries a declarative shadow root with the stylesheet, a placeholder of the height the diff will take and
 * both texts as JSON. The browser builds the view from the texts once the element comes near the viewport.
 */
public final class DiffHtmlRenderer {

	private static final String TEXT_DIFF_STYLESHEET = "KnowWEExtension/css/KnowWE-Plugin-TextDiff.css";

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
		StringBuilder sb = new StringBuilder();
		sb.append("<knowwe-text-diff data-layout=\"").append(layoutAttribute(options.view())).append("\"");
		appendAttribute(sb, "data-context", options.context());
		sb.append(" data-context-lines=\"").append(options.contextLines()).append("\">");
		sb.append("<template shadowrootmode=\"open\">");
		sb.append("<link rel=\"stylesheet\" href=\"").append(TEXT_DIFF_STYLESHEET).append("\">");
		appendPlaceholder(sb, diff, options);
		sb.append("</template>");
		sb.append("</knowwe-text-diff>");
		return sb.toString();
	}

	/**
	 * Renders {@code change} as a {@code <knowwe-file-change>} with an embedded {@code <knowwe-text-diff>} when
	 * {@link FileChange#diff()} is present.
	 */
	@NotNull
	public static String renderFileChange(@NotNull FileChange change) {
		return renderFileChange(change, DiffRenderOptions.defaults());
	}

	/**
	 * Renders {@code change} as a {@code <knowwe-file-change>} with the given diff options. The line counts in the
	 * header come from the diff and are replaced by the browser once it computed its own.
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
			TextDiff.Stats stats = diff.stats();
			if (stats.added() > 0) appendAttribute(sb, "data-additions", Integer.toString(stats.added()));
			if (stats.removed() > 0) appendAttribute(sb, "data-deletions", Integer.toString(stats.removed()));
		}
		sb.append(">");
		if (diff != null) {
			sb.append(renderTextDiff(diff, options));
		}
		sb.append("</knowwe-file-change>");
		return sb.toString();
	}

	private static String layoutAttribute(DiffView view) {
		return switch (view) {
			case UNIFIED -> "unified";
			case SIDE_BY_SIDE -> "split";
			case AUTO -> "auto";
		};
	}

	// The placeholder takes the height of the lines the view will show, so the page does not shift when the view
	// appears. The browser replaces it once it has read the JSON.
	private static void appendPlaceholder(StringBuilder sb, TextDiff diff, DiffRenderOptions options) {
		sb.append("<div class=\"diff-frame diff-placeholder\" part=\"frame\" style=\"--diff-placeholder-lines: ")
				.append(Math.max(1, countVisibleLines(diff.lines(), options.contextLines()))).append("\"></div>");
		sb.append("<script type=\"application/json\" class=\"diff-data\">")
				.append(diff.texts().toJson())
				.append("</script>");
	}

	private static int countVisibleLines(List<TextDiff.Line> lines, int contextLines) {
		if (contextLines < 0) return lines.size();
		boolean[] visible = new boolean[lines.size()];
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).status() == TextDiff.Line.Status.COMMON) continue;
			int from = Math.max(0, i - contextLines);
			int to = Math.min(lines.size() - 1, i + contextLines);
			for (int j = from; j <= to; j++) visible[j] = true;
		}
		int count = 0;
		for (boolean v : visible) if (v) count++;
		return count;
	}

	private static void appendAttribute(StringBuilder sb, String name, @Nullable String value) {
		if (value == null) return;
		sb.append(' ').append(name).append("=\"").append(Strings.encodeHtml(value)).append('"');
	}
}
