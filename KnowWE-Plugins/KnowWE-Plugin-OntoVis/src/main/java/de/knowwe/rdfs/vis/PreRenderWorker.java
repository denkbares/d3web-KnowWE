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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.d3web.utils.Log;
import de.d3web.utils.Triple;
import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.ontology.compile.OntologyCompilerFinishedEvent;
import de.knowwe.rdf2go.Rdf2GoCoreDestroyEvent;
import de.knowwe.rdfs.vis.markup.PreRenderer;

/**
 * Helper class that handles the asynchronous (pre)rendering of all visualisation sections.
 *
 * @author Albrecht Striffler
 */
public class PreRenderWorker implements EventListener {

	private static final Object mutex = new Object();

	private static PreRenderWorker instance;

	private final Map<String, Triple<Future, PreRenderer, Section<?>>> cache;
	private final ExecutorService executor;

	public static PreRenderWorker getInstance() {
		synchronized (mutex) {
			if (instance == null) {
				instance = new PreRenderWorker();
			}
		}
		return instance;
	}

	private PreRenderWorker() {
		cache = new HashMap<>();
		executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
		EventManager.getInstance().registerListener(this);
	}

	/**
	 * Queues a rendering task for the given section.
	 */
	private Future getPreRenderFuture(Section<? extends Type> section, UserContext user, PreRenderer preRenderer) {
		synchronized (mutex) {
			String fileID = preRenderer.getCacheFileID(section);
			Triple<Future, PreRenderer, Section<?>> triple = cache.get(fileID);
			if (triple == null) {
				Future renderJobFuture = executor.submit(() -> preRenderer.preRender(section, user));
				triple = new Triple<>(renderJobFuture, preRenderer, section);
				cache.put(fileID, triple);
			}
			return triple.getA();
		}
	}

	/**
	 * Starts and caches a prerender job for each section. Waits until the prerendering is done. If this method is
	 * called multiple times for the same section, the prerendering will only be done once, until the cache is cleared.
	 */
	public void handlePreRendering(Section<?> section, UserContext user, PreRenderer preRenderer) {
		// create a new rendering task or get currently running task
		Future renderJobFuture = getPreRenderFuture(section, user, preRenderer);

		try {
			if (renderJobFuture != null) {
				// wait for the rendering to complete
				renderJobFuture.get();
			}
		}
		catch (ExecutionException | InterruptedException e) {
			Log.severe("Exception while generating and caching graphs", e);
		}

	}

	public void clearCache(Section<?> section) {
		synchronized (mutex) {
			Iterator<Map.Entry<String, Triple<Future, PreRenderer, Section<?>>>> entryIterator = cache.entrySet().iterator();
			String keyToRemove = null;
			while(entryIterator.hasNext()) {
				// find entry in cache
				Map.Entry<String, Triple<Future, PreRenderer, Section<?>>> entry = entryIterator.next();
				if(entry.getValue().getC().equals(section)) {
					keyToRemove = entry.getKey();
				}
			}
			if(keyToRemove != null) {
				// remove entry from cache
				cache.remove(keyToRemove);
			}
		}
	}

	public void clearCache() {
		synchronized (mutex) {
			if (!cache.isEmpty()) {
				for (Triple<Future, PreRenderer, Section<?>> value : cache.values()) {
					value.getA().cancel(true);
					value.getB().cleanUp(value.getC());
				}
				cache.clear();
			}
		}
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		Collection<Class<? extends Event>> events = new ArrayList<>(1);
		events.add(OntologyCompilerFinishedEvent.class);
		return events;
	}

	@Override
	public void notify(Event event) {
		// TODO: only clean for current compiler!
		clearCache();
	}
}
