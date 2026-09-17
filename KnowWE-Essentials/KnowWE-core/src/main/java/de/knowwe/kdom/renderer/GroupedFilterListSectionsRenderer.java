/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import com.denkbares.utils.Predicates;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.elements.Div;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.HtmlNode;
import de.knowwe.core.kdom.rendering.elements.HtmlProvider;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.util.Icon;

/**
 * Utility class to get a list of {@link ListSectionsRenderer}s and prints them as individual groups, each with an
 * optional header.
 * <br>
 * Furthermore, this class allows the filtering of the contained sections by text and count. Free text filter input is
 * matched against the sections' context whereas {@code name:} and {@code prompt:} keywords can be used for more
 * fine-grained filtering.
 *
 * @author Jonas Müller
 * @created 2019-08-12
 */
public class GroupedFilterListSectionsRenderer<T extends Type> {

	private static final int DEFAULT_COUNT = 50;
	private static final int LOWER_PAGINATION_THRESHOLD = 20;
	private static final int[] PAGINATION_COUNT_OPTIONS = { 10, 25, 50, 100, 200, 500, 1000, Integer.MAX_VALUE };
	private static final Map<String, Integer> COUNT_OPTIONS = new TreeMap<>(NumberAwareComparator.CASE_SENSITIVE);
	public static final Pattern SEARCH_PATTERN = Pattern.compile("([^\u00A0\\h\\s\\v]+?)([:<>=]+)[\u00A0\\h\\s\\v]*([^\u00A0\\h\\s\\v]+)");
	public static final Pattern QUOTES_PATTERN = Pattern.compile("\".+\"");

	static {
		COUNT_OPTIONS.put("Show All", -1);
		COUNT_OPTIONS.put("1000", 1000);
		COUNT_OPTIONS.put("500", 500);
		COUNT_OPTIONS.put("200", 200);
		COUNT_OPTIONS.put("100", 100);
		COUNT_OPTIONS.put("50", 50);
		COUNT_OPTIONS.put("25", 25);
		COUNT_OPTIONS.put("10", 10);
	}

	private final List<Pair<String, ListSectionsRenderer<T>>> renderers;
	private final String id;
	// the markup section this list is rendered for, if known; required for per-column filters
	@Nullable
	private final Section<?> self;
	private final UserContext context;
	private String placeholder;

	// contains filter providers that are only applied to a specific key (e.g. filter by name)
	// The key has to be provided in the search field (e.g. name=<name>)
	private final Map<String, Function<Section<T>, String>> keyFilterProviders;

	// contains filter providers that are only applied to a specific key (e.g. filter by name)
	// The key has to be provided in the search field (e.g. name=<name>)
	private final Map<String, Pair<Function<Section<T>, Double>, Function<String, Double>>> numKeyFilterProviders = new HashMap<>();

	// contains filters that are always applied (chained with OR). No key needed.
	private final Set<Function<Section<T>, String>> keylessFilterProviders;

	private Predicate<Section<T>> noFilterPredicate = Predicates.TRUE();
	private Predicate<Section<T>> alwaysShowPredicate = Predicates.FALSE();
	private int noFilterLimit = -1;

	private String emptyText = "-- no entries --";
	private String searchHint = null;

	/**
	 * Initializes an instance given only a single {@link ListSectionsRenderer} without a header. This can be used to
	 * make a single instance of {@link ListSectionsRenderer} filterable.
	 *
	 * @param id          A unique and stable ID to store the user settings for, e.g. the section id this renderer is
	 *                    supposed to render into
	 * @param placeholder some placeholder text for the filter text field
	 * @param renderer    The single instance of {@link ListSectionsRenderer}
	 */
	public GroupedFilterListSectionsRenderer(String id, String placeholder, ListSectionsRenderer<T> renderer) {
		this(null, id, placeholder, renderer.getContext(), Collections.singletonList(new Pair<>(null, renderer)));
		this.emptyText = renderer.getEmptyText();
	}

