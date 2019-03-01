package de.knowwe.kdom.table;

import java.util.Collection;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.preview.AbstractPreviewRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.ContentType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Render previews of default markups containing a table. Can be used in "Show Info" tool (CompositeEdit).
 * <p>
 * The flag <tt>showNonTableSections</tt> allows to specify whether sections outside the table will be shown only if
 * they contain relevant sections or always
 *
 * @author Veronika Sehne (denkbares GmbH)
 * @created 20.11.17
 */
public class TablePreviewRenderer extends AbstractPreviewRenderer {

	private final boolean showNonTableSections = true;

	protected String getHeader() {
		return "";
	}

	protected String getOpeningTag(Section<?> sec) {
		return "<div class=\"table-edit\" id=\"" + sec.getID() + "\">";
	}

	protected String getClosingTag() {
		return "</div>";
	}

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {

		Sections<ContentType> contentType = $(section).successor(ContentType.class);

		for (Section<Type> child : contentType.children()) {

			if (child.get() instanceof Table) {
				renderTable(section, child, relevantSubSections, user, result);
			}
			else {
				if (showNonTableSections ||
						$(relevantSubSections).ancestor(child.get().getClass()).anyMatch(s -> s == child)) {
					result.append(child, user);
				}
			}
		}
		for (Section<AnnotationType> annotationTypeSection : $(section).successor(AnnotationType.class)) {

			if (showNonTableSections ||
					$(relevantSubSections).ancestor(AnnotationType.class).anyMatch(s -> s == annotationTypeSection)) {
				result.append(annotationTypeSection, user).append("\n");
			}
		}
	}

	private void renderTable(Section<?> section, Section<?> table, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		boolean sortable = TableUtils.sortOption(section);

		result.appendHtml(getOpeningTag(section));

		if (sortable) {
			result.appendHtml("<div class=\"sortable\" style='overflow:auto;white-space:normal;'>");
		}
		else {
			result.appendHtml("<div style='overflow:auto;white-space:normal;'>");
		}
		result.appendHtml("<table style='border:1px solid #999999;' id='table_" + section.getID()
				+ "' class='wikitable knowwetable' border='1'><tbody>");
		result.appendHtml(getHeader());

		boolean foundRow = false;
		for (Section<TableLine> row : $(section).successor(TableLine.class)) {
			if (row.equals(table.getChildren().get(0))) {
				result.append(row, user);
			}
			else if ($(relevantSubSections).ancestor(row.get().getClass()).anyMatch(s -> s == row)) {
				result.append(row, user);
				foundRow = true;
				break;
			}
		}
		if (!foundRow) {
			result.appendHtmlTag("tr").appendHtmlElement("td", "...").appendHtmlTag("/tr");
		}
		result.appendHtml("</tbody></table>");
		result.appendHtml("</div>");
		result.appendHtml(getClosingTag());
	}
}
