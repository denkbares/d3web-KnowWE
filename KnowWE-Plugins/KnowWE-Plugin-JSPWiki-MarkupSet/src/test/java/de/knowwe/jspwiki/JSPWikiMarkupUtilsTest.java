/*
 * Copyright (C) 2026 denkbares GmbH
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
package de.knowwe.jspwiki;

import org.junit.Test;

import com.denkbares.utils.ContentDisposition;

import static org.junit.Assert.assertEquals;

/**
 * Tests the file names suggested for downloads of sections, e.g. the xlsx export of sparql and wiki tables.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 30.09.2026
 */
public class JSPWikiMarkupUtilsTest {

	@Test
	public void keepsUmlauts() {
		// KNOWSEC-269: the umlauts were removed, resulting in "Liste Stoffbersicht Screening - Luft PMT"
		assertEquals("Liste Stoffübersicht Screening - Läuft PMT",
				JSPWikiMarkupUtils.toFileName("Liste Stoffübersicht Screening - Läuft PMT"));
		assertEquals("Größe Äpfel Öl Überschuss", JSPWikiMarkupUtils.toFileName("Größe Äpfel Öl Überschuss"));
		assertEquals("Élève à côté", JSPWikiMarkupUtils.toFileName("Élève à côté"));
	}

	@Test
	public void keepsDecomposedUmlauts() {
		// "ä" as "a" followed by the combining diaeresis, as e.g. file systems of macOS provide it
		assertEquals("Läuft", JSPWikiMarkupUtils.toFileName("Läuft"));
	}

	@Test
	public void keepsAsciiNames() {
		assertEquals("Page_1 - Results 2026", JSPWikiMarkupUtils.toFileName("Page_1 - Results 2026"));
	}

	@Test
	public void removesMarkupAndSpecialCharacters() {
		assertEquals("Ergebnisse", JSPWikiMarkupUtils.toFileName("%%(color: red)Ergebnisse%"));
		assertEquals("Stoffe Übersicht", JSPWikiMarkupUtils.toFileName("Stoffe: \"Übersicht\""));
		assertEquals("abetc", JSPWikiMarkupUtils.toFileName("a/b\\\\..\\etc"));
		assertEquals("Test script", JSPWikiMarkupUtils.toFileName("  'Test' <script>?*| "));
	}

	@Test
	public void limitsLength() {
		assertEquals(100, JSPWikiMarkupUtils.toFileName("ä".repeat(150)).length());
	}

	@Test
	public void headerOffersUmlauts() {
		// the browser prefers filename* over the ascii fallback of filename
		String fileName = JSPWikiMarkupUtils.toFileName("Liste Stoffübersicht Screening - Läuft PMT") + ".xlsx";
		assertEquals("attachment; filename=\"Liste Stoff_bersicht Screening - L_uft PMT.xlsx\"; "
					 + "filename*=UTF-8''Liste%20Stoff%C3%BCbersicht%20Screening%20-%20L%C3%A4uft%20PMT.xlsx",
				ContentDisposition.attachment(fileName));
	}
}
