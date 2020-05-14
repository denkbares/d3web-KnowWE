/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import de.knowwe.core.Environment;
import de.knowwe.core.taghandler.ObjectInfoTagHandler;

/**
 * CreateObjectHomePageAction
 * <p>
 * Gets a name of a object as parameter and checks if there exist a wiki article
 * with this name. If not this action will create such an article with the
 * ObjectInfoTagHandler for the specified object as content.
 *
 * @author Sebastian Furth
 * @created Dec 6, 2010
 * @see ObjectInfoTagHandler
 */
public class CreateObjectHomePageAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String objectName = context.getParameter(ObjectInfoTagHandler.OBJECT_NAME);
		String currentArticle = context.getParameter("page");

		// We assume that the user is privileged to create a new article if
		// he is allowed to edit the current article
		if (Environment.getInstance().getWikiConnector().userCanEditArticle(currentArticle,
				context.getRequest())) {

			// Article doesn't exist -> create it!
			if (objectName != null
					&& Environment.getInstance().getArticle(context.getWeb(), objectName) == null) {
				String content = "[{KnowWEPlugin objectInfo , objectname=" + objectName
						+ "}]";
				Environment.getInstance().getWikiConnector().createArticle(objectName,
						context.getUserName(), content);
			}
		}
		else {
			// Error Code 403: Forbidden
			fail(context, 403, "Not allowed to create page");
		}
	}
}
