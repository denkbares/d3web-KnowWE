/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

import com.denkbares.strings.Identifier;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.action.UserActionContext;

/**
 * An event signaling that a term has been renamed.
 *
 * Created by Albrecht Striffler (denkbares GmbH) on 30.09.2015.
 */
public class TermRenamingFinishEvent extends TermRenamingEvent {

	public TermRenamingFinishEvent(ArticleManager articleManager, UserActionContext context, Identifier term, Identifier replacementTerm) {
		super(articleManager, context, term, replacementTerm);
	}
}
