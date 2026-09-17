/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.ontology.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.junit.Test;

import static org.junit.Assert.*;

public class OntologyExporterTest {

	/** A removed ID must cancel the delayed export as well as its registry entry. No timing sleeps are needed. */
	@Test
	public void cleanupCancelsTimerForObsoleteSection() {
		Timer timer = new Timer(true);
		Map<String, Timer> timers = new HashMap<>();
		timers.put(UUID.randomUUID().toString(), timer);
		try {
			OntologyExporter.cancelOutdatedTimers(timers);
			assertTrue(timers.isEmpty());
			assertThrows(IllegalStateException.class, () -> timer.schedule(new TimerTask() {
				@Override public void run() { }
			}, 60000));
		}
		finally { timer.cancel(); }
	}
}
