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

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;

/**
 * This Interface indicates, that Sections with ObjectTypes that implement this
 * Interface contain Knowledge which can be modified in an already existing 
 * KnowledgeBase.
 * 
 * @author astriffler
 *
 */
public interface KnowledgeRecyclingObjectType {
	
	/**
	 * This method is called after all new an changed knowledge is parsed to
	 * the KnowledgeBase. It then needs to delete all knowledge from the given
	 * KnowledgeBase that is not longer in the given Article.
	 * 
	 * @param article is the Article the KBM belongs to
	 * @param kbm is the KBM the Article belongs to
	 */
	public void cleanKnowledge(KnowWEArticle article, KnowledgeBaseManagement kbm);

}
