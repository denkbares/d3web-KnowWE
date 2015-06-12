/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.knowwe.core.kdom.rendering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.d3web.utils.Pair;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.filter.SectionFilter;

public class RenderResult {

	private static final String storeKey = RenderResult.class.getName();
	private static final String[] HTML = new String[] {
			"[{", "}]", "\"", "'", ">", "<", "[", "]" };

	private final String maskKey;
	private final String[] maskedHtml;
	private final StringBuilder builder = new StringBuilder();

	private List<Pair<SectionFilter, Renderer>> customRenderers = Collections.emptyList();

	public RenderResult(UserContext context) {
		this(context.getRequest());
	}

	public RenderResult(HttpServletRequest request) {
		this.maskKey = createMaskKey(request);
		this.maskedHtml = createMaskHtml(maskKey);
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
		this.maskedHtml = parent.maskedHtml; // NOSONAR
		this.customRenderers = parent.customRenderers;
	}

	public void addCustomRenderer(SectionFilter filter, Renderer renderer) {
		List<Pair<SectionFilter, Renderer>> newList = new ArrayList<>(customRenderers.size() + 1);
		newList.add(0, new Pair<>(filter, renderer));
		newList.addAll(customRenderers);
		customRenderers = newList;
	}

	private static String createMaskKey(HttpServletRequest request) {
		Object storedMaskKey = request.getAttribute(storeKey);
		if (storedMaskKey != null) return (String) storedMaskKey;

		int rnd = Math.abs(new Random().nextInt());
		String maskKey = Integer.toString(rnd, Character.MAX_RADIX);
		request.setAttribute(storeKey, maskKey);
		return maskKey;
	}

