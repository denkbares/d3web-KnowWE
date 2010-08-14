/**
 * 
 */
package de.d3web.we.flow.persistence;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author hatko
 * 
 */
public abstract class AbstractNodeHandler implements NodeHandler {

	private final AbstractKnowWEObjectType type;
	private final String markup;

	public AbstractNodeHandler(AbstractKnowWEObjectType type, String markup) {
		this.type = type;
		this.markup = markup;
	}

	protected Section<AbstractXMLObjectType> getNodeInfo(Section<?> nodeSection) {
		Section<AbstractXMLObjectType> child = (Section<AbstractXMLObjectType>) nodeSection.findSuccessor(type.getClass());

		if (child == null) return null; // no child of expected type

		if (markup == null || markup == "") return child; // no constraints of
															// markup given,
															// return true;

		String actualMarkup = AbstractXMLObjectType.getAttributeMapFor(child).get("markup");

		if (markup.equalsIgnoreCase(actualMarkup)) return child;
		else return null;

	}

	@Override
	public KnowWEObjectType getObjectType() {
		return type;
	}

}
