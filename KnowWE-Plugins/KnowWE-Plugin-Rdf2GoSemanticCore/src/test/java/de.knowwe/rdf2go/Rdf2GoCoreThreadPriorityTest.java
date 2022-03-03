/*
 * Copyright (C) 2019 denkbares GmbH, Germany
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

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.util.concurrent.AtomicDouble;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.denkbares.plugin.test.InitPluginManager;
import com.denkbares.semanticcore.config.RepositoryConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.denkbares.utils.Stopwatch;

import static junit.framework.TestCase.assertTrue;

/**
 * Test that sparql threads with higher priority get executed first
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.12.2019
 */
public class Rdf2GoCoreThreadPriorityTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(Rdf2GoCoreThreadPriorityTest.class);

	public static final int PARALLEL_THREADS = 100;

	@BeforeClass
	public static void init() throws IOException {
		InitPluginManager.init();
	}

	@Test
	public void testPriorityQueue() throws RepositoryException, RDFParseException, IOException, InterruptedException {
		Rdf2GoCore core = new Rdf2GoCore("http://localhost:8080/KnowWE/Wiki.jsp?page=", RepositoryConfigs.find("OWL_HORST_OPTIMIZED_WITH_PROPERTY_CHAINS"));
		core.readFrom(this.getClass().getResourceAsStream("dbpedia_2016-10.nt"), RDFFormat.NTRIPLES);

		ExecutorService executorService = Executors.newCachedThreadPool();

		AtomicDouble lastPriority = new AtomicDouble();
		AtomicLong failureCount = new AtomicLong(0);
		AtomicLong finishedCount = new AtomicLong(0);
		// Query that takes between 200 to 1000 ms
		String query = "SELECT * WHERE { ?x ?y ?z . ?a ?b ?c . FILTER (?x = ?a) } LIMIT 100000 ";
		for (int i = 0; i < PARALLEL_THREADS; i++) {
			final int index = i;
			executorService.execute(() -> {
				Rdf2GoCore.Options options = new Rdf2GoCore.Options().noCache().timeout(10000000).priority(Math.random());
				core.sparqlSelect(query, options);
				Stopwatch stopwatch = new Stopwatch();
				synchronized (lastPriority) {
					if (options.priority < lastPriority.get()) {
						failureCount.incrementAndGet();
						LOGGER.error("Query with high priority (" + lastPriority.get() + ") finished later than one with low priority (" + options.priority + ").");
					}
					lastPriority.set(options.priority);
				}
				stopwatch.log("Query " + index + ", (" + finishedCount.incrementAndGet() + "/" + PARALLEL_THREADS + "): " + options.priority);
			});
		}
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);
		core.close();

		int maxExpected = (int) (PARALLEL_THREADS * 0.25);
		String result = "Expected: < " + maxExpected + ", was: " + failureCount.get();
		assertTrue("More than expected queries finished in the wrong order given by its priority. " + result,
				failureCount.get() < maxExpected);
		LOGGER.info("Success: " + result);
	}
}
