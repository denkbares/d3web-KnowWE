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

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import de.d3web.strings.Strings;
import de.knowwe.core.user.UserContext;

public class RenderResult {

	private final String storeKey = RenderResult.class.getName();

	private String maskKey;

	private final StringBuilder builder = new StringBuilder();

	private static final String[] HTML = new String[] {
			"[{", "}]", "\"", "'", ">", "<", "[", "]" };

	private String[] maskedHtml = new String[HTML.length];

	public RenderResult(UserContext context) {
		this(context.getRequest());
	}

	public RenderResult(HttpServletRequest request) {
		initMaskKey(request);
		initMaskHtml();
	}

	/**
	 * Creates a new {@link RenderResult} using the same masking key as the
	 * given parent.<br>
	 * <b>Attention: </b> The newly instantiated {@link RenderResult} will not
	 * contain any content of the given parent {@link RenderResult}. The parent
	 * is only needed for the masking key.
	 * 
	 * @param parent the parent needed for using the same masking key
	 */
	public RenderResult(RenderResult parent) {
		this.maskKey = parent.maskKey;
		this.maskedHtml = parent.maskedHtml; // NOSONAR
	}

	private void initMaskKey(HttpServletRequest request) {
		Object storedMaskKey = request.getAttribute(storeKey);
		if (storedMaskKey == null) {
			int rnd = Math.abs(new Random().nextInt());
			maskKey = Integer.toString(rnd, Character.MAX_RADIX);
			request.setAttribute(storeKey, maskKey);
		}
		else {
			maskKey = (String) storedMaskKey;
		}
	}

	private void initMaskHtml() {
		for (int i = 0; i < HTML.length; i++) {
			maskedHtml[i] = "@@" + maskKey + "_" + i + "@@";
		}
	}

	public RenderResult append(boolean arg0) {
		builder.append(arg0);
		return this;
	}

	public RenderResult append(char arg0) {
		builder.append(arg0);
		return this;
	}

	public RenderResult append(char[] arg0) {
		builder.append(arg0);
		return this;
	}

	public RenderResult append(char[] arg0, int arg1, int arg2) {
		builder.append(arg0, arg1, arg2);
		return this;
	}

	public RenderResult append(CharSequence arg0) {
		builder.append(arg0);
		return this;
	}

	public RenderResult append(CharSequence arg0, int arg1, int arg2) {
		builder.append(arg0, arg1, arg2);
		return this;
	}

	public RenderResult append(double arg0) {
		builder.append(arg0);
		return this;
	}

	public RenderResult append(float arg0) {
		builder.append(arg0);
		return this;
	}

	public RenderResult append(int arg0) {
		builder.append(arg0);
		return this;
	}

	public RenderResult append(long arg0) {
		builder.append(arg0);
		return this;
	}

	public RenderResult append(Object arg0) {
		builder.append(arg0);
		return this;
	}

	public RenderResult append(RenderResult arg0) {
		builder.append(arg0.builder);
		return this;
	}

	public RenderResult append(String arg0) {
		builder.append(arg0);
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

	public RenderResult appendJSPWikiMarkup(RenderResult result) {
		StringBuilder tempBuilder = new StringBuilder(result.builder);
		Strings.maskJSPWikiMarkup(tempBuilder);
		builder.append(tempBuilder);
		return this;
	}

	public RenderResult appendJSPWikiMarkup(String markup) {
		builder.append(Strings.maskJSPWikiMarkup(markup));
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
	 * <b>Attention:</b> Do not use this method for append this
	 * {@link RenderResult} to another. There are two other methods allowing
	 * this: {@link RenderResult#toStringRaw()} and
	 * {@link RenderResult#append(RenderResult)}.
	 */
	@Override
	public String toString() {
		return unmask(builder.toString());
	}

	/**
	 * Returns the still masked string of this {@link RenderResult}.
	 * 
	 * @created 11.02.2013
	 * @return the still masked string of this {@link RenderResult}
	 */
	public String toStringRaw() {
		return builder.toString();
	}

	public String unmask(String string) {
		for (int i = 0; i < maskedHtml.length; i++) {
			// somehow this is way faster for large strings than
			// StringUtils.replaceEach(String, String[], String[]).
			string = StringUtils.replace(string, maskedHtml[i], HTML[i]);
		}
		return string;
	}

	/**
	 * Appends an opening and masked HTML element without having to fiddle with
	 * strings and quoting. Just set tag name and the attributes. Attributes
	 * need to be given in pairs. First the name of the attribute, second the
	 * content of the attribute.
	 * 
	 * 
	 * @created 05.02.2013
	 * @param result the {@link RenderResult} the tag will be appended to
	 * @param tag the tag name of the HTML element
	 * @param attributes the attributes of the HTML element: the odd elements
	 *        are the attribute names and the even elements the attribute
	 *        contents
	 * @return an opening and masked HTML element with attributes
	 */
	public void appendHtmlTag(String tag, String... attributes) {
		StringBuilder html = new StringBuilder();
		html.append("<" + tag);
		for (int i = 0; i + 2 <= attributes.length; i += 2) {
			html.append(getAttribute(attributes[i], attributes[i + 1]));
		}
		html.append(">");
		appendHtml(html.toString());
	}

	private static String getAttribute(String attributeName, String attribute) {
		return " " + attributeName + "=\"" + Strings.encodeHtml(attribute) + "\"";
	}

	/**
	 * Appends a complete and masked HTML element without having to fiddle with
	 * strings and quoting. Just set tag name, content and the attributes.
	 * Attributes need to be given in pairs. First the name of the attribute,
	 * second the content of the attribute.
	 * 
	 * 
	 * @created 05.02.2013
	 * @param result the {@link RenderResult} the element will be appended to
	 * @param tag the tag name of the HTML element
	 * @param content the content of the HTML element
	 * @param attributes the attributes of the HTML element: the odd elements
	 *        are the attribute names and the even elements the attribute
	 *        contents
	 */
	public void appendHtmlElement(String tag, String content, String... attributes) {
		appendHtmlTag(tag, attributes);
		append(content);
		appendHtml("</" + tag + ">");
	}

}
