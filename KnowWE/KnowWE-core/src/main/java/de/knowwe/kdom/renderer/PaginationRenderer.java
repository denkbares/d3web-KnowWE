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
import de.knowwe.util.FontAwesomeIcon;

/**
 * {@link #PaginationRenderer(de.knowwe.core.kdom.rendering.Renderer)} serves two main purposes: to have a
 * standard
 * and easy way to obtain all variables which are needed for controlling HTML table navigation/sorting/filtering
 * behaviour and to have a consistent
 * style for
 * the pagination of these tables. {@link #PaginationRenderer(de.knowwe.core.kdom.rendering.Renderer)} can either
 * decorate an existing {@link de.knowwe.core.kdom.rendering.Renderer} or you can use its
 * static methods for a little bit more freedom. (It's not possible at the moment to use the static methods within an
 * AsynchronRenderer.)
 * <p>
 * IMPORTANT: If you don't use the {@link #PaginationRenderer(de.knowwe.core.kdom.rendering.Renderer)} as a decorating
 * renderer, you have to add the
 * attribute 'pagination' where its value is the ID of that section to the table tag({@code<table
 * pagination='$sectionId'>}). <br><br>
 * The values of the pagination can be obtained by using {@link #getStartRow(de.knowwe.core.kdom.parsing.Section,
 * de.knowwe.core.user.UserContext)} and {@link #getCount(de.knowwe.core.kdom.parsing.Section,
 * de.knowwe.core.user.UserContext)} for the navigation.<br><br>
 * Optionally: If you want to enable sorting for the table it must have defined table column headers by using
 * {@code<th>...</th>} and additionally you need to add the parameter 'sortable' with either the value 'single'
 * ({@code<table
 * pagination='$sectionId' sortable='single'>}) or
 * 'multi' ({@code<table
 * pagination='$sectionId' sortable='multi'>}) to get single column sorting or multiple column sorting
 * respectively.<br>
 * The values for the sorting can be fetched by {@link #getSingleColumnSorting(de.knowwe.core.kdom.parsing.Section,
 * de.knowwe.core.user.UserContext)} and {@link #getMultiColumnSorting(de.knowwe.core.kdom.parsing.Section,
 * de.knowwe.core.user.UserContext)} respectively. <br>
 * You can exclude columns from sorting by stating this explicitly like this: {@code<th
 * class="notSortable">...</th>}<br><br>
 * Optionally: You can apply filters to rows by preparing your table column headers with {@code<th
 * class="filterable">...</th>} and use in your decorated render class {@link #createFilter(String, String...)} and
 * then
 * {@link #setFilterList(de.knowwe.core.user.UserContext, de.d3web.utils.Pair[])}  }. To get the active filters use
 * {@link #getFilters(de.knowwe.core.kdom.parsing.Section, de.knowwe.core.user.UserContext)}<br><br>
 * Optionally: If the decorated renderer knows the size of its result it can use {@link
 * #setResultSize(de.knowwe.core.user.UserContext, de.knowwe.core.kdom.parsing.Section, int)} to render more
 * information in the pagination bar.
 *
 * @author Stefan Plehn
 * @created 14.01.2014
 */
public class PaginationRenderer implements Renderer {

	private final Renderer decoratedRenderer;

	public static final String STARTROW = "startRow";
	private static final String STARTROW_DEFAULT = "1";
	public static final String COUNT = "count";
	private static final String COUNT_DEFAULT = "50";
	public static final String SORTING = "sorting";
	public static final String RESULTSIZE = "resultsize";
	private static final String FILTER = "filter";
	private static final String ACTIVEFILTERS = "filters";

	public PaginationRenderer(Renderer decoratedRenderer) {
		this.decoratedRenderer = decoratedRenderer;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		result.appendHtmlTag("div", "class", "knowwe-paginationWrapper", "id", section.getID());
		RenderResult table = new RenderResult(user);
		decoratedRenderer.render(section, user, table);
		RenderResult navigation = new RenderResult(user);
		renderTableSizeSelector(section, user, navigation);
		renderNavigation(section, user, navigation);

		result.append(navigation);
		result.append(table);
		result.append(navigation);
		renderHiddenFilterDiv(user, result, section);
		result.appendHtml("</div>");

	}

	public static void renderToolSeparator(RenderResult navigation) {
		navigation.appendHtml("<div class='knowwe-paginationToolSeparator'>");
	}

