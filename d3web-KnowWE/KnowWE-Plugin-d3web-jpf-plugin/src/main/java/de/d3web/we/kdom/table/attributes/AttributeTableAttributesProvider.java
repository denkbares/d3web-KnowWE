/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.Map;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.table.Table;
import de.d3web.we.kdom.table.TableAttributesProvider;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * 
 * TODO: Revise this!
 * 
 * @author Sebastian Furth
 * @created 28/10/2010
 */
public class AttributeTableAttributesProvider implements TableAttributesProvider {

	@Override
	public String[] getAttributeValues(Section<? extends TableCellContent> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestorOfType(AbstractXMLObjectType.class);
		if (xml != null) {
			Map<String, String> map = AbstractXMLObjectType.getAttributeMapFor(xml);
			String str = map.get(TableAttributesProvider.ATT_VALUES);
			if (str != null) return str.split(",");
		}
		return null;
	}

	@Override
	public String getNoEditColumnAttribute(Section<Table> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestorOfType(AbstractXMLObjectType.class);
		if (xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(
					TableAttributesProvider.ATT_NOEDIT_COLUMN);
		}
		return null;
	}

	@Override
	public String getNoEditRowAttribute(Section<Table> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestorOfType(AbstractXMLObjectType.class);
		if (xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(
					TableAttributesProvider.ATT_NOEDIT_ROW);
		}
		return null;
	}

	@Override
	public String getWidthAttribute(Section<Table> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestorOfType(AbstractXMLObjectType.class);
		if (xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(
					TableAttributesProvider.ATT_WIDTH);
		}
		return null;
	}

}
