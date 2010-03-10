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
package de.d3web.we.kdom.semanticAnnotation;

import org.openrdf.model.URI;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.module.semantic.OwlGenerator;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;

/**
 * @author kazamatzuri
 * 
 */
public class SemanticAnnotationPropertyName extends
		DefaultAbstractKnowWEObjectType implements OwlGenerator {

	@Override
	public void init() {
		this.sectionFinder = new AllTextSectionFinder();
	}

	public IntermediateOwlObject getOwl(Section s) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		UpperOntology uo = UpperOntology.getInstance();
		String prop = s.getOriginalText();
		URI property = null;
		if (prop.equals("subClassOf") || prop.equals("subPropertyOf")) {
			property = uo.getRDFS(prop);
		} else if (prop.equals("type")) {
			property = uo.getRDF(prop);
		} else if (prop.contains(":")) {
			String ns = SemanticCore.getInstance().getNameSpaces().get(
					prop.split(":")[0]);
			if (ns==null|| ns.length()==0){
				io.setBadAttribute("no namespace given");
				io.setValidPropFlag(false);			
			} else if (ns.equals(prop.split(":")[0])) {				
				io.setBadAttribute(ns);
				io.setValidPropFlag(false);
			} else {
				property = uo.getHelper().createURI(ns, prop.split(":")[1]);
			}
		} else {
			property = uo.getHelper().createlocalURI(prop);
		}
		io.addLiteral(property);
		return io;
	}

}