	public static String getToolSeparator() {
		return "<div class='knowwe-paginationToolSeparator'></div>";
	}

	/**
	 * Renders the result size selector.
	 *
	 * @param sec
	 * @param user
	 * @param result
	 */
	public static void renderTableSizeSelector(Section<?> sec, UserContext user, RenderResult result) {
		int count = getCount(sec, user);
		result.appendHtml("<div class='knowwe-paginationToolbar' pagination=")
				.append(Strings.quoteSingle(sec.getID()))
				.appendHtml(">");

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
		result.appendHtml(getResultSizeTag(sec, user));
		result.appendHtml("<div class='toolSeparator'>");
		result.appendHtml("</div>");
		result.appendHtml("</div>");

	}

	/**
	 * Renders the navigation icons.
	 *
	 * @param sec
	 * @param user
	 * @param result
	 */
	public static void renderNavigation(Section<?> sec, UserContext user, RenderResult result) {
		String id = sec.getID();
		int count = getCount(sec, user);
		int startRow = getStartRow(sec, user);
		result.appendHtml("<div class='knowwe-paginationToolbar avoidMenu' pagination=")
				.append(Strings.quoteSingle(sec.getID()))
				.appendHtml(">");
		if (count != Integer.MAX_VALUE) {
			renderToolbarButton(
					FontAwesomeIcon.FIRST, "KNOWWE.core.plugin.pagination.navigate('"
							+ id + "', 'begin')",
					(startRow > 1), result);
			renderToolbarButton(
					FontAwesomeIcon.PREVIOUS, "KNOWWE.core.plugin.pagination.navigate('"
							+ id + "', 'back')",
					(startRow > 1), result);
			result.appendHtml("<span class=fillText> Lines </span>");

			result.appendHtml("<input size=3 class='startRow' type=\"field\" value='"
					+ startRow + "'>");

			result.appendHtml("<span class=fillText> to </span>");

			String resultSize = getResultSize(user);
			boolean forward = true;
			if (!resultSize.equals("maximum lines")) {
				if (Integer.parseInt(resultSize) < startRow + count - 1) {
					forward = false;
					result.append(resultSize);
				}
			}
			else {
				result.append((startRow + count - 1));
			}

			renderToolbarButton(
					FontAwesomeIcon.NEXT, "KNOWWE.core.plugin.pagination.navigate('"
							+ id + "', 'forward')", forward, result);

		}
		if (count == Integer.MAX_VALUE) {
			renderToolbarButton(
					FontAwesomeIcon.FIRST, "KNOWWE.core.plugin.pagination.navigate('"
							+ id + "', 'begin')",
					false, result);
			renderToolbarButton(
					FontAwesomeIcon.PREVIOUS, "KNOWWE.core.plugin.pagination.navigate('"
							+ id + "', 'back')",
					false, result);
			result.appendHtml("<span class=fillText> Lines </span>");
			result.appendHtml("<input size=3 class='startRow' type=\"field\" value='1'>");

			result.appendHtml("<span class=fillText> to " + getResultSize(user) + "</span>");

			renderToolbarButton(
					FontAwesomeIcon.NEXT, "KNOWWE.core.plugin.pagination.navigate('"
							+ id + "', 'forward')", false, result);
		}

		result.appendHtml("</div>");
	}

