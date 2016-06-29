/*
 * Copyright (C) 2012 denkbares GmbH, Germany
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
package de.knowwe.instantedit.table.test;

import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.instantedit.table.TableSectionFinder;

/**
 * 
 * @author volker_belli
 * @created 16.03.2012
 */
public class TableSectionFinderTest {

	@Test
	public void notMatching() {
		assertMatchCount("foo \n\rbla\r\r\n\r", 0);
		assertMatchCount("\n |foo|bla\n\n", 0);
	}

	@Test
	public void matchingLF() {
		String wikiPage = "foo\n||a||b\n|c|d\n\n|1|2\n|3|4\n\n|\n";
		assertMatchCount(wikiPage, 3);
		assertMatches(wikiPage, 0, 4, 16);
		assertMatches(wikiPage, 1, 17, 27);
		assertMatches(wikiPage, 2, 28, 30);
	}

	@Test
	public void matchingLFCR() {
		String wikiPage = "foo\n\r||a||b\n\r|c|d\n\r\n\r|1|2\n\r|3|4\n\r\n\r|\n\r";
		assertMatchCount(wikiPage, 3);
		assertMatches(wikiPage, 0, 5, 19);
		assertMatches(wikiPage, 1, 21, 33);
		assertMatches(wikiPage, 2, 35, 38);
	}

	@Test
	public void matchingCRLF() {
		String wikiPage = "foo\r\n||a||b\r\n|c|d\r\n\r\n|1|2\r\n|3|4\r\n\r\n|\r\n";
		assertMatchCount(wikiPage, 3);
		assertMatches(wikiPage, 0, 5, 20);
		assertMatches(wikiPage, 1, 21, 34);
		assertMatches(wikiPage, 2, 35, 38);
	}

	@Test
	public void matchingFullString() {
		String wikiPage = "||1||2\n||3||4";
		assertMatchCount(wikiPage, 1);
		assertMatches(wikiPage, 0, 0, wikiPage.length());
	}

	private void assertMatchCount(String wikiPage, int count) {
		TableSectionFinder finder = new TableSectionFinder();
		List<SectionFinderResult> all = finder.lookForSections(wikiPage, null, null);
		Assert.assertEquals("wrong number of matches", count, all.size());
	}

	private void assertMatches(String wikiPage, int index, int start, int end) {
		TableSectionFinder finder = new TableSectionFinder();
		List<SectionFinderResult> all = finder.lookForSections(wikiPage, null, null);

		if (all.isEmpty()) return;
		SectionFinderResult result = all.get(index);
		Assert.assertEquals("table start not matched", start, result.getStart());
		Assert.assertEquals("table end not matched", end, result.getEnd());
	}
}