	/**
	 * Initializes an instance given only a single {@link ListSectionsRenderer} without a header. This can be used to
	 * make a single instance of {@link ListSectionsRenderer} filterable.
	 *
	 * @param self     The section this renderer is supposed to render into
	 * @param renderer The single instance of {@link ListSectionsRenderer}
	 */
	public GroupedFilterListSectionsRenderer(Section<?> self, ListSectionsRenderer<T> renderer) {
		this(self, renderer.getContext(), Collections.singletonList(new Pair<>(null, renderer)));
		this.emptyText = renderer.getEmptyText();
	}

	/**
	 * Initializes an instance given several {@link ListSectionsRenderer}s each paired with a header. The header should
	 * be an HTML String representation.
	 *
	 * @param self      The section this renderer is supposed to render into
	 * @param context   The user context
	 * @param renderers The groups of {@link ListSectionsRenderer} each paired with an HTML String
	 *                  representation
	 *                  of the associated header
	 */
	public GroupedFilterListSectionsRenderer(Section<?> self, UserContext context, List<Pair<String, ListSectionsRenderer<T>>> renderers) {
		this(self, self.getID(), "Filter " + self.getArticle().getTitle(), context, renderers);
	}

	private GroupedFilterListSectionsRenderer(@Nullable Section<?> self, String id, String placeholder, UserContext context, List<Pair<String, ListSectionsRenderer<T>>> renderers) {
		this.self = self;
		this.id = id;
		this.renderers = renderers;
		this.context = context;
		this.placeholder = placeholder;
		this.keyFilterProviders = new LinkedHashMap<>();
		this.keylessFilterProviders = new HashSet<>();
	}

	/**
	 * Returns whether the columns of this list can be filtered individually. This requires the markup to be able to
	 * rebuild this very list in a subsequent request, so that {@link ListSectionsFilterProviderAction} can determine
	 * the values available for each column.
	 *
	 * @return whether per-column filters are supported
	 */
	private boolean isColumnFilteringSupported() {
		return self != null && self.get() instanceof ListSectionsProvider;
	}

	public GroupedFilterListSectionsRenderer<T> filter(Function<Section<T>, String> filter) {
		keylessFilterProviders.add(filter);
		return this;
	}

	/**
	 * Adds a new filter provider to the supported filters. The filter provider is a function that maps each line to a
	 * searchable string that is used to filter the lines for.
	 *
	 * @param name   the type of information to filter for, e.g. "prompt", "name", "description", etc.
	 * @param filter the function that maps each line to searchable content
	 * @return this instance to chain method calls
	 * @throws IllegalArgumentException if the name is assigned to a filter
	 */
	public GroupedFilterListSectionsRenderer<T> filter(String name, Function<Section<T>, String> filter) {
		this.keyFilterProviders.put(name, filter);
		return this;
	}

	/**
	 * Adds a new filter provider to the supported filters. The filter provider is a function that maps each line to a
	 * searchable string that is used to filter the lines for.
	 *
	 * @param name   the type of information to filter for, e.g. "prompt", "name", "description", etc.
	 * @param filter the function that maps each line to searchable content
	 * @return this instance to chain method calls
	 * @throws IllegalArgumentException if the name is assigned to a filter
	 */
	public GroupedFilterListSectionsRenderer<T> filter(String name, Function<Section<T>, Double> filter, Function<String, Double> phraseToDoubleParser) {
		this.numKeyFilterProviders.put(name, new Pair<>(filter, phraseToDoubleParser));
		return this;
	}

	/**
	 * Specifies the sections of the original list that should be included in the fully expanded ist, if no filter is
	 * specified by the user. This is helpful if the original list should only display a subset of the potentially
	 * searched sections, but the search should potentially find additional sections. This method includes the first n
	 * elements of the original list.
	 *
	 * @param limit the count of the first n sections that are included.
	 * @return this instance to chain method calls
	 */
	public GroupedFilterListSectionsRenderer<T> noFilter(int limit) {
		noFilterLimit = limit;
		return this;
	}

