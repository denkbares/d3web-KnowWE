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

package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;
import de.d3web.we.module.semantic.owl.helpers.OwlHelper;

public class XCLHead extends DefaultAbstractKnowWEObjectType {
	
	
	
	public class XCLHeadSectionFinder extends SectionFinder{
		
		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			if(text.length() == 0) return null;
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			
			int start = 0;
			while(text.charAt(start) == ' ' || text.charAt(start) == '\n'
						|| text.charAt(start) == '\r') {
				start++;
				if(start == text.length()) break;
			}
			int end=text.indexOf('{');
						
			
			if(start <= end) {
			    String solution=text.substring(start,end).trim();
				result.add(new SectionFinderResult(start, end));				
				
				if (father != null) {
					DefaultSubjectContext con=new DefaultSubjectContext();
					con.setSubject(solution);
					ContextManager.getInstance().attachContext(father, con);
				}
			}
			
			return result;
		}
	}

	@Override
	public IntermediateOwlObject getOwl(Section section) {
	    IntermediateOwlObject io = new IntermediateOwlObject();
		DefaultSubjectContext sol = (DefaultSubjectContext) ContextManager.getInstance()
				.getContext(section, DefaultSubjectContext.CID);
		String solution = sol != null ? sol.getSubject() : null;
		if (solution != null) {
			UpperOntology uo = SemanticCore.getInstance().getUpper();

			try {
				URI solutionuri = uo.getHelper().createlocalURI(solution);
				io.addStatement(uo.getHelper().createStatement(solutionuri,
						RDF.TYPE, OwlHelper.SOLUTION));
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return io;
	}



	@Override
	protected void init() {
		this.sectionFinder = new XCLHeadSectionFinder();
		this.setCustomRenderer(XCLHeadHighlightingRenderer.getInstance());
	}

}
