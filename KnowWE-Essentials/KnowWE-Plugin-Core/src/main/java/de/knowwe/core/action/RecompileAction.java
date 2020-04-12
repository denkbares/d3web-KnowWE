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

package de.knowwe.core.action;

import java.io.IOException;
import java.util.Collection;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Does a full compilation of the current article or the compilers compiling this article
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 12.04.2020
 */
public class RecompileAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String command = context.getParameter("command");
		String title = context.getParameter("title");
		Article article = context.getArticleManager().getArticle(title);
		if (article == null) {
			failUnexpected(context, "No article found for title '" + title + ", unable to recompile");
			return;
		}
		if ("recompileAll".equals(command)) {
			recompileAll(article);
		}
		else if ("recompile".equals(command)) {
			recompile(article);
		}
		else {
			failUnexpected(context, "Unknown command for RecompileAction: " + command);
		}
	}

	private void recompile(Article article) {
		ArticleManager articleManager = article.getArticleManager();
		assert articleManager != null;
		articleManager.registerArticle(cloneArticle(article));
	}

	private void recompileAll(Article article) {
		ArticleManager articleManager = article.getArticleManager();
		assert articleManager != null;
		articleManager.open();
		try {
			$(article).successor(DefaultMarkupType.class)
					.map(s -> Compilers.getCompilers(s, PackageCompiler.class))
					.flatMap(Collection::stream)
					.distinct()
					.map(c -> c.getCompileSection().getArticle())
					.forEach(compilerArticle -> articleManager.registerArticle(cloneArticle(compilerArticle)));
		}
		finally {
			articleManager.commit();
		}
	}

	private Article cloneArticle(Article compilerArticle) {
		return Article.createArticle(compilerArticle.getText(), compilerArticle
				.getTitle(), compilerArticle
				.getWeb());
	}
}
