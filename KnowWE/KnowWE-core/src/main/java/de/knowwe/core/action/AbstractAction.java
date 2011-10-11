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
 * Abstract implementation of the Action Interface (KnowWEActions or Servlets).
 *
 * Please note that this standard implementation returns false for the
 * isAdminAction()-Method. If you want to implement an action which is only
 * executable for admins you should implement the Action Interface
 *
 * @see Action
 * @author Sebastian Furth
 */
public abstract class AbstractAction implements Action {

	/**
	 * Returns always false - which means that your action can be executed by
	 * every user. If you want to implement a "AdminAction" you should consider
	 * implementing the Action interface instead of extending AbstractAction.
	 */
	@Override
	public boolean isAdminAction() {
		return false;
	}

	public abstract void execute(UserActionContext context) throws IOException;

}
