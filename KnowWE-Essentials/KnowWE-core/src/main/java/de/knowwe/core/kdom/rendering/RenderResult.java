/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 */
package de.knowwe.core.kdom.rendering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.elements.HtmlProvider;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.filter.SectionFilter;

public class RenderResult {
	private static final Logger LOGGER = LoggerFactory.getLogger(RenderResult.class);

	private static final String storeKey = RenderResult.class.getName();
	// unmask parses mask token indices as a single digit, so HTML must never exceed 10 entries
	private static final String[] HTML = new String[] {
			"[{", "}]", "\\\\", "\"", "'", ">", "<", "[", "]" };
	private static final int MASK_QUOTE = 3;
	private static final int MASK_GT = 5;
	private static final int MASK_LT = 6;

	private final String maskKey;
	private final String maskPrefix;
	private final String[] maskedHtml;
	private final StringBuilder builder = new StringBuilder();

	@SuppressWarnings("rawtypes")
	private List<Pair<SectionFilter, Renderer>> customRenderers = Collections.emptyList();

	public RenderResult(UserContext context) {
		this(context.getRenderResultKeyValueStore());
	}

	private RenderResult(RenderResultKeyValueStore keyStore) {
		this.maskKey = createMaskKey(keyStore);
		this.maskPrefix = "@@" + maskKey + "_";
		this.maskedHtml = createMaskHtml(maskPrefix);
	}

	/**
	 * Creates a new {@link RenderResult} using the same masking key as the given parent.<br>
	 * <b>Attention: </b> The newly instantiated {@link RenderResult} will not contain any content
	 * of the given parent {@link RenderResult}. The parent is only needed for the masking key.
	 *
	 * @param parent the parent needed for using the same masking key
	 */
	public RenderResult(RenderResult parent) {
		this.maskKey = parent.maskKey;
		this.maskPrefix = parent.maskPrefix;
		this.maskedHtml = parent.maskedHtml; // NOSONAR
		this.customRenderers = parent.customRenderers;
	}

	@SuppressWarnings("rawtypes")
	public void addCustomRenderer(SectionFilter filter, Renderer renderer) {
		List<Pair<SectionFilter, Renderer>> newList = new ArrayList<>(customRenderers.size() + 1);
		newList.add(0, new Pair<>(filter, renderer));
		newList.addAll(customRenderers);
		customRenderers = newList;
	}

	private String createMaskKey(RenderResultKeyValueStore keyStore) {
		if (keyStore != null) {
			String storedMaskKey = keyStore.getAttribute(storeKey);
			if (storedMaskKey != null) return storedMaskKey;
		}

		int rnd = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
		String maskKey = Integer.toString(rnd, Character.MAX_RADIX);
		if (keyStore != null) {
			keyStore.setAttribute(storeKey, maskKey);
		}
		return maskKey;
	}

	private String[] createMaskHtml(String maskPrefix) {
		String[] maskedHtml = new String[HTML.length];
		for (int i = 0; i < HTML.length; i++) {
			maskedHtml[i] = maskPrefix + i + "@@";
		}
		return maskedHtml;
	}

	public RenderResult append(boolean bool) {
		builder.append(bool);
		return this;
	}

	public RenderResult append(char c) {
		builder.append(c);
		return this;
	}

	public RenderResult append(char[] str) {
		builder.append(str);
		return this;
	}

	public RenderResult append(char[] str, int offset, int len) {
		builder.append(str, offset, len);
		return this;
	}

	public RenderResult append(CharSequence str) {
		builder.append(str);
		return this;
	}

	public RenderResult append(CharSequence str, int start, int end) {
		builder.append(str, start, end);
		return this;
	}

	public RenderResult append(double d) {
		builder.append(d);
		return this;
	}

	public RenderResult append(float f) {
		builder.append(f);
		return this;
	}

	public RenderResult append(int i) {
		builder.append(i);
		return this;
	}

	public RenderResult append(long l) {
		builder.append(l);
		return this;
	}

	public RenderResult append(Object object) {
		builder.append(object);
		return this;
	}

	public RenderResult append(RenderResult result) {
		builder.append(result.builder);
		return this;
	}

