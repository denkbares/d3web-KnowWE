/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */
package de.knowwe.core.utils;

import org.junit.Test;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.wikiConnector.NotAuthorizedException;

import static org.junit.Assert.*;

/**
 * Pins that a permission check without a resource refuses the request instead of failing on the missing resource.
 * Actions read the page or the section they check from the request, so a request that does not carry it must get a
 * refusal that says so, not an internal error.
 *
 * @author Konstantin Herud (denkbares GmbH)
 * @created 15.09.2026
 */
public class PermissionAssertionTest {

	@Test
	public void refusesViewWithoutPage() {
		NotAuthorizedException refusal = assertThrows(NotAuthorizedException.class,
				() -> KnowWEUtils.assertCanView((String) null, null));
		assertTrue(refusal.getMessage(), refusal.getMessage().contains("KWiki_Topic"));
	}

	@Test
	public void refusesWriteWithoutPage() {
		NotAuthorizedException refusal = assertThrows(NotAuthorizedException.class,
				() -> KnowWEUtils.assertCanWrite("   ", null));
		assertTrue(refusal.getMessage(), refusal.getMessage().contains("KWiki_Topic"));
	}

	@Test
	public void refusesUploadWithoutPage() {
		assertThrows(NotAuthorizedException.class, () -> KnowWEUtils.assertCanUpload(null, null));
	}

	@Test
	public void refusesViewWithoutSection() {
		assertThrows(NotAuthorizedException.class, () -> KnowWEUtils.assertCanView((Section<?>) null, null));
	}

	@Test
	public void refusesWriteWithoutSection() {
		assertThrows(NotAuthorizedException.class, () -> KnowWEUtils.assertCanWrite((Section<?>) null, null));
	}
}
