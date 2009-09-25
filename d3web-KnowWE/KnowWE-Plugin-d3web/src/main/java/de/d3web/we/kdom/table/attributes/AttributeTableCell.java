/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.table.attributes;

import java.util.ArrayList;
import java.util.List;

import de.d3web.KnOfficeParser.txttable.TxtAttributeTableBuilder;
import de.d3web.KnOfficeParser.txttable.TxtTableParser;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.table.TableCellContentRenderer;
import de.d3web.we.kdom.table.TableCellSectionFinder;
import de.d3web.we.kdom.table.TableUtils;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class AttributeTableCell extends DefaultAbstractKnowWEObjectType {
	
	public AttributeTableCell(TxtAttributeTableBuilder builder) {
		this.sectionFinder = new TableCellSectionFinder(builder);
		this.setCustomRenderer(new TxtAttributeTableCellRenderer());
	}
	
	protected class TxtAttributeTableCellRenderer extends TableCellContentRenderer {
		
		@Override
		public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
			string.append(wrappContent(TxtTableParser.compile(sec.getOriginalText()), sec, user));
		}
		
		/**
		 * Returns the column of the table in which the current cell occurs.
		 * 
		 * @param section
		 *             current section
		 * @return
		 */
		protected int getColumn( Section section ) {
			Section tableLine = KnowWEObjectTypeUtils.getAncestorOfType( section, AttributeTableLine.class.getName() );
			List<Section> tmpSections = new ArrayList<Section>();
			TableUtils.getCertainSections( tableLine, AttributeTableCell.class.getName(), tmpSections );
			
			return tmpSections.indexOf( section ) + 1;
		}
		
		/**
		 * Returns the row of the table in which the current cell occurs.
		 * 
		 * @param section
		 *             current section
		 * @return
		 */
		protected int getRow( Section section ) {
			Section tableContent = KnowWEObjectTypeUtils.getAncestorOfType( section, AttributeTableContent.class.getName() );
			
			List<Section> sections = new ArrayList<Section>();
			TableUtils.getCertainSections( tableContent, AttributeTableLine.class.getName(), sections );
			
			int row = -1;
			for(int i = 0; i < sections.size(); i++)
			{			
				List<Section> tmpSections = new ArrayList<Section>();
				TableUtils.getCertainSections( sections.get(i), AttributeTableCell.class.getName(), tmpSections );
				if( tmpSections.contains( section )) row = i;
			}
			return row + 1;
		}
		
	}

}
