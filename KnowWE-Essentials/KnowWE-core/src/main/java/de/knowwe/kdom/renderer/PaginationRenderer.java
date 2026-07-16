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
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.elements.A;
import de.knowwe.core.kdom.rendering.elements.Button;
import de.knowwe.core.kdom.rendering.elements.Div;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.HtmlNode;
import de.knowwe.core.kdom.rendering.elements.HtmlProvider;
import de.knowwe.core.kdom.rendering.elements.Span;
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
	protected final boolean supportFiltering;

	protected static final String UNKNOWN_RESULT_SIZE = "unknown";
	private static final String START_ROW = "startRow";
	private static final String RESULT_SIZE = "resultsize";
	private static final String OPEN_RESULT = "openPaginationResult";
	private static final String DISPLAYED_COUNT = "paginationDisplayedCount";
	private static final String HAS_MORE = "paginationHasMore";
	private static final String COUNT = "count";
	private static final String START_ROW_DEFAULT = "1";
	private static final String SORTING = "sorting";
	private static final int COUNT_DEFAULT = 20;
	private static final int[] COUNT_OPTIONS = { 10, 20, 50, 100, 200, 500, 1000, Integer.MAX_VALUE };
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
	public static Set<String> getHiddenColumns(Section<?> section, UserContext user) {
		Set<String> hiddenColumns = new HashSet<>();
		JSONObject paginationSettings = getPaginationSettings(user);
		if (paginationSettings == null) return hiddenColumns;
		JSONObject filter = paginationSettings.optJSONObject(FILTER);
		if (filter == null) return hiddenColumns;
		if (!filter.optBoolean(ACTIVE, false)) return hiddenColumns;
		JSONObject columns = filter.optJSONObject(COLUMNS);
		if (columns == null) return hiddenColumns;
		for (String columnKey : columns.keySet()) {
			JSONObject columnObject = columns.optJSONObject(columnKey);
			if (columnObject == null) continue;
			if (columnObject.optBoolean("hidden", false)) {
				hiddenColumns.add(columnKey);
			}
		}
		return hiddenColumns;
	}

	@NotNull
	public static Map<String, Set<Pattern>> getFilter(Section<?> section, UserContext user) {
		HashMap<String, Set<Pattern>> filterMap = new HashMap<>();

		JSONObject paginationSettings = getPaginationSettings(user);
		if (paginationSettings == null) return filterMap;
		JSONObject filter = paginationSettings.optJSONObject(FILTER);
		if (filter == null) return filterMap;
		if (!filter.optBoolean(ACTIVE, false)) return filterMap;
		JSONObject columns = filter.optJSONObject(COLUMNS);
		if (columns == null) return filterMap;

		for (String columnKey : columns.keySet()) {
			JSONObject columnObject = columns.optJSONObject(columnKey);
			JSONArray selectedTexts = columnObject.optJSONArray(SELECTED_TEXTS);
			Set<Pattern> patterns = filterMap.computeIfAbsent(columnKey, k -> new HashSet<>());
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
					if (!selectedTexts.isEmpty()) {
						StringBuilder regex = new StringBuilder("(?s)^(?!(?:");
						regex.append(cleanedTexts.stream().map(Pattern::quote).collect(Collectors.joining("|")));
						regex.append(")\\z).").append(containsEmptyString ? "+" : "*");
						patterns
								.add(Pattern.compile(regex.toString()));
					}
				}
				// with !selectAll, only the texts given in the array are considered selected
				else {
					if (!selectedTexts.isEmpty()) {
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
		RenderResult table = new RenderResult(user);
		renderFunction.accept(table);

		Div wrapper = new Div();
		wrapper.clazz("knowwe-paginationWrapper")
				.id(section.getID())
				.attributes("sorting-mode", sorting.name(), "filtering", Boolean.toString(supportFiltering))
				.children(
						page -> renderPaginationInternal(section, user, page),
						page -> page.append(table),
						page -> renderPaginationInternal(section, user, page)
				);
		result.append(wrapper);
	}

	public static void renderPagination(Section<?> section, UserContext user, RenderResult result) {
		renderPagination(section, user, result, SortingMode.off, false);
	}

	public static void renderPagination(Section<?> section, UserContext user, RenderResult result, SortingMode sortingMode, boolean supportFiltering) {
		PaginationRenderer paginationRenderer = new PaginationRenderer(null, sortingMode, supportFiltering);
		paginationRenderer.renderPaginationInternal(section, user, result);
	}

	/**
	 * Renders controls for an open-ended result whose total size is unknown. Before calling this
	 * method, the caller must provide the current page information using
	 * {@link #setOpenResult(UserContext, String, int, boolean)}.
	 *
	 * @param id            stable section ID used for client-side state and rerendering
	 * @param user          current user context
	 * @param result        render result receiving the controls
	 * @param defaultCount  page size used when no client-side setting exists
	 * @param countOptions  selectable page sizes, using {@link Integer#MAX_VALUE} for all rows
	 */
	public static void renderOpenPagination(String id, UserContext user, RenderResult result, int defaultCount, int... countOptions) {
		PaginationRenderer paginationRenderer = new PaginationRenderer(null);
		paginationRenderer.renderPaginationInternal(id, user, result, defaultCount, countOptions);
	}

	protected void renderPaginationInternal(Section<?> section, UserContext user, RenderResult result) {
		renderTableSizeSelector(section, user, result);
		renderNavigation(section.getID(), user, result, COUNT_DEFAULT);
		if (supportFiltering) renderFilter(section, user, result);
	}

	private void renderPaginationInternal(String id, UserContext user, RenderResult result, int defaultCount, int[] countOptions) {
		renderTableSizeSelector(id, user, result, defaultCount, countOptions);
		renderNavigation(id, user, result, defaultCount);
	}

	protected void renderFilter(Section<?> section, UserContext user, RenderResult result) {
		// generate unique id in case the filter is added multiple times
		String id = "filter-activator-" + section.getID();
		String activators = user.getRenderResultKeyValueStore().getAttribute(id);
		if (activators == null) activators = "0";
		String uniqueId = id + "-" + activators;
		user.getRenderResultKeyValueStore().setAttribute(id, activators + 1);

		HtmlElement input = new HtmlElement("input")
				.clazz("filter-activator filter-style")
				.id(uniqueId)
				.attributes("type", "checkbox", "name", uniqueId);
		HtmlElement label = new HtmlElement("label")
				.clazz("fillText")
				.attributes("for", uniqueId)
				.content("Filter");
		RenderResult filterTools = new RenderResult(result);
		renderFilterTools(section, user, filterTools);

		result.append(toolBar(section.getID(), input, label,
				new Div().clazz("filter-tools").children(page -> page.append(filterTools))));
	}

	protected void renderFilterTools(Section<?> section, UserContext user, RenderResult result) {
		result.append(new Button("Columns").clazz("pagination-column-filter"));
		result.append(new Button("Clear Filter").clazz("clear-filter"));
	}

	public static void renderToolSeparator(RenderResult navigation) {
		navigation.append(new Div().clazz("knowwe-paginationToolSeparator"));
	}

	public static String getToolSeparator() {
		return new Div().clazz("knowwe-paginationToolSeparator").toString();
	}

	/**
	 * Renders the result size selector.
	 */
	protected void renderTableSizeSelector(Section<?> sec, UserContext user, RenderResult result) {
		renderTableSizeSelector(sec.getID(), user, result, COUNT_DEFAULT, COUNT_OPTIONS,
				new HtmlNode(getResultSizeTag(sec, user)));
	}

	private void renderTableSizeSelector(String id, UserContext user, RenderResult result, int defaultCount, int[] countOptions) {
		renderTableSizeSelector(id, user, result, defaultCount, countOptions,
				getResultSizeProvider(id, user, defaultCount));
	}

	private void renderTableSizeSelector(String id, UserContext user, RenderResult result, int defaultCount,
			int[] countOptions, HtmlProvider resultSize) {
		int count = getCount(user, defaultCount);
		List<HtmlProvider> options = new ArrayList<>();
		boolean foundSelected = false;
		for (int size : countOptions) {
			boolean selected = count == size;
			if (selected) foundSelected = true;
			boolean setSelected = selected || size == Integer.MAX_VALUE && !foundSelected;
			HtmlElement option = new HtmlElement("option")
					.attributes("value", String.valueOf(size))
					.content(size == Integer.MAX_VALUE ? "All" : String.valueOf(size));
			if (setSelected) option.attributes("selected", "selected");
			options.add(option);
		}

		HtmlElement select = new HtmlElement("select")
				.clazz("count")
				.children(options.toArray(HtmlProvider[]::new));
		result.append(toolBar(id,
				new Span("Show ").clazz("fillText"),
				select,
				resultSize,
				new Div().clazz("toolSeparator")));
	}

	/**
	 * Renders the navigation icons.
	 */
	protected void renderNavigation(Section<?> sec, UserContext user, RenderResult result) {
		renderNavigation(sec.getID(), user, result, COUNT_DEFAULT);
	}

	private void renderNavigation(String id, UserContext user, RenderResult result, int defaultCount) {
		int count = getCount(user, defaultCount);
		int startRow = getStartRow(user);
		if (isOpenResult(user, id)) {
			renderOpenNavigation(id, user, result, count, startRow);
			return;
		}
		String resultSizeString = getResultSizeString(user);
		int resultSize = getResultSize(user);
		int resultSizeStringLength = Math.min(Math.max(2, ((int) Math.log10(resultSize)) + 1), 6);
		int endRow = Math.min(resultSize, startRow + count - 1);

		int fill = ((int) (Math.log10(resultSize)) - ((int) Math.log10(endRow)));
		StringBuilder fillString = new StringBuilder();
		fillString.append("&nbsp;".repeat(Math.max(0, fill)));

		List<HtmlProvider> navigation = new ArrayList<>();
		if (count != Integer.MAX_VALUE) {
			navigation.add(toolbarButton(Icon.FIRST, navigationAction(id, "begin"), startRow > 1));
			navigation.add(toolbarButton(Icon.PREVIOUS, navigationAction(id, "back"), startRow > 1));
			navigation.add(new Span(" Rows ").clazz("fillText"));
			navigation.add(new HtmlElement("input").clazz("startRow").attributes(
					"size", String.valueOf(resultSizeStringLength),
					"type", "field",
					"value", String.valueOf(startRow)));
			navigation.add(new Span().clazz("fillText").children(
					new HtmlNode(" to " + fillString + endRow + "&nbsp;")));

			boolean forward = resultSize >= startRow + count;
			navigation.add(toolbarButton(Icon.NEXT, navigationAction(id, "forward"), forward));
			navigation.add(toolbarButton(Icon.LAST, navigationAction(id, "end"), forward));
		}
		else {
			navigation.add(toolbarButton(Icon.FIRST, navigationAction(id, "begin"), false));
			navigation.add(toolbarButton(Icon.PREVIOUS, navigationAction(id, "back"), false));
			navigation.add(new Span(" Lines ").clazz("fillText"));
			navigation.add(new HtmlElement("input").clazz("startRow").attributes(
					"size", String.valueOf(resultSizeStringLength),
					"type", "field",
					"value", "1"));
			String range = resultSizeString.equals(UNKNOWN_RESULT_SIZE)
					? " til end&nbsp;"
					: " to " + fillString + endRow + "&nbsp;";
			navigation.add(new Span().clazz("fillText").children(new HtmlNode(range)));
			navigation.add(toolbarButton(Icon.NEXT, navigationAction(id, "forward"), false));
			navigation.add(toolbarButton(Icon.LAST, navigationAction(id, "end"), false));
		}
		result.append(toolBar(id, navigation.toArray(HtmlProvider[]::new)));
	}

	private void renderOpenNavigation(String id, UserContext user, RenderResult result, int count, int startRow) {
		int displayedCount = getDisplayedCount(user, id);
		boolean hasMore = hasMore(user, id);
		int page = count == Integer.MAX_VALUE ? 1 : ((startRow - 1) / count) + 1;
		int endRow = startRow + Math.max(0, displayedCount - 1);

		List<HtmlProvider> navigation = new ArrayList<>();
		navigation.add(toolbarButton(Icon.FIRST, navigationAction(id, "begin"), startRow > 1));
		navigation.add(toolbarButton(Icon.PREVIOUS, navigationAction(id, "back"), startRow > 1));
		navigation.add(new HtmlElement("input").clazz("startRow").attributes(
				"type", "hidden", "value", String.valueOf(startRow)));
		String range = displayedCount > 0 ? " &middot; Rows " + startRow + " to " + endRow : "";
		navigation.add(new Span().clazz("fillText").children(
				new HtmlNode(" Page " + page + range + "&nbsp;")));
		navigation.add(toolbarButton(Icon.NEXT, navigationAction(id, "forward"), hasMore));
		result.append(toolBar(id, navigation.toArray(HtmlProvider[]::new)));
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

	protected void renderToolBarElement(Section<?> sec, RenderResult result, Runnable contentRenderer) {
		renderToolBarElement(sec.getID(), result, contentRenderer);
	}

	private void renderToolBarElement(String id, RenderResult result, Runnable contentRenderer) {
		result.appendHtml("<div class='knowwe-paginationToolbar noselect' pagination=")
				.append(Strings.quoteSingle(id)).appendHtml(">");
		contentRenderer.run();
		result.appendHtml("</div>");
	}

	private static HtmlElement toolBar(String id, HtmlProvider... children) {
		return new Div()
				.clazz("knowwe-paginationToolbar noselect")
				.attributes("pagination", id)
				.children(children);
	}

	private static String navigationAction(String id, String direction) {
		return "KNOWWE.core.plugin.pagination.navigate('" + id + "', '" + direction + "')";
	}

	private static HtmlProvider toolbarButton(Icon icon, String action, boolean enabled) {
		HtmlNode iconHtml = new HtmlNode(icon.increaseSize(Icon.Percent.by33)
				.addClasses("knowwe-paginationNavigationIcons")
				.toHtml());
		if (!enabled) return iconHtml;
		return new A().attributes("onclick", action + ";").children(iconHtml);
	}

	private static JSONObject getPaginationSettings(UserContext user) {
		return AbstractAction.getLocalSectionStorage(user).optJSONObject(PAGINATION_KEY);
	}

	public static int getStartRow(Section<?> sec, UserContext user) {
		return getStartRow(user);
	}

	/**
	 * Returns the one-based index of the first row selected for the current section.
	 *
	 * @param user current user context
	 * @return one-based start row
	 */
	public static int getStartRow(UserContext user) {
		try {
			JSONObject jsonObject = getPaginationSettings(user);
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
		if (user.getRenderResultKeyValueStore().getAttribute(RESULT_SIZE) != null) {
			return user.getRenderResultKeyValueStore().getAttribute(RESULT_SIZE);
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
		return getCount(user, COUNT_DEFAULT);
	}

	/**
	 * Returns the selected page size, using the supplied default if no client-side setting exists.
	 *
	 * @param user         current user context
	 * @param defaultCount page size to use without a stored setting
	 * @return selected page size
	 */
	public static int getCount(UserContext user, int defaultCount) {
		try {
			JSONObject jsonObject = getPaginationSettings(user);
			if (jsonObject != null && jsonObject.has(COUNT)) {
				return jsonObject.optInt(COUNT, defaultCount);
			}
		}
		catch (JSONException e) {
			LOGGER.warn("Exception while parsing count", e);
		}
		return defaultCount;
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
		if (sorting.isEmpty()) {
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
			JSONObject jsonObject = getPaginationSettings(user);
			if (jsonObject != null && jsonObject.has(SORTING)) {
				return jsonObject.getJSONArray(SORTING);
			}
		}
		catch (JSONException e) {
			LOGGER.warn("Exception while parsing sorting", e);
		}
		return new JSONArray();
	}

	protected String getResultSizeTag(Section<?> sec, UserContext user) {
		return getResultSizeElements(sec.getID(), user, COUNT_DEFAULT).stream()
				.map(Object::toString)
				.collect(Collectors.joining());
	}

	private HtmlProvider getResultSizeProvider(String id, UserContext user, int defaultCount) {
		List<HtmlProvider> elements = getResultSizeElements(id, user, defaultCount);
		return result -> elements.forEach(result::append);
	}

	private List<HtmlProvider> getResultSizeElements(String id, UserContext user, int defaultCount) {
		String resultSize = getResultSizeString(user);
		if (resultSize.equals(UNKNOWN_RESULT_SIZE)) {
			String text = isOpenResult(user, id) ? " rows" : " rows (overall number unknown)";
			return List.of(new Span(text).clazz("fillText"));
		}

		int resultSizeInt = Integer.parseInt(resultSize);
		HtmlElement input = new HtmlElement("input")
				.clazz("resultSize")
				.style("display:none")
				.attributes("value", resultSize);
		String prefix = getCount(user, defaultCount) == Integer.MAX_VALUE ? " " : " of ";
		Span label = new Span(prefix + resultSize + " " + Strings.pluralOf(resultSizeInt, "row", false));
		label.clazz("fillText");
		return List.of(input, label);
	}

	/**
	 * If the decorated Renderer knows about the size of its results it can set it here for an
	 * increase of information at the rendering in the navigation bar.
	 *
	 * @created 27.01.2014
	 */
	public static void setResultSize(UserContext context, int maxResult) {
		context.getRenderResultKeyValueStore().setAttribute(RESULT_SIZE, Integer.toString(maxResult));
	}

	/**
	 * Supplies information about a page whose overall result size has deliberately not been
	 * calculated. The information is used to render correct forward navigation without an exact
	 * total.
	 *
	 * @param context        current user context
	 * @param id             stable section ID used by the pagination controls
	 * @param displayedCount number of rows displayed on the current page
	 * @param hasMore        whether at least one further matching row exists
	 */
	public static void setOpenResult(UserContext context, String id, int displayedCount, boolean hasMore) {
		if (displayedCount < 0) throw new IllegalArgumentException("Displayed count must not be negative");
		context.getRenderResultKeyValueStore().setAttribute(RESULT_SIZE, UNKNOWN_RESULT_SIZE);
		context.getRenderResultKeyValueStore().setAttribute(key(OPEN_RESULT, id), Boolean.TRUE.toString());
		context.getRenderResultKeyValueStore().setAttribute(key(DISPLAYED_COUNT, id), Integer.toString(displayedCount));
		context.getRenderResultKeyValueStore().setAttribute(key(HAS_MORE, id), Boolean.toString(hasMore));
	}

	private static String key(String key, String id) {
		return key + "." + id;
	}

	private static boolean isOpenResult(UserContext user, String id) {
		return Boolean.parseBoolean(user.getRenderResultKeyValueStore().getAttribute(key(OPEN_RESULT, id)));
	}

	private static int getDisplayedCount(UserContext user, String id) {
		String value = user.getRenderResultKeyValueStore().getAttribute(key(DISPLAYED_COUNT, id));
		if (value == null) return 0;
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException ignored) {
			return 0;
		}
	}

	private static boolean hasMore(UserContext user, String id) {
		return Boolean.parseBoolean(user.getRenderResultKeyValueStore().getAttribute(key(HAS_MORE, id)));
	}
}
