/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.reviseHandler;

import java.util.logging.Logger;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;

public abstract class D3webSubtreeHandler<T extends Type> extends SubtreeHandler<T> {

	/**
	 * You can get the KnowledgeBaseUtils for the given article.
	 * 
	 * @param article is the article you need the KBM from
	 * @returns the KBM for the given article
	 */
	protected KnowledgeBase getKB(Article article) {
		if (article == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"Article was null. KB wasn't loaded.");
			return null;
		}
		return D3webUtils.getKnowledgeBase(article.getWeb(), article.getTitle());
	}

	/**
	 * By default, a full parse is triggered to assure a consistent compilation.
	 * Avoid a full parse by overwriting this method and instead destroying
	 * everything that is created in the create method.
	 * 
	 * @see SubtreeHandler#destroy(Article, Section)
	 */
	@Override
	public void destroy(Article article, Section<T> section) {
		article.setFullParse(this.getClass());
		return;
	}

}
