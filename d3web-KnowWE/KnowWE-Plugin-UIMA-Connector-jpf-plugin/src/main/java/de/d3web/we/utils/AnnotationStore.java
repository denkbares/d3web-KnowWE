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
package de.d3web.we.utils;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.sectionfinder.NestedTypeSectionFinder;

/**
 * Used to store the accesscounts of the Annotations. See
 * {@link TypeSectionfinder} and {@link NestedTypeSectionFinder} why this is
 * needed.
 * 
 * @author Johannes Dienst
 * 
 */
public class AnnotationStore {

	/**
	 * Unique instance
	 */
	private static AnnotationStore instance = null;

	/**
	 * Access count map. TypeName->count
	 */
	private Map<String, Integer> accessCount = new HashMap<String, Integer>();

	/**
	 * Singleton.
	 * 
	 * @return
	 */
	public static AnnotationStore getInstance() {
		if (instance == null) instance = new AnnotationStore();
		return instance;
	}

	public int getAccessCount(String key) {
		Integer i = accessCount.get(key);
		return i;
	}

	public void incrementAccessCount(String key) {
		Integer i = accessCount.get(key);
		accessCount.put(key, ++i);
	}

	public boolean contains(String key) {
		return accessCount.containsKey(key);
	}

	public void initKey(String key) {
		accessCount.put(key, 0);
	}

	public void removeKey(String key) {
		accessCount.remove(key);
	}

	/**
	 * Reset this AnnotationSore when Section gets rendered.
	 */
	public void reset() {
		this.accessCount = new HashMap<String, Integer>();
	}
}
