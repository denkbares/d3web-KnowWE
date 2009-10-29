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

package de.d3web.we.hermes.kdom;

import java.util.List;

import de.d3web.we.hermes.kdom.conceptMining.AnnotationObjectInTimeEvent;
import de.d3web.we.hermes.kdom.conceptMining.ConceptOccurrence;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.semanticAnnotation.AnnotationContent;
import de.d3web.we.kdom.semanticAnnotation.AnnotationObject;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotation;

public class TimeEventDescriptionType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.childrenTypes.add(new ConceptOccurrence());
		SemanticAnnotation semanticAnnotation = new SemanticAnnotation();
		insertCustomAnnotationObjectType(semanticAnnotation);
		
		this.childrenTypes.add(semanticAnnotation);
		sectionFinder = new AllTextSectionFinder();
	}

	private void insertCustomAnnotationObjectType(
			SemanticAnnotation semanticAnnotation) {
		KnowWEObjectType content = findContentType(semanticAnnotation);;
		
		if(content != null) {
			List<KnowWEObjectType> allowedChildrenTypes = content.getAllowedChildrenTypes();
			//removing usual annotationObjectType-object
			KnowWEObjectType type = allowedChildrenTypes.remove(allowedChildrenTypes.size()-1);
			if(! (type instanceof AnnotationObject)) {
				throw new IllegalStateException("removed unexpected KnowWEObjectType:" +type.getClass().getName()+" instead of"+AnnotationObject.class.getName());
			}
			//replaced by customized one
			content.getAllowedChildrenTypes().add(new AnnotationObjectInTimeEvent());
		}
	}

	private KnowWEObjectType findContentType(SemanticAnnotation semanticAnnotation) {
		List<KnowWEObjectType> annoChildren = semanticAnnotation.getAllowedChildrenTypes();
		for (KnowWEObjectType knowWEObjectType : annoChildren) {
			if(knowWEObjectType instanceof AnnotationContent) {
				return knowWEObjectType;
			}
		}
		return null;
	
	}

}
