package de.d3web.we.flow.testcase;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLContent;

public class TestcaseTableXMLType extends AbstractXMLObjectType {

	public TestcaseTableXMLType(String tagName) {
		super(tagName);
	}

	public TestcaseTableXMLType() {
		super("Testcase");
	}

	@Override
	protected void init() {
		childrenTypes.add(new XMLContent(new TestcaseTable()));
	}
}
