/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

/**
 * Sections with this type contain the start of the annotation.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.11.2011
 */
public class AnnotationNameType extends AbstractType {

	private static final String ANNOTATION_NAME_KEY = "annotationNameKey";
	private final DefaultMarkup.Annotation annotation;
	private final Pattern namePattern;

	public AnnotationNameType(DefaultMarkup.Annotation annotation) {
		this.annotation = annotation;
		this.namePattern = Pattern.compile("^@("
				+ annotation.getName() + ")\\s*[:=]?", Pattern.CASE_INSENSITIVE);
		this.setSectionFinder(new RegexSectionFinder(namePattern));
		Type[] nameTypes = this.annotation.getNameTypes();
		for (Type type : nameTypes) {
			this.addChildType(type);
		}
	}

	/**
	 * Returns the name defined in the underlying annotation.
	 * 
	 * @return the annotation's name
	 */
	@Override
	public String getName() {
		return this.annotation.getName();
	}

	/**
	 * Returns the actual name parsed from the {@link AnnotationNameType}
	 * Section. This can differ, if the name was given as a regular expression.
	 * 
	 * @return the parsed name of the annotation
	 */
	public String getName(Section<AnnotationNameType> section) {
		if (!(section.get() instanceof AnnotationNameType)) {
			throw new IllegalArgumentException("section must have the type "
					+ AnnotationNameType.class.getSimpleName());
		}
		String name = (String) section.getObject(ANNOTATION_NAME_KEY);
		if (name == null) {
			Matcher matcher = namePattern.matcher(section.getText());
			matcher.find();
			// the has to be true/find something, because the
			// section is parsed by the same regex
			name = matcher.group(1);
			section.storeObject(ANNOTATION_NAME_KEY, name);
		}
		return name;
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
