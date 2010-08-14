package de.d3web.we.kdom.edit;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * The Edit class. Used for inplace editing of wiki article content.
 * 
 * @author smark
 * @since 2009/11/18
 * @see AbstractXMLObjectType
 */
public class Edit extends AbstractXMLObjectType {

	public Edit(String tagName) {
		super(tagName);
	}

	public Edit() {
		super("Edit");
	}

	@Override
	protected void init() {
		childrenTypes.add(new EditContent());
	}
}
