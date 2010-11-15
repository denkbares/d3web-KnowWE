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
 * @author hatko
 * 
 */
public abstract class AbstractNodeHandler implements NodeHandler {

	private final AbstractKnowWEObjectType type;
	private final String markup;

	public AbstractNodeHandler(AbstractKnowWEObjectType type, String markup) {
		this.type = type;
		this.markup = markup;
	}

	protected Section<AbstractXMLObjectType> getNodeInfo(Section<?> nodeSection) {
		Section<AbstractXMLObjectType> child = (Section<AbstractXMLObjectType>) nodeSection.findSuccessor(type.getClass());

		if (child == null) return null; // no child of expected type

		if (markup == null || markup == "") return child; // no constraints of
															// markup given,
															// return true;

		String actualMarkup = AbstractXMLObjectType.getAttributeMapFor(child).get("markup");

		if (markup.equalsIgnoreCase(actualMarkup)) return child;
		else return null;

	}

	@Override
	public KnowWEObjectType getObjectType() {
		return type;
	}

}
