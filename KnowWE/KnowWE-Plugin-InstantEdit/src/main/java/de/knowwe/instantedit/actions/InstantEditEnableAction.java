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

import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Enables the InstantEdit mode.
 *
 * @author Stefan Mark
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.06.2011
 */
public class InstantEditEnableAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String title = context.getTitle();
		String web = context.getWeb();
		String id = context.getParameter("KdomNodeId");

		String mode = context.getParameter("mode");

		Article art = Environment.getInstance().getArticle(web, title);
		if (art == null) {
			context.sendError(404, "Page '" + title + "' could not be found.");
			return;
		}

		if (Sections.get(id) == null) {
			context.sendError(409, "Section '" + id
					+ "' could not be found, possibly because somebody else"
					+ " has edited the page.");
			return;
		}

		if (!Environment.getInstance().getWikiConnector().userCanEditArticle(title,
				context.getRequest())) {
			context.sendError(403, "You do not have the permission to edit this page.");
			return;
		}

		boolean isLocked = Environment.getInstance().getWikiConnector().isArticleLocked(title);
		boolean isLockedCurrentUser = Environment.getInstance().getWikiConnector().isArticleLockedCurrentUser(
				title, context.getUserName());

		String result = "{\"locked\":true}";

		if (!isLocked || isLockedCurrentUser) {
			if (!"add".equals(mode)) {
				Environment.getInstance().getWikiConnector().lockArticle(title, context.getUserName());
			}
			result = "{\"locked\":false}";
		}

		if (context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(result);
		}
	}

}
