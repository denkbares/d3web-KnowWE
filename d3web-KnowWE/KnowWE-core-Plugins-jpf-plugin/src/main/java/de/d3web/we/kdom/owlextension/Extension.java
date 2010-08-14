/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.kdom.owlextension;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class Extension extends AbstractXMLObjectType {

	public static final String EXTENSION_SOURCE_KEY = "EXTENSION_SOURCE";
	public static final String EXTENSION_OBJECT_KEY = "EXTENSION_OBJECT";
	public static final String EXTENSION_RESULT_KEY = "EXTENSION_RESULT";

	public Extension() {
		super("extension");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.dom.AbstractKnowWEObjectType#init()
	 */
	@Override
	protected void init() {
		// this.setCustomRenderer(ExtensionRenderer.getInstance());
		childrenTypes.add(new ExtensionContent());
		// this.sectionFinder=(new ExtensionSectionFinder(this));
	}
}
