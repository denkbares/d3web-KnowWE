/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.knowwe.d3web.ontology.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OntologyBridgeTest {

	@Test(timeout = 10000)
	public void supportsConcurrentRegistrationLookupAndRemoval() throws Exception {
		int workerCount = 8;
		int registrationsPerWorker = 500;
		String testRunId = UUID.randomUUID().toString();
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(workerCount);
		List<Future<?>> tasks = new ArrayList<>();
		try {
			for (int worker = 0; worker < workerCount; worker++) {
				int workerId = worker;
				tasks.add(executor.submit(() -> {
					assertTrue(start.await(2, TimeUnit.SECONDS));
					for (int registration = 0; registration < registrationsPerWorker; registration++) {
						String suffix = testRunId + '-' + workerId + '-' + registration;
						String d3webSectionId = "d3web-" + suffix;
						String ontologySectionId = "ontology-" + suffix;
						OntologyBridge.registerBridge(d3webSectionId, ontologySectionId);
						assertEquals(ontologySectionId, OntologyBridge.getMappedOntologySectionId(d3webSectionId));
						assertEquals(d3webSectionId, OntologyBridge.getMappedD3webSectionId(ontologySectionId));
						OntologyBridge.unregisterBridge(d3webSectionId);
						assertNull(OntologyBridge.getMappedOntologySectionId(d3webSectionId));
					}
					return null;
				}));
			}
			start.countDown();
			for (Future<?> task : tasks) {
				task.get(5, TimeUnit.SECONDS);
			}
		}
		finally {
			executor.shutdownNow();
			assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
		}
	}
}