	/**
	 * Appends the section to this render result. The section is rendered by using the sections defined renderer. The
	 * method is a common shortcut for <code>DelegateRenderer.getRenderer(section, user).render(section, user,
	 * result)</code>. The method is null-secure for the section. If null is specified as the section, nothing is
	 * rendered.
	 * <p>
	 * This method additionally consider custom renderer that has previously been set to overwrite the default rendering
	 * behaviour.
	 *
	 * @param section the section to be rendered
	 * @return this render result
	 * @created 15.02.2014
	 */
	public RenderResult append(Section<?> section, UserContext user) {
		if (section != null) {
			Renderer renderer = DelegateRenderer.getRenderer(section, user);
			//noinspection rawtypes
			for (Pair<SectionFilter, Renderer> pair : customRenderers) {
				//noinspection unchecked
				if (pair.getA().accept(section)) {
					renderer = pair.getB();
					break;
				}
			}
			renderer.render(section, user, this);
		}
		return this;
	}

	/**
	 * Append the given HTML element (and its successors) to this render result.
	 */
	public RenderResult append(HtmlProvider element) {
		element.write(this);
		return this;
	}

	public RenderResult appendAvoidParagraphs(RenderResult result) {
		String raw = result.builder.toString();
		raw = raw.replaceAll("\n\n", "\n \n");
		builder.append(raw);
		return this;
	}

	public RenderResult append(String text) {
		builder.append(text);
		return this;
	}

	public RenderResult appendHtml(RenderResult html) {
		maskInto(html.builder, 0, builder);
		return this;
	}

	public RenderResult appendHtml(String html) {
		if (html == null) {
			builder.append((String) null);
			return this;
		}
		maskInto(html, 0, builder);
		return this;
	}

	/**
	 * Appends the specified string encoded as html entities, using {@link Strings#encodeHtml(String)}.
	 */
	public RenderResult appendEntityEncoded(String text) {
		if (text == null) {
			builder.append((String) null);
			return this;
		}
		Strings.encodeHtml(text, builder);
		return this;
	}

	/**
	 * Append JSPWiki markup and mask it, so it is NOT rendered as JSPWiki markup
	 */
	public RenderResult appendJSPWikiMarkup(RenderResult result) {
		StringBuilder tempBuilder = new StringBuilder(result.builder);
		KnowWEUtils.maskJSPWikiMarkup(tempBuilder);
		builder.append(tempBuilder);
		return this;
	}

	/**
	 * Append JSPWiki markup and mask it, so it is NOT rendered as JSPWiki markup
	 */
	public RenderResult appendJSPWikiMarkup(String markup) {
		builder.append(KnowWEUtils.maskJSPWikiMarkup(markup));
		return this;
	}

	public char charAt(int arg0) {
		return builder.charAt(arg0);
	}

	public RenderResult delete(int start, int end) {
		builder.delete(start, end);
		return this;
	}

	public RenderResult deleteCharAt(int index) {
		builder.deleteCharAt(index);
		return this;
	}