	/**
	 * Specifies the sections of the original list that should be included in the fully expanded ist, if no filter is
	 * specified by the user. This is helpful if the original list should only display a subset of the potentially
	 * searched sections, but the search should potentially find additional sections. This method includes the elements
	 * of the original list that matches the filter.
	 *
	 * @param filter the filter predicate to be applied to the original list, if no user-filter is applied
	 * @return this instance to chain method calls
	 */
	public GroupedFilterListSectionsRenderer<T> noFilter(Predicate<Section<T>> filter) {
		noFilterPredicate = filter;
		return this;
	}

	/**
	 * Specifies the sections of the original list that should be always included in the filtered list, even if they
	 * would be skipped according to a user-specified filter. If this method is called multiple times, all of these
	 * always-items are shown.
	 * <p>
	 * Note: if the specified items are not included in the original list, the also will not been displayed!
	 *
	 * @param always the items to be never filtered out
	 * @return this instance to chain method calls
	 */
	@SuppressWarnings("unchecked")
	public GroupedFilterListSectionsRenderer<T> always(Section<T>... always) {
		return always(Arrays.asList(always));
	}

	/**
	 * Specifies the sections of the original list that should be always included in the filtered list, even if they
	 * would be skipped according to a user-specified filter. If this method is called multiple times, all of these
	 * always-items are shown.
	 * <p>
	 * Note: if the specified items are not included in the original list, the also will not been displayed!
	 *
	 * @param always the items to be never filtered out
	 * @return this instance to chain method calls
	 */
	public GroupedFilterListSectionsRenderer<T> always(Collection<Section<T>> always) {
		return always(new HashSet<>(always)::contains);
	}

	/**
	 * Specifies the sections of the original list that should be always included in the filtered list, even if they
	 * would be skipped according to a user-specified filter. If this method is called multiple times, all of these
	 * always-items are shown.
	 *
	 * @param filter the predicate to be applied to the filtered-out item, to include then anyway if true
	 * @return this instance to chain method calls
	 */
	public GroupedFilterListSectionsRenderer<T> always(Predicate<Section<T>> filter) {
		alwaysShowPredicate = alwaysShowPredicate.or(filter);
		return this;
	}

	/**
	 * Sets the placeholder text to be displayed if there are no entries in the list. You may specify null to display
	 * nothing if the rendered section list is empty.
	 *
	 * @param emptyText placeholder text to be displayed if the list is empty
	 * @return this instance to chain builder calls
	 */
	public GroupedFilterListSectionsRenderer<T> empty(String emptyText) {
		this.emptyText = emptyText;
		return this;
	}

