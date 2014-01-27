/*
 * Copyright (C) 2014 University Wuerzburg, Computer Science VI
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
package de.knowwe.kdom.renderer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.json.JSONException;
import org.json.JSONObject;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * 
 * Decorate an existing Renderer which renders a table with a pagination bar.<br>
 * Following things must be considered: <li>The table must have a defined header
 * by using {@code<th>...</th>} <li>The decorated renderer must implement the
 * sorting and the correct selection of the result. The values of the pagination
 * can be obtained by using getStartrow(..), getCount(..), getSorting(..) and
 * getNaturalOrder(..). <li>Optionally: You can exclude columns from sorting by
 * stating this explicitly like this: {@code<th class="notSortable">...</th>}
 * <li>Optionally: If the decorated renderer knows the size of its result it can
 * use setResultSize(..) to enable further rendering at the pagination bar.
 * 
 * 
 * @author Stefan Plehn
 * @created 14.01.2014
 */
public class PaginationDecoratingRenderer implements Renderer {

	private final Renderer decoratedRenderer;

	public static final String STARTROW = "startRow";
	private static final String STARTROW_DEFAULT = "1";
	public static final String COUNT = "count";
	private static final String COUNT_DEFAULT = "50";
	public static final String SORTING = "sorting";
	private static final String SORTING_DEFAULT = "none";
	public static final String NATURALORDER = "naturalOrder";
	private static final String NATURALORDER_DEFAULT = "true";

	public PaginationDecoratingRenderer(Renderer decoratedRenderer) {
		this.decoratedRenderer = decoratedRenderer;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		RenderResult table = new RenderResult(user);
		decoratedRenderer.render(section, user, table);
		result.appendHtmlTag("div", "class", "navigationPaginationWrapper", "id", section.getID());
		RenderResult navigation = new RenderResult(user);
		navigation.appendHtmlTag("div", "class", "navigationPagination");

		renderNavigation(getStartRow(section, user),
				getCount(section, user), section.getID(),
				navigation);
		renderTableSizeSelector(getCount(section, user), section.getID(),
				navigation, user);
		navigation.appendHtml("</div>");
		result.append(navigation);
		result.append(table);
		result.append(navigation);
		result.appendHtml("</div>");
	}

	private void renderTableSizeSelector(int count, String id, RenderResult result, UserContext user) {

		result.appendHtml("<div class='toolBar tableSize'>");

		Integer[] sizeArray = getSizeChoices(user);

		result.appendHtml("<span class=fillText>Show </span>"
				+ "<select class='count'>");

		for (Integer size : sizeArray) {
			if (new Integer(count).equals(size)) {
				result.appendHtml("<option selected='selected' value='" + size + "'>" + size
						+ "</option>");
			}
			else {
				result.appendHtml("<option value='" + size + "'>" + size
						+ "</option>");
			}
		}
		if (new Integer(count).equals(Integer.MAX_VALUE)) {
			result.appendHtml("<option selected='selected' value='Max'>Max</option>");
		}
		else {
			result.appendHtml("<option value='Max'>Max</option>");
		}
		result.appendHtml("</select>");
		result.appendHtml(getResultSize(user));
		result.appendHtml("</div>");

	}

	private void renderNavigation(int startRow, int count, String id, RenderResult result) {
		result.appendHtml("<div class='toolBar avoidMenu'>");
		renderToolbarButton(
				"begin.png", "KNOWWE.core.plugin.pagination.navigate('"
						+ id + "', 'begin')",
				(startRow > 1), result);
		renderToolbarButton(
				"back.png", "KNOWWE.core.plugin.pagination.navigate('"
						+ id + "', 'back')",
				(startRow > 1), result);
		result.appendHtml("<span class=fillText> Lines </span>");
		result.appendHtml("<input size=3 class='startRow' type=\"field\" value='"
				+ startRow + "'>");
		result.appendHtml("<span class=fillText> to </span>");
		if (new Integer(count).equals(Integer.MAX_VALUE)) {
			result.append("maximum lines");
		}
		else {
			result.append((startRow + count - 1));
		}
		renderToolbarButton(
				"forward.png", "KNOWWE.core.plugin.pagination.navigate('"
						+ id + "', 'forward')", true, result);
		result.appendHtml("</div>");
	}

