package de.d3web.we.flow.testcase;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.table.Table;
import de.d3web.we.kdom.table.TableAttributesProvider;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class TestcaseTable extends Table implements TableAttributesProvider{
	

	@Override
	public String getAttributeValues(Section<Table> s) {

		Section<AbstractXMLObjectType> xml = s.findAncestor(AbstractXMLObjectType.class);
		if(xml != null) {
			return TestcaseUtils.getKnowledge(xml);
		}	
		return null;
	}

	@Override
	public String getNoEditColumnAttribute(Section<Table> s) {
//		Section<AbstractXMLObjectType> xml = s.findAncestor(AbstractXMLObjectType.class);
//		if(xml != null) {
//			return AbstractXMLObjectType.getAttributeMapFor(xml).get(Table.ATT_NOEDIT_COLUMN);
//		}	
//		return null;
		return "0";
	}

	@Override
	public String getNoEditRowAttribute(Section<Table> s) {
//		Section<AbstractXMLObjectType> xml = s.findAncestor(AbstractXMLObjectType.class);
//		if(xml != null) {
//			return AbstractXMLObjectType.getAttributeMapFor(xml).get(Table.ATT_NOEDIT_ROW);
//		}	
//		return null;
		return "0";
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