	private static String[] createMaskHtml(String maskKey) {
		String[] maskedHtml = new String[HTML.length];
		for (int i = 0; i < HTML.length; i++) {
			maskedHtml[i] = "@@" + maskKey + "_" + i + "@@";
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
	 * Appends the section to this render result. The section is rendered by using the sections
	 * defined renderer. The method is a common shortcut for <code>DelegateRenderer.getRenderer(section,
	 * user).render(section, user, result)</code>. The method is null-secure for the section. If
	 * null is specified as the section, nothing is rendered.
	 * <p/>
	 * This method additionally consider custom renderer that has previously been set to overwrite
	 * the default rendering behaviour.
	 *
	 * @param section the section to be rendered
	 * @return this render result
	 * @created 15.02.2014
	 */
	public RenderResult append(Section<?> section, UserContext user) {
		if (section != null) {
			Renderer renderer = DelegateRenderer.getRenderer(section, user);
			for (Pair<SectionFilter, Renderer> pair : customRenderers) {
				if (pair.getA().accept(section)) {
					renderer = pair.getB();
					break;
				}
			}
			renderer.render(section, user, this);
		}
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
		builder.append(mask(html.toStringRaw()));
		return this;
	}

	public RenderResult appendHtml(String html) {
		builder.append(mask(html));
		return this;
	}

	/**
	 * Appends the specified string encoded as html entities, using {@link
	 * Strings#encodeHtml(String)}.
	 *
	 * @param text the text to be appended
	 * @return a reference to this object.
	 * @created 20.08.2013
	 */
	public RenderResult appendEntityEncoded(String text) {
		builder.append(Strings.encodeHtml(text));
		return this;
	}

	public RenderResult appendJSPWikiMarkup(RenderResult result) {
		StringBuilder tempBuilder = new StringBuilder(result.builder);
		KnowWEUtils.maskJSPWikiMarkup(tempBuilder);
		builder.append(tempBuilder);
		return this;
	}

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

	private String mask(String html) {
		for (int i = 0; i < HTML.length; i++) {
			// somehow this is way faster for large strings than
			// StringUtils.replaceEach(String, String[], String[]).
			html = StringUtils.replace(html, HTML[i], maskedHtml[i]);
		}
		return html;
	}

	public static String mask(String string, UserContext context) {
		RenderResult renderResult = new RenderResult(context);
		return renderResult.mask(string);
	}

	public static String mask(String string, RenderResult parent) {
		RenderResult renderResult = new RenderResult(parent);
		return renderResult.mask(string);
	}

	public static String mask(String string, HttpServletRequest request) {
		RenderResult renderResult = new RenderResult(request);
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
	 * <p/>
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

	private String unmask(String string) {
		for (int i = 0; i < maskedHtml.length; i++) {
			// somehow this is way faster for large strings than
			// StringUtils.replaceEach(String, String[], String[]).
			string = StringUtils.replace(string, maskedHtml[i], HTML[i]);
		}
		return string;
	}

	public static String unmask(String string, UserContext context) {
		RenderResult renderResult = new RenderResult(context);
		return renderResult.unmask(string);
	}

	public static String unmask(String string, RenderResult parent) {
		RenderResult renderResult = new RenderResult(parent);
		return renderResult.unmask(string);
	}

	public static String unmask(String string, HttpServletRequest request) {
		RenderResult renderResult = new RenderResult(request);
		return renderResult.unmask(string);
	}

	/**
	 * Appends an opening and masked HTML element without having to fiddle with strings and quoting.
	 * Just set tag name and the attributes. Attributes need to be given in pairs. First the name of
	 * the attribute, second the content of the attribute.
	 *
	 * @param tag the tag name of the HTML element
	 * @param attributes the attributes of the HTML element: the odd elements are the attribute
	 * names and the even elements the attribute contents
	 * @created 05.02.2013
	 */
	public void appendHtmlTag(String tag, String... attributes) {
		appendHtmlTag(tag, true, attributes);
	}

	/**
	 * Appends an opening and masked HTML element without having to fiddle with strings and quoting.
	 * Just set tag name and the attributes. Attributes need to be given in pairs. First the name of
	 * the attribute, second the content of the attribute.
	 *
	 * @param tag the tag name of the HTML element
	 * @param encode decides whether the attributes will be html encoded or not
	 * @param attributes the attributes of the HTML element: the odd elements are the attribute
	 * names and the even elements the attribute contents
	 * @created 05.02.2013
	 */
	public void appendHtmlTag(String tag, boolean encode, String... attributes) {
		StringBuilder html = new StringBuilder();
		html.append("<").append(tag);
		for (int i = 0; i + 2 <= attributes.length; i += 2) {
			String attributeName = attributes[i];
			String attributeValue = attributes[i + 1];
			if (attributeName == null) continue;
			if (attributeValue == null) continue;
			html.append(getAttribute(encode, attributeName, attributeValue));
		}
		html.append(">");
		appendHtml(html.toString());
	}

	private static String getAttribute(boolean encode, String attributeName, String attribute) {
		return " " + attributeName + "=\""
				+ (encode ? Strings.encodeHtml(attribute) : attribute)
				+ "\"";
	}

	/**
	 * Appends a complete and masked HTML element without having to fiddle with strings and quoting.
	 * Just set tag name, content and the attributes. Attributes need to be given in pairs. First
	 * the name of the attribute, second the content of the attribute.
	 *
	 * @param tag the tag name of the HTML element
	 * @param content the content of the HTML element
	 * @param attributes the attributes of the HTML element: the odd elements are the attribute
	 * names and the even elements the attribute contents
	 * @created 05.02.2013
	 */
	public void appendHtmlElement(String tag, String content, String... attributes) {
		if (tag.equals("img") && Strings.isBlank(content)) {
			appendHtmlTag(tag, attributes);
		}
		else {
			appendHtmlTag(tag, attributes);
			append(content);
			appendHtml("</" + tag + ">");
		}
	}

	public void appendException(String message, Exception e) {
		appendHtmlElement("span", message, "class", "error");
		Log.severe(message, e);
	}
}
