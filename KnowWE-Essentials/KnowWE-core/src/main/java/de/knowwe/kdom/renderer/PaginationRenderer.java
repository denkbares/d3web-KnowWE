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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import de.knowwe.core.Attributes;
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
 * AsynchronousRenderer.)
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
 * stating this explicitly like this: {@code <th class="notSortable">...</th>}
 * <p>
 * Optionally: If the decorated renderer knows the size of
 * its result it can use {@link #setResultSize(de.knowwe.core.user.UserContext, int)} to render more
 * information in the pagination bar.
 *
 * @author Stefan Plehn
 * @created 14.01.2014
 */
public class PaginationRenderer implements AsyncPreviewRenderer {
	private static final Logger LOGGER = LoggerFactory.getLogger(PaginationRenderer.class);

	public static final String REASON_PAGINATION = "pagination";
	private static final String PAGINATION_KEY = "pagination";
	private static final String COLUMNS = "columns";
	private static final String SELECTED_TEXTS = "selectedTexts";
	private static final String SELECTED_CUSTOM_TEXTS = "selectedCustomTexts";
	public static final String SELECT_ALL = "selectAll";
	private static final String ACTIVE = "active";
	private final Renderer decoratedRenderer;
	private final SortingMode sorting;
	private final boolean supportFiltering;

	private static final String UNKNOWN_RESULT_SIZE = "unknown";
	private static final String START_ROW = "startRow";
	private static final String RESULT_SIZE = "resultsize";
	private static final String COUNT = "count";
	private static final String START_ROW_DEFAULT = "1";
	private static final String SORTING = "sorting";
	private static final int COUNT_DEFAULT = 20;
	private static final String FILTER = "filter";

	public enum SortingMode {
		/**
		 * Sorting not supported
		 */
		off,
		/**
		 * Support sorting one column at a time
		 */
		single,
		/**
		 * Support multiple columns at the same time
		 */
		multi
	}

	public PaginationRenderer(Renderer decoratedRenderer) {
		this(decoratedRenderer, SortingMode.off, false);
	}

	public PaginationRenderer(Renderer decoratedRenderer, SortingMode sorting, boolean supportFiltering) {
		this.decoratedRenderer = decoratedRenderer;
		this.sorting = sorting;
		this.supportFiltering = supportFiltering;
	}

	@Override
	public void renderAsyncPreview(Section<?> section, UserContext user, RenderResult result) {
		if (decoratedRenderer instanceof AsyncPreviewRenderer) {
			renderWithPagination(section, user, result, (table) -> ((AsyncPreviewRenderer) decoratedRenderer).renderAsyncPreview(section, user, table));
		}
		else {
			render(section, user, result);
		}
	}

	public static boolean isPaginationRerendering(UserContext user) {
		return REASON_PAGINATION.equals(user.getParameter(Attributes.REASON));
	}

	@Override
	public boolean shouldRenderAsynchronous(Section<?> section, UserContext user) {
		return AsyncPreviewRenderer.shouldRenderAsynchronous(decoratedRenderer, section, user);
	}

	@NotNull
	public static Map<String, Set<Pattern>> getFilter(Section<?> section, UserContext user) {
		HashMap<String, Set<Pattern>> filterMap = new HashMap<>();

		JSONObject paginationSettings = getJsonObject(section, user);
		if (paginationSettings == null) return filterMap;
		JSONObject filter = paginationSettings.optJSONObject(FILTER);
		if (filter == null) return filterMap;
		if (!filter.optBoolean(ACTIVE, false)) return filterMap;
		JSONObject columns = filter.optJSONObject(COLUMNS);
		if (columns == null) return filterMap;

		for (Object columnKey : columns.keySet()) {
			String columnName = (String) columnKey;
			JSONObject columnObject = columns.optJSONObject(columnName);
			JSONArray selectedTexts = columnObject.optJSONArray(SELECTED_TEXTS);
			Set<Pattern> patterns = filterMap.computeIfAbsent(columnName, k -> new HashSet<>());
			if (selectedTexts != null) {
				boolean containsEmptyString = false;
				List<String> cleanedTexts = new ArrayList<>();
				for (int i = 0; i < selectedTexts.length(); i++) {
					String text = selectedTexts.getString(i);
					if (Strings.isBlank(text)) {
						containsEmptyString = true;
					}
					else {
						cleanedTexts.add(text);
					}
				}
				// with selectAll, all texts are considered selected, except the ones given in the array
				if (columnObject.optBoolean(SELECT_ALL)) {
					// we have to generate a reverse pattern matching everything except the given texts
					// will look something like: ^(?!(?:text1|text2|text3)$).*
					if (selectedTexts.length() > 0) {
						StringBuilder regex = new StringBuilder("(?s)^(?!(?:");
						regex.append(cleanedTexts.stream().map(Pattern::quote).collect(Collectors.joining("|")));
						regex.append(")\\z).").append(containsEmptyString ? "+" : "*");
						patterns
								.add(Pattern.compile(regex.toString()));
					}
				}
				// with !selectAll, only the texts given in the array are considered selected
				else {
					if (selectedTexts.length() > 0) {
						for (String text : cleanedTexts) {
							patterns.add(Pattern.compile(Pattern.quote(text)));
						}
						if (containsEmptyString) patterns.add(Pattern.compile("\\s*"));
					}
					// special case... not useful but we want to be correct -> !selectAll and nothing selected
					// generate pattern that matches nothing at all
					else {
						//noinspection RegExpUnexpectedAnchor
						patterns.add(Pattern.compile("a^"));
					}
				}
			}
			JSONArray selectedCustomTexts = columnObject.optJSONArray(SELECTED_CUSTOM_TEXTS);
			if (selectedCustomTexts != null) {
				for (int i = 0; i < selectedCustomTexts.length(); i++) {
					String regex = selectedCustomTexts.getString(i);
					Pattern pattern;
					try {
						pattern = Pattern.compile(regex);
					}
					catch (PatternSyntaxException e) {
						pattern = Pattern.compile(Pattern.quote(regex));
					}
					patterns.add(pattern);
				}
			}
		}

		return filterMap;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		renderWithPagination(section, user, result, (table) -> this.decoratedRenderer.render(section, user, table));
	}

	private void renderWithPagination(Section<?> section, UserContext user, RenderResult result, Consumer<RenderResult> renderFunction) {
		result.appendHtmlTag("div", "class", "knowwe-paginationWrapper", "id", section.getID(),
				"sorting-mode", sorting.name(), "filtering", this.supportFiltering ? "true" : "false");
		RenderResult table = new RenderResult(user);
		renderFunction.accept(table);

		renderPaginationInternal(section, user, result);
		result.append(table);
		renderPaginationInternal(section, user, result);

		result.appendHtml("</div>");
	}

	public static void renderPagination(Section<?> section, UserContext user, RenderResult result) {
		renderPagination(section, user, result, SortingMode.off, false);
	}

	public static void renderPagination(Section<?> section, UserContext user, RenderResult result, SortingMode sortingMode, boolean supportFiltering) {
		PaginationRenderer paginationRenderer = new PaginationRenderer(null, sortingMode, supportFiltering);
		paginationRenderer.renderPaginationInternal(section, user, result);
	}

	private void renderPaginationInternal(Section<?> section, UserContext user, RenderResult result) {
		renderTableSizeSelector(section, user, result);
		renderNavigation(section, user, result);
		if (supportFiltering) renderFilter(section, user, result);
	}

	private void renderFilter(Section<?> section, UserContext user, RenderResult result) {
		renderToolBarElement(section, result, () -> {
			// generate unique id in case the filter is added multiple times
			String id = "filter-activator-" + section.getID();
			Integer activators = (Integer) user.getRequest().getAttribute(id);
			if (activators == null) activators = 0;
			String uniqueId = id + "-" + activators;
			user.getRequest().setAttribute(id, activators + 1);

			result.appendHtmlTag("input", "class", "filter-activator", "type", "checkbox", "id", uniqueId, "name", uniqueId);
			result.appendHtmlElement("label", "Filter", "class", "fillText", "for", uniqueId);
			result.appendHtmlElement("button", "Clear Filter", "class", "clear-filter");
		});
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
	private void renderTableSizeSelector(Section<?> sec, UserContext user, RenderResult result) {
		int count = getCount(sec, user);
		renderToolBarElement(sec, result, () -> {
			Integer[] sizeArray = getSizeChoices(user);
			result.appendHtml("<span class='fillText'>Show </span><select class='count'>");

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
		});
	}

	/**
	 * Renders the navigation icons.
	 */
	private void renderNavigation(Section<?> sec, UserContext user, RenderResult result) {
		String id = sec.getID();
		int count = getCount(sec, user);
		int startRow = getStartRow(sec, user);
		String resultSizeString = getResultSizeString(user);
		int resultSize = getResultSize(user);
		int resultSizeStringLength = Math.min(Math.max(2, ((int) Math.log10(resultSize)) + 1), 6);
		int endRow = Math.min(resultSize, startRow + count - 1);

		int fill = ((int) (Math.log10(resultSize)) - ((int) Math.log10(endRow)));
		StringBuilder fillString = new StringBuilder();
		fillString.append("&nbsp;".repeat(Math.max(0, fill)));

		renderToolBarElement(sec, result, () -> {
			if (count != Integer.MAX_VALUE) {
				renderToolbarButton(Icon.FIRST, "KNOWWE.core.plugin.pagination.navigate('" + id + "', 'begin')", (startRow > 1), result);
				renderToolbarButton(Icon.PREVIOUS, "KNOWWE.core.plugin.pagination.navigate('" + id + "', 'back')", (startRow > 1), result);
				result.appendHtml("<span class=fillText> Rows </span>");

				result.appendHtml("<input size=").append(resultSizeStringLength)
						.appendHtml(" class='startRow' type='field' value='")
						.append(startRow).appendHtml("'>");

				result.appendHtml("<span class=fillText> to ")
						.append(fillString)
						.append(endRow)
						.appendHtml("&nbsp;</span>");

				boolean forward = resultSize >= startRow + count;
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
		});
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

	private void renderToolBarElement(Section<?> sec, RenderResult result, Runnable contentRenderer) {
		result.appendHtml("<div class='knowwe-paginationToolbar noselect' pagination=")
				.append(Strings.quoteSingle(sec.getID())).appendHtml(">");
		contentRenderer.run();
		result.appendHtml("</div>");
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
		return sizes.toArray(new Integer[0]);
	}

	private static JSONObject getJsonObject(Section<?> section, UserContext user) {
		String sectionStorage = user.getParameter(Attributes.LOCAL_SECTION_STORAGE);
		if (sectionStorage == null) return null;
		try {
			return new JSONObject(sectionStorage).optJSONObject(PAGINATION_KEY);
		}
		catch (JSONException e) {
			LOGGER.warn("Exception while parsing json", e);
			return null;
		}
	}

	public static int getStartRow(Section<?> sec, UserContext user) {
		try {
			JSONObject jsonObject = getJsonObject(sec, user);
			if (jsonObject != null && jsonObject.has(START_ROW)) {
				return jsonObject.getInt(START_ROW);
			}
		}
		catch (JSONException e) {
			LOGGER.warn("Exception while parsing start row", e);
		}
		return Integer.parseInt(START_ROW_DEFAULT);
	}

	private static String getResultSizeString(UserContext user) {
		if (user.getRequest().getAttribute(RESULT_SIZE) != null) {
			return user.getRequest().getAttribute(RESULT_SIZE).toString();
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
				return jsonObject.optInt(COUNT, COUNT_DEFAULT);
			}
		}
		catch (JSONException e) {
			LOGGER.warn("Exception while parsing count", e);
		}
		return COUNT_DEFAULT;
	}

	/**
	 * @param sec  the section
	 * @param user the user context
	 * @return the latest sorting in form of a Pair. Value A represents the sorting value, value B
	 * the natural order (true is ascending)
	 */
	public static Pair<String, Boolean> getSingleColumnSorting(Section<?> sec, UserContext user) {
		List<Pair<String, Boolean>> sorting = getSorting(sec, user, true);
		return sorting.isEmpty() ? null : sorting.get(0);
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
				LOGGER.error("Invalid JSON", e);
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
			LOGGER.warn("Exception while parsing sorting", e);
		}
		return new JSONArray();
	}

	private static String getResultSizeTag(Section<?> sec, UserContext user) {
		String resultSize = getResultSizeString(user);
		String tag = "";
		if (resultSize.equals(UNKNOWN_RESULT_SIZE)) {
			tag += "<span class=fillText> rows (overall number unknown)</span>";
		}
		else {
			int resultSizeInt = Integer.parseInt(resultSize);
			tag += "<input class='resultSize' style='display:none' value='" + resultSize + "'/>";
			tag += "<span class=fillText>" + (getCount(sec, user) == Integer.MAX_VALUE ? "" : " of");
			tag += " " + resultSize + " " + Strings.pluralOf(resultSizeInt, "row", false) + "</span>";
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
		context.getRequest().setAttribute(RESULT_SIZE, Integer.toString(maxResult));
	}
}
