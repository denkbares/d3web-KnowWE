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

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.UserActionContext;
import de.d3web.we.core.KnowWEEnvironment;

/**
 * Disables the InstantEdit mode.
 * 
 * @author Stefan Mark
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.06.2011
 */
public class InstantEditDisableAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String result = handle(context);
		if (result != null && context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(result);
		}
	}

	/**
	 * Disables the InstantEdit mode for the given section. Returns the success
	 * as JSON string for further processing on the client-side within the
	 * JavaScript.
	 * 
	 * @created 15.06.2011
	 * @param context
	 * @return success JSON string
	 * @throws IOException
	 */
	private String handle(UserActionContext context) throws IOException {

		String topic = context.getTitle();

		if (KnowWEEnvironment.getInstance().getArticle(context.getWeb(), topic) == null) {
			context.sendError(404, "Page '" + topic + "' could not be found.");
			return "{\"success\":false}";
		}

		if (!KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(topic,
				context.getRequest())) {
			context.sendError(403, "You do not have the permission to edit this page.");
			return "{\"success\":false, \"locked\":false}";
		}

		KnowWEEnvironment.getInstance().getWikiConnector().undoPageLocked(topic);
		return "{\"success\":true}";
	}
}
