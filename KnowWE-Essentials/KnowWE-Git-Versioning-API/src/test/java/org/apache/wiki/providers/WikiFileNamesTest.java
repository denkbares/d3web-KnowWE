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

package org.apache.wiki.providers;

import org.junit.jupiter.api.Test;

import static org.apache.wiki.providers.WikiFileNames.fileNameOfPage;
import static org.apache.wiki.providers.WikiFileNames.isPageFile;
import static org.apache.wiki.providers.WikiFileNames.pageNameOfFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mapping between page names and the file names the file system providers write them to. The expected file names are
 * the ones {@code AbstractFileProvider.mangleName} produces for a UTF-8 wiki.
 */
class WikiFileNamesTest {

	@Test
	void plainNameOnlyGetsTheExtension() {
		assertRoundTrip("MyPage", "MyPage.txt");
	}

	@Test
	void spaceBecomesPlus() {
		assertRoundTrip("My Page", "My+Page.txt");
	}

	@Test
	void nonAsciiIsPercentEncodedAsUtf8() {
		// ISO-8859-1 would yield a single byte %D6 here, the providers encode UTF-8 by configuration
		assertRoundTrip("Ölstand", "%C3%96lstand.txt");
	}

	@Test
	void plusInAPageNameIsEscapedSoItSurvivesTheRoundTrip() {
		assertRoundTrip("C++", "C%2B%2B.txt");
	}

	@Test
	void slashIsEscapedSoTheNameStaysASingleFile() {
		assertRoundTrip("Foo/Bar", "Foo%2FBar.txt");
	}

	@Test
	void leadingDotIsEscapedSoTheFileIsNotHidden() {
		assertRoundTrip(".hidden", "%2Ehidden.txt");
	}

	@Test
	void aPageNamedLikeAFileGetsTheExtensionAnyway() {
		// no "already mangled" guessing, a page may legitimately be called Foo.txt
		assertRoundTrip("Foo.txt", "Foo.txt.txt");
	}

	@Test
	void onlyATrailingExtensionIsStripped() {
		assertEquals("Notes.txt.backup", pageNameOfFile("Notes.txt.backup"));
		assertEquals("Notes.txt.backup", pageNameOfFile("Notes.txt.backup.txt"));
	}

	@Test
	void fileWithoutExtensionIsDecodedUnchanged() {
		assertEquals("README", pageNameOfFile("README"));
	}

	@Test
	void onlyTopLevelTextFilesArePageFiles() {
		assertTrue(isPageFile("MyPage.txt"));
		assertFalse(isPageFile("MyPage-att/image.png"));
		assertFalse(isPageFile("MyPage-att/document.txt"));
		assertFalse(isPageFile("dependencies.json"));
	}

	private void assertRoundTrip(String pageName, String fileName) {
		assertEquals(fileName, fileNameOfPage(pageName));
		assertEquals(pageName, pageNameOfFile(fileName));
	}
}
