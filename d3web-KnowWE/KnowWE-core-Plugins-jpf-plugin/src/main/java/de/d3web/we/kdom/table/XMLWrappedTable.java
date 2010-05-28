package de.d3web.we.kdom.table;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class XMLWrappedTable extends Table implements TableAttributesProvider{
	

	@Override
	public String getAttributeValues(Section<Table> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestor(AbstractXMLObjectType.class);
		if(xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(Table.ATT_VALUES);
		}	
		return null;
	}

	@Override
	public String getNoEditColumnAttribute(Section<Table> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestor(AbstractXMLObjectType.class);
		if(xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(Table.ATT_NOEDIT_COLUMN);
		}	
		return null;
	}

	@Override
	public String getNoEditRowAttribute(Section<Table> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestor(AbstractXMLObjectType.class);
		if(xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(Table.ATT_NOEDIT_ROW);
		}	
		return null;
	}

	@Override
	public String getWidthAttribute(Section<Table> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestor(AbstractXMLObjectType.class);
		if(xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(Table.ATT_WIDTH);
		}	
		return null;
	}
}
