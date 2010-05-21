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
import de.d3web.we.hermes.kdom.conceptMining.LocationOccurrence;
import de.d3web.we.hermes.kdom.conceptMining.PersonOccurrence;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotation;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationContent;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationObject;

public class TimeEventDescriptionType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {

		SemanticAnnotation semanticAnnotation = new SemanticAnnotation();


		// first grab annotated concepts
		this.childrenTypes.add(semanticAnnotation);

		// then search for un-annotated concepts
		this.childrenTypes.add(new PersonOccurrence());
		this.childrenTypes.add(new LocationOccurrence());

		sectionFinder = new AllTextSectionFinder();
	}

	// should not be necessary any more, should work with standard annotations
	// with using contexts or something
	private void insertCustomAnnotationObjectType(
			SemanticAnnotation semanticAnnotation) {
		AbstractKnowWEObjectType content = (AbstractKnowWEObjectType)findContentType(semanticAnnotation);;

		if(content != null) {
			List<KnowWEObjectType> allowedChildrenTypes = content.getAllowedChildrenTypes();
			//removing usual annotationObjectType-object
			KnowWEObjectType type = content.removeChild(allowedChildrenTypes.size()-1);
			if(! (type instanceof SemanticAnnotationObject)) {
				throw new IllegalStateException("removed unexpected KnowWEObjectType:" +type.getClass().getName()+" instead of"+SemanticAnnotationObject.class.getName());
			}
			//replaced by customized one
			content.addChildType(new AnnotationObjectInTimeEvent());
		}
	}

	private KnowWEObjectType findContentType(SemanticAnnotation semanticAnnotation) {
		List<KnowWEObjectType> annoChildren = semanticAnnotation.getAllowedChildrenTypes();
		for (KnowWEObjectType knowWEObjectType : annoChildren) {
			if(knowWEObjectType instanceof SemanticAnnotationContent) {
				return knowWEObjectType;
			}
		}
		return null;

	}

}
