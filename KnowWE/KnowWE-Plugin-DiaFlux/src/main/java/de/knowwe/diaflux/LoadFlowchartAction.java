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

package de.knowwe.diaflux;

import java.io.IOException;

import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.diaflux.type.FlowchartType;

/**
 * @author Reinhard Hatko
 * 
 *         Created: 18.06.2010
 */
public class LoadFlowchartAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String nodeID = context.getParameter(Attributes.TARGET);

		@SuppressWarnings("unchecked")
		Section<FlowchartType> section = (Section<FlowchartType>) Sections.getSection(nodeID);
		if (section != null) {

			String source = FlowchartUtils.getFlowSourceWithoutPreview(section);

			// TODO fix xml-soup of source and set content type to xml
			// Problem: '<' and '>' in e.g. conditions that would have to be escaped properly
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(source);

		}
	}

}
