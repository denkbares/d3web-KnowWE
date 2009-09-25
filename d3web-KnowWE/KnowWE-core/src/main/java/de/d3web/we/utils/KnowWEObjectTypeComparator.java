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

package de.d3web.we.utils;

import java.util.Comparator;

import de.d3web.we.kdom.KnowWEObjectType;

/**
 * Used in KnowWEObjectTypeUtils to sort
 * KnowWEObjectTypes lexicographical.
 * 
 * @author Johannes Dienst
 *
 */
public class KnowWEObjectTypeComparator implements Comparator<KnowWEObjectType> {

	@Override
	public int compare(KnowWEObjectType o1, KnowWEObjectType o2) {
		int i = o1.getName().compareTo(o2.getName());
		
		if (i < 0) {
			return -1;
		}
		
		if (i > 0) {
			return 1;
		}
		
		return 0;
	}

}
