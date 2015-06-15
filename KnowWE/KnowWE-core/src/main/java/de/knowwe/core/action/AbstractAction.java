/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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
package de.knowwe.core.action;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Abstract implementation of the Action Interface (KnowWEActions or Servlets).
 * <p/>
 * Please note that this standard implementation returns false for the
 * isAdminAction()-Method. If you want to implement an action which is only
 * executable for admins you should implement the Action Interface
 *
 * @author Sebastian Furth
 * @see Action
 */
public abstract class AbstractAction implements Action {

	/**
	 * Returns always false - which means that your action can be executed by
	 * every user. If you want to implement a "AdminAction" you should consider
	 * implementing the Action interface instead of extending AbstractAction.
	 */
	@Override
	public boolean isAdminAction() {
		return false;
	}

	public Section<?> getSection(UserActionContext context) throws IOException {
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		if (sectionId == null) sectionId = context.getParameter("KdomNodeId"); // compatibility
		if (sectionId == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND,
					"The request did not contain a section id, unable to execute action!");
			throw new IOException("The request did not contain a section id, unable to execute action!");
		}
		Section<?> section = Sections.get(sectionId);
		if (section == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND,
					"The referenced section was not found. " +
							"Maybe the page content is outdated. Please reload.");
			throw new IOException("Section with id '" + sectionId + "' was not found");
		}
		else if (!KnowWEUtils.canView(section, context)) {
			String actionName = this.getClass()
					.getSimpleName();
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You are not allowed to execute " + actionName);
			throw new IllegalAccessError("User '" + context.getUserName() + "' tried to execute "
					+ actionName + " with section '" + sectionId + "' but has no view rights for this section.");
		}
		return section;
	}

	public abstract void execute(UserActionContext context) throws IOException;

}
