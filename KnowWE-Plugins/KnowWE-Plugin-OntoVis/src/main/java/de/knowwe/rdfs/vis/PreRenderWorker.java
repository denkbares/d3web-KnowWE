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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class PreRenderWorker {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreRenderWorker.class);

	private static final Object mutex = new Object();

	private static PreRenderWorker instance;

	private final ExecutorService executor;

	private PreRenderWorker() {
		executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
	}

	public static PreRenderWorker getInstance() {
		synchronized (mutex) {
			if (instance == null) {
				instance = new PreRenderWorker();
			}
		}
		return instance;
	}

	/**
	 * Queues a rendering task for the given section.
	 */
	private <Artefact extends CleanableArtefact> Future<Artefact> getPreRenderFuture(Section<? extends Type> section, UserContext user, PreRenderer<Artefact> preRenderer) {
		synchronized (mutex) {
			return section.computeIfAbsent(null, this.getClass().getName(),
					(c, s) -> executor.submit(() -> preRenderer.preRender(section, user)));
		}
	}

	/**
	 * Starts and caches a prerender job for each section. Waits until the prerendering is done. If this method is
	 * called multiple times for the same section, the prerendering will only be done once, until the cache is cleared.
	 */
	public <Artefact extends CleanableArtefact> Artefact getPreRenderedArtefact(Section<?> section, UserContext user, PreRenderer<Artefact> preRenderer) {
		// create a new rendering task or get currently running task
		try {
			return getPreRenderFuture(section, user, preRenderer).get();
		}
		catch (ExecutionException | InterruptedException e) {
			LOGGER.error("Exception while generating and caching graphs", e);
		}
		return null;
	}

	public void clearCache(Section<?> section) {
		synchronized (mutex) {
			Future<CleanableArtefact> future = section.removeObject(null, this.getClass().getName());
			if (future == null) return;
			try {
				CleanableArtefact artefact = future.get();
				if (artefact == null) return;
				artefact.cleanUp();
			}
			catch (ExecutionException | InterruptedException e) {
				LOGGER.error("Exception while generating and caching graphs", e);
			}
		}
	}
}
