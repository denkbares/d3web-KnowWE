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

package de.d3web.we.kdom.subtreeHandler;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;


/**
 * Abstract class for a ReviseSubtreeHandler. This handler has to be registered to a type and then,
 * after the KDOM is build, this handler is called with that section and
 * the subtree can be processed (e.g. translated to a target representation)
 *
 * @author Jochen
 *
 */
public interface SubtreeHandler<T extends KnowWEObjectType> {

	/**
	 * Revises the subtree of this section.
	 *
	 * @param article is the article that called this method... not necessarily the
	 * 		article the Section is hooked into directly, since Sections can also be included!
	 * @param s is the root section of the subtree to revise
	 */
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section<T> s);
	

}
