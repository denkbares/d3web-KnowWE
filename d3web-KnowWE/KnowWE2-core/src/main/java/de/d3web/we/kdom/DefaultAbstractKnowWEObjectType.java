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


/**
 * @author Jochen
 * 
 * Contains empty implementations of methods of AbstractKnowWEObjectType that do not need
 * to be implemented in every case. 
 *
 */
public abstract class DefaultAbstractKnowWEObjectType extends AbstractKnowWEObjectType{

	@Override
	protected void init() {
		//empty default implementation, if no initialization is necessary
	}



	@Override
	public void cleanStoredInfos(String articleName) {
		//empty default implementation, if no cleaning is necessary
	}


	
	
	

}
