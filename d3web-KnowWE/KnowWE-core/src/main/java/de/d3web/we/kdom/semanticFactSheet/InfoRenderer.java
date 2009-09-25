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

import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
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
    public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
    	boolean verbose = false;
		Map<String, String> params =
				AbstractXMLObjectType.getAttributeMapFor(sec.getFather());
		
		if (params != null) {
			if (params.containsKey("verbose")) {
				verbose = true;
			}
		}
		
		if (!verbose)
			return;
		
		String text = sec.getOriginalText();
		IntermediateOwlObject io = sec.getObjectType().getOwl(sec);
		
		if (!io.getValidPropFlag()) {
			text = KnowWEEnvironment
					.maskHTML("<p class=\"box error\">invalid property:"
							+ io.getBadAttribute() + "</p>");
		}
		
		// SemanticCore sc=SemanticCore.getInstance();
		
		if (true) {
			for (String cur : text.split("\r\n|\r|\n")) {
				if (cur.trim().length() > 0)
					string.append(cur.trim() + "\\\\");
			}
		}
    }

}