	/**
	 * Renders the grouped List Sections on the given page by consecutively rendering each header and calling each
	 * {@link ListSectionsRenderer}'s render function
	 *
	 * @param page The page to render to
	 * @see ListSectionsRenderer#render(RenderResult)
	 */
	public void render(RenderResult page) {
		boolean renderControls = !context.isRenderingPreview();
		boolean columnFiltering = renderControls && isColumnFilteringSupported();
		String searchPhrase = getFilterFromCookie();
		SearchPredicate searchPredicate = new SearchPredicate(searchPhrase);
		ColumnFilterPredicate columnPredicate = columnFiltering
				? new ColumnFilterPredicate(PaginationRenderer.getFilter(context))
				: null;
		boolean textFiltered = Strings.nonBlank(searchPhrase);
		boolean columnFiltered = columnPredicate != null && !columnPredicate.isEmpty();

		// the restrictions of the unfiltered list only apply as long as the user does not filter at all
		Predicate<Section<T>> filter;
		if (textFiltered) {
			filter = searchPredicate.or(alwaysShowPredicate);
		}
		else if (columnFiltered) {
			filter = Predicates.TRUE();
		}
		else {
			filter = noFilterPredicate;
		}
		if (columnFiltered) filter = filter.and(columnPredicate);

		int defaultCount = getDefaultCount();
		int count = PaginationRenderer.getCount(context, defaultCount);
		int startRow = count == Integer.MAX_VALUE ? 1 : PaginationRenderer.getStartRow(context);
		int maximumMatches = textFiltered || columnFiltered ? -1 : noFilterLimit;
		OpenPaginationPredicate<Section<T>> pagination = new OpenPaginationPredicate<>(filter, startRow - 1,
				count, maximumMatches);

		// render the groups
		List<HtmlProvider> listChildren = new ArrayList<>();
		for (Pair<String, ListSectionsRenderer<T>> rendererPair : renderers) {
			searchPredicate.setRenderer(rendererPair.getB());
			if (columnPredicate != null) columnPredicate.setRenderer(rendererPair.getB());
			if (columnFiltering) {
				rendererPair.getB()
						.filterProviderAction(ListSectionsFilterProviderAction.class.getSimpleName());
			}
			ListSectionsRenderer<T> filtered = rendererPair.getB().filter(pagination);
			if (filtered.isEmpty()) continue;
			String header = rendererPair.getA();
			if (Strings.isNotBlank(header)) {
				listChildren.add(new HtmlNode(header));
			}
			RenderResult renderedGroup = new RenderResult(page);
			filtered.render(renderedGroup);
			listChildren.add(result -> result.append(renderedGroup));
		}

		// render empty text if there are no items to be displayed
		if (listChildren.isEmpty()) {
			listChildren.add(new Div().clazz("empty-list-sections").content(emptyText));
		}

		HtmlProvider content = new Div()
				.clazz("list-section-wrapper")
				.attributes("sectionId", id)
				.children(listChildren.toArray(HtmlProvider[]::new));
		if (renderControls) {
			PaginationRenderer.setOpenResult(context, id, pagination.getDisplayedCount(), pagination.hasMore());
			if (!pagination.hasMore()) {
				PaginationRenderer.setResultSize(context, pagination.getMatchCount());
			}
			boolean showBottomPagination = startRow > 1 || pagination.hasMore()
					|| pagination.getDisplayedCount() > LOWER_PAGINATION_THRESHOLD;
			List<HtmlProvider> paginationChildren = new ArrayList<>();
			paginationChildren.add(result -> PaginationRenderer.renderOpenPagination(
					id, context, result, defaultCount, PAGINATION_COUNT_OPTIONS));
			if (columnFiltering) {
				paginationChildren.add(result -> PaginationRenderer.renderOpenFilter(id, context, result));
			}
			paginationChildren.add(content);
			if (showBottomPagination) {
				paginationChildren.add(result -> PaginationRenderer.renderOpenPagination(
						id, context, result, defaultCount, PAGINATION_COUNT_OPTIONS));
			}
			content = new Div()
					.clazz("knowwe-paginationWrapper list-sections-pagination")
					.id(id)
					.attributes(
							"sorting-mode", PaginationRenderer.SortingMode.off.name(),
							"filtering", Boolean.toString(columnFiltering),
							"reset-start-row-on-count-change", "true")
					.children(paginationChildren.toArray(HtmlProvider[]::new));
		}

		if (ReRenderSectionMarkerRenderer.requiresWrapper(context)) {
			content = ReRenderSectionMarkerRenderer.createMarker(id, content);
		}

		if (!context.isReRendering() && renderControls) {
			if (searchHint != null) {
				page.append(new Div()
						.clazz("grouped-list-search-hint")
						.children(new HtmlNode(searchHint)));
			}
			content = new Div()
					.clazz("grouped-list-section-wrapper")
					.children(createFilterFields(), content);
		}

		page.append(content);
	}

	private HtmlElement createFilterFields() {
		HtmlElement input = new HtmlElement("input")
				.clazz("form-control filter-list-section-input")
				.attributes("type", "text", "placeholder", placeholder);
		String filter = getFilterFromCookie();
		if (Strings.isNotBlank(filter)) input.attributes("value", filter);

		return new Div()
				.clazz("form-inline form-group cage filter-input")
				.children(
						input,
						new HtmlNode(Icon.DELETE.addClasses("clear-filter").toHtml()),
						new HtmlNode(Icon.INFO.addTitle(getInfoTitle()).toHtml()));
	}

	private String getInfoTitle() {
		StringBuilder infoTitleBuilder = new StringBuilder();
		infoTitleBuilder.append("Filter by typing free text or using the following keywords:");
		keyFilterProviders.keySet().forEach(k -> infoTitleBuilder
				.append("<br><strong>").append(k).append("</strong>")
				.append(": <span style='font-style:italic'>your text</span>"));
		return infoTitleBuilder.toString();
	}

