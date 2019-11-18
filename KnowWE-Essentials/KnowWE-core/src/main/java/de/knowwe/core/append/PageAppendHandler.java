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
package de.knowwe.core.append;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

/**
 * Allows to append some content to the rendering of wiki-pages. Either
 * the content can be put at the beginning of the the page or appended
 * to the end of the page.
 *
 * @author Jochen
 * @created 17.08.2010
 */
public interface PageAppendHandler {

	/**
	 * Returns the content to be rendered out with the wiki page. Content has to
	 * be returned in (masked) HTML or wiki-syntax
	 */
	void append(Article title, UserContext user, RenderResult result);

	/**
	 * Specifies whether the content should be inserted at the top/beginning
	 * (true) of a page, or at the end/bottom
	 *
	 * @created 17.08.2010
	 */
	default boolean isPre() {
		return false;
	}
}
