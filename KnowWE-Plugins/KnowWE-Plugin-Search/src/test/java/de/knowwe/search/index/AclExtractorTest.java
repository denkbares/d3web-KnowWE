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

package de.knowwe.search.index;

import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Guards who the index believes may read a page.
 * <p>
 * Both directions are dangerous here. Reading a grant that is not there shows a page to someone who may not see it;
 * missing one hides a page from someone who may.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class AclExtractorTest {

	private final AclExtractor extractor = new AclExtractor();

	@Test
	public void aPageWithoutAnAclIsNotRestricted() {
		assertTrue(extractor.viewPrincipals("!! Kapitel\nGanz normaler Text.\n").isEmpty());
	}

	@Test
	public void anAllowViewNamesWhoMayRead() {
		assertEquals(Set.of("Admins"), extractor.viewPrincipals("[{ALLOW view Admins}]\nText.\n"));
	}

	@Test
	public void severalPrincipalsAreSeparatedByCommas() {
		assertEquals(Set.of("Admins", "Albrecht", "Authenticated"),
				extractor.viewPrincipals("[{ALLOW view Admins, Albrecht,Authenticated}]\n"));
	}

	@Test
	public void editingImpliesReading() {
		// JSPWiki derives view from comment, comment from edit, edit from modify -- someone who may write may read
		assertEquals(Set.of("Autoren"), extractor.viewPrincipals("[{ALLOW edit Autoren}]\n"));
		assertEquals(Set.of("Autoren"), extractor.viewPrincipals("[{ALLOW modify Autoren}]\n"));
		assertEquals(Set.of("Autoren"), extractor.viewPrincipals("[{ALLOW comment Autoren}]\n"));
		assertEquals(Set.of("Autoren"), extractor.viewPrincipals("[{ALLOW upload Autoren}]\n"));
	}

	@Test
	public void deletingAndRenamingDoNotImplyReading() {
		// the trap: taken as a view grant, "[{ALLOW delete Admins}]" would hide the page from everybody else
		assertTrue(extractor.viewPrincipals("[{ALLOW delete Admins}]\n").isEmpty());
		assertTrue(extractor.viewPrincipals("[{ALLOW rename Admins}]\n").isEmpty());
	}

	@Test
	public void aDeleteGrantDoesNotSwallowTheViewGrantBesideIt() {
		assertEquals(Set.of("Leser"),
				extractor.viewPrincipals("[{ALLOW delete Admins}]\n[{ALLOW view Leser}]\nText.\n"));
	}

	@Test
	public void theSpellingOfTheEntryMayVary() {
		assertEquals(Set.of("Admins"), extractor.viewPrincipals("[{   ALLOW   view   Admins   }]"));
	}

	@Test
	public void severalEntriesAddUp() {
		assertEquals(Set.of("Admins", "Leser"),
				extractor.viewPrincipals("[{ALLOW view Admins}]\nText\n[{ALLOW edit Leser}]\n"));
	}
}
