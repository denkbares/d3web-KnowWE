package com.denkbares.knowwe.changeannotations;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.rendering.elements.A;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.Table;
import de.knowwe.core.kdom.rendering.elements.Tbody;
import de.knowwe.core.kdom.rendering.elements.Td;
import de.knowwe.core.kdom.rendering.elements.TextNode;
import de.knowwe.core.kdom.rendering.elements.Tr;

/**
 * Renders a {@link PageAnnotation} as a self-contained {@code <knowwe-page-annotate>} web
 * component (declarative shadow DOM, bundled stylesheet) — so the result can drop into any
 * host page without colliding with surrounding styles.
 *
 * <p>The renderer needs the current page text alongside the annotation: the annotation
 * model carries blame metadata only, the actual line content comes from the page itself
 * at render time.
 */
public final class PageAnnotationRenderer {

	private static final String STYLESHEET = "KnowWEExtension/css/KnowWE-Plugin-ChangeAnnotations.css";
	private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

	private PageAnnotationRenderer() {
	}

	/**
	 * Renders {@code annotation} alongside the lines from {@code currentText}. The
	 * annotation must carry one {@link LineBlame} per visible line of the page — see
	 * {@link PageAnnotator} for how the line counts are kept in sync.
	 *
	 * @param linkBuilder produces the diff URL per version; pass {@link DiffLinkBuilder#NONE}
	 *                    to render version numbers without links
	 * @throws IllegalArgumentException if {@code annotation} and {@code currentText} disagree on line count
	 */
	@NotNull
	public static String render(@NotNull PageAnnotation annotation,
								@NotNull String currentText,
								@NotNull DiffLinkBuilder linkBuilder) {
		List<String> lines = PageLines.split(currentText);
		List<LineBlame> blames = annotation.lines();
		if (lines.size() != blames.size()) {
			throw new IllegalArgumentException(
					"Line count mismatch: annotation has " + blames.size()
							+ " entries but currentText has " + lines.size() + " lines.");
		}

		Tbody tbody = new Tbody();
		for (int i = 0; i < lines.size(); i++) {
			tbody.children(buildRow(blames.get(i), lines.get(i), linkBuilder));
		}

		return new HtmlElement("knowwe-page-annotate").children(
				new HtmlElement("template").attributes("shadowrootmode", "open").children(
						new HtmlElement("link").attributes("rel", "stylesheet", "href", STYLESHEET),
						new Table().clazz("annotate").attributes("part", "annotate").children(tbody)))
				.toString();
	}

	private static HtmlElement buildRow(LineBlame blame, String text, DiffLinkBuilder linkBuilder) {
		return new Tr().attributes("part", "line").children(
				buildVersionCell(blame, linkBuilder),
				buildAuthorCell(blame),
				buildDateCell(blame),
				buildTextCell(text));
	}

	private static HtmlElement buildVersionCell(LineBlame blame, DiffLinkBuilder linkBuilder) {
		String label = "v" + blame.introducedInVersion();
		String url = linkBuilder.diffLink(blame.introducedInVersion());
		HtmlElement labelNode = url == null
				? new TextNode(label)
				: new A(label, url).title("View diff against previous version");
		return new Td().clazz("version").attributes("part", "version").children(labelNode);
	}

	private static HtmlElement buildAuthorCell(LineBlame blame) {
		// Author can contain arbitrary characters (e.g. configured display names) — escape it.
		HtmlElement td = new Td().clazz("author").attributes("part", "author")
				.content(Strings.encodeHtml(blame.author()));
		if (Strings.nonBlank(blame.changeNote())) {
			td.title(blame.changeNote());
		}
		return td;
	}

	private static HtmlElement buildDateCell(LineBlame blame) {
		return new Td().clazz("date").attributes("part", "date").title(blame.date().toString())
				.content(DATE.format(blame.date()));
	}

	private static HtmlElement buildTextCell(String text) {
		return new Td().clazz("text").attributes("part", "text").content(Strings.encodeHtml(text));
	}

}
