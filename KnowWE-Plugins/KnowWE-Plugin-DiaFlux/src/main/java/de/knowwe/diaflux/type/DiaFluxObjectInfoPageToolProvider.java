/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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
package de.knowwe.diaflux.type;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.tools.CompositeEditToolProvider;
import de.knowwe.core.user.UserContext;
import de.knowwe.diaflux.type.FlowchartXMLHeadType.FlowchartTermDef;
import de.knowwe.tools.Tool;

/**
 * Adds an InfoPage Tool to DiaFlux.
 *
 * @author Reinhard Hatko
 * @created 21.05.2013
 */
public class DiaFluxObjectInfoPageToolProvider extends CompositeEditToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		Section<FlowchartTermDef> termDef = Sections.successor(section, FlowchartTermDef.class);
		if (termDef != null) {
			return new Tool[] { getCompositeEditTool(section, termDef.get().getTermIdentifier(termDef)) };
		}
		else {
			return new Tool[] {};
		}
	}
}
