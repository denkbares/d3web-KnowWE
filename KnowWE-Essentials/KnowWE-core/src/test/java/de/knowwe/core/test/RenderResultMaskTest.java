/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */
package de.knowwe.core.test;

import org.junit.Test;

import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.RenderResultKeyValueStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests that masking HTML hides all JSPWiki-interpretable tokens and that unmasking restores the exact
 * original — so HTML appended via appendHtml* (including attribute values, e.g. file names in data-*
 * attributes) survives the JSPWiki rendering pipeline unchanged. Also tests that
 * {@link RenderResult#appendHtmlPreservingJSPWikiMarkup(String)} keeps intentionally allowed JSPWiki
 * markup (e.g. in SPARQL result cells) visible to JSPWiki while still hiding the HTML structure.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 */
public class RenderResultMaskTest {

	// shared store so mask and unmask use the same mask key (like within one user session)
	private static final RenderResultKeyValueStore STORE = new RenderResultKeyValueStore() {
		private final java.util.Map<String, Object> attributes = new java.util.HashMap<>();

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAttribute(String storeKey) {
			return (T) attributes.get(storeKey);
		}

		@Override
		public void setAttribute(String storeKey, Object value) {
			attributes.put(storeKey, value);
		}
	};

	private static void assertRoundTrip(String html) {
		String masked = RenderResult.mask(html, STORE);
		assertEquals(html, RenderResult.unmask(masked, STORE));
	}

	@Test
	public void maskHidesAllJSPWikiTokens() {
		// JSPWiki must see none of its markup tokens in HTML — some are active even mid-text / inside
		// attribute values (__bold__, {{monospace}}, %%style), others at line starts (||, ----)
		String masked = RenderResult.mask("<span data-file=\"a__b__c {{x}} 50%% e|f||g ---- h\\i [j]\">", STORE);
		for (String token : de.knowwe.core.utils.KnowWEUtils.JSPWIKI_TOKENS) {
			assertFalse("token still visible to JSPWiki: " + token, masked.contains(token));
		}
		assertFalse(masked.contains("<"));
		assertFalse(masked.contains("\""));
	}

	@Test
	public void preservingMarkupHidesHtmlButKeepsJSPWikiMarkup() {
		// for content that is explicitly allowed to contain JSPWiki markup (SPARQL cells with
		// allowJSPWikiMarkup): the HTML structure must stay hidden from JSPWiki, the markup must not,
		// otherwise JSPWiki cannot render it and it ends up on the page as literal text
		String html = "<span data-cell=\"x\">%%(color:gray)Text%% <i>kursiv</i> __bold__ [Link] {{mono}}</span>";
		String masked = RenderResult.maskPreservingJSPWikiMarkup(html, STORE);

		assertTrue(masked.contains("%%(color:gray)"));
		assertTrue(masked.contains("__bold__"));
		assertTrue(masked.contains("{{mono}}"));
		assertFalse(masked.contains("<"));
		assertFalse(masked.contains("\""));
		assertFalse(masked.contains("["));
		assertEquals(html, RenderResult.unmask(masked, STORE));
	}

	@Test
	public void unmaskRestoresOriginalExactly() {
		assertRoundTrip("<span data-file=\"Dies [ist] <ein> __Test__und 'hier' noch''kursiv''.txt\">x</span>");
		assertRoundTrip("plain text without any tokens");
		assertRoundTrip("nested braces {{{code}}} and {{mono}} and [{plugin}]");
		assertRoundTrip("style %%red text/% and \\\\ linebreak");
		assertRoundTrip("|| table | syntax and ---- ruler and C:\\path\\file");
	}
}
