/**
 * 
 */
package de.d3web.we.kdom.semanticFactSheet;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author kazamatzuri
 *
 */
public class Info extends AbstractXMLObjectType{

    /**
     * @param type
     */
    public Info() {
	super("info");
    }
    
    @Override
	protected void init() {
		childrenTypes.add(new InfoContent());
	}

}
