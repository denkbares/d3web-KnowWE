/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.flow.type;

import de.knowwe.core.kdom.objects.KnowWETerm;
import de.knowwe.core.kdom.objects.StringReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * 
 * @author Reinhard Hatko
 * @created 08.12.2010
 */
public class StartNodeReference extends StringReference {

	@Override
	public String getTermIdentifier(Section<? extends KnowWETerm<String>> s) {
		Section<FlowchartReference> ref = Sections.findSuccessor(s.getFather(),
				FlowchartReference.class);

		return ref.getOriginalText() + "(" + s.getOriginalText() + ")";
	}

	@Override
	public String getTermObjectDisplayName() {
		return "Start node";
	}

}