	@SuppressWarnings("EqualsOnSuspiciousObject")
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RenderResult) {
			return builder.equals(((RenderResult) obj).builder);
		}
		return builder.equals(obj);
	}

	@Override
	public int hashCode() {
		return builder.hashCode();
	}

	public int indexOf(String str) {
		return builder.indexOf(str);
	}

	public int indexOf(String str, int fromIndex) {
		return builder.indexOf(str, fromIndex);
	}

	public RenderResult insert(int offset, boolean b) {
		builder.insert(offset, b);
		return this;
	}

	public RenderResult insert(int offset, char c) {
		builder.insert(offset, c);
		return this;
	}

	public RenderResult insert(int offset, char[] str) {
		builder.insert(offset, str);
		return this;
	}

	public RenderResult insert(int index, char[] str, int offset, int len) {
		builder.insert(index, str, offset, len);
		return this;
	}

	public RenderResult insert(int dstOffset, CharSequence s) {
		builder.insert(dstOffset, s);
		return this;
	}

	public RenderResult insert(int dstOffset, CharSequence s, int start, int end) {
		builder.insert(dstOffset, s, start, end);
		return this;
	}

	public RenderResult insert(int offset, double d) {
		builder.insert(offset, d);
		return this;
	}

	public RenderResult insert(int offset, float f) {
		builder.insert(offset, f);
		return this;
	}

	public RenderResult insert(int offset, int i) {
		builder.insert(offset, i);
		return this;
	}

	public RenderResult insert(int offset, long l) {
		builder.insert(offset, l);
		return this;
	}

	public RenderResult insert(int offset, Object obj) {
		builder.insert(offset, obj);
		return this;
	}

	public RenderResult insert(int offset, String str) {
		builder.insert(offset, str);
		return this;
	}

	public int lastIndexOf(String str) {
		return builder.lastIndexOf(str);
	}

	public int lastIndexOf(String str, int fromIndex) {
		return builder.lastIndexOf(str, fromIndex);
	}

	public int length() {
		return builder.length();
	}

	/**
	 * Masks the given text in a single pass. If nothing requires masking, the given string instance is returned
	 * unchanged.
	 */
	private String mask(String html) {
		if (html == null) return null;
		int length = html.length();
		int first = 0;
		while (first < length && maskPatternAt(html, first) < 0) first++;
		if (first == length) return html;
		StringBuilder result = new StringBuilder(length + 64);
		result.append(html, 0, first);
		maskInto(html, first, result);
		return result.toString();
	}

	/**
	 * Masks the given text in a single pass, appending the masked text directly to the given string builder.
	 */
	private void maskInto(CharSequence html, int from, StringBuilder result) {
		int length = html.length();
		int plainStart = from;
		int i = from;
		while (i < length) {
			int pattern = maskPatternAt(html, i);
			if (pattern >= 0) {
				result.append(html, plainStart, i).append(maskedHtml[pattern]);
				i += HTML[pattern].length();
				plainStart = i;
			}
			else {
				i++;
			}
		}
		result.append(html, plainStart, length);
	}

	/**
	 * Returns the index of the HTML pattern starting at the given position, or -1 if there is none. Two-char patterns
	 * take precedence over their one-char prefixes, matching the replacement order of the HTML array.
	 */
	private static int maskPatternAt(CharSequence html, int index) {
		char c = html.charAt(index);
		boolean hasNext = index + 1 < html.length();
		return switch (c) {
			case '[' -> (hasNext && html.charAt(index + 1) == '{') ? 0 : 7;
			case '}' -> (hasNext && html.charAt(index + 1) == ']') ? 1 : -1;
			case '\\' -> (hasNext && html.charAt(index + 1) == '\\') ? 2 : -1;
			case '"' -> MASK_QUOTE;
			case '\'' -> 4;
			case '>' -> MASK_GT;
			case '<' -> MASK_LT;
			case ']' -> 8;
			default -> -1;
		};
	}

	public static String mask(String string, UserContext context) {
		RenderResult renderResult = new RenderResult(context);
		return renderResult.mask(string);
	}

	public static String mask(String string, RenderResult parent) {
		RenderResult renderResult = new RenderResult(parent);
		return renderResult.mask(string);
	}

	public static String mask(String string, RenderResultKeyValueStore keyStore) {
		RenderResult renderResult = new RenderResult(keyStore);
		return renderResult.mask(string);
	}

	public RenderResult replace(int start, int end, String str) {
		builder.replace(start, end, str);
		return this;
	}

	public CharSequence subSequence(int arg0, int arg1) {
		return builder.subSequence(arg0, arg1);
	}

	public String substring(int arg0) {
		return builder.substring(arg0);
	}

	public String substring(int arg0, int arg1) {
		return builder.substring(arg0, arg1);
	}

	/**
	 * Returns the <b>unmasked</b> String of this {@link RenderResult}.
	 * <p>
	 * <b>Attention:</b> Do not use this method for append this {@link RenderResult} to another.
	 * There are two other methods allowing this: {@link RenderResult#toStringRaw()} and {@link
	 * RenderResult#append(RenderResult)}.
	 */
	@Override
	public String toString() {
		return unmask(builder.toString());
	}

	/**
	 * Returns the still masked string of this {@link RenderResult}.
	 *
	 * @return the still masked string of this {@link RenderResult}
	 * @created 11.02.2013
	 */
	public String toStringRaw() {
		return builder.toString();
	}

	/**
	 * Unmasks the given text in a single pass. If it contains no mask token, the given string instance is returned
	 * unchanged.
	 */
	private String unmask(String string) {
		if (string == null) return null;
		int index = string.indexOf(maskPrefix);
		if (index < 0) return string;
		StringBuilder result = new StringBuilder(string.length());
		int plainStart = 0;
		while (index >= 0) {
			int digitIndex = index + maskPrefix.length();
			int pattern = digitIndex < string.length() ? string.charAt(digitIndex) - '0' : -1;
			if (pattern >= 0 && pattern < HTML.length && string.startsWith("@@", digitIndex + 1)) {
				result.append(string, plainStart, index).append(HTML[pattern]);
				plainStart = digitIndex + 3;
				index = string.indexOf(maskPrefix, plainStart);
			}
			else {
				// not a valid mask token, keep it verbatim and continue scanning behind its first char
				index = string.indexOf(maskPrefix, index + 1);
			}
		}
		result.append(string, plainStart, string.length());
		return result.toString();
	}

	public static String unmask(String string, UserContext context) {
		RenderResult renderResult = new RenderResult(context);
		return renderResult.unmask(string);
	}

	public static String unmask(String string, RenderResult parent) {
		RenderResult renderResult = new RenderResult(parent);
		return renderResult.unmask(string);
	}

	public static String unmask(String string, RenderResultKeyValueStore keyStore) {
		RenderResult renderResult = new RenderResult(keyStore);
		return renderResult.unmask(string);
	}

	/**
	 * Appends an opening and masked HTML element without having to fiddle with strings and quoting. Just set tag name
	 * and the attributes. Attributes need to be given in pairs. First the name of the attribute, second the content of
	 * the attribute.
	 *
	 * @param tag        the tag name of the HTML element
	 * @param attributes the attributes of the HTML element: the odd elements are the attribute names and the even
	 *                   elements the attribute contents
	 * @created 05.02.2013
	 */
	public RenderResult appendHtmlTag(String tag, String... attributes) {
		return appendHtmlTag(tag, true, attributes);
	}

	/**
	 * Appends an opening and masked HTML element without having to fiddle with strings and quoting. Just set tag name
	 * and the attributes. Attributes need to be given in pairs. First the name of the attribute, second the content of
	 * the attribute.
	 *
	 * @param tag        the tag name of the HTML element
	 * @param encode     decides whether the attributes will be html encoded or not
	 * @param attributes the attributes of the HTML element: the odd elements are the attribute names and the even
	 *                   elements the attribute contents
	 * @created 05.02.2013
	 */
	public RenderResult appendHtmlTag(String tag, boolean encode, String... attributes) {
		// emit the mask tokens of the tag structure directly, so the text we assemble here is never scanned;
		// encoded attribute values need no masking, because encodeHtml encodes every maskable character
		builder.append(maskedHtml[MASK_LT]);
		maskInto(tag, 0, builder);
		for (int i = 0; i + 2 <= attributes.length; i += 2) {
			String attributeName = attributes[i];
			String attributeValue = attributes[i + 1];
			if (attributeName == null) continue;
			if (attributeValue == null) continue;
			builder.append(' ');
			maskInto(attributeName, 0, builder);
			builder.append('=').append(maskedHtml[MASK_QUOTE]);
			if (encode) {
				Strings.encodeHtml(attributeValue, builder);
			}
			else {
				maskInto(attributeValue, 0, builder);
			}
			builder.append(maskedHtml[MASK_QUOTE]);
		}
		builder.append(maskedHtml[MASK_GT]);
		return this;
	}

	/**
	 * Appends a complete and masked HTML element without having to fiddle with strings and quoting. Just set tag name,
	 * content and the attributes. Attributes need to be given in pairs. First the name of the attribute, second the
	 * content of the attribute.
	 *
	 * @param tag        the tag name of the HTML element
	 * @param content    the content of the HTML element
	 * @param attributes the attributes of the HTML element: the odd elements are the attribute names and the even
	 *                   elements the attribute contents
	 * @created 05.02.2013
	 */
	public RenderResult appendHtmlElement(String tag, String content, String... attributes) {
		if (tag.equals("img") && Strings.isBlank(content)) {
			appendHtmlTag(tag, attributes);
		}
		else {
			appendHtmlTag(tag, attributes);
			append(content);
			appendHtml("</" + tag + ">");
		}
		return this;
	}

	public RenderResult appendError(String message) {
		appendHtmlElement("span", message, "class", "error");
		return this;
	}

	public RenderResult appendWarning(String message) {
		appendHtmlElement("span", message, "class", "warning");
		return this;
	}

	public RenderResult appendException(String message, Throwable e) {
		appendHtmlElement("span", message, "class", "error");
		LOGGER.error(message, e);
		return this;
	}

	public RenderResult appendException(Throwable e) {
		appendException("Exception while rendering: " + e.getClass()
				.getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage()), e);
		return this;
	}
}
