/*
 * Copyright (C) 2018 denkbares GmbH, Germany
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

import com.denkbares.strings.Strings;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Returns the text content of an Article.
 *
 * @author Jonas Müller
 * @created 09.07.18
 */
public class GetWikiArticleTextAction extends AbstractGetTextAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String articleName = context.getParameter("articleName");
		if (Strings.isBlank(articleName)) {
			context.sendError(409, "Please provide an article name");
			return;
		}
		Article article = context.getArticleManager().getArticle(articleName);
		if (article == null) {
			context.sendError(409, "The article with the provided name " + articleName + " could not be found.");
			return;
		}
		if (!KnowWEUtils.canView(article, context)) {
			context.sendError(403, "You are not allowed to view this article");
			return;
		}
		writeJsonResponse(article.getText(), context);
	}
}
