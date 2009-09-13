/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * 
 */
package de.d3web.we.kdom.semanticFactSheet;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.SolutionContext;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.PropertyManager;
import de.d3web.we.module.semantic.owl.UpperOntology;

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
			io.merge(UpperOntology.getInstance().getHelper().createProperty(subjectconcept, prop, value, s));			
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