	public static boolean isSearching(UserContext context, String id) {
		return Strings.nonBlank(KnowWEUtils.getCookie("list-section.identifier." + id, context));
	}

	private int getCountFromCookie() {
		String countFromCookie = getCookieContent("list-section.count." + id);
		if (Strings.isBlank(countFromCookie) || !COUNT_OPTIONS.containsKey(countFromCookie)) return DEFAULT_COUNT;
		return COUNT_OPTIONS.get(countFromCookie);
	}

	private int getDefaultCount() {
		int legacyCount = getCountFromCookie();
		return legacyCount == -1 ? Integer.MAX_VALUE : legacyCount;
	}

	private String getFilterFromCookie() {
		return getCookieContent("list-section.identifier." + id);
	}

	private String getCookieContent(String key) {
		String cookie = KnowWEUtils.getCookie(key, context);
		if (Strings.isBlank(cookie)) return null;
		return Strings.decodeURL(cookie).trim();
	}

	public GroupedFilterListSectionsRenderer<T> placeHolder(String placeHolder) {
		this.placeholder = placeHolder;
		return this;
	}

	public GroupedFilterListSectionsRenderer<T> searchHint(String searchHintHtml) {
		this.searchHint = searchHintHtml;
		return this;
	}

	/**
	 * Collects the values that are available for filtering the specified column, to be offered in the column's filter
	 * popup. The values are narrowed down by the current free text filter and by the filters of all other columns, so
	 * that the offered values match what filtering for them would actually display. The filter of the specified column
	 * itself is deliberately ignored, so that the user is able to widen an existing selection again.
	 * <p>
	 * Cells without content are returned as an empty string.
	 *
	 * @param columnName      the name of the column to collect the values for, see
	 *                        {@link ListSectionsRenderer#getFilterableColumns()}
	 * @param filterTextQuery an optional text the values have to contain, or null to accept all values
	 * @param maxCount        the maximum number of values to be collected
	 * @return the distinct values of the column, in the order the rows are displayed in
	 */
	@NotNull
	public List<String> collectFilterValues(String columnName, @Nullable String filterTextQuery, int maxCount) {
		String searchPhrase = getFilterFromCookie();
		SearchPredicate searchPredicate = new SearchPredicate(searchPhrase);
		Map<String, Set<Pattern>> columnFilters = new HashMap<>(PaginationRenderer.getFilter(context));
		// ignore the column's own filter, otherwise the current selection could never be widened again
		columnFilters.remove(columnName);
		ColumnFilterPredicate columnPredicate = new ColumnFilterPredicate(columnFilters);

		Set<String> values = new LinkedHashSet<>();
		for (Pair<String, ListSectionsRenderer<T>> rendererPair : renderers) {
			ListSectionsRenderer<T> renderer = rendererPair.getB();
			Function<Section<T>, String> accessor = renderer.getFilterableColumns().get(columnName);
			if (accessor == null) continue; // the column is not part of this group
			searchPredicate.setRenderer(renderer);
			columnPredicate.setRenderer(renderer);
			for (Section<T> section : renderer.getSections()) {
				if (Strings.nonBlank(searchPhrase) && !searchPredicate.test(section)) continue;
				if (!columnPredicate.test(section)) continue;
				String value = accessor.apply(section);
				if (value == null) value = "";
				// empty cells are always offered, so the user can filter for them
				if (Strings.nonBlank(value) && Strings.nonBlank(filterTextQuery)
					&& !Strings.containsIgnoreCase(value, filterTextQuery)) {
					continue;
				}
				values.add(value);
				if (values.size() >= maxCount) return new ArrayList<>(values);
			}
		}
		return new ArrayList<>(values);
	}

	/**
	 * Applies the filters the user has selected in the columns' filter popups. Because each group of this renderer may
	 * define its own columns, the predicate has to be bound to the group it is applied to, using
	 * {@link #setRenderer(ListSectionsRenderer)}.
	 */
	private final class ColumnFilterPredicate implements Predicate<Section<T>> {

