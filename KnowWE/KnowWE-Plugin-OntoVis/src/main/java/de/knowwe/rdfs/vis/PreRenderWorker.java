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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.d3web.utils.Log;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.rdfs.vis.markup.PreRenderer;
import de.knowwe.rdfs.vis.util.Utils;

/**
 * Helper class that handles the asynchronous (pre)rendering of all visualisation-sections.
 *
 * @author Johanna Latt
 * @created 26.08.2014
 */
public class PreRenderWorker {

	private static PreRenderWorker prw;

	private static Map<String, Future> workerPool;
	private static ExecutorService es;
	private static ExecutorService priorityes;

	public static PreRenderWorker getInstance() {
		if (prw == null) {
			prw = new PreRenderWorker();
			workerPool = Collections.synchronizedMap(new HashMap<>());
			es = Executors.newFixedThreadPool(Math.max(1,
					Runtime.getRuntime().availableProcessors() - 2));
			priorityes = Executors.newSingleThreadScheduledExecutor();
		}
		return prw;
	}

	/**
	 * Starts a rendering task for the given section (and assumes that @cancelAllRunningPreRenderTasks has been called
	 * before).
	 *
	 * @param section
	 * @return
	 */
	public Future queueSectionPreRendering(PreRenderer r, Section<? extends Type> section, UserContext user, RenderResult string, boolean priority) {
		Runnable renderSection = new Runnable() {
			@Override
			public void run() {
				r.preRender(section, user, string);
			}
		};
		Future futureRenderTask;
		if (priority) {
			futureRenderTask = priorityes.submit(renderSection);
		}
		else {
			futureRenderTask = es.submit(renderSection);
		}
		workerPool.put(Utils.getFileID(section), futureRenderTask);
		return futureRenderTask;
	}

	/**
	 * Starts a rendering task for the given section IF the section is not currently rendering already. In any case it
	 * is waited for the new or already running rendering task and afterwards the resulting file is displayed.
	 *
	 * @param section
	 * @param user
	 * @param string
	 */
	public void preRenderSectionAndWait(PreRenderer r, Section<?> section, UserContext user, RenderResult string) {
		// create a new rendering task or get currently running task
		Future renderJob;
		boolean cache = false;
		if (isPreRendering(section)) {
			cache = true;
			renderJob = getRunningPreRenderTaskFor(section);
		}
		else {
			renderJob = queueSectionPreRendering(r, section, user, string, true);
		}

		// wait for the rendering to complete
		try {
			if (renderJob != null) {
				renderJob.get();

				// if the rendering was already running the file only has to be cached now
				if (cache) {
					r.cacheGraph(section, string);
				}
			}
		}
		catch (ExecutionException | InterruptedException e) {
			Log.severe("Exception while generating and caching graphs", e);
		}

	}

	public boolean isPreRendering(Section<? extends Type> section) {
		String fileID = Utils.getFileID(section);
		return workerPool.containsKey(fileID);
	}

	public Future getRunningPreRenderTaskFor(Section<? extends Type> section) {
		if (workerPool.containsKey(Utils.getFileID(section))) {
			return workerPool.get(Utils.getFileID(section));
		}
		return null;
	}

	public void cancelAllRunningPreRenderTasks() {
		if (!workerPool.isEmpty()) {
			for (Future f : workerPool.values()) {
				f.cancel(true);
			}
			workerPool.clear();
		}
	}
}
