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
package de.knowwe.include;

import org.junit.Test;

import static de.knowwe.include.InterWikiImportAnnotationFormat.normalizeWiki;
import static org.junit.Assert.assertEquals;

public class InterWikiImportNormalizeWikiTest {

	@Test
	public void blank() {
		assertEquals("", normalizeWiki(null));
		assertEquals("", normalizeWiki("  "));
	}

	@Test
	public void addsSchemeAndTrailingSlash() {
		assertEquals("https://leo2-turm.ilme.dev/", normalizeWiki("leo2-turm.ilme.dev"));
		assertEquals("http://localhost:8080/KnowWE/", normalizeWiki(" http://localhost:8080/KnowWE "));
	}

	@Test
	public void lowerCasesSchemeAndHost() {
		assertEquals("https://leo2-a8-turm.ilme.dev/", normalizeWiki("https://leo2-A8-turm.ilme.dev/"));
		assertEquals("https://leo2-fahrgestell.ilme.dev/", normalizeWiki("leo2-Fahrgestell.ilme.dev"));
		assertEquals("https://wiki.ilme.dev/", normalizeWiki("HTTPS://Wiki.ILME.dev"));
		assertEquals(normalizeWiki("https://leo2-A8-turm.ilme.dev/"), normalizeWiki("https://leo2-a8-turm.ilme.dev"));
	}

	@Test
	public void keepsPathAndUserInfo() {
		assertEquals("https://wiki.ilme.dev/KnowWE/", normalizeWiki("https://Wiki.ilme.dev/KnowWE"));
		assertEquals("https://env+WIKI_User:env+Wiki_Pass@wiki.ilme.dev/",
				normalizeWiki("https://env+WIKI_User:env+Wiki_Pass@Wiki.ilme.dev/"));
		assertEquals("http://User:Secret@localhost:8080/KnowWE/", normalizeWiki("http://User:Secret@LocalHost:8080/KnowWE"));
	}
}
