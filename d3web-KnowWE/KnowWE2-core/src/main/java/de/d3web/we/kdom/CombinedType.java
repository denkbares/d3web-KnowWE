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

package de.d3web.we.kdom;

import de.d3web.we.kdom.rendering.KnowWEDomRenderer;

/**
 * @author Jochen
 * 
 * This combindedType should enable that a Section can have multiple Types at the same time
 *
 */
public class CombinedType implements KnowWEType {

	@Override
	public boolean isAssignableFromType(Class<? extends KnowWEObjectType> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isType(Class<? extends KnowWEObjectType> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		// TODO Auto-generated method stub
		return null;
	}

}
