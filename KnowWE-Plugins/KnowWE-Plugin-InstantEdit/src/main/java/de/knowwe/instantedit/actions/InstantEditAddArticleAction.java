/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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

import com.google.gson.Gson;

import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.10.2011
 */
public class InstantEditAddArticleAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String title = context.getTitle();
		String jsonData = context.getParameter("data");

		Gson g = new Gson();
		SaveActionDTO data = g.fromJson(jsonData, SaveActionDTO.class);
		String articleText = data.buildArticle();


		if (articleText.equals("POST\n")) {
			articleText = "";
		}

		addNewArticle(context, title, articleText);
	}

	protected void addNewArticle(UserActionContext context, String title, String articleText) throws IOException {
		boolean canWrite = Environment.getInstance().getWikiConnector().userCanCreateArticles(context);
		if (!canWrite) {
			context.sendError(403, "You are not allowed to create this article");
			return;
		}

		// we wait in case this thread (reused via the thread pool) has already a compilation going
		Compilers.awaitTermination(context.getArticleManager().getCompilerManager());

		Article article = Environment.getInstance().getArticle(context.getWeb(), title);
		if (article == null) {
			Environment.getInstance().getWikiConnector().createArticle(title, context.getUserName(), articleText);
		}
		else {
			Environment.getInstance().getWikiConnector().writeArticleToWikiPersistence(title, articleText, context);
		}
	}
}
