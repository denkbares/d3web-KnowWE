/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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

package de.knowwe.rdf2go;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Caches the result of sparql tasks of the Rdf2GoCore.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 21.03.2020
 */
public class SparqlCache {
	private static final int DEFAULT_MAX_CACHE_SIZE = 1000000; // should be below 100 MB of cache (we count each cell)

	private final Rdf2GoCore core;
	private int cachedSize = 0;
	private final Map<String, SparqlTask> cache = new LinkedHashMap<>(16, 0.75f, true);

	SparqlCache(Rdf2GoCore core) {
		this.core = core;
	}

	public SparqlTask get(String query) {
		return cache.get(query);
	}

	public void put(String query, SparqlTask task) {
		SparqlTask previous = cache.put(query, task);
		if (previous != null) {
			this.cachedSize -= previous.getSize();
		}
	}

	/**
	 * Removes th cached result for the given sparql query. This way, if the query is executed again, it has to be
	 * calculated anew.
	 *
	 * @param query the query for which the cached result should be removed
	 * @return true if a result was cached, false if not
	 */
	public boolean remove(String query) {
		String completeQuery = core.prependPrefixesToQuery(core.getNamespaces(), query);
		synchronized (this.cache) {
			SparqlTask removed = this.cache.remove(completeQuery);
			if (removed != null) {
				this.cachedSize -= removed.getSize();
				return true;
			}
			return false;
		}
	}

	/**
	 * Clears the whole cache.
	 */
	public synchronized void clear() {
		this.cache.clear();
		this.cachedSize = 0;
	}

	public void handleCacheSize(SparqlTask task) {
		this.cachedSize += task.getSize();
		if (this.cachedSize > DEFAULT_MAX_CACHE_SIZE) {
			synchronized (this.cache) {
				Iterator<Map.Entry<String, SparqlTask>> iterator = this.cache.entrySet().iterator();
				while (iterator.hasNext() && this.cachedSize > DEFAULT_MAX_CACHE_SIZE) {
					Map.Entry<String, SparqlTask> next = iterator.next();
					iterator.remove();
					try {
						this.cachedSize -= next.getValue().getSize();
					}
					catch (Exception ignore) {
						// nothing to do, cache size wasn't increase either
					}
				}
			}
		}
	}
}
