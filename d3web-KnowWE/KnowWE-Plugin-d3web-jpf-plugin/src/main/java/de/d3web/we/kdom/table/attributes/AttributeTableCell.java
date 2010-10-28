/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.kdom.table.attributes;

import de.d3web.we.kdom.table.TableCell;

public class AttributeTableCell extends TableCell {

	public AttributeTableCell() {
		childrenTypes.add(2, new AttributeTableCellContent());
	}

	// public AttributeTableCell(TxtAttributeTableBuilder builder) {
	// this.sectionFinder = new TableCellSectionFinder(builder);
	// this.setCustomRenderer(new TxtAttributeTableCellRenderer());
	// }
	//
	// protected class TxtAttributeTableCellRenderer extends
	// TableCellContentRenderer {
	//
	// @Override
	// public void render(KnowWEArticle article, Section sec, KnowWEUserContext
	// user, StringBuilder string) {
	// string.append(wrappContent(TxtTableParser.compile(sec.getOriginalText()),
	// sec, user));
	// }
	//
	// /**
	// * Returns the column of the table in which the current cell occurs.
	// *
	// * @param section current section
	// * @return
	// */
	// protected int getColumn(Section<?> section) {
	// Section<AttributeTableLine> tableLine =
	// section.findAncestorOfType(AttributeTableLine.class);
	// List<Section<AttributeTableCell>> tmpSections = new
	// ArrayList<Section<AttributeTableCell>>();
	// tableLine.findSuccessorsOfType(AttributeTableCell.class, tmpSections);
	//
	// return tmpSections.indexOf(section) + 1;
	// }
	//
	// /**
	// * Returns the row of the table in which the current cell occurs.
	// *
	// * @param section current section
	// * @return
	// */
	// protected int getRow(Section<?> section) {
	// Section<AttributeTableContent> tableContent =
	// section.findAncestorOfType(AttributeTableContent.class);
	//
	// List<Section<AttributeTableLine>> lines = new
	// ArrayList<Section<AttributeTableLine>>();
	// tableContent.findSuccessorsOfType(AttributeTableLine.class, lines);
	//
	// // TODO: Refactor
	//
	// int row = -1;
	// for (int i = 0; i < lines.size(); i++) {
	// List<Section<AttributeTableCell>> tmpSections = new
	// ArrayList<Section<AttributeTableCell>>();
	// lines.get(i).findSuccessorsOfType(AttributeTableCell.class, tmpSections);
	// if (tmpSections.contains(section)) row = i;
	// }
	// return row;
	// }
	//
	// }

}
