/**
 * 
 */
package de.d3web.we.kdom.owlextension;

import java.util.ResourceBundle;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author kazamatzuri
 * 
 */
public class OwlPropertiesRenderer extends KnowWEDomRenderer {
    private static ResourceBundle kwikiBundle = ResourceBundle
	    .getBundle("KnowWE_messages");
    private static OwlPropertiesRenderer instance;

    public static synchronized OwlPropertiesRenderer getInstance() {
	if (instance == null)
	    instance = new OwlPropertiesRenderer();
	return instance;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }

    private OwlPropertiesRenderer() {

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
	String text = sec.getOriginalText();
	String output = "";
	for (String cur : text.split("\r\n|\r|\n")) {
	    if (cur.trim().length() > 0)
		output += cur.trim() + "\\\\";
	}
	return output;
    }
}