	private void renderToolbarButton(String icon, String action, boolean enabled, RenderResult builder) {
		int index = icon.lastIndexOf('.');
		String suffix = icon.substring(index);
		icon = icon.substring(0, index);
		if (enabled) {
			builder.appendHtml("<a onclick=\"");
			builder.appendHtml(action);
			builder.appendHtml(";\">");
		}
		builder.appendHtml("<span class='toolButton ");
		builder.appendHtml(enabled ? "enabled" : "disabled");
		builder.appendHtml("'>");
		builder.appendHtml("<img src='KnowWEExtension/navigation_icons/");
		builder.appendHtml(icon);
		if (!enabled) builder.appendHtml("_deactivated");
		builder.appendHtml(suffix).appendHtml("' /></span>");
		if (enabled) {
			builder.appendHtml("</a>");
		}
	}

	private static Integer[] getSizeChoices(UserContext user) {
		List<Integer> sizes = new LinkedList<Integer>();
		int[] sizeArray = new int[] {
				10, 20, 50, 100 };
		for (int size : sizeArray) {
			sizes.add(size);
		}
		return sizes.toArray(new Integer[sizes.size()]);
	}

	private static Map<String, String> getPaginationValues(Section<?> section, UserContext user) {
		Map<String, String> paginationValues = new HashMap<String, String>();
		JSONObject json;
		if (getJSONCookieString(section,
				user) != null) {
			try {
				json = new JSONObject(Strings.decodeURL(getJSONCookieString(section,
						user)));
				paginationValues.put(STARTROW,
						getValueFromJsonObject(json, STARTROW, STARTROW_DEFAULT));
				paginationValues.put(COUNT, getValueFromJsonObject(json, COUNT, COUNT_DEFAULT));
				paginationValues.put(SORTING,
						getValueFromJsonObject(json, SORTING, SORTING_DEFAULT));
				paginationValues.put(NATURALORDER,
						getValueFromJsonObject(json, NATURALORDER, NATURALORDER_DEFAULT));
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else {
			paginationValues.put(STARTROW, STARTROW_DEFAULT);
			paginationValues.put(COUNT, COUNT_DEFAULT);
			paginationValues.put(SORTING, SORTING_DEFAULT);
			paginationValues.put(NATURALORDER, NATURALORDER_DEFAULT);
		}
		return paginationValues;
	}

	private static String getValueFromJsonObject(JSONObject json, String value, String defaultValue) throws JSONException {
		String returnValue = defaultValue;
		if (json.has(value)) {
			returnValue = json.getString(value);
		}
		return returnValue;
	}

	private static String getJSONCookieString(Section<?> sec, UserContext user) {
		return getCookie(user, "PaginationDecoratingRenderer-" + sec.getID(), null);
	}

	private static String getCookie(UserContext user, String cookieName, String defaultValue) {
		if (user != null && user.getRequest() != null && user.getRequest().getCookies() != null) {
			for (Cookie cookie : user.getRequest().getCookies()) {
				if (cookie.getName().equals(cookieName)) {
					return cookie.getValue();
				}
			}
		}
		return defaultValue;
	}

	public static int getStartRow(Section<?> sec, UserContext user) {
		return Integer.parseInt(getPaginationValues(sec, user).get(STARTROW));
	}

	/**
	 * Handle appropriately in the decorated renderer. Be aware of maximum
	 * lengths (e.g. 10000 chars without \n for jspwiki pipeline.)
	 * 
	 * @created 22.01.2014
	 * @param sec
	 * @param user
	 * @return the count of elements to be shown (if "Max" is selected
	 *         Integer.MAX_VALUE is returned!)
	 */
	public static int getCount(Section<?> sec, UserContext user) {
		String count = getPaginationValues(sec, user).get(COUNT);
		if (count.equals("Max")) {
			return Integer.MAX_VALUE;
		}

		return Integer.parseInt(getPaginationValues(sec, user).get(COUNT));
	}

	public static String getSorting(Section<?> sec, UserContext user) {
		return getPaginationValues(sec, user).get(SORTING);
	}

	public static boolean getNaturalOrder(Section<?> sec, UserContext user) {
		return Boolean.parseBoolean(getPaginationValues(sec, user).get(NATURALORDER));
	}

	private static String getResultSize(UserContext context) {
		String maxResult = (String) context.getSession().getAttribute("maxResult");
		if (maxResult != null) {
			return "</select><span class=fillText> lines of </span>  " + maxResult;
		}
		else {
			return "";
		}
	}

	/**
	 * If the decorated Renderer knows about the size of its results it can set
	 * it here for an increase in information at the rendering in the navigation
	 * bar.
	 * 
	 * @created 27.01.2014
	 * @param context
	 * @param maxResult
	 */
	public static void setResultSize(UserContext context, int maxResult) {
		context.getSession().setAttribute("maxResult", Integer.toString(maxResult));
	}

}
