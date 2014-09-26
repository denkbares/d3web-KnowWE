/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.event;

import de.knowwe.core.ArticleManager;

/**
 * Gets fired every time the {@link de.knowwe.core.DefaultArticleManager} of the wiki is opened (a new Article is
 * registered). It is fired, before the article manager is changed or anything is compiled. Use it to terminate
 * asynchronous tasks to avoid concurrent modifications and such.
 * <p/>
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.06.2014
 */
public class ArticleManagerOpenedEvent extends ArticleManagerEvent {

	public ArticleManagerOpenedEvent(ArticleManager articleManager) {
		super(articleManager);
	}
}