		private final Map<String, Set<Pattern>> columnFilters;
		private Predicate<Section<T>> delegate = Predicates.TRUE();

		private ColumnFilterPredicate(Map<String, Set<Pattern>> columnFilters) {
			this.columnFilters = columnFilters;
		}

		/**
		 * Returns whether nothing is actually filtered out. Columns the user has opened the filter popup for, but has
		 * not restricted, do not have any patterns to filter for.
		 */
		private boolean isEmpty() {
			return columnFilters.values().stream().allMatch(Set::isEmpty);
		}

		private void setRenderer(ListSectionsRenderer<T> renderer) {
			Predicate<Section<T>> predicate = Predicates.TRUE();
			Map<String, Function<Section<T>, String>> accessors = renderer.getFilterableColumns();
			for (Entry<String, Set<Pattern>> entry : columnFilters.entrySet()) {
				Function<Section<T>, String> accessor = accessors.get(entry.getKey());
				if (accessor == null) continue; // the column is not part of this group
				Set<Pattern> patterns = entry.getValue();
				if (patterns.isEmpty()) continue;
				predicate = predicate.and(section -> matches(accessor, patterns, section));
			}
			this.delegate = predicate;
		}

		private boolean matches(Function<Section<T>, String> accessor, Set<Pattern> patterns, Section<T> section) {
			String text = accessor.apply(section);
			if (text == null) text = "";
			for (Pattern pattern : patterns) {
				if (pattern.matcher(text).matches()) return true;
			}
			return false;
		}

		@Override
		public boolean test(Section<T> section) {
			return delegate.test(section);
		}
	}

	private final class SearchPredicate implements Predicate<Section<T>> {

		private final List<Predicate<Section<T>>> filters = new ArrayList<>();
		private final String searchPhrase;
		private ListSectionsRenderer<T> renderer;
		private boolean initialized;

		public SearchPredicate(String searchPhrase) {
			this.searchPhrase = searchPhrase;
		}

		private void init() {
			if (initialized) return;
			// Search phrases with :<>=
			Matcher matcher = SEARCH_PATTERN.matcher(searchPhrase);
			while (matcher.find()) {
				String name = matcher.group(1);
				String operator = matcher.group(2);
				String value = matcher.group(3);
				addKeyFilter(keyFilterProviders, name, operator, value);
				addNumFilter(numKeyFilterProviders, name, operator, value);
				addKeyFilter(renderer.getKeyFilters(), name, operator, value);
			}
			// remove all matched filters and add all remaining phrases
			String rest = Strings.trim(matcher.replaceAll(""));

			// Search phrases written in quotes ""
			if (Strings.nonBlank(rest)) {
				matcher = QUOTES_PATTERN.matcher(rest);
				List<Predicate<Section<T>>> filtersList = new ArrayList<>();
				while (matcher.find()) {
					String phrase = matcher.group();
					Predicate<Section<T>> sectionTextFilter = createFilter(Section::getText, phrase.replaceAll("\"", ""));
					filtersList.add(sectionTextFilter);
					for (Function<Section<T>, String> filter : keylessFilterProviders) {
						filtersList.add(createFilter(filter, phrase));
					}
				}
				filtersList.stream().reduce(Predicates::or).ifPresent(filters::add);

				rest = Strings.trim(matcher.replaceAll(""));
			}

			// other (simple) search phrases
			if (Strings.nonBlank(rest)) {
				Predicate<Section<T>> simpleFilter = createFilter(s -> getSearchableText(s, renderer), clean(rest));
				Predicate<Section<T>> filtersPredicate = Predicates.FALSE(); // an empty predicate
				for (Function<Section<T>, String> filter : keylessFilterProviders) {
					filtersPredicate = filtersPredicate.or(createFilter(filter, rest.split("[\u00A0\\h\\s\\v]+")));
				}
				filters.add(Predicates.or(simpleFilter, filtersPredicate));
			}
			initialized = true;
		}

