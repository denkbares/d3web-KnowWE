/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.kdom.defaultMarkup;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;

public class AnnotationContentType extends AbstractType {

	private final DefaultMarkup.Annotation annotation;

	public AnnotationContentType(DefaultMarkup.Annotation annotation) {
		this.annotation = annotation;

		this.setSectionFinder(new AllTextFinderTrimmed());

		Type[] contentTypes = this.annotation.getContentTypes();
		for (Type type : contentTypes) {
			addChildType(type);
		}
	}

	/**
	 * Returns the name of the underlying annotation defined.
	 * 
	 * @return the annotation's name
	 */
	@Override
	public String getName() {
		return this.annotation.getName();
	}

	/**
	 * Returns the actual name parsed from the name section belonging to this
	 * content section. This can differ, if the name was given as a regular
	 * expression.
	 * 
	 * @return the parsed name of the annotation
	 */
	public String getName(Section<? extends AnnotationContentType> section) {
		if (!(section.get() instanceof AnnotationContentType)) {
			throw new IllegalArgumentException("section must have the type "
					+ AnnotationContentType.class.getSimpleName());
		}
		Section<AnnotationType> annotationSection = Sections.ancestor(section,
				AnnotationType.class);
		Section<AnnotationNameType> nameSection = Sections.child(annotationSection,
				AnnotationNameType.class);
		return nameSection.get().getName(nameSection);
	}

	/**
	 * Returns the underlying annotation.
	 * 
	 * @return the underlying annotation
	 */
	public DefaultMarkup.Annotation getAnnotation() {
		return annotation;
	}

}