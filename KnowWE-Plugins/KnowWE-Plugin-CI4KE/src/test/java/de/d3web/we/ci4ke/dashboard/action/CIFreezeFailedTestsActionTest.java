/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.d3web.we.ci4ke.dashboard.action;

import org.junit.Test;

import static org.junit.Assert.*;

/** Frozen report keys must ignore both legacy and lifecycle-based Section IDs. */
public class CIFreezeFailedTestsActionTest {

	@Test
	public void removesLegacyAndCurrentSectionIds() {
		for (String id : new String[] { "a", "1234abcd", "8d1246ced95736ae84f74f9b7e420311" }) {
			assertEquals("[Label|Page]", CIFreezeFailedTestsAction.normalizeLink("[Label|Page#" + id + "]"));
		}
		assertEquals("[Page] and [Other]", CIFreezeFailedTestsAction.normalizeLink(
				"[Page#1234abcd] and [Other#8d1246ced95736ae84f74f9b7e420311]"));
	}

	@Test
	public void headerKeySurvivesSectionVersionAndIdFormatChanges() {
		String frozen = CIFreezeFailedTestsAction.normalizeHeader("__WARNING__: 1 warning was found in [KB#1234abcd]");
		assertEquals(frozen, CIFreezeFailedTestsAction.normalizeHeader(
				"__WARNING__: 2 warnings were found in [KB#8d1246ced95736ae84f74f9b7e420311]"));
		assertEquals(frozen, CIFreezeFailedTestsAction.normalizeHeader(
				"__WARNING__: 3 warnings were found in [KB#d5b06476a0ac3381861fd852c9a861f6]"));
		assertNotEquals(frozen, CIFreezeFailedTestsAction.normalizeHeader(
				"__WARNING__: 1 warning was found in [Other KB#1234abcd]"));
	}

	@Test
	public void preservesOtherAnchorsAndText() {
		for (String text : new String[] {
				"[Page#section-heading]", "[Page#123456789]", "[Page#" + "a".repeat(31) + "]",
				"[Page#" + "a".repeat(33) + "]", "[Page#]", "[Page]", "plain #1234abcd text"
		}) {
			assertEquals(text, CIFreezeFailedTestsAction.normalizeLink(text));
		}
	}
}
