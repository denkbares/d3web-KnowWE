/*
 * Copyright (C) 2013 denkbares GmbH
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.ontology.kdom.table;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.table.TableCellContentRenderer;
import de.knowwe.kdom.table.TableUtils;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 27.04.15
 */
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
