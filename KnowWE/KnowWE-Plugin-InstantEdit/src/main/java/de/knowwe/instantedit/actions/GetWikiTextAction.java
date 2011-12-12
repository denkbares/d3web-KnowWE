/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.instantedit.actions;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Returns the text content of a Section.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 22.06.2011
 */
public class GetWikiTextAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String sectionID = context.getParameter("KdomNodeId");

		Section<? extends Type> sec = Sections.getSection(sectionID);

		if (sec == null) {
			context.sendError(409, "Section '" + sectionID
					+ "' could not be found, possibly because somebody else"
					+ " has edited the page.");
			return;
		}

		if (context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(sec.getText());
		}
	}

}
