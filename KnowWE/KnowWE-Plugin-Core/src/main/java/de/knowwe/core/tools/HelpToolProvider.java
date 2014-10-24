/*
 * Copyright (C) 2012 denkbares GmbH, Germany
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
package de.knowwe.core.tools;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;

/**
 * 
 * @author volker_belli
 * @created 06.03.2012
 */
public class HelpToolProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return getDocArticle(section, userContext) != null;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {

		Article article = getDocArticle(section, userContext);
		if (article == null) return ToolUtils.emptyToolArray();

		String link = KnowWEUtils.getURLLink(article);

		String js = "window.location.href = '" + link + "';";
		Tool help = new DefaultTool(
				"KnowWEExtension/d3web/icon/help16.gif",
				"Help: " + section.get().getName(),
				"Open help page for this markup.",
				js,
				Tool.CATEGORY_INFO);
		return new Tool[] { help };
	}

	private Article getDocArticle(Section<?> section, UserContext userContext) {
		ArticleManager articleManager =
				Environment.getInstance().getArticleManager(userContext.getWeb());
		String markupName = section.get().getName();

		// looking for possible help articles
		Article article = articleManager.getArticle("Doc " + markupName);
		if (article == null) {
			article = articleManager.getArticle("Doc " + markupName + "s");
		}
		return article;
	}

}
