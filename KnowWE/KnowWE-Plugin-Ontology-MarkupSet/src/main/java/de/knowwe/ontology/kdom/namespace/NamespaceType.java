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
package de.knowwe.ontology.kdom.namespace;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.basicType.LineBreak;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.kdom.OntologyLineType;

public class NamespaceType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Namespace");
		PackageManager.addPackageAnnotation(MARKUP);
		OntologyLineType lineType = new OntologyLineType();
		// remove EndLineComm, its not practical with urls...
		lineType.clearChildrenTypes();
		lineType.addChildType(new LineBreak());
		lineType.addChildType(new NamespaceAbbreviationDefinition());
		MARKUP.addContentType(lineType);
	}

	public NamespaceType() {
		super(MARKUP);
	}

}
