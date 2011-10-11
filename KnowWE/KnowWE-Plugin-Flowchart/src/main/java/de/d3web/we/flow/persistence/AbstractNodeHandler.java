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

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.xml.AbstractXMLType;

/**
 * @author Reinhard Hatko
 * 
 */
public abstract class AbstractNodeHandler implements NodeHandler {

	protected final AbstractType type;
	protected final String markup;

	public AbstractNodeHandler(AbstractType type, String markup) {
		this.type = type;
		this.markup = markup;
	}

	public AbstractNodeHandler(AbstractType type) {
		this(type, null);
	}

	@SuppressWarnings("unchecked")
	protected Section<AbstractXMLType> getNodeInfo(Section<?> nodeSection) {
		Section<AbstractXMLType> child = (Section<AbstractXMLType>) Sections.findSuccessor(
				nodeSection, type.getClass());

		if (child == null) {
			return null; // no child of expected type
		}

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

	protected String getMarkup(Section<AbstractXMLType> child) {
		return AbstractXMLType.getAttributeMapFor(child).get("markup");
	}

	@Override
	public Type get() {
		return type;
	}

	public AbstractType getType() {
		return type;
	}

}
