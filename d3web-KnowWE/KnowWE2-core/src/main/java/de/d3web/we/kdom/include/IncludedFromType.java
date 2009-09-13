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

package de.d3web.we.kdom.include;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;


public class IncludedFromType extends DefaultAbstractKnowWEObjectType {

	/**
	 * The tagName of the IncludedFromType
	 */
	public static final String TAG = "includedFrom";
	
	/**
	 * Pattern that matches both Tags of the IncludedFromType
	 */
	public static final String PATTERN_BOTH = "</?" + TAG + "[^>]*>";

	/**
	 * Pattern that matches the HEAD of the IncludedFromType
	 */
	public static final String PATTERN_HEAD = "<" + TAG + "[^>]*>";
	
	/**
	 * Pattern that matches the TAIL of the IncludedFromType
	 */
	public static final String PATTERN_TAIL = "</" + TAG + ">";
	
	@Override
	protected void init() {
		this.setCustomRenderer(new IncludedFromSectionRenderer());
	}
	
	public static String removeIncludedFromTags(String s) {
		s = s.replaceAll(PATTERN_BOTH, "");
		return s;
	}
	
	
	

	
}
