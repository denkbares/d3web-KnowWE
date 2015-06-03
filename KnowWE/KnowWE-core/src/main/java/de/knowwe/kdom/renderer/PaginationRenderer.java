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

import java.util.Collections;
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
import de.knowwe.util.Icon;

/**
 * {@link #PaginationRenderer(de.knowwe.core.kdom.rendering.Renderer)} serves two main purposes: to
 * have a standard and easy way to obtain all variables which are needed for controlling HTML table
 * navigation/sorting/filtering behaviour and to have a consistent style for the pagination of these
 * tables. {@link #PaginationRenderer(de.knowwe.core.kdom.rendering.Renderer)} can either decorate
 * an existing {@link de.knowwe.core.kdom.rendering.Renderer} or you can use its static methods for
 * a little bit more freedom. (It's not possible at the moment to use the static methods within an
 * AsynchronRenderer.)
 * <p/>
 * IMPORTANT: If you don't use the {@link #PaginationRenderer(de.knowwe.core.kdom.rendering.Renderer)}
 * as a decorating renderer, you have to add the attribute 'pagination' where its value is the ID of
 * that section to the table tag({@code &lt;table pagination='$sectionId'&gt;}). <br><br> The values
 * of the pagination can be obtained by using {@link #getStartRow(de.knowwe.core.kdom.parsing.Section,
 * de.knowwe.core.user.UserContext)} and {@link #getCount(de.knowwe.core.kdom.parsing.Section,
 * de.knowwe.core.user.UserContext)} for the navigation.<br><br> Optionally: If you want to enable
 * sorting for the table it must have defined table column headers by using {@code
 * &lt;th&gt;...&lt;/th&gt;} and additionally you need to add the parameter 'sortable' with either
 * the value 'single' ({@code &lt;table pagination='$sectionId' sortable='single'&gt;}) or 'multi'
 * ({@code &lt;table pagination='$sectionId' sortable='multi'&gt;}) to get single column sorting or
 * multiple column sorting respectively.<br> The values for the sorting can be fetched by {@link
 * #getSingleColumnSorting(de.knowwe.core.kdom.parsing.Section, de.knowwe.core.user.UserContext)}
 * and {@link #getMultiColumnSorting(de.knowwe.core.kdom.parsing.Section,
 * de.knowwe.core.user.UserContext)} respectively. <br> You can exclude columns from sorting by
 * stating this explicitly like this: {@code &lt;th class="notSortable"&gt;...&lt/th&gt;}<br><br>
 * Optionally: You can apply filters to rows by preparing your table column headers with {@code
 * &lt;th class="filterable"&gt;...&lt;/th&gt;} and use in your decorated render class {@link
 * #createFilter(String, String...)} and then {@link #setFilterList(de.knowwe.core.user.UserContext,
 * de.d3web.utils.Pair[])}  }. To get the active filters use {@link #getFilters(de.knowwe.core.kdom.parsing.Section,
 * de.knowwe.core.user.UserContext)}<br><br> Optionally: If the decorated renderer knows the size of
 * its result it can use {@link #setResultSize(de.knowwe.core.user.UserContext, int)} to render more
 * information in the pagination bar.
 *
 * @author Stefan Plehn
 * @created 14.01.2014
 */
public class PaginationRenderer implements Renderer {

	public static final int DEFAULT_SHOW_NAVIGATION_MAX_RESULTS = 10;
	private final Renderer decoratedRenderer;

	public static final String UNKNOWN_RESULT_SIZE = "unknown";
	public static final String STARTROW = "startRow";
	private static final String STARTROW_DEFAULT = "1";
	public static final String COUNT = "count";
	private static final String COUNT_DEFAULT = "20";
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
		RenderResult pagination = new RenderResult(user);

		String resultString = PaginationRenderer.getResultSizeString(user);
		boolean show = true;
		if (!resultString.equals(UNKNOWN_RESULT_SIZE)) {
			int resultSize = Integer.parseInt(resultString);
			show = resultSize > DEFAULT_SHOW_NAVIGATION_MAX_RESULTS;
		}
		PaginationRenderer.renderPagination(section, user, pagination, show);
		result.append(pagination);
		result.append(table);
		result.append(pagination);

