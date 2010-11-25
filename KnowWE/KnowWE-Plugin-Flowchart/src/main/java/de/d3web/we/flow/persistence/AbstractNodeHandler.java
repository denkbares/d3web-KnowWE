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

/**
 * 
 */
package de.d3web.we.flow.persistence;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author Reinhard Hatko
 * 
 */
public abstract class AbstractNodeHandler implements NodeHandler {

	protected final AbstractKnowWEObjectType type;
	protected final String markup;

	public AbstractNodeHandler(AbstractKnowWEObjectType type, String markup) {
		this.type = type;
		this.markup = markup;
	}

	public AbstractNodeHandler(AbstractKnowWEObjectType type) {
		this(type, null);
	}

	protected Section<AbstractXMLObjectType> getNodeInfo(Section<?> nodeSection) {
		Section<AbstractXMLObjectType> child = (Section<AbstractXMLObjectType>) nodeSection.findSuccessor(type.getClass());

		if (child == null) return null; // no child of expected type

		if (markup == null || markup == "") {
			// no constraints of markup given
			return child;
		}

		String actualMarkup = getMarkup(child);

		if (markup.equalsIgnoreCase(actualMarkup)) {
			return child;
		}
		else {
			return null;
		}

	}

	protected String getMarkup(Section<AbstractXMLObjectType> child) {
		return AbstractXMLObjectType.getAttributeMapFor(child).get("markup");
	}

	@Override
	public KnowWEObjectType getObjectType() {
		return type;
	}

	public AbstractKnowWEObjectType getType() {
		return type;
	}


}
