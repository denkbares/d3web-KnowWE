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
package de.knowwe.ontology.kdom.individual;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.kdom.OntologyLineType;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;

public class IndividualType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;
	public static final String TYPE_ANNOTATION_NAME = "type";

	static {
		MARKUP = new DefaultMarkup("Individual");
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(IndividualType.TYPE_ANNOTATION_NAME, false);
		MARKUP.addAnnotationContentType(TYPE_ANNOTATION_NAME, new AbbreviatedResourceReference());
		OntologyLineType lineType = new OntologyLineType();
		lineType.addChildType(new AbbreviatedIndividualDefinition());
		MARKUP.addContentType(lineType);
	}

	public IndividualType() {
		super(MARKUP);
	}

}
