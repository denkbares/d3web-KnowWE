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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.LockSupport;

import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.jetbrains.annotations.NotNull;

import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;

/**
 * Future for SPARQL queries with some addition control to stop it and get info about state.
 */
class SparqlTask extends FutureTask<Object> implements Comparable<SparqlTask> {

	private long startTime = Long.MIN_VALUE;
	private final SparqlCallable callable;
	private final double priority;
	private Thread thread = null;
	private int size = 50; // reasonable default value till the result is set
	private long runTime = Long.MIN_VALUE;

	SparqlTask(SparqlCallable callable, double priority) {
		super(callable);
		this.callable = callable;
		this.priority = priority;
	}

	public double getPriority() {
		return priority;
	}

	long getTimeOutMillis() {
		return this.callable.getTimeOutMillis();
	}

	public synchronized void setSize(int size) {
		this.size = size;
	}

	public synchronized int getSize() {
		return this.size;
	}

	synchronized long getRunDuration() {
		return hasStarted()
				? this.runTime == Long.MIN_VALUE
						? System.currentTimeMillis() - this.startTime
						: this.runTime
				: 0;
	}

	synchronized boolean hasStarted() {
		return this.startTime != Long.MIN_VALUE;
	}

	synchronized boolean isAlive() {
		return !hasStarted() || (this.thread != null && this.thread.isAlive());
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		boolean canceled = super.cancel(mayInterruptIfRunning);
		if (canceled) {
			Log.warning("SPARQL query was canceled after "
					+ Strings.getDurationVerbalization(getRunDuration())
					+ ": " + callable.getReadableQuery());
			ThreadLocalCleaner.cleanThreadLocals();
		}
		return canceled;
	}

	public synchronized void stop() {
		if (this.thread != null) {
			//noinspection deprecation
			this.thread.stop();
			LockSupport.unpark(this.thread);
			this.thread = null;
			ThreadLocalCleaner.cleanThreadLocals();
			Log.warning("SPARQL query was stopped after "
					+ Strings.getDurationVerbalization(getRunDuration())
					+ ": " + callable.getReadableQuery());
		}
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		return super.get();
	}

	@Override
	public void run() {
		synchronized (this) {
			this.thread = Thread.currentThread();
			this.startTime = System.currentTimeMillis();
		}
		try {
			// deactivated, since it causes severe issues with GraphDB right now
//				sparqlReaperPool.execute(new SparqlTaskReaper(this));
			super.run();
		}
		finally {
			synchronized (this) {
				this.thread = null;
			}
			this.runTime = System.currentTimeMillis() - this.startTime;
			ThreadLocalCleaner.cleanThreadLocals();
		}
	}

	@Override
	protected void set(Object o) {
		super.set(o);
		if (this.callable.isCached()) {
			setSize(getResultSize(o));
		}
		if (getRunDuration() > 1000) {
			Log.info("SPARQL query finished after "
					+ Strings.getDurationVerbalization(getRunDuration())
					+ ": " + callable.getReadableQuery());
		}
	}

	@Override
	public int compareTo(@NotNull SparqlTask o) {
		return Double.compare(this.priority, o.priority);
	}

	private int getResultSize(Object result) {
		if (result instanceof TupleQueryResult) {
			TupleQueryResult cacheResult = (TupleQueryResult) result;
			try {
				return cacheResult.getBindingNames().size() * cacheResult.getBindingSets().size();
			}
			catch (QueryEvaluationException ignore) {
			}
		}
		return 1;
	}
}
