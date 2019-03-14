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

package de.knowwe.core.action;

import java.io.IOException;

/**
 * Interface for Actions. Actions are used for user interactions in KnowWE. The
 * most important method of this interface is execute() because this method will
 * be called when an action is executed.
 *
 * Additionally you can specify whether your action is "free for all" or only
 * executable for admins by overwritting the isAdminAction() method accordingly.
 * 
 * @author Sebastian Furth
 * @created Mar 9, 2011
 */
public interface Action {

	String XML = "application/xml; charset=UTF-8";
	String HTML = "text/html; charset=UTF-8";
	String JSON = "application/json; charset=UTF-8";
	String PLAIN_TEXT = "text/plain; charset=UTF-8";
	String BINARY = "application/x-bin";

	/**
	 * Executes the Action.
	 *
	 * @created Mar 9, 2011
	 * @param context the context for this action
	 */
	void execute(UserActionContext context) throws IOException;

	/**
	 * This method should return true if only admins are allowed to execute this
	 * action. Otherwise this method should return false.
	 *
	 * @created Mar 9, 2011
	 * @return true if the action is a admin action otherwise false.
	 */
	boolean isAdminAction();

}
