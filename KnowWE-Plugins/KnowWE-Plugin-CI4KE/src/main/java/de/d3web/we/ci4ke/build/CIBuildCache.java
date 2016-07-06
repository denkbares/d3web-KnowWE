/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke.build;

import java.util.LinkedList;
import java.util.TreeMap;

import de.d3web.testing.BuildResult;

public class CIBuildCache {

	private final int maxSize;

	private final LinkedList<Integer> addOrder = new LinkedList<>();

	private final TreeMap<Integer, BuildResult> cache = new TreeMap<>();

	private BuildResult latestBuild = null;

	public CIBuildCache(int maxSize) {
		this.maxSize = maxSize;
	}

	public CIBuildCache() {
		this(50);
	}

	public synchronized void addBuild(BuildResult build) {
		int buildNumber = build.getBuildNumber();
		boolean alreadyCached = cache.containsKey(buildNumber);
		cache.put(buildNumber, build);
		if (!alreadyCached) {
			addOrder.add(buildNumber);
			if (addOrder.size() > maxSize) {
				cache.remove(addOrder.removeFirst());
			}
		}
	}

	public synchronized void setLatestBuild(BuildResult latestBuild) {
		this.latestBuild = latestBuild;
	}

	public synchronized BuildResult getLatestBuild() {
		return this.latestBuild;
	}

	public synchronized BuildResult getBuild(int buildNumber) {
		if (buildNumber < 1) return getLatestBuild();
		return cache.get(buildNumber);
	}

	public synchronized void clear() {
		addOrder.clear();
		cache.clear();
		latestBuild = null;
	}
}
