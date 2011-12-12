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

import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Enables the InstantEdit mode.
 * 
 * @author Stefan Mark
 * @author Albrecht Striffler (denkbares GmbH)
 * 
 * @created 15.06.2011
 */
public class InstantEditEnableAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String topic = context.getTitle();
		String web = context.getWeb();
		String id = context.getParameter("KdomNodeId");

		KnowWEArticle art = KnowWEEnvironment.getInstance().getArticle(web, topic);
		if (art == null) {
			context.sendError(404, "Page '" + topic + "' could not be found.");
			return;
		}

		if (Sections.getSection(id) == null) {
			context.sendError(409, "Section '" + id
					+ "' could not be found, possibly because somebody else"
					+ " has edited the page.");
			return;
		}

		if (!KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(topic,
				context.getRequest())) {
			context.sendError(403, "You do not have the permission to edit this page.");
			return;
		}

		boolean isLocked = KnowWEEnvironment.getInstance().getWikiConnector().isPageLocked(topic);
		boolean isLockedCurrentUser = KnowWEEnvironment.getInstance().getWikiConnector().isPageLockedCurrentUser(
				topic, context.getUserName());

		String result = "{\"locked\":true}";

		if (!isLocked || isLockedCurrentUser) {
			KnowWEEnvironment.getInstance().getWikiConnector().setPageLocked(topic,
					context.getUserName());
			result = "{\"locked\":false}";
		}

		if (context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(result);
		}
	}

}
