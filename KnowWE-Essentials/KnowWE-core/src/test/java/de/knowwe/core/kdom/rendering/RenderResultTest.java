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
 * Tests the single-pass mask/unmask implementation against the legacy sequential-replace behavior.
 *
 * @author Konstantin Herud (denkbares GmbH)
 * @created 03.08.2026
 */
public class RenderResultTest {

	private static final String[] HTML = { "[{", "}]", "\\\\", "\"", "'", ">", "<", "[", "]" };

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

	private String maskReference(String html) {
		String[] maskedHtml = maskedHtml(maskKey());
		for (int i = 0; i < HTML.length; i++) {
			html = html.replace(HTML[i], maskedHtml[i]);
		}
		return html;
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
				"<div class=\"foo\">[link]</div>" };
		for (String text : cases) {
			assertEquals("mask(" + text + ")", maskReference(text), RenderResult.mask(text, store));
		}
	}

	@Test
	public void maskMatchesSequentialReplace() {
		Random random = new Random(42);
		char[] alphabet = "ab[]{}\\\"'<> ".toCharArray();
		for (int run = 0; run < 2000; run++) {
			char[] chars = new char[random.nextInt(40)];
			for (int i = 0; i < chars.length; i++) {
				chars[i] = alphabet[random.nextInt(alphabet.length)];
			}
			String text = new String(chars);
			String masked = RenderResult.mask(text, store);
			assertEquals("mask(" + text + ")", maskReference(text), masked);
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
				"@@" + key + "_9@@", "@@" + key + "_12@@", "@@otherkey_3@@", key };
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
		char[] alphabet = "ab[]{}\\\"'<>&#% ".toCharArray();
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
	public void appendEntityEncoded() {
		RenderResult result = new RenderResult(user);
		result.appendEntityEncoded("a<b & [c]");
		assertEquals(Strings.encodeHtml("a<b & [c]"), result.toStringRaw());
		assertEquals(Strings.encodeHtml("a<b & [c]"), result.toString());
	}

	@Test
	public void encodedHtmlIsMaskInert() {
		// appendHtmlTag appends encoded attribute values without masking them, which is only
		// correct while encodeHtml encodes every character occurring in a maskable pattern
		for (char c = 0; c < 128; c++) {
			String encoded = Strings.encodeHtml(Character.toString(c));
			assertSame("encodeHtml('" + c + "') must not be maskable", encoded, RenderResult.mask(encoded, store));
		}
		for (String pattern : HTML) {
			String encoded = Strings.encodeHtml(pattern);
			assertSame("encodeHtml(" + pattern + ") must not be maskable", encoded, RenderResult.mask(encoded, store));
		}
	}
}
