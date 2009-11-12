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

import de.d3web.we.core.KnowWEParameterMap;

/**
 * Abstract superclass for KnowWEActions with standard implementations
 * for most methods.
 * 
 * @author Reinhard Hatko
 * Created on: 12.11.2009
 */
public abstract class AbstractKnowWEAction implements KnowWEAction {

	@Override
	public boolean isAdminAction() {
		return false;
	}

	@Override
	public abstract String perform(KnowWEParameterMap parameterMap);

}
