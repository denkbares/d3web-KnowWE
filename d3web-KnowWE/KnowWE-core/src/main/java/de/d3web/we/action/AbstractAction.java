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
package de.d3web.we.action;

import java.io.IOException;

/**
 * Abstract superclass for Actions (KnowWEActions or Servlets)
 * with standard implementations for most methods.
 * 
 * The ActionContext of the execute method provides you everything
 * you need for your Actions. If you want to have some textual
 * output just use context.getWriter().write(...). In case you
 * are developing a KnowWEAction your output will be written to
 * KnowWE.jsp where it is applicable for further processing 
 * (via JavaScript etc.).
 * 
 * Additionally you have the possibility to stream almost any kind
 * of content via the OutputStream of the response. Simply use
 * context.getOutputStream().
 * 
 * @author Sebastian Furth
 */
public abstract class AbstractAction implements Action {

	@Override
	public boolean isAdminAction() {
		return false;
	}

	public abstract void execute(ActionContext context) throws IOException;
	
}
