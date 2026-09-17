/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */
package de.knowwe.core.kdom.rendering;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.denkbares.strings.Strings;
import de.knowwe.core.user.UserContext;

import static org.junit.Assert.*;

/**
 * Tests the single-pass mask/unmask implementation against a naive reference over the token table, and
 * unmask additionally against the legacy sequential-replace behavior.
 *
 * @author Konstantin Herud (denkbares GmbH)
 * @created 03.08.2026
 */
public class RenderResultTest {

	// the token table RenderResult is expected to derive, longest first: the html structural characters
	// plus KnowWEUtils.JSPWIKI_TOKENS. Written out on purpose, so that a change to either of the two
	// sources surfaces here as a failing reference comparison instead of passing silently.
	private static final String[] HTML = {
			"----", "{{{", "}}}", "[{", "}]", "{{", "}}", "%%", "__", "''", "||",
			"\"", "'", ">", "<", "[", "]", "\\", "|" };

	private static class TestKeyValueStore implements RenderResultKeyValueStore {
		private final Map<String, Object> attributes = new HashMap<>();

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAttribute(String storeKey) {
			return (T) attributes.get(storeKey);
		}

		@Override
		public void setAttribute(String storeKey, Object value) {
			attributes.put(storeKey, value);
		}
	}

	private static UserContext userContext(RenderResultKeyValueStore store) {
		return (UserContext) Proxy.newProxyInstance(
				RenderResultTest.class.getClassLoader(),
				new Class<?>[] { UserContext.class },
				(proxy, method, args) -> {
					if (method.getName().equals("getRenderResultKeyValueStore")) return store;
					throw new UnsupportedOperationException(method.getName());
				});
	}

	private final TestKeyValueStore store = new TestKeyValueStore();
	private final UserContext user = userContext(store);

	private String maskKey() {
		// trigger key creation, then read the key the RenderResult stored
		RenderResult.mask("", store);
		return store.getAttribute(RenderResult.class.getName());
	}

	private String[] maskedHtml(String maskKey) {
		String[] maskedHtml = new String[HTML.length];
		for (int i = 0; i < HTML.length; i++) {
			maskedHtml[i] = "@@" + maskKey + "_" + i + "@@";
		}
		return maskedHtml;
	}

	/**
	 * Masks leftmost first, and at each position the longest matching pattern — the rule a single left to
	 * right pass can implement. Written out naively over the token table, independent of the dispatch
	 * structure RenderResult derives from it.
	 * <p>
	 * This replaces the sequential replace of the legacy implementation, which applied the patterns one
	 * after the other in table order. Both hide every token, but they break ties differently as soon as two
	 * patterns start with the same character: on "}}]" the sequential order masks the "}]" at offset 1 and
	 * leaves a bare "}" behind, the single pass masks the "}}" at offset 0. Since the table now holds the
	 * JSPWiki tokens, such overlaps are the rule rather than the exception, so byte equality with the
	 * legacy order is no longer a meaningful contract. What has to hold is checked separately: an exact
	 * round trip and no token left visible.
	 */
	private String maskReference(String html) {
		String[] maskedHtml = maskedHtml(maskKey());
		StringBuilder result = new StringBuilder();
		int i = 0;
		while (i < html.length()) {
			int pattern = longestPatternAt(html, i);
			if (pattern < 0) {
				result.append(html.charAt(i));
				i++;
			}
			else {
				result.append(maskedHtml[pattern]);
				i += HTML[pattern].length();
			}
		}
		return result.toString();
	}

	private static int longestPatternAt(String html, int index) {
		int longest = -1;
		for (int i = 0; i < HTML.length; i++) {
			if (!html.startsWith(HTML[i], index)) continue;
			if (longest < 0 || HTML[i].length() > HTML[longest].length()) longest = i;
		}
		return longest;
	}

	private void assertNoTokenVisible(String masked, String original) {
		for (String pattern : HTML) {
			assertFalse("mask(" + original + ") leaves " + pattern + " visible to JSPWiki",
					masked.contains(pattern));
		}
	}

	private String unmaskReference(String string) {
		String[] maskedHtml = maskedHtml(maskKey());
		for (int i = 0; i < HTML.length; i++) {
			string = string.replace(maskedHtml[i], HTML[i]);
		}
		return string;
	}

