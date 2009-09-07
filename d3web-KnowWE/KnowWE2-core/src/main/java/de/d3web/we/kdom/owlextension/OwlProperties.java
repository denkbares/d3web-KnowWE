/**
 * 
 */
package de.d3web.we.kdom.owlextension;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author kazamatzuri
 *
 */
public class OwlProperties extends AbstractXMLObjectType{
	
	public OwlProperties(){
	    super("properties");
		
	}
	
	@Override
	protected void init() {
		childrenTypes.add(new OwlPropertiesContent());
	}


}