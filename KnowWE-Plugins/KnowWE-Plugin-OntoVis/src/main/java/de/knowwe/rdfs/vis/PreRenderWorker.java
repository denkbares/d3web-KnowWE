/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.rdfs.vis;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Environment;
import de.knowwe.core.EnvironmentService;
import de.knowwe.core.ServiceKey;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.rdfs.vis.markup.PreRenderer;
import de.knowwe.visualization.CleanableArtefact;

/**
 * Helper class that handles the asynchronous (pre)rendering of all visualisation sections.
 *
 * @author Albrecht Striffler
 */
public class PreRenderWorker implements EnvironmentService {
	public static final ServiceKey<PreRenderWorker> SERVICE = ServiceKey.of(PreRenderWorker.class, environment -> new PreRenderWorker());

	private static final Logger LOGGER = LoggerFactory.getLogger(PreRenderWorker.class);

	private static final String KEY = PreRenderWorker.class.getName();
	private static final String LOCK_KEY = KEY + ".lock";

	private final ExecutorService executor;

	/**
	 * The maximum time to wait for a pre-rendering. The pre-rendering runs external processes and queries, a hanging
	 * one must not keep the rendering threads waiting forever.
	 */
	private final long maxWaitMillis;

	private PreRenderWorker() {
		this(TimeUnit.MINUTES.toMillis(10));
	}

	/**
	 * Creates a worker waiting at most the specified time for a pre-rendering. For tests, which cannot wait for the
	 * default time.
	 */
	PreRenderWorker(long maxWaitMillis) {
		this.maxWaitMillis = maxWaitMillis;
		executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
	}

	/**
	 * Returns the pre-render worker of the given environment. Every environment has its own worker (and its own
	 * executor), so that closed environments cannot leak rendering threads into new ones.
	 *
	 * @param environment the environment to get the worker for
	 * @return the environment's pre-render worker
	 */
	public static PreRenderWorker of(@org.jetbrains.annotations.NotNull Environment environment) {
		return environment.getService(PreRenderWorker.SERVICE);
	}

	@Override
	public void close() {
		// queued tasks will never run, cancel them, so nobody waits for them
		for (Runnable task : executor.shutdownNow()) {
			if (task instanceof Future<?> future) future.cancel(false);
		}
	}

	/**
	 * Returns the lock ordering the clearing and the new pre-rendering of the specified section: a new pre-rendering
	 * starts only after the former artefact of the section is cleaned up, as both may use the same files. The lock is
	 * per section, so a slow section does not hold up the others.
	 */
	private static Object getLock(Section<?> section) {
		return section.computeIfAbsent(null, LOCK_KEY, (c, s) -> new Object());
	}

	/**
	 * Queues a rendering task for the given section.
	 */
	private <Artefact extends CleanableArtefact> Future<Artefact> getPreRenderFuture(Section<? extends Type> section, UserContext user, PreRenderer<Artefact> preRenderer) {
		synchronized (getLock(section)) {
			return section.computeIfAbsent(null, KEY,
					(c, s) -> executor.submit(() -> preRenderer.preRender(section, user)));
		}
	}

	/**
	 * Starts and caches a prerender job for each section. Waits until the prerendering is done, but at most the
	 * maximum waiting time; returns null if the prerendering fails or does not finish in time. If this method is
	 * called multiple times for the same section, the prerendering will only be done once, until the cache is cleared.
	 */
	public <Artefact extends CleanableArtefact> Artefact getPreRenderedArtefact(Section<?> section, UserContext user, PreRenderer<Artefact> preRenderer) {
		// create a new rendering task or get currently running task
		try {
			return getPreRenderFuture(section, user, preRenderer).get(maxWaitMillis, TimeUnit.MILLISECONDS);
		}
		catch (TimeoutException e) {
			// the task stays cached, a later rendering gets its result once it is done
			LOGGER.warn("Pre-rendering of a visualization on page {} did not finish within {} ms", section.getTitle(), maxWaitMillis);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Interrupted while waiting for the generation of graphs", e);
		}
		catch (ExecutionException | CancellationException e) {
			LOGGER.error("Exception while generating and caching graphs", e);
		}
		return null;
	}

	public void clearCache(Section<?> section) {
		synchronized (getLock(section)) {
			Future<CleanableArtefact> future = section.removeObject(null, KEY);
			if (future == null) return;
			try {
				CleanableArtefact artefact = future.get(maxWaitMillis, TimeUnit.MILLISECONDS);
				if (artefact == null) return;
				artefact.cleanUp();
			}
			catch (TimeoutException e) {
				// it will not be used any more, stop it, so the section can be pre-rendered anew
				future.cancel(true);
				LOGGER.warn("Pre-rendering of a visualization on page {} did not finish within {} ms, cancelled it", section.getTitle(), maxWaitMillis);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.error("Interrupted while waiting for the generation of graphs", e);
			}
			catch (ExecutionException | CancellationException e) {
				LOGGER.error("Exception while generating and caching graphs", e);
			}
		}
	}
}