	@Test
	public void maskEdgeCases() {
		String[] cases = {
				"", "[", "]", "[{", "}]", "{", "}", "\\", "\\\\", "\\\\\\", "[[{", "}]]", "[{}]",
				"a[", "a\\", "a}", "x}]y", "\"'<>", "[{[{", "text ends with [", "plain text",
				"<div class=\"foo\">[link]</div>",
				// the JSPWiki tokens, including the prefix shadowing cases
				"_", "__", "___", "|", "||", "|||", "'", "''", "'''", "%", "%%",
				"-", "--", "---", "----", "-----", "{", "{{", "{{{", "{{{{", "}", "}}", "}}}", "}}}}",
				"a__b__c", "|| head | cell", "text ---- rule", "{{mono}} and {{{code}}}",
				"50%% off", "C:\\path\\file", "text ends with -", "text ends with {" };
		for (String text : cases) {
			String masked = RenderResult.mask(text, store);
			assertEquals("mask(" + text + ")", maskReference(text), masked);
			assertNoTokenVisible(masked, text);
			assertEquals("roundtrip(" + text + ")", text, RenderResult.unmask(masked, store));
		}
	}

	@Test
	public void maskMatchesReferenceScan() {
		Random random = new Random(42);
		char[] alphabet = "ab[]{}\\\"'<>_|-%~ ".toCharArray();
		for (int run = 0; run < 2000; run++) {
			char[] chars = new char[random.nextInt(40)];
			for (int i = 0; i < chars.length; i++) {
				chars[i] = alphabet[random.nextInt(alphabet.length)];
			}
			String text = new String(chars);
			String masked = RenderResult.mask(text, store);
			assertEquals("mask(" + text + ")", maskReference(text), masked);
			assertNoTokenVisible(masked, text);
			assertEquals("roundtrip(" + text + ")", text, RenderResult.unmask(masked, store));
		}
	}

	@Test
	public void unmaskMatchesSequentialReplace() {
		String key = maskKey();
		Random random = new Random(4711);
		// fragments deliberately include valid tokens and token lookalikes, but no partial tokens that
		// concatenation could complete into tokens overlapping each other. On such adversarial overlaps
		// (impossible in real mask output) the single left-to-right pass intentionally differs from the
		// legacy pattern-priority order.
		String[] fragments = {
				"a", "b ", "@@", "@", "_", "@@" + key + "_0@@", "@@" + key + "_8@@",
				"@@" + key + "_9@@", "@@" + key + "_12@@", "@@" + key + "_18@@",
				// out of range, over long and empty indices have to stay verbatim
				"@@" + key + "_19@@", "@@" + key + "_99@@", "@@" + key + "_123@@", "@@" + key + "_@@",
				"@@otherkey_3@@", key };
		for (int run = 0; run < 2000; run++) {
			StringBuilder text = new StringBuilder();
			int count = random.nextInt(8);
			for (int i = 0; i < count; i++) {
				text.append(fragments[random.nextInt(fragments.length)]);
			}
			String string = text.toString();
			assertEquals("unmask(" + string + ")", unmaskReference(string), RenderResult.unmask(string, store));
		}
	}

	@Test
	public void sameInstanceWithoutSpecialCharacters() {
		String plain = "no special characters at all";
		assertSame(plain, RenderResult.mask(plain, store));
		assertSame(plain, RenderResult.unmask(plain, store));
	}

	@Test
	public void appendPlusAppendHtmlRoundTrip() {
		RenderResult result = new RenderResult(user);
		result.append("plain [text] with \"quotes\"");
		result.appendHtml("<div class=\"foo\">");
		result.append("nested & more");
		result.appendHtml("</div>");
		assertEquals("plain [text] with \"quotes\"<div class=\"foo\">nested & more</div>", result.toString());
	}

	@Test
	public void appendHtmlRenderResultKeepsMaskingSemantics() {
		RenderResult nested = new RenderResult(user);
		nested.appendHtml("<b>");
		nested.append("plain < text");
		RenderResult outer = new RenderResult(user);
		outer.appendHtml(nested);
		// the legacy behavior masks the plain parts of the nested result as well
		assertEquals("<b>plain < text", outer.toString());
		assertEquals(RenderResult.mask("plain < text", store),
				outer.toStringRaw().substring(RenderResult.mask("<b>", store).length()));
	}

