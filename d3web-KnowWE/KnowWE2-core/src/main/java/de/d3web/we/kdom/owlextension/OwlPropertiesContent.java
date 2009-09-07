/**
 * 
 */
package de.d3web.we.kdom.owlextension;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.PropertyManager;

/**
 * @author kazamatzuri
 *
 */
public class OwlPropertiesContent extends XMLContent {

   

    	
	@Override
	public void init() {	    
	    this.setCustomRenderer(OwlPropertiesRenderer.getInstance());
	    this.sectionFinder=new AllTextFinder(this);
	}
	

	/* (non-Javadoc)
	 * @see de.d3web.we.dom.AbstractKnowWEObjectType#getOwl(de.d3web.we.dom.Section)
	 */
	@Override
	public IntermediateOwlObject getOwl(Section s) {
	    IntermediateOwlObject io=new IntermediateOwlObject();
	    String text=s.getOriginalText();
	    PropertyManager pm=PropertyManager.getInstance();
	    for (String cur:text.split("\r\n|\r|\n")){
		if (cur.trim().length()>0)
		    io.merge(pm.createProperty(cur.trim()));		
	    }
	    return io;	    
	}
	

}
