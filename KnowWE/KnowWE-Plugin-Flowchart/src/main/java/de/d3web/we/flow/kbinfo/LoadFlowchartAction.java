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

package de.d3web.we.flow.kbinfo;

import java.io.IOException;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.UserActionContext;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.flow.FlowchartUtils;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.Section;

/**
 * @author Reinhard Hatko
 * 
 *         Created: 18.06.2010
 */
public class LoadFlowchartAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String web = context.getParameter(KnowWEAttributes.WEB);
		String nodeID = context.getParameter(KnowWEAttributes.TARGET);

		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		Section<FlowchartType> section = (Section<FlowchartType>) mgr.getSection(nodeID);
		if (section == null) {
			// TODO error handling
		}
		else {

			String source = FlowchartUtils.getFlowSourceWithoutPreview(section);

			// TODO fix xml-soup of source and set content type to xml
			context.setContentType("text/plain; charset=UTF-8");
			String escapeXml = source;
			context.getWriter().write(escapeXml);
			
			
		}

	}

}