	@Test
	public void appendHtmlTagMatchesLegacy() {
		Random random = new Random(1337);
		char[] alphabet = "ab[]{}\\\"'<>&#%_|-~ ".toCharArray();
		String[] tags = { "div", "span", "a" };
		for (boolean encode : new boolean[] { true, false }) {
			for (int run = 0; run < 500; run++) {
				String tag = tags[random.nextInt(tags.length)];
				int attributeCount = random.nextInt(3) * 2;
				String[] attributes = new String[attributeCount];
				for (int i = 0; i < attributeCount; i += 2) {
					attributes[i] = "attr" + i;
					char[] chars = new char[random.nextInt(10)];
					for (int j = 0; j < chars.length; j++) {
						chars[j] = alphabet[random.nextInt(alphabet.length)];
					}
					attributes[i + 1] = new String(chars);
				}
				RenderResult result = new RenderResult(user);
				result.appendHtmlTag(tag, encode, attributes);
				assertEquals(appendHtmlTagReference(tag, encode, attributes), result.toStringRaw());
			}
		}
	}

	@Test
	public void appendHtmlTagSkipsNullAndOddAttributes() {
		RenderResult result = new RenderResult(user);
		result.appendHtmlTag("div", true, "id", null, null, "x", "class", "foo", "dangling");
		assertEquals(appendHtmlTagReference("div", true, "id", null, null, "x", "class", "foo", "dangling"),
				result.toStringRaw());
		assertEquals("<div class=\"foo\">", result.toString());
	}

	private String appendHtmlTagReference(String tag, boolean encode, String... attributes) {
		StringBuilder html = new StringBuilder();
		html.append("<").append(tag);
		for (int i = 0; i + 2 <= attributes.length; i += 2) {
			String attributeName = attributes[i];
			String attributeValue = attributes[i + 1];
			if (attributeName == null) continue;
			if (attributeValue == null) continue;
			html.append(" ").append(attributeName).append("=\"")
					.append(encode ? Strings.encodeHtml(attributeValue) : attributeValue).append("\"");
		}
		html.append(">");
		return maskReference(html.toString());
	}

	@Test
	public void appendPlainText() {
		RenderResult result = new RenderResult(user);
		result.appendPlainText("a<b & [c]");
		// html encoding alone does not make text inert: [c] would still be a JSPWiki link, so the
		// encoded text is masked as well. Unmasking has to give the encoded text back unchanged.
		assertEquals(Strings.encodeHtml("a<b & [c]"), result.toString());
		assertEquals(maskReference(Strings.encodeHtml("a<b & [c]")), result.toStringRaw());
	}

	@Test
	public void plainTextIsInertForJSPWiki() {
		// what appendPlainText promises: neither html nor JSPWiki markup survives into the raw result
		String text = "a__b__c {{x}} 50%% e|f||g ---- h" + "\\" + "i [j] <k> \'l\' \"m\"";
		RenderResult result = new RenderResult(user);
		result.appendPlainText(text);
		String raw = result.toStringRaw();
		for (String pattern : HTML) {
			assertFalse("still visible to JSPWiki: " + pattern, raw.contains(pattern));
		}
		assertEquals(Strings.encodeHtml(text), result.toString());
	}

	@Test
	public void encodedAttributeValuesHideEveryToken() {
		// appendHtmlTag encodes attribute values and masks what the encoding leaves behind. A file name
		// like Bericht__final__.docx in a data attribute must not reach JSPWiki as markup.
		for (char c = 0; c < 128; c++) {
			assertAttributeValueIsInert(Character.toString(c));
		}
		for (String pattern : HTML) {
			assertAttributeValueIsInert(pattern);
			assertAttributeValueIsInert("x" + pattern + "y");
		}
		assertAttributeValueIsInert("Bericht__final__.docx");
	}

	private void assertAttributeValueIsInert(String attributeValue) {
		RenderResult result = new RenderResult(user);
		result.appendHtmlTag("div", true, "data-file", attributeValue);
		String raw = result.toStringRaw();
		for (String pattern : HTML) {
			assertFalse("value " + attributeValue + " leaves " + pattern + " visible to JSPWiki",
					raw.contains(pattern));
		}
		assertEquals("<div data-file=\"" + Strings.encodeHtml(attributeValue) + "\">", result.toString());
	}
}
