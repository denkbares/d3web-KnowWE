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

package de.knowwe.core.wikiConnector;

import de.knowwe.core.action.Action;
import de.knowwe.core.user.UserContext;

/**
 * Exception we throw if the user is not authorized for the desired action.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.09.15
 */
public class NotAuthorizedException extends RuntimeException {

	public NotAuthorizedException(String msg) {
		super(msg);
	}

	public NotAuthorizedException() {
	}

	public String getMessage(Action action, UserContext context) {
		String exceptionMsg = getMessage();
		return "User '" + context.getUserName() + "' is not authorized to execute action " + action
				.getClass().getSimpleName() + (exceptionMsg == null ? "" : ": " + exceptionMsg);
	}
}
