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
package de.d3web.we.kdom.dashTree.subclassing;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElementContent;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;

public class SubClassingDashTreeElement extends DashTreeElement {

	@Override
	public IntermediateOwlObject getOwl(Section s) {   //warning
		Section<DashTreeElement> element = (Section<DashTreeElement>)s;  //warning 
		IntermediateOwlObject io = new IntermediateOwlObject();
		if (s.getObjectType().isAssignableFromType(DashTreeElement.class)) {
			Section<? extends DashTreeElement> father = DashTreeElement.getDashTreeFather(element);
			if (father != null) {
				Section<DashTreeElementContent> fatherElement = father
						.findChildOfType(new DashTreeElementContent());
				Section<DashTreeElementContent> childElement = element.findChildOfType(DashTreeElementContent.getDefaultInstance());
				createSubClassRelation(childElement,
						fatherElement, io);
			}
		}

		return io;
	}

	private void createSubClassRelation(Section<? extends DashTreeElementContent> child, Section<? extends DashTreeElementContent> fatherElement,
			IntermediateOwlObject io) {
		UpperOntology uo = UpperOntology.getInstance();
		URI localURI = uo.getHelper().createlocalURI(child.getOriginalText());
		URI fatherURI = uo.getHelper().createlocalURI(
				fatherElement.getOriginalText());
		try {
			io.addStatement(uo.getHelper().createStatement(localURI, RDFS.SUBCLASSOF,
					fatherURI));
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
