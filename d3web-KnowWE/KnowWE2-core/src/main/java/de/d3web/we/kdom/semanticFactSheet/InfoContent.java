/**
 * 
 */
package de.d3web.we.kdom.semanticFactSheet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.SolutionContext;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.PropertyManager;

/**
 * @author kazamatzuri
 * 
 */
public class InfoContent extends XMLContent {


    @Override
    public void init() {
	this.setCustomRenderer(InfoRenderer.getInstance());
	
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.d3web.we.dom.AbstractKnowWEObjectType#getOwl(de.d3web.we.dom.Section)
     */
    @Override
    public IntermediateOwlObject getOwl(Section s) {
	IntermediateOwlObject io = new IntermediateOwlObject();
	String text = s.getOriginalText();
	PropertyManager pm = PropertyManager.getInstance();
	String subjectconcept = ((SolutionContext) ContextManager
		.getInstance().getContext(s, SolutionContext.CID))
		.getSolution();
	for (String cur : text.split("\r\n|\r|\n")) {
	    if (cur.trim().length() > 0) {
		String[] spaces = cur.split(" ");
		if (spaces.length > 0) {
		    String prop = cur.split(" ")[0].trim();
		    boolean valid = pm.isValid(prop);
		    if (valid) {
			String value=cur.substring(cur.indexOf(" "),cur.length()).trim();			
			io.merge(pm.createProperty(subjectconcept, prop, value, s));			
		    } else {
			io.setValidPropFlag(valid);
			io.setBadAttribute(prop.trim());
			//break at first bad property
			return io;
		    }
		}

	    }
	}
	return io;
    }

}
