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

import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.semanticcore.BooleanQuery;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.RepositoryConnection;
import com.denkbares.semanticcore.TupleQuery;
import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdf2go.utils.SparqlType;

/**
 * Does the work and retrieves the SPARQL result.
 */
class SparqlCallable implements Callable<Object> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SparqlCallable.class);

	private static final int LOG_TIMEOUT = 1000;

	private final Rdf2GoCore core;
	private final String query;
	private final SparqlType type;
	private final boolean cached;
	private final long timeOutMillis;

	// we may have optional prepared (shared) queries, that should be used if available
	private final TupleQuery preparedSelectQuery;
	private final BooleanQuery preparedAskQuery;
	private final GraphQuery preparedConstructQuery;
	private final Map<String, Value> preparedBindings;

	SparqlCallable(Rdf2GoCore core, String query, SparqlType type, long timeOutMillis, boolean cached) {
		this.core = core;
		this.query = query;
		this.type = type;
		this.cached = cached;
		// timeouts shorter than 1 seconds are not possible with sesame
		this.timeOutMillis = Math.max(1000, timeOutMillis);
		this.preparedAskQuery = null;
		this.preparedSelectQuery = null;
		this.preparedBindings = null;
		this.preparedConstructQuery = null;
	}

	SparqlCallable(Rdf2GoCore core, TupleQuery query, Map<String, Value> bindings, SparqlType type, long timeOutMillis) {
		this.core = core;
		this.query = query.getQueryString();
		this.type = type;
		// for the moment, we cannot cache prepared queries
		this.cached = false;
		// timeouts shorter than 1 seconds are not possible with sesame
		this.timeOutMillis = Math.max(1000, timeOutMillis);
		this.preparedAskQuery = null;
		this.preparedConstructQuery = null;
		this.preparedSelectQuery = query;
		this.preparedBindings = bindings;
	}

	SparqlCallable(Rdf2GoCore core, GraphQuery query, Map<String, Value> bindings, SparqlType type, long timeOutMillis) {
		this.core = core;
		this.query = query.toString();
		this.type = type;
		// for the moment, we cannot cache prepared queries
		this.cached = false;
		// timeouts shorter than 1 seconds are not possible with sesame
		this.timeOutMillis = Math.max(1000, timeOutMillis);
		this.preparedAskQuery = null;
		this.preparedConstructQuery = query;
		this.preparedSelectQuery = null;
		this.preparedBindings = bindings;
	}

	SparqlCallable(Rdf2GoCore core, BooleanQuery query, Map<String, Value> bindings, SparqlType type, long timeOutMillis) {
		this.core = core;
		this.query = query.getQueryString();
		this.type = type;
		// for the moment, we cannot cache prepared queries
		this.cached = false;
		// timeouts shorter than 1 seconds are not possible with sesame
		this.timeOutMillis = Math.max(1000, timeOutMillis);
		this.preparedAskQuery = query;
		this.preparedSelectQuery = null;
		this.preparedConstructQuery = null;
		this.preparedBindings = bindings;
	}

	public long getTimeOutMillis() {
		return timeOutMillis;
	}

	public boolean isCached() {
		return cached;
	}

	public String getQuery() {
		return query;
	}

	public SparqlType getType() {
		return type;
	}

	@Override
	public Object call() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		Object result = executeQuery();
		if (Thread.currentThread().isInterrupted()) {
			// not need to waste cache size (e.g. in case of half done results that were aborted)
			result = null;
		}
		return result;
	}

	private Object executeQuery() {
		int timeOutSeconds = (int) Math.min(this.timeOutMillis / 1000, Integer.MAX_VALUE);
		return switch (this.type) {
			case SELECT -> (preparedSelectQuery == null)
					? executeSelect(timeOutSeconds, new Stopwatch())
					: executeSelect(timeOutSeconds, new Stopwatch(), preparedSelectQuery);
			case ASK -> (preparedAskQuery == null)
					? executeAsk(timeOutSeconds, new Stopwatch())
					: executeAsk(timeOutSeconds, new Stopwatch(), preparedAskQuery);
			case CONSTRUCT -> (preparedConstructQuery == null)
					? executeConstruct(timeOutSeconds, new Stopwatch())
					: executeConstruct(timeOutSeconds, new Stopwatch(), preparedConstructQuery);
			default -> throw new UnsupportedOperationException("query type is not supported yet: " + type);
		};
	}

	private Object executeConstruct(int timeOutSeconds, Stopwatch stopwatch) {
		// if the query is not a prepared (shared) query, create a new one, on a new connection, and close afterwards
		core.getUsageLock().lock();
		try (RepositoryConnection connection = core.getRepositoryConnectionPK()) {
			GraphQuery graphQuery = connection.prepareGraphQuery(this.query);
			return executeConstruct(timeOutSeconds, stopwatch, graphQuery);
		}
		finally {
			core.getUsageLock().unlock();
		}
	}

	private Object executeConstruct(int timeOutSeconds, Stopwatch stopwatch, GraphQuery graphQuery) {
		graphQuery.setMaxExecutionTime(timeOutSeconds);
		GraphQueryResult result = graphQuery.evaluate();
		logSlowEvaluation(stopwatch.getTime());
		return result;
	}

	@NotNull
	private Object executeAsk(int timeOutSeconds, Stopwatch stopwatch) {
		// if the query is not a prepared (shared) query, create a new one, on a new connection, and close afterwards
		core.getUsageLock().lock();
		try (RepositoryConnection connection = core.getRepositoryConnectionPK()) {
			BooleanQuery booleanQuery = connection.prepareBooleanQuery(this.query);
			return executeAsk(timeOutSeconds, stopwatch, booleanQuery);
		}
		finally {
			core.getUsageLock().unlock();
		}
	}

	@NotNull
	private Object executeAsk(int timeOutSeconds, Stopwatch stopwatch, BooleanQuery booleanQuery) {
		booleanQuery.setMaxExecutionTime(timeOutSeconds);
		boolean result = (preparedBindings == null)
				? booleanQuery.evaluate()
				: booleanQuery.evaluate(preparedBindings);
		logSlowEvaluation(stopwatch.getTime());
		return result;
	}

	@NotNull
	private Object executeSelect(int timeOutSeconds, Stopwatch stopwatch) {
		// if the query is not a prepared (shared) query, create a new one, on a new connection, and close afterwards
		core.getUsageLock().lock();
		try (RepositoryConnection connection = core.getRepositoryConnectionPK()) {
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, this.query);
			return executeSelect(timeOutSeconds, stopwatch, tupleQuery);
		}
		finally {
			core.getUsageLock().unlock();
		}
	}

	@NotNull
	private Object executeSelect(int timeOutSeconds, Stopwatch stopwatch, TupleQuery tupleQuery) {
		tupleQuery.setMaxExecutionTime(timeOutSeconds);
		TupleQueryResult result = (preparedBindings == null)
				? tupleQuery.evaluate()
				: tupleQuery.evaluate(preparedBindings);
		CachedTupleQueryResult bindingSets = result.cachedAndClosed();
		long evalTime = stopwatch.getTime();
		logSlowEvaluation(evalTime);
		bindingSets.setEvaluationTime(evalTime);
		return bindingSets;
	}

	private void logSlowEvaluation(long evalTime) {
		if (this.cached && evalTime > LOG_TIMEOUT) {
			LOGGER.info("SPARQL query evaluation finished after "
						+ Strings.getDurationVerbalization(evalTime)
						+ ", retrieving results...: " + Rdf2GoUtils.getReadableQuery(this.query, this.type) + "...");
		}
	}

	public String getReadableQuery() {
		return Rdf2GoUtils.getReadableQuery(this.query, this.getType());
	}
}
