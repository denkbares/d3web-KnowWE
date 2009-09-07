/**
 * 
 */
package de.d3web.we.kdom;

import java.util.regex.Pattern;

import de.d3web.we.kdom.sectionFinder.RegexSectioner;

/**
 * @author kazamatzuri
 * 
 */
public class VerbatimType extends DefaultAbstractKnowWEObjectType {

    /*
     * (non-Javadoc)
     * 
     * @see de.d3web.we.dom.AbstractKnowWEObjectType#init()
     */
    @Override
    protected void init() {
	this.sectionFinder = new RegexSectioner(
		"\\{\\{\\{\\s*(.+?)\\s*\\}\\}\\}", this, Pattern.DOTALL);
    }

}