	private static void renderToolbarButton(FontAwesomeIcon icon, String action, boolean enabled, RenderResult builder) {
		if (enabled) {
			builder.appendHtml("<a onclick=\"");
			builder.appendHtml(action);
			builder.appendHtml(";\">");
		}
		builder.appendHtml(icon.increaseSize(33, "knowwe-paginationNavigationIcons"));
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

	private static String getResultSize(UserContext user) {
		if (user.getRequest().getAttribute(RESULTSIZE) != null) {
			return user.getRequest().getAttribute(RESULTSIZE).toString();
		}
		else {
			return "maximum lines";
		}

	}

	/**
	 * Get the chosen count in pagination bar
	 * Handle appropriately in the decorated renderer. Be aware of maximum lengths (e.g. 10000 chars without \n for
	 * jspwiki pipeline.)
	 *
	 * @param sec  the section
	 * @param user the user context
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

	/**
	 * @param sec  the section
	 * @param user the user context
	 * @return the latest sorting in form of a Pair. Value A represents the sorting value, value B the natural order
	 * (true is ascending)
	 */
	public static Pair<String, Boolean> getSingleColumnSorting(Section<?> sec, UserContext user) {
		if (!(getSorting(sec, user, true).isEmpty())) {
			return getSorting(sec, user, true).stream().findFirst().get();
		}
		else {
			return null;
		}

	}

	/**
	 * @param sec  the section
	 * @param user the user context
	 * @return a list of pairs where Value A represents the sorting value, value B the natural order
	 * (true is ascending) ordered by their chronology (newest = first)
	 */
	public static List<Pair<String, Boolean>> getMultiColumnSorting(Section<?> sec, UserContext user) {
		return getSorting(sec, user, false);
	}

	private static List<Pair<String, Boolean>> getSorting(Section<?> sec, UserContext user, boolean onlyFirst) {
		List<Pair<String, Boolean>> list = new LinkedList<>();
		JSONArray sorting = getSortingArray(sec, user);
		if (sorting.length() == 0) {
			return list;
		}
		int length;
		if (onlyFirst) {
			length = 1;
		}
		else {
			length = sorting.length();
		}
		for (int i = 0; i < length; i++) {
			try {
				String sort = sorting.getJSONObject(i).getString("sort");
				boolean naturalOrder = sorting.getJSONObject(i).getBoolean("naturalOrder");
				Pair<String, Boolean> sortObject = new Pair<>(sort, naturalOrder);
				list.add(sortObject);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	private static JSONArray getSortingArray(Section<?> sec, UserContext user) {
		try {
			if (getJsonObject(sec, user) != null && getJsonObject(sec, user).has(SORTING)) {
				return getJsonObject(sec, user).getJSONArray(SORTING);
			}
		}
		catch (JSONException e) {
			Log.warning("Exception while parsing sorting", e);
		}
		return new JSONArray();
	}

	/**
	 * Get all filters selected for this tab.le,
	 *
	 * @param sec  the section
	 * @param user the user context
	 * @return a map with all filter names which have values to be filtered by.
	 */
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

	private static String getResultSizeTag(Section<?> sec, UserContext user) {
		String maxResult = getResultSize(user);
		return "<span class=fillText> lines of " + maxResult + "</span>";
	}

	/**
	 * If the decorated Renderer knows about the size of its results it can set it here for an increase of information
	 * at the rendering in the navigation bar.
	 *
	 * @created 27.01.2014
	 */
	public static void setResultSize(UserContext context, int maxResult) {
		context.getRequest().setAttribute(RESULTSIZE, Integer.toString(maxResult));
	}

	/**
	 * Use this method to enable those filters you created by {@link #createFilter(String, String...)}.
	 *
	 * @param context the user context
	 * @param filters created by {@link #createFilter(String, String...)}
	 */
	public static void setFilterList(UserContext context, Pair<String, List<String>>... filters) {
		List<Pair<String, List<String>>> filterList = new LinkedList<Pair<String, List<String>>>();
		for (Pair<String, List<String>> filter : filters) {
			filterList.add(filter);
		}
		context.getRequest().setAttribute(FILTER, filterList);
	}

	/**
	 * Create a new filter for a table column
	 *
	 * @param header The name of the HTML table column header (=the string inside the {@code <th></th>}) tags
	 * @param values All the values you want to be able to filter by
	 * @return a new filter (you have to enable it (and others for other columns) by calling {@link
	 * #setFilterList(de.knowwe.core.user.UserContext, de.d3web.utils.Pair[])}.
	 */
	public static Pair<String, List<String>> createFilter(String header, String... values) {
		List<String> filterTerms = new LinkedList<String>();
		for (String value : values) {
			filterTerms.add(value);
		}
		Pair filter = new Pair<>(header, filterTerms);
		return filter;
	}

	private static List<Pair<String, List<String>>> getFilterList(UserContext context) {
		List<Pair<String, List<String>>> filterList = (List<Pair<String, List<String>>>) context.getRequest()
				.getAttribute(FILTER);
		if (filterList == null) {
			return new LinkedList<>();
		}
		return filterList;
	}

	private static void renderHiddenFilterDiv(UserContext context, RenderResult result, Section<?> section) {
		List<Pair<String, List<String>>> filterList = getFilterList(context);
		result.appendHtmlTag("div", "id", "paginationFilters", "display", "none");

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
				result.appendHtml("<input type='checkbox' class='knowwe-paginationFilter' onchange='KNOWWE.core.plugin.pagination.filter(this, &#39;" + section
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
