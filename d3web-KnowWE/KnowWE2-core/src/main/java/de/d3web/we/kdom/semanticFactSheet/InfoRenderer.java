/**
 * 
 */
package de.d3web.we.kdom.semanticFactSheet;

import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author kazamatzuri
 * 
 */
public class InfoRenderer extends KnowWEDomRenderer {

    private static InfoRenderer instance;

    public static synchronized InfoRenderer getInstance() {
	if (instance == null)
	    instance = new InfoRenderer();
	return instance;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.d3web.we.dom.renderer.KnowWEDomRenderer#render(de.d3web.we.dom.Section
     * , java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String render(Section sec, KnowWEUserContext user, String web, String topic) {
	boolean verbose = false;
	Map<String, String> params = ((Info) sec.getFather().getObjectType())
		.getMapFor(sec.getFather());
	if (params != null) {
	    if (params.containsKey("verbose")) {
		verbose = true;
	    }
	}
	if (!verbose)
	    return "";
	String text = sec.getOriginalText();
	StringBuilder output = new StringBuilder();
	IntermediateOwlObject io = sec.getObjectType().getOwl(sec);
	if(!io.getValidPropFlag()){
	    text=KnowWEEnvironment.maskHTML("<p class=\"box error\">invalid property:"+io.getBadAttribute()+"</p>");
	}
//	SemanticCore sc=SemanticCore.getInstance();
	if (true) {
	    for (String cur : text.split("\r\n|\r|\n")) {
		if (cur.trim().length() > 0)
		    output.append(cur.trim() + "\\\\");
	    }
	}
	return output.toString();
    }

}
