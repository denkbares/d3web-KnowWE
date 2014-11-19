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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.d3web.utils.Pair;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Decorate an existing Renderer which renders a table with a pagination bar.<br>
 * Following things must be considered:
 * <li>The table must have a defined header by using {@code<th>...</th>}
 * <li>The decorated renderer must implement the
 * sorting and the correct selection of the result. The values of the pagination can be obtained by using
 * getStartrow(..), getCount(..), getSorting(..) and getNaturalOrder(..).
 * <li>Optionally: You can exclude columns from
 * sorting by stating this explicitly like this: {@code<th class="notSortable">...</th>}
 * <li>Optionally: You can build filters for rows. First prepare your table header: {@code<th
 * class="filterable">...</th>}. Second createFilter(..) and then setFilterList(..). To get the active filters use
 * getFilters(..).
 * <li>Optionally: If the
 * decorated renderer knows the size of its result it can use setResultSize(..) to enable further rendering at the
 * pagination bar.
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
	private static final String FILTER = "filter";
	private static final String ACTIVEFILTERS = "filters";
	private static final String ACTIVEFILTERS_DEFAULT = "none";

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

		int count = getCount(section, user);
		renderNavigation(getStartRow(section, user),
				count, section.getID(),
				navigation);
		renderTableSizeSelector(count, section.getID(),
				navigation, user);
		//renderHiddenFilterDiv(user, navigation);
		navigation.appendHtml("</div>");
		result.append(navigation);
		result.append(table);
		result.append(navigation);
		renderHiddenFilterDiv(user, result, section);
		result.appendHtml("</div>");
	}

	private void renderTableSizeSelector(int count, String id, RenderResult result, UserContext user) {

		result.appendHtml("<div class='toolBar tableSize'>");

		Integer[] sizeArray = getSizeChoices(user);

		result.appendHtml("<span class=fillText>Show </span>"
				+ "<select class='count'>");

		for (Integer size : sizeArray) {
			result.appendHtml("<option "
					+ (count == size ? "selected='selected' " : "")
					+ "value='" + size + "'>"
					+ (size == Integer.MAX_VALUE ? "All" : String.valueOf(size))
					+ "</option>");
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
				10, 20, 50, 100, Integer.MAX_VALUE };
		for (int size : sizeArray) {
			sizes.add(size);
		}
		return sizes.toArray(new Integer[sizes.size()]);
	}

	private static JSONObject getJsonObject(Section<?> section, UserContext user) {
		if (getJSONCookieString(section,
				user) != null) {
			try {
				return new JSONObject(Strings.decodeURL(getJSONCookieString(section,
						user)));
			}
			catch (JSONException e) {
				Log.warning("Exception while parsing json", e);
			}
		}
		return null;
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
		try {
			if (getJsonObject(sec, user) != null && getJsonObject(sec, user).has(STARTROW)) {
				return getJsonObject(sec, user).getInt(STARTROW);
			}
		}
		catch (JSONException e) {
			Log.warning("Exception while parsing start row", e);
		}
		return Integer.parseInt(STARTROW_DEFAULT);
	}

	/**
	 * Handle appropriately in the decorated renderer. Be aware of maximum lengths (e.g. 10000 chars without \n for
	 * jspwiki pipeline.)
	 *
	 * @param sec
	 * @param user
	 * @return the count of elements to be shown (if "Max" is selected Integer.MAX_VALUE is returned!)
	 * @created 22.01.2014
	 */
	public static int getCount(Section<?> sec, UserContext user) {
		try {
			if (getJsonObject(sec, user) != null && getJsonObject(sec, user).has(COUNT)) {
				return getJsonObject(sec, user).getInt(COUNT);
			}
		}
		catch (JSONException e) {
			Log.warning("Exception while parsing count", e);
		}
		return Integer.parseInt(COUNT_DEFAULT);
	}

	public static String getSorting(Section<?> sec, UserContext user) {
		try {
			if (getJsonObject(sec, user) != null && getJsonObject(sec, user).has(SORTING)) {
				return getJsonObject(sec, user).getString(SORTING);
			}
		}
		catch (JSONException e) {
			Log.warning("Exception while parsing sorting", e);
		}
		return SORTING_DEFAULT;
	}

	public static Map<String, List<String>> getFilters(Section<?> sec, UserContext user) {
		Map<String, List<String>> activeFilters = new HashMap<String, List<String>>();
		try {
			if (getJsonObject(sec, user) != null && getJsonObject(sec, user).has(ACTIVEFILTERS)) {
				JSONObject json = getJsonObject(sec, user).getJSONObject(ACTIVEFILTERS);

				Iterator iterator = json.keys();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					List<String> filterValues = new LinkedList<String>();
					JSONArray jsonArray = json.getJSONArray(key);
					for (int i = 0; i < jsonArray.length(); i++) {
						filterValues.add(jsonArray.get(i).toString());
					}
					activeFilters.put(key, filterValues);
				}
			}
			return activeFilters;
		}
		catch (JSONException e) {
			Log.warning("Exception while parsing filters", e);
		}
		return activeFilters;
	}

	public static boolean getNaturalOrder(Section<?> sec, UserContext user) {
		try {
			if (getJsonObject(sec, user) != null && getJsonObject(sec, user).has(NATURALORDER)) {
				return getJsonObject(sec, user).getBoolean(NATURALORDER);
			}
		}
		catch (JSONException e) {
			Log.warning("Exception while parsing order", e);
		}
		return Boolean.parseBoolean(NATURALORDER_DEFAULT);
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
	 * If the decorated Renderer knows about the size of its results it can set it here for an increase in information
	 * at the rendering in the navigation bar.
	 *
	 * @created 27.01.2014
	 */
	public static void setResultSize(UserContext context, int maxResult) {
		context.getSession().setAttribute("maxResult", Integer.toString(maxResult));
	}

	public static void setFilterList(UserContext context, Pair<String, List<String>>... filters) {
		List<Pair<String, List<String>>> filterList = new LinkedList<Pair<String, List<String>>>();
		for (Pair<String, List<String>> filter : filters) {
			filterList.add(filter);
		}
		context.getSession().setAttribute(FILTER, filterList);
	}

	public static Pair<String, List<String>> createFilter(String header, String... terms) {
		List<String> filterTerms = new LinkedList<String>();
		for (String term : terms) {
			filterTerms.add(term);
		}
		Pair filter = new Pair<String, List<String>>(header, filterTerms);
		return filter;
	}

	private static List<Pair<String, List<String>>> getFilterList(UserContext context) {
		List<Pair<String, List<String>>> filterList = (List<Pair<String, List<String>>>) context.getSession()
				.getAttribute(FILTER);
		if (filterList == null) {
			return new LinkedList<Pair<String, List<String>>>();
		}
		return filterList;
	}

	private static void renderHiddenFilterDiv(UserContext context, RenderResult result, Section<?> section) {
		List<Pair<String, List<String>>> filterList = getFilterList(context);
		result.appendHtmlTag("div", "id", "paginationFilters", "display", "block");

		for (Pair<String, List<String>> filter : filterList) {
			result.appendHtmlTag("div", "filterName", filter.getA());
			List<String> filters = filter.getB();
			for (String filterValue : filters) {
				result.appendHtmlTag("div", "filterValue", filterValue);
				result.appendHtmlTag("span");
				String checked = "";
				if (getFilters(section, context).containsKey(filter.getA()) &&
						getFilters(section, context).get(filter.getA()).contains(filterValue)) {
					checked = "checked";
				}
				result.appendHtml("<input type='checkbox' onchange='KNOWWE.core.plugin.pagination.filter(this, &#39;" + section
						.getID() + "&#39;)' filterkey='" + filter.getA() + "' filtervalue='" + filterValue + "' " + checked + ">");
				result.appendHtml(filterValue);
				result.appendHtml("</span>");
				result.appendHtml("</div>");
			}
			result.appendHtml("</div>");
		}
		result.appendHtml("</ul>");
		result.appendHtml("</div>");
	}

}
