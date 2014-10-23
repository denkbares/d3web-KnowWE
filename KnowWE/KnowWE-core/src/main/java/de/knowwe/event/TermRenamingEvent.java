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

import de.d3web.strings.Identifier;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.event.Event;

/**
 * Event that gets fired after a term was renamed.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 23.10.14
 */
public class TermRenamingEvent extends Event {

	private ArticleManager articleManager;
	private final Identifier term;
	private final Identifier replacementTerm;


	public TermRenamingEvent(ArticleManager articleManager, Identifier term, Identifier replacementTerm) {
		this.articleManager = articleManager;
		this.term = term;
		this.replacementTerm = replacementTerm;
	}

	public Identifier getReplacementTerm() {
		return replacementTerm;
	}

	public Identifier getTerm() {
		return term;
	}

	public ArticleManager getArticleManager() {
		return articleManager;
	}
}
