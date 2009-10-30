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

package de.d3web.we.kdom.semanticAnnotation;

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;
import de.d3web.we.module.semantic.owl.helpers.OwlHelper;

public class AnnotationKnowledgeSliceSubject extends DefaultAbstractKnowWEObjectType {


	public static class AnnotationKnowledgeSliceSubjectSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			if(father.hasRightSonOfType(AnnotationRelationOperator.class, text)) {				
				List<SectionFinderResult> sections =
					new AllTextSectionFinder().lookForSections(text, father);
				DefaultSubjectContext con=new DefaultSubjectContext();
				con.setSubject(text.trim());
				ContextManager.getInstance().attachContext(father,con);
				return sections;
			}
			return null;
		}
	}
	
	@Override
	public IntermediateOwlObject getOwl(Section section) {
	    IntermediateOwlObject io =new IntermediateOwlObject();		
		DefaultSubjectContext sol = 
			(DefaultSubjectContext)ContextManager.
				getInstance().getContext(section, DefaultSubjectContext.CID);		
		UpperOntology uo=SemanticCore.getInstance().getUpper();
		try {
			URI solutionuri=sol.getSolutionURI();
			sol.setSubjectURI(solutionuri);
			io.addStatement(
					uo.getHelper().createStatement(
							solutionuri,RDF.TYPE, OwlHelper.SOLUTION));
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return io;
	}
	
	@Override
	protected void init() {
		this.sectionFinder = new AnnotationKnowledgeSliceSubjectSectionFinder();
		
	}
}