		renderHiddenFilterDiv(user, result, section);
		result.appendHtml("</div>");

	}

	public static void renderPagination(Section<?> section, UserContext user, RenderResult result, boolean show) {
		renderTableSizeSelector(section, user, result, show);
		renderNavigation(section, user, result, show);
	}

	public static void renderToolSeparator(RenderResult navigation) {
		navigation.appendHtml("<div class='knowwe-paginationToolSeparator'>");
	}

	public static String getToolSeparator() {
		return "<div class='knowwe-paginationToolSeparator'></div>";
	}

	/**
	 * Renders the result size selector.
	 */
	public static void renderTableSizeSelector(Section<?> sec, UserContext user, RenderResult result, boolean show) {
		int count = getCount(sec, user);
		renderToolBarElementHeader(sec, result, show);

		Integer[] sizeArray = getSizeChoices(user);

		result.appendHtml("<span class=fillText>Show </span><select class='count'>");

		boolean foundSelected = false;
		for (Integer size : sizeArray) {
			boolean selected = count == size;
			if (selected) foundSelected = true;
			boolean setSelected = selected || size == Integer.MAX_VALUE && !foundSelected;
			result.appendHtml("<option "
					+ (setSelected ? "selected='selected' " : "")
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
	 */
	public static void renderNavigation(Section<?> sec, UserContext user, RenderResult result, boolean show) {
		String id = sec.getID();
		int count = getCount(sec, user);
		int startRow = getStartRow(sec, user);
		String resultSizeString = getResultSizeString(user);
		int resultSize = getResultSize(user);
		int resultSizeStringLength = Math.min(Math.max(2, ((int) Math.log10(resultSize)) + 1), 6);
		int endRow = Math.min(resultSize, startRow + count - 1);

		int fill = ((int) (Math.log10(resultSize)) - ((int) Math.log10(endRow)));
		StringBuilder fillString = new StringBuilder("");
		for (int i = 0; i < fill; i++) {
			fillString.append("&nbsp;");
		}

		renderToolBarElementHeader(sec, result, show);
		if (count != Integer.MAX_VALUE) {
			renderToolbarButton(Icon.FIRST, "KNOWWE.core.plugin.pagination.navigate('" + id + "', 'begin')", (startRow > 1), result);
			renderToolbarButton(Icon.PREVIOUS, "KNOWWE.core.plugin.pagination.navigate('" + id + "', 'back')", (startRow > 1), result);
			result.appendHtml("<span class=fillText> Rows </span>");

			result.appendHtml("<input size=").append(resultSizeStringLength)
					.appendHtml(" class='startRow' type='field' value='")
					.append(startRow).appendHtml("'>");

			boolean forward = false;
			if (resultSize >= startRow + count) {
				forward = true;
			}
			result.appendHtml("<span class=fillText> to ")
					.append(fillString)
					.append(endRow)
					.appendHtml("&nbsp;</span>");

			renderToolbarButton(Icon.NEXT, "KNOWWE.core.plugin.pagination.navigate('" + id + "', 'forward')", forward, result);
			renderToolbarButton(Icon.LAST, "KNOWWE.core.plugin.pagination.navigate('" + id + "', 'end')", forward, result);

		}
		if (count == Integer.MAX_VALUE) {
			renderToolbarButton(Icon.FIRST, "KNOWWE.core.plugin.pagination.navigate('" + id + "', 'begin')", false, result);
			renderToolbarButton(Icon.PREVIOUS, "KNOWWE.core.plugin.pagination.navigate('" + id + "', 'back')", false, result);
			result.appendHtml("<span class=fillText> Lines </span>");
			result.appendHtml("<input size=")
					.append(resultSizeStringLength)
					.appendHtml(" class='startRow' type='field' value='1'>");

			if (resultSizeString.equals(UNKNOWN_RESULT_SIZE)) {
				result.appendHtml("<span class=fillText> til end&nbsp;</span>");
			}
			else {
				result.appendHtml("<span class=fillText> to ")
						.append(fillString)
						.append(endRow)
						.appendHtml("&nbsp;</span>");
			}

			renderToolbarButton(Icon.NEXT, "KNOWWE.core.plugin.pagination.navigate('" + id + "', 'forward')", false, result);
			renderToolbarButton(Icon.LAST, "KNOWWE.core.plugin.pagination.navigate('" + id + "', 'end')", false, result);
		}

		result.appendHtml("</div>");
	}

	public static int getResultSize(UserContext user) {
		String resultSizeString = getResultSizeString(user);
		int resultSize = Integer.MAX_VALUE;
		try {
			resultSize = Integer.parseInt(resultSizeString);
		}
		catch (NumberFormatException ignored) {
		}
		return resultSize;
	}

	private static void renderToolBarElementHeader(Section<?> sec, RenderResult result, boolean show) {
		result.appendHtml("<div class='knowwe-paginationToolbar noselect' pagination=")
				.append(Strings.quoteSingle(sec.getID()));
		if (!show) {
			result.append(" style='display:none'");
		}
		result.appendHtml(">");
	}

	private static void renderToolbarButton(Icon icon, String action, boolean enabled, RenderResult builder) {
		if (enabled) {
			builder.appendHtml("<a onclick=\"");
			builder.appendHtml(action);
			builder.appendHtml(";\">");
		}
		builder.appendHtml(icon.increaseSize(Icon.Percent.by33)
				.addClasses("knowwe-paginationNavigationIcons")
				.toHtml());
		if (enabled) {
			builder.appendHtml("</a>");
		}
	}

	private static Integer[] getSizeChoices(UserContext user) {
		List<Integer> sizes = new LinkedList<>();
		int[] sizeArray = new int[] {
				10, 20, 50, 100, 200, 500, 1000, Integer.MAX_VALUE };
		int resultSize = getResultSize(user);
		for (int size : sizeArray) {
			if (size <= resultSize || size == Integer.MAX_VALUE) sizes.add(size);
		}
		return sizes.toArray(new Integer[sizes.size()]);
	}

	private static JSONObject getJsonObject(Section<?> section, UserContext user) {
		if (getJSONCookieString(section, user) != null) {
			try {
				return new JSONObject(Strings.decodeURL(getJSONCookieString(section, user)));
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
			JSONObject jsonObject = getJsonObject(sec, user);
			if (jsonObject != null && jsonObject.has(STARTROW)) {
				return jsonObject.getInt(STARTROW);
			}
		}
		catch (JSONException e) {
			Log.warning("Exception while parsing start row", e);
		}
		return Integer.parseInt(STARTROW_DEFAULT);
	}

	private static String getResultSizeString(UserContext user) {
		if (user.getRequest().getAttribute(RESULTSIZE) != null) {
			return user.getRequest().getAttribute(RESULTSIZE).toString();
		}
		else {
			return UNKNOWN_RESULT_SIZE;
		}

	}

	/**
	 * Get the chosen count in pagination bar Handle appropriately in the decorated renderer. Be
	 * aware of maximum lengths (e.g. 10000 chars without \n for jspwiki pipeline.)
	 *
	 * @param sec  the section
	 * @param user the user context
	 * @return the count of elements to be shown (if "Max" is selected Integer.MAX_VALUE is
	 * returned!)
	 * @created 22.01.2014
	 */
	public static int getCount(Section<?> sec, UserContext user) {
		try {
			JSONObject jsonObject = getJsonObject(sec, user);
			if (jsonObject != null && jsonObject.has(COUNT)) {
				return jsonObject.getInt(COUNT);
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
	 * @return the latest sorting in form of a Pair. Value A represents the sorting value, value B
	 * the natural order (true is ascending)
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
			JSONObject jsonObject = getJsonObject(sec, user);
			if (jsonObject != null && jsonObject.has(SORTING)) {
				return jsonObject.getJSONArray(SORTING);
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
		Map<String, List<String>> activeFilters = new HashMap<>();
		try {
			JSONObject jsonObject = getJsonObject(sec, user);
			if (jsonObject != null && jsonObject.has(ACTIVEFILTERS)) {
				JSONObject json = jsonObject.getJSONObject(ACTIVEFILTERS);

				Iterator iterator = json.keys();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					List<String> filterValues = new LinkedList<>();
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
		String resultSize = getResultSizeString(user);
		String tag = "";
		if (resultSize.equals(UNKNOWN_RESULT_SIZE)) {
			tag += "<span class=fillText> rows (overall number unknown)</span>";
		}
		else {
			tag += "<input class='resultSize' style='display:none' value='" + resultSize + "'/>";
			tag += "<span class=fillText>" + (getCount(sec, user) == Integer.MAX_VALUE ? "" : " of");
			tag += " " + resultSize + " rows</span>";
		}
		return tag;
	}

	/**
	 * If the decorated Renderer knows about the size of its results it can set it here for an
	 * increase of information at the rendering in the navigation bar.
	 *
	 * @created 27.01.2014
	 */
	public static void setResultSize(UserContext context, int maxResult) {
		context.getRequest().setAttribute(RESULTSIZE, Integer.toString(maxResult));
	}

	/**
	 * Use this method to enable those filters you created by {@link #createFilter(String,
	 * String...)}.
	 *
	 * @param context the user context
	 * @param filters created by {@link #createFilter(String, String...)}
	 */
	public static void setFilterList(UserContext context, Pair<String, List<String>>... filters) {
		List<Pair<String, List<String>>> filterList = new LinkedList<>();
		Collections.addAll(filterList, filters);
		context.getRequest().setAttribute(FILTER, filterList);
	}

	/**
	 * Create a new filter for a table column
	 *
	 * @param header The name of the HTML table column header (=the string inside the {@code
	 *               <th></th>}) tags
	 * @param values All the values you want to be able to filter by
	 * @return a new filter (you have to enable it (and others for other columns) by calling {@link
	 * #setFilterList(de.knowwe.core.user.UserContext, de.d3web.utils.Pair[])}.
	 */
	public static Pair<String, List<String>> createFilter(String header, String... values) {
		List<String> filterTerms = new LinkedList<>();
		Collections.addAll(filterTerms, values);
		return new Pair<>(header, filterTerms);
	}

	private static List<Pair<String, List<String>>> getFilterList(UserContext context) {
		@SuppressWarnings("unchecked")
		List<Pair<String, List<String>>> filterList = (List<Pair<String, List<String>>>) context.getRequest()
				.getAttribute(FILTER);
		if (filterList == null) {
			return new LinkedList<>();
		}
		return filterList;
	}

	public static void renderHiddenFilterDiv(UserContext context, RenderResult result, Section<?> section) {
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
