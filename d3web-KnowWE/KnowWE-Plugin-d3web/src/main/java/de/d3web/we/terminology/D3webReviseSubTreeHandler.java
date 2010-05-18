/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.terminology;

import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public abstract class D3webReviseSubTreeHandler<T extends KnowWEObjectType> implements SubtreeHandler<T> {

	/**
	 * @param article is the article you need the KBM from
	 * @param sec is the knowledge containing section you need the KBM for
	 * @returns the KBM or <tt>null</tt> for article <tt>article</tt>. <tt>null</tt>
	 * is returned if the Knowledge of the given section doesn't need to be rebuild.
	 */
	protected KnowledgeBaseManagement getKBM(KnowWEArticle article, Section sec) {
		return D3webModule.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(article, this, sec);
	}

//	/**
//	 * Just uses the old Knowledge for this section from this article
//	 */
//	protected void useOldKnowledge(KnowWEArticle article, Section s) {
//		KnowledgeRepresentationHandler handler = KnowledgeRepresentationManager.getInstance().getHandler("d3web");
//		if (handler instanceof D3webTerminologyHandler) {
//			handler.buildKnowledge(article, s);
//		}
//	}

}
