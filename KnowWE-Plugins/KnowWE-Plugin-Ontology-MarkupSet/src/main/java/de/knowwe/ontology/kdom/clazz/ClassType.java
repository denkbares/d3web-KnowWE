/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.ontology.kdom.clazz;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.kdom.OntologyLineType;

public class ClassType extends DefaultMarkupType {

	private static final String PROPERTY_ANNOTATION_NAME = "property";
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Class");
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(PROPERTY_ANNOTATION_NAME, false);
		MARKUP.addAnnotationContentType(PROPERTY_ANNOTATION_NAME, new PropertyAnnotationType());
		OntologyLineType lineType = new OntologyLineType();
		lineType.addChildType(new AbbreviatedClassDefinition());
		MARKUP.addContentType(lineType);
	}

	public ClassType() {
		super(MARKUP);
	}

}
