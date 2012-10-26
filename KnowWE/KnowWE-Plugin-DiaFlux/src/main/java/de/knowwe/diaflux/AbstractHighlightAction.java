/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
 * 
 * @author Reinhard Hatko
 * @created 26.10.2012
 */
public abstract class AbstractHighlightAction extends
		AbstractAction {

	public static final String PARENTID = "parentid";

	@Override
	public void execute(UserActionContext context) throws IOException {

		String parentid = context.getParameter(PARENTID);
		String kdomid = context.getParameter(Attributes.SECTION_ID);
		Section<FlowchartType> flowchart = Sections.getSection(kdomid, FlowchartType.class);

		if (flowchart == null) {
			Highlight.writeEmpty(context);
			return;
		}

		Highlight highlight = new Highlight(parentid, getPrefix());

		insertHighlighting(flowchart, highlight, context);

		highlight.write(context);

	}


	/**
	 * Returns the prefix of css classes to be removed, if highlighting is
	 * updated without reloading the page, e.g. trace. If the highlighting is
	 * just updated on page load, this doesnt matter.
	 * 
	 * @created 26.10.2012
	 * @return
	 */
	public abstract String getPrefix();

	/**
	 * 
	 * @created 26.10.2012
	 * @param flowchart TODO
	 * @param highlight
	 * @param context
	 * @throws IOException
	 */
	public abstract void insertHighlighting(Section<FlowchartType> flowchart, Highlight highlight, UserActionContext context) throws IOException;

}