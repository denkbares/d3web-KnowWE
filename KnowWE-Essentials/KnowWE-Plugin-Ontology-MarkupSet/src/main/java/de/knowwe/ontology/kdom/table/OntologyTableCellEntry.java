/*
 * Copyright (C) 2016 denkbares GmbH, Germany
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

package de.knowwe.ontology.kdom.table;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;

/**
 * Inbetween-type in cells that does not do much,
 * but allows to plug a renderer for a cell.
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 29.02.16.
 */
public class OntologyTableCellEntry extends AbstractType {

	public OntologyTableCellEntry(AbstractType childType) {
		childType.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(childType);
	}
}
