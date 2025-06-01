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

		String topic = context.getTitle();

		if (Environment.getInstance().getArticle(context.getWeb(), topic) == null) {
			context.sendError(404, "Page '" + topic + "' could not be found.");
			return;
		}

		if (!Environment.getInstance().getWikiConnector().userCanViewArticle(topic,
				context)) {
			context.sendError(403, "You do not have the permission to edit this page.");
			return;
		}

		Environment.getInstance().getWikiConnector().unlockArticle(topic, context.getUserName());
	}

}
