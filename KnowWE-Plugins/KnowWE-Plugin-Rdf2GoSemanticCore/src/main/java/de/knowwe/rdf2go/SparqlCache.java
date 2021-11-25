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

	public enum State {
		/**
		 * A result is cached and ready/done
		 */
		available,
		/**
		 * A result from a previous compilation is ready/done
		 */
		outdated,
		/**
		 * No cached and ready/done result is available
		 */
		unavailable
	}

	private static final int DEFAULT_MAX_CACHE_SIZE = 1000000; // should be below 100 MB of cache (we count each cell)

	private final Rdf2GoCore core;
	private final Map<String, SparqlTask> cache = new LinkedHashMap<>(16, 0.75f, true);
	private final Map<String, SparqlTask> outdated = new LinkedHashMap<>(16, 0.75f, true);

	SparqlCache(Rdf2GoCore core) {
		this.core = core;
	}

	public State getState(String query) {
		SparqlTask sparqlTask = get(query);
		if (sparqlTask != null && sparqlTask.isDone()) {
			return State.available;
		}
		else if (getOutdated(query) != null) {
			return State.outdated;
		}
		else {
			return State.unavailable;
		}
	}

	public SparqlTask get(String query) {
		return cache.get(query);
	}

	public SparqlTask getOutdated(String query) {
		return outdated.get(query);
	}

	public void put(String query, SparqlTask task) {
		cache.put(query, task);
		checkCacheSize(this.cache);
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
			return this.cache.remove(completeQuery) != null;
		}
	}

	/**
	 * Clears the whole cache.
	 */
	public synchronized void invalidate() {
		for (Map.Entry<String, SparqlTask> entry : this.cache.entrySet()) {
			SparqlTask task = entry.getValue();
			if (!task.isDone()) {
				task.cancel(true);
				continue;
			}
			this.outdated.put(entry.getKey(), task);
		}
		checkCacheSize(this.outdated);

		this.cache.clear();
	}

	private void checkCacheSize(Map<String, SparqlTask> cache) {
		int currentSize = cache.values().stream().mapToInt(SparqlTask::getSize).sum();
		if (currentSize > DEFAULT_MAX_CACHE_SIZE) {
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (cache) {
				Iterator<Map.Entry<String, SparqlTask>> iterator = cache.entrySet().iterator();
				while (iterator.hasNext() && currentSize > DEFAULT_MAX_CACHE_SIZE) {
					Map.Entry<String, SparqlTask> next = iterator.next();
					iterator.remove();
					try {
						currentSize -= next.getValue().getSize();
					}
					catch (Exception ignore) {
						// nothing to do, cache size wasn't increase either
					}
				}
			}
		}
	}
}
