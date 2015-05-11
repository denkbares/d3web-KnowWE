/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.ontology.turtlePimped;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.turtle.TurtleContent;

/**
 * 
 * @author Jochen Reutelshöfer
 * @created 08.07.2013
 */
@Deprecated
public class TurtleMarkupPimped extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("TurtlePimped");
		PackageManager.addPackageAnnotation(MARKUP);
		TurtleContent markupN3Content = new TurtleContent();
		MARKUP.addContentType(markupN3Content);
	}

	public TurtleMarkupPimped() {
		super(MARKUP);
	}

}
