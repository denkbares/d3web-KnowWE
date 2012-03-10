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
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.10.2011
 */
public class InstantEditAddArticleAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String title = context.getTitle();
		String value = context.getParameter("data");

		if (value.equals("POST\n")) {
			value = "";
		}

		Environment.getInstance().getWikiConnector().createWikiPage(title, "",
				context.getUserName());
		// errors and security are handled inside replaceKDOMNodesSaveAndBuild
		boolean written = Environment.getInstance().getWikiConnector()
				.writeArticleToWikiEnginePersistence(title, value, context);

		if (!written) {
			context.sendError(500, "Unable to perform request.");
		}

	}

}
