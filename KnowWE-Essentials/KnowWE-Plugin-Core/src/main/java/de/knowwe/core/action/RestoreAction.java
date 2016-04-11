package de.knowwe.core.action;

/*
 * Copyright (C) 2012 denkbares GmbH
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

import java.io.IOException;
import java.net.URLEncoder;

import de.knowwe.core.Environment;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * @author Benedikt Kaemmerer
 * @created 17.12.2012
 */

public class RestoreAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		// restore old version
		String title = context.getTitle();
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		if (!wikiConnector.userCanEditArticle(title, context.getRequest())) {
			context.sendError(403, "You are not allowed to change version of this article.");
			return;
		}
		String versionToRestore = wikiConnector.getArticleText(title, Integer.parseInt(context.getParameter("restoreThisVersion")));
		wikiConnector.writeArticleToWikiPersistence(title, versionToRestore, context);

		// write response with pagetitle in url format
		String pageTitle = title;
		pageTitle = URLEncoder.encode(pageTitle, "UTF-8");
		pageTitle = pageTitle.replaceAll("\\+", "%20");
		context.getWriter().write(pageTitle);

	}

}