		@NotNull
		private String getSearchableText(Section<T> s, ListSectionsRenderer<T> renderer) {
			StringBuilder b = new StringBuilder();
			b.append(clean(s.getText()));
			for (Function<Section<T>, String> textFunction : renderer.getKeyFilters().values()) {
				b.append(" ").append(clean(textFunction.apply(s)));
			}
			return b.toString();
		}

		@NotNull
		private static String clean(String rest) {
			return rest == null ? "" : rest.replaceAll("[-/\\\\\u00A0\\h\\s\\v]", "");
		}

		private void addNumFilter(Map<String, Pair<Function<Section<T>, Double>, Function<String, Double>>> numKeyFilterProviders, String name, String operator, String value) {
			numKeyFilterProviders.entrySet().stream()
					.filter(entry -> entry.getKey().startsWith(name.toLowerCase()))
					.map(Entry::getValue)
					.map(funPair -> createNumFilter(funPair.getA(), operator, funPair.getB().apply(value)))
					.reduce(Predicate::or).ifPresent(filters::add);
		}

		private void addKeyFilter(Map<String, Function<Section<T>, String>> keyFilterProviders, String name, String operator, String value) {
			keyFilterProviders.entrySet().stream()
					.filter(entry -> {
						String plainKeyLC = Strings.htmlToPlain(entry.getKey()).toLowerCase();
						return plainKeyLC.startsWith(name.toLowerCase())
							   || plainKeyLC.replaceAll("\\W", "")
									   .startsWith(name.toLowerCase().replaceAll("\\W", ""));
					})
					.map(Entry::getValue)
					.map(textFun -> createFilter(textFun, operator, value))
					.reduce(Predicate::or).ifPresent(filters::add);
		}

		@Override
		public boolean test(Section<T> section) {
			init();
			return filters.stream().allMatch(pred -> pred.test(section));
		}

		private Predicate<Section<T>> createFilter(Function<Section<T>, String> textFunction, String... phrases) {
			return section -> Strings.containsIgnoreCase(textFunction.apply(section), phrases);
		}

		private Predicate<Section<T>> createFilter(Function<Section<T>, String> textFunction, String operator, String phrase) {
			return switch (operator) {
				case "<" -> createFilter(textFunction, x -> x < 0, phrase);
				case "<=" -> createFilter(textFunction, x -> x <= 0, phrase);
				case ">" -> createFilter(textFunction, x -> x > 0, phrase);
				case ">=" -> createFilter(textFunction, x -> x >= 0, phrase);
				default -> createFilter(textFunction, phrase);
			};
		}

		private Predicate<Section<T>> createFilter(Function<Section<T>, String> textFunction, DoublePredicate compare, String phrase) {
			return section -> {
				String text = textFunction.apply(section);
				return Strings.nonBlank(text) &&
					   compare.test(NumberAwareComparator.CASE_INSENSITIVE.compare(text, phrase));
			};
		}

		private Predicate<Section<T>> createNumFilter(Function<Section<T>, Double> numFun, String operator, Double value) {
			return switch (operator) {
				case "<" -> createNumFilter(numFun, x -> x < 0, value);
				case "<=" -> createNumFilter(numFun, x -> x <= 0, value);
				case ">" -> createNumFilter(numFun, x -> x > 0, value);
				case ">=" -> createNumFilter(numFun, x -> x >= 0, value);
				case "=" -> createNumFilter(numFun, x -> x == 0, value);
				default -> createFilter(s -> numFun.apply(s).toString(), value.toString());
			};
		}

		private Predicate<Section<T>> createNumFilter(Function<Section<T>, Double> numFunction, DoublePredicate compare, Double value) {
			return section -> {
				if (Double.isNaN(value)) return false;
				Double sectionValue = numFunction.apply(section);
				if (sectionValue == null || Double.isNaN(sectionValue)) return false;
				int comparatorResult = Double.compare(sectionValue, value);
				return compare.test(comparatorResult);
			};
		}

		public void setRenderer(ListSectionsRenderer<T> renderer) {
			this.renderer = renderer;
			this.initialized = false;
		}
	}
}
