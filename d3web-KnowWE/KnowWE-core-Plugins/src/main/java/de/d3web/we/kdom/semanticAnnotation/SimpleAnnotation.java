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

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.module.semantic.OwlGenerator;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;

/**
 * @author kazamatzuri
 * 
 */
public class SimpleAnnotation extends DefaultAbstractKnowWEObjectType implements OwlGenerator{

	public static class SimpleAnnotationSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
		
			ArrayList<SectionFinderResult> result =
						new ArrayList<SectionFinderResult>();
			if (text.trim().length() > 0) {
				result.add(new SectionFinderResult(0, text.length()));
			}
			return result;
		}
	}

	@Override
	public void init() {
		this.sectionFinder = new SimpleAnnotationSectionFinder();
	}

	
	public IntermediateOwlObject getOwl(Section s) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		UpperOntology uo = UpperOntology.getInstance();
		String annos = s.getOriginalText().trim().replaceAll(" ", "_");
		URI anno = null;
		if (annos.contains(":")) {
			String[] list = annos.split(":");
			String ns =list[0];
			if (ns.equals("ns")){
				ns=uo.getBaseNS();
			}
			anno = uo.getHelper().createURI(ns, list[1]);
		} else {
			anno = uo.getHelper().createlocalURI(annos);
		}
		if (anno != null) {
			io.addLiteral(anno);
		}
		return io;
	}

}
