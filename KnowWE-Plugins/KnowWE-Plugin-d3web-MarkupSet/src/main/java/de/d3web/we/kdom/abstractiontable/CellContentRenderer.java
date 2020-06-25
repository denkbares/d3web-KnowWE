package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.table.TableCellContentRenderer;
import de.knowwe.kdom.table.TableUtils;

public class CellContentRenderer extends TableCellContentRenderer {

	@Override
	protected String getClasses(Section<?> tableCell, UserContext user) {
		String classes = "";
		if (tableCell.hasErrorInSubtree()) {
			classes += " cellerror";
		}
		return classes;
	}

	@Override
	public String getStyle(Section<?> tableCell, UserContext user) {
		int columns = TableUtils.getNumberOfColumns(tableCell);
		int column = TableUtils.getColumn(tableCell);
		final int actionColumns = AbstractionTableMarkup.getActionColumns(tableCell);

		boolean headerRow = TableUtils.isHeaderRow(tableCell);

		String style = "";
		if (columns == column + actionColumns) {
			style += "border-left: solid 3px #ddd;";
		}
		if (headerRow) {
			style += " border-bottom: solid 3px #ddd";
		}
		return style;
	}

}
