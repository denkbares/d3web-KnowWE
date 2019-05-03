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

		boolean headerRow = TableUtils.isHeaderRow(tableCell);

		String style = "";
		if (columns == column + 1) {
			style += "border-left-width: 3px;";
		}
		if (headerRow) {
			style += " border-bottom-width: 3px;";
		}
		return style;
	}

}
