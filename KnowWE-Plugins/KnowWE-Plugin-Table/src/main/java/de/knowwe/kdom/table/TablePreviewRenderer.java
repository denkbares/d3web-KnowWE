package de.knowwe.kdom.table;

import java.util.Collection;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.preview.AbstractPreviewRenderer;
import de.knowwe.core.user.UserContext;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Veronika Sehne (denkbares GmbH)
 * @created 20.11.17
 */
public class TablePreviewRenderer extends AbstractPreviewRenderer {

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

		outerLoop:
		for (Section<? extends Type> subsection : section.getChildren()) {
			for (Section<? extends Type> table : subsection.getChildren()) {
				for (Section<? extends Type> row : table.getChildren()) {
					if (row.equals(table.getChildren().get(0))) {
						Renderer renderer = DelegateRenderer.getRenderer(row, user);
						if (renderer instanceof TableLineRenderer) {
							renderer.render(row, user, result);
						}
					}
					else {
						for (Section<?> relevantSubSection : relevantSubSections) {
							Section<? extends Type> firstMatch = $(row).successor(relevantSubSection.get()
									.getClass())
									.filter(s -> s.getID().equals(relevantSubSection.getID()))
									.getFirst();
							if (firstMatch != null) {
								Renderer renderer = DelegateRenderer.getRenderer(row, user);
								renderer.render(row, user, result);
								break outerLoop;
							}
						}
					}
				}
			}
		}
		result.appendHtml("</tbody></table>");
		result.appendHtml("</div>");

		result.appendHtml(getClosingTag());
	}
}
