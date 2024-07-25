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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import com.denkbares.utils.Predicates;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
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
		this(id, placeholder, renderer.getContext(), Collections.singletonList(new Pair<>(null, renderer)));
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
		this(self.getID(), "Filter " + self.getArticle().getTitle(), context, renderers);
	}

	private GroupedFilterListSectionsRenderer(String id, String placeholder, UserContext context, List<Pair<String, ListSectionsRenderer<T>>> renderers) {
		this.id = id;
		this.renderers = renderers;
		this.context = context;
		this.placeholder = placeholder;
		this.keyFilterProviders = new LinkedHashMap<>();
		this.keylessFilterProviders = new HashSet<>();
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
		if (!context.isReRendering() && !context.isRenderingPreview()) {
			if (searchHint != null) {
				page.appendHtmlTag("div", "class", "grouped-list-search-hint");
				page.appendHtml(searchHint);
				page.appendHtmlTag("/div");
			}
			page.appendHtmlTag("div", "class", "grouped-list-section-wrapper");
			appendFilterFields(page);
		}
		boolean requiresWrapper = ReRenderSectionMarkerRenderer.requiresWrapper(context);
		if (requiresWrapper) ReRenderSectionMarkerRenderer.renderOpen(id, page);
		page.appendHtmlTag("div", "class", "list-section-wrapper", "sectionId", id);
		String searchPhrase = getFilterFromCookie();
		Predicate<Section<T>> filter = Strings.isBlank(searchPhrase)
				? Predicates.limit(noFilterPredicate, (noFilterLimit == -1) ? getCountFromCookie() : noFilterLimit)
				: Predicates.limit(new SearchPredicate(searchPhrase).or(alwaysShowPredicate), getCountFromCookie());

		// render the groups
		AtomicBoolean anyLines = new AtomicBoolean(false);
		renderers.forEach(rendererPair -> {
			ListSectionsRenderer<T> filtered = rendererPair.getB().filter(filter);
			if (filtered.isEmpty()) return;
			anyLines.set(true);
			String header = rendererPair.getA();
			if (Strings.isNotBlank(header)) {
				page.appendHtml(header);
			}
			filtered.render(page);
		});

		// render empty text if there are no items to be displayed
		if (!anyLines.get()) {
			page.appendHtmlElement("div", emptyText, "class", "empty-list-sections");
		}

		page.appendHtmlTag("/div");
		if (requiresWrapper) ReRenderSectionMarkerRenderer.renderClose(page);
		if (!context.isReRendering() && !context.isRenderingPreview()) page.appendHtmlTag("/div");
	}

	private void appendFilterFields(RenderResult page) {
		StringBuilder filterBuilder = new StringBuilder();
		filterBuilder.append("<div class='form-inline form-group cage filter-input'>")
				.append("<input type='text' class='form-control filter-list-section-input'")
				.append(" placeholder='").append(placeholder).append("'");
		String filter = getFilterFromCookie();
		if (Strings.isNotBlank(filter)) filterBuilder.append(" value='").append(filter).append("'");
		filterBuilder.append(">").append(Icon.DELETE.addClasses("clear-filter").toHtml());

		filterBuilder.append("<select class='list-sections-filter-select'>");
		int countFromCookie = getCountFromCookie();
		for (String c : COUNT_OPTIONS.keySet()) {
			filterBuilder.append("<option value='").append(c).append("'");
			if (COUNT_OPTIONS.get(c) == countFromCookie) filterBuilder.append(" selected='selected'");
			filterBuilder.append(">").append(c).append("</option>");
		}
		filterBuilder.append("</select>");
		filterBuilder.append(Icon.INFO.addTitle(getInfoTitle()).toHtml());
		filterBuilder.append("</div>");
		page.appendHtml(filterBuilder.toString());
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

	private final class SearchPredicate implements Predicate<Section<T>> {

		private final List<Predicate<Section<T>>> filters = new ArrayList<>();

		public SearchPredicate(String searchPhrase) {
			// Search phrases with :<>=
			Matcher matcher = SEARCH_PATTERN.matcher(searchPhrase);
			while (matcher.find()) {
				String name = matcher.group(1);
				String operator = matcher.group(2);
				String value = matcher.group(3);
				addKeyFilter(keyFilterProviders, name, operator, value);
				addNumFilter(numKeyFilterProviders, name, operator, value);
				for (Pair<String, ListSectionsRenderer<T>> renderer : renderers) {
					addKeyFilter(renderer.getB().getKeyFilters(), name, operator, value);
				}
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
				Predicate<Section<T>> simpleFilter = createFilter(GroupedFilterListSectionsRenderer.this::getSearchableText, clean(rest));
				Predicate<Section<T>> filtersPredicate = Predicates.FALSE(); // an empty predicate
				for (Function<Section<T>, String> filter : keylessFilterProviders) {
					filtersPredicate = filtersPredicate.or(createFilter(filter, rest.split("[\u00A0\\h\\s\\v]+")));
				}
				filters.add(Predicates.or(simpleFilter, filtersPredicate));
			}
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
	}

	@NotNull
	private String getSearchableText(Section<T> s) {
		StringBuilder b = new StringBuilder();
		b.append(clean(s.getText()));
		for (Pair<String, ListSectionsRenderer<T>> renderer : this.renderers) {
			for (Function<Section<T>, String> textFunction : renderer.getB().getKeyFilters().values()) {
				b.append(" ").append(clean(textFunction.apply(s)));
			}
		}
		return b.toString();
	}

	@NotNull
	private static String clean(String rest) {
		return rest == null ? "" : rest.replaceAll("[-/\\\\\u00A0\\h\\s\\v]", "");
	}
}
