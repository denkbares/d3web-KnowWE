/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Functions;
import com.denkbares.utils.Pair;
import com.denkbares.utils.Predicates;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Utility class to get a list of sections to be linked and prints them as a list of sections, each with a name
 * (representing the link), some additional description text, and some tool actions (usually from the sections' tool
 * menus).
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 17.03.2019
 */
public class ListSectionsRenderer<T extends Type> {

	public static final String CSS_TOOL_COLUMN = "list-sections-tools";
	public static final String CSS_NAME_COLUMN = "list-sections-name";
	public static final String CSS_DESCRIPTION_COLUMN = "list-sections-description";
	public static final String CSS_ERROR_COLUMN = "list-sections-error";
	public static final String CSS_HTML_COLUMN = "list-sections-html";
	public static final String CSS_NUMBER_COLUMN = "list-sections-number";
	public static final String CSS_PACKAGES_COLUMN = "list-sections-packages";
	public static final String CSS_COMPILERS_COLUMN = "list-sections-compilers";
	public static final String CSS_LINK = "list-sections-link";

	private final List<Section<T>> sections;
	private final UserContext context;

	private final TreeMap<Integer, String> headers = new TreeMap<>();
	private final List<Pair<String, Function<Section<T>, String>>> columns = new ArrayList<>();
	private final List<Function<Section<T>, Collection<? extends Tool>>> tools = new ArrayList<>();

	private Function<Section<T>, Boolean> greyoutFunction; // decides which lines are greyed out

	private String emptyText = "-- no entries --";

	public ListSectionsRenderer(Sections<T> sections, UserContext context) {
		this(sections.asList(), context);
	}

	public ListSectionsRenderer(Collection<Section<T>> sections, UserContext context) {
		this.sections = new ArrayList<>(sections);
		this.context = context;
	}

	/**
	 * Returns the user context for the current user, this list renderer is created for.
	 *
	 * @return the current user context
	 */
	public UserContext getContext() {
		return context;
	}

	/**
	 * Adds a new column header to the column(s) specified after this method call. All subsequent columns are jointly
	 * labeled with the specified heading, until a new heading is defined. The columns may be defined using, e.g.,
	 * {@link #name(Function)}, {@link #html(Function)}, {@link #description(Function)}, {@link #column(String,
	 * Function)} or {@link #error(Function)} (and others).
	 * <p>
	 * Note that tool columns are not labeled.
	 *
	 * @param heading the column's heading text (plain text to be displayed, also html markup characters are printed as
	 *                they are in the heading sting)
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> header(String heading) {
		this.headers.put(columns.size(), Strings.encodeHtml(heading));
		return this;
	}

	/**
	 * Adds a new column header to the column(s) specified after this method call. All subsequent columns are jointly
	 * labeled with the specified heading, until a new heading is defined. The columns may be defined using, e.g.,
	 * {@link #name(Function)}, {@link #html(Function)}, {@link #description(Function)}, {@link #column(String,
	 * Function)} or {@link #error(Function)} (and others).
	 * <p>
	 * The specified explainHtml is some documentation about the column's content. In contrast to the heading text, it
	 * may contain html-markup that is rendered as html. The explanation is displayed in a tooltip for the header,
	 * indicated by some symbol.
	 * <p>
	 * Note that tool columns are not labeled.
	 *
	 * @param heading     the column's heading text (plain text to be displayed, also html markup characters are printed
	 *                    as they are in the heading sting)
	 * @param explainHtml some html text that will be available as description of the particular column
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> header(String heading, String explainHtml) {
		this.headers.put(columns.size(), "<span class='tooltipster' title='" + Strings.encodeHtml(explainHtml) + "'>" +
				Strings.encodeHtml(heading) + "<span class='knowwe-superscript'>" + Icon.INFO.toHtml() + "</span></span>");
		return this;
	}

	/**
	 * Adds a new column to show the name of the row's section. The specified function should return the name of the
	 * row's section, as plain text, to be rendered as the cell content. The column is added right after the already
	 * existing columns. The rendered name is rendered as a link to the row's section.
	 * <p>
	 * Note: The name column(s) can be used to sort the list for.
	 *
	 * @param name the accessor to the name of each row's section
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> name(Function<Section<T>, String> name) {
		// we use a cached version, because this field is probably used for sorting
		column(CSS_NAME_COLUMN, Functions.cache(name));
		return this;
	}

	/**
	 * Adds a new column to show some description text of the row's section. The specified function should return some
	 * plain text description of the row's section, to be rendered as the cell content. The column is added right after
	 * the already existing columns.
	 *
	 * @param description the accessor to the description of each row's section
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> description(Function<Section<T>, String> description) {
		return column(CSS_DESCRIPTION_COLUMN, description);
	}

	/**
	 * Adds a new column that automatically shows if there are any errors or warnings in the row section's KDOM
	 * subtree.
	 *
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> error() {
		return error(s -> s);
	}

	/**
	 * Adds a new column that automatically shows if there are any errors or warnings of a section's KDOM subtree. The
	 * section to get the errors or warnings from is determined by the mapper function, that maps the row's section to
	 * the section (or it's subtree) to get the errors from.
	 *
	 * @param mapper maps each row's section to the KDOM-subtree, to get the errors/warnings from
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> error(Function<Section<T>, Section<?>> mapper) {
		return column(CSS_ERROR_COLUMN, line -> {
			Section<?> source = mapper.apply(line);
			if (Messages.hasMessagesInSubtree(source, Message.Type.ERROR)) {
				return Icon.ERROR.addTitle(getMessagesString(source, Message.Type.ERROR)).toHtml();
			}
			if (Messages.hasMessagesInSubtree(source, Message.Type.WARNING)) {
				return Icon.WARNING.addTitle(getMessagesString(source, Message.Type.WARNING)).toHtml();
			}
			return "";
		});
	}

	private String getMessagesString(Section<?> section, Message.Type type) {
		return Messages.getMessagesMapFromSubtree(section, type)
				.values()
				.stream()
				.flatMap(Collection::stream)
				.map(Message::getVerbalization)
				.collect(Collectors.joining("\n<br>"));
	}

	/**
	 * Adds a new column to show some html content based on the row's section. The specified function should return some
	 * html content of the row's section, to be rendered as the cell content. The column is added right after the
	 * already existing columns.
	 *
	 * @param html the accessor to the html content of each row's section
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> html(Function<Section<T>, String> html) {
		return column(CSS_HTML_COLUMN, html);
	}

	/**
	 * Adds a new column to show some number content based on the row's section, with the specified number of digits. If
	 * digits is "0", all returned numeric values are shown as integer values. If digits is below 0, the normal string
	 * representation is used. Otherwise all returned numeric values are shown with the fixed number of digits. Null
	 * values are shown as empty cells. All other non-numeric values are shown as its String representation.  The column
	 * is added right after the already existing columns.
	 *
	 * @param number the accessor to the numeric content of each row's section
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> number(int digits, Function<Section<T>, ?> number) {
		return column(CSS_NUMBER_COLUMN, section -> {
			Object value = number.apply(section);
			if (value == null) return "";
			if (value instanceof Number) {
				double val = ((Number) value).doubleValue();
				if (Double.isFinite(val)) {
					return (digits <= 0) ? String.valueOf(Math.round(val))
							: String.format("<span title='%s'>%." + digits + "f</span>", value, val);
				}
			}
			return value.toString();
		});
	}

	/**
	 * Adds a new column to show some textual content of the row's section. The specified function should return some
	 * plain text content of the row's section, to be rendered as the cell content. The column is added right after the
	 * already existing columns. In addition you may specify some own styling class to be used to render the cell
	 * content.
	 *
	 * @param content the accessor to the plain text content of each row's section
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> column(String cssClass, Function<Section<T>, String> content) {
		assert Strings.nonBlank(cssClass);
		this.columns.add(new Pair<>(cssClass, content));
		return this;
	}

	/**
	 * Adds a new column to show some {@link Tool}s for each row. The specified function should return some tools, based
	 * on the the row's section, to be offered to the user.
	 * <p>
	 * Note: Tools columns are currently always rendered as the first columns, before each other column. This might
	 * change in the future, so if you want the columns to remain the first ones, add the tools before adding other
	 * columns.
	 *
	 * @param toolsAccessor the accessor to get the tools based on each row's section
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> tools(Function<Section<T>, Collection<? extends Tool>> toolsAccessor) {
		this.tools.add(toolsAccessor);
		return this;
	}

	/**
	 * Adds a new column to show some {@link Tool}s for each row. The specified tool provider should return some tools,
	 * based on the the row's section, to be offered to the user.
	 * <p>
	 * Note: Tools columns are currently always rendered as the first columns, before each other column. This might
	 * change in the future, so if you want the columns to remain the first ones, add the tools before adding other
	 * columns.
	 *
	 * @param toolProvider the provider to get the tools based on each row's section
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> tools(ToolProvider toolProvider) {
		return tools(toolProvider, Predicates.TRUE());
	}

	/**
	 * Adds a new column to show some {@link Tool}s for each row. The specified tool provider should return some tools,
	 * based on the the row's section, to be offered to the user. Additionally the specified toolFilter is used to
	 * select a subset of the tools.
	 * <p>
	 * Note: Tools columns are currently always rendered as the first columns, before each other column. This might
	 * change in the future, so if you want the columns to remain the first ones, add the tools before adding other
	 * columns.
	 *
	 * @param toolProvider the provider to get the tools based on each row's section
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> tools(ToolProvider toolProvider, Predicate<Tool> toolFilter) {
		this.tools.add(section -> Stream.of(toolProvider.getTools(section, context))
				.filter(toolFilter).collect(Collectors.toList()));
		return this;
	}

	/**
	 * Adds a new column to show all ServiceMateCompilers for each section. The column will contain plain text of all
	 * compiler names separated by a comma
	 *
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> compiler(Class<? extends Compiler> compilerType) {
		return column(CSS_COMPILERS_COLUMN, line -> {
			Collection<? extends Compiler> compilers = Compilers.getCompilers(line, compilerType);
			return compilers.stream().map(Compilers::getCompilerName).collect(Collectors.joining(", "));
		});
	}

	/**
	 * Adds a new column to show all packages for each section. The column will contain the package rules (as plain
	 * text) separated by a comma if there are more than one.
	 *
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> packages() {
		return column(CSS_PACKAGES_COLUMN, line -> {
			Set<String> packageRules = KnowWEUtils.getPackageManager(line)
					.getPackageStatementsOfSection(line);
			return String.join(", ", packageRules);
		});
	}

	/**
	 * Adds a new column to display a link to a page. The specified function should return the name of the page you
	 * want
	 * to link to, i.e. the name you would usually write in brackets[] when linking to the page, but without brackets.
	 * The
	 * column is added right after the already
	 * existing columns. The rendered name is rendered as a link to the row's section.
	 *
	 * @param pageName name of the page you want to link to
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> link(Function<Section<T>, String> pageName) {
		column(CSS_LINK, section -> {
			String linkName = pageName.apply(section);
			return "[" + linkName + "]";
		});
		return this;
	}

	/**
	 * Sorts the sections of this list renderer by the name column(s), where the most recently added name columns are
	 * the main sorting criteria.
	 *
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> sortByName() {
		List<Function<Section<T>, String>> names = columns.stream()
				.filter(col -> CSS_NAME_COLUMN.equals(col.getA())).map(Pair::getB).collect(Collectors.toList());
		if (names.isEmpty()) throw new IllegalStateException("No name column, sorting not possible");
		Collections.reverse(names);
		Comparator<Section<T>> comparator = (s1, s2) -> {
			for (Function<Section<T>, String> name : names) {
				int comp = NumberAwareComparator.CASE_INSENSITIVE.compare(name.apply(s1), name.apply(s2));
				if (comp != 0) return comp;
			}
			return 0;
		};
		sections.sort(comparator);
		return this;
	}

	public <C extends Comparable<C>> ListSectionsRenderer<T> sort(Function<Section<T>, ? extends C> keyExtractor) {
		this.sections.sort(Comparator.comparing(keyExtractor));
		return this;
	}

	public <C> ListSectionsRenderer<T> sort(Function<Section<T>, ? extends C> keyExtractor, Comparator<? super C> comparator) {
		this.sections.sort(Comparator.comparing(keyExtractor, comparator));
		return this;
	}

	public GroupedFilterListSectionsRenderer<T> searchable(String id, String placeholder) {
		return new GroupedFilterListSectionsRenderer<>(id, placeholder, this);
	}

	public GroupedFilterListSectionsRenderer<T> searchable(Section<?> section) {
		return new GroupedFilterListSectionsRenderer<>(section, this);
	}

	/**
	 * Sets the placeholder text to be displayed if there are no entries in the list. You may specify null to display
	 * nothing if the rendered section list is empty.
	 *
	 * @param emptyText placeholder text to be displayed if the list is empty
	 * @return this instance to chain builder calls
	 */
	public ListSectionsRenderer<T> empty(String emptyText) {
		this.emptyText = emptyText;
		return this;
	}

	protected String getEmptyText() {
		return emptyText;
	}

	public void render(RenderResult page) {
		if (sections.isEmpty()) {
			page.appendHtmlElement("div", emptyText, "class", "empty-list-sections");
			return;
		}

		page.appendHtmlTag("table", "class", "list-sections");
		renderHeader(page);
		for (Section<T> section : sections) {
			try {
				renderLine(page, section);
			}
			catch (Throwable e) {
				renderExceptionLine(page, section, e);
			}
		}
		page.appendHtmlTag("/table");
	}

	private void renderExceptionLine(RenderResult page, Section<T> section, Throwable e) {
		page.appendHtmlTag("tr");
		String stackTrace = Strings.getStackTrace(e);
		stackTrace = "<div style='white-space: pre'>" + stackTrace + "</div>";
		page.appendHtmlTag("td", "colspan", String.valueOf(tools.size() + columns.size()), "title", stackTrace,
				"class", "tooltipster knowwe-error");
		page.append("Unable to render section from article '" + section.getTitle() + "' due to exception");
		page.appendHtmlTag("/td");
		page.appendHtmlTag("/tr");
	}

	private void renderHeader(RenderResult page) {
		// skip if no headings are defined
		if (headers.isEmpty()) return;

		// start header line
		page.appendHtmlTag("tr");

		// skip tool columns
		tools.forEach(tool -> page.appendHtmlElement("th", ""));

		// render column groups
		headers.putIfAbsent(0, "");
		headers.forEach((index, heading) -> {
			int next = Optional.ofNullable(headers.higherKey(index)).orElse(columns.size());
			int colspan = next - index;
			page.appendHtmlTag("th", "colspan", String.valueOf(colspan));
			page.appendHtml(heading);
			page.appendHtmlTag("/th");
		});

		// close header line
		page.appendHtmlTag("/tr");
	}

	private void renderLine(RenderResult page, Section<T> line) {
		RenderResult lineResult = new RenderResult(page);
		lineResult.appendHtml("<tr");
		lineResult.appendHtml(greyoutFunction != null && greyoutFunction.apply(line) ? " class='greyed-out'>" : ">");

		// render tool columns for the section
		for (Function<Section<T>, Collection<? extends Tool>> provider : tools) {
			lineResult.appendHtmlTag("td", "class", CSS_TOOL_COLUMN);
			for (Tool tool : provider.apply(line)) {
				renderTool(lineResult, tool);
			}
			lineResult.appendHtmlTag("/td");
		}

		// render column cells
		for (Pair<String, Function<Section<T>, String>> column : columns) {
			String css = column.getA();
			String text = column.getB().apply(line);

			// render empty cells slightly differently
			if (Strings.isBlank(text)) {
				lineResult.appendHtmlElement("td", "", "class", css + " list-sections-blank");
				continue;
			}

			// render cell and it's content
			lineResult.appendHtmlTag("td", "class", css);
			// name styled columns become links
			// other columns are plain text
			switch (css.split("\\s+")[0]) {
				case CSS_NAME_COLUMN, CSS_DESCRIPTION_COLUMN ->
						lineResult.appendHtmlElement("a", KnowWEUtils.maskJSPWikiMarkup(text),
								"href", KnowWEUtils.getURLLink(line));
				case CSS_ERROR_COLUMN, CSS_HTML_COLUMN, CSS_NUMBER_COLUMN -> lineResult.appendHtml(text);
				case CSS_LINK -> lineResult.appendHtmlElement("span", text);
				default -> lineResult.appendHtmlElement("span", KnowWEUtils.maskJSPWikiMarkup(text));
			}
			lineResult.appendHtmlTag("/td");
		}

		lineResult.appendHtmlTag("/tr");
		page.append(lineResult); // only append if we don't get exceptions while rendering
	}

	private void renderTool(RenderResult page, Tool tool) {
		String description = tool.getDescription();
		String tooltip = Strings.isBlank(description)
				? tool.getTitle()
				: tool.getTitle() + ":\n" + description;
		page.appendHtmlTag("a", false, "title", Strings.encodeHtml(tooltip),
				ToolUtils.getActionAttributeName(tool), ToolUtils.getActionAttributeValue(tool));
		if (tool.getIcon() != null) {
			page.appendHtml(tool.getIcon().fixWidth().toHtml());
		}
		else {
			page.appendHtmlElement("span", tool.getTitle());
		}
		page.appendHtmlTag("/a");
	}

	/**
	 * Returns all sections of the specified type that are contained in the specified packages. If no packages are
	 * specified, all matching sections of the whole wiki (web) is returned.
	 *
	 * @param web          the web to search the sections in
	 * @param sectionType  the type of sections to be returned
	 * @param packageNames the package to get the sections for, or null / empty array for the whole wiki
	 * @return the sections matching the specified section type
	 */
	public static <T extends Type> Sections<T> getAllSectionsOfType(String web, Class<T> sectionType, String... packageNames) {
		// if package(s) is not specified, use all articles, otherwise the package articles
		//noinspection RedundantCast (help type inference)
		Sections<?> packageSections = (packageNames.length == 0)
				? $(KnowWEUtils.getArticleManager(web).getArticles().stream().map(Article::getRootSection))
				: $((Collection<Section<?>>) KnowWEUtils.getPackageManager(web).getSectionsOfPackage(packageNames));
		return $(packageSections.stream().distinct()).successor(sectionType);
	}

	public static <T extends Type> Sections<T> getAllSectionsOfType(Section<? extends DefaultMarkupType> packageDefinition, Class<T> sectionType) {
		Collection<String> packageNames = new ArrayList<>(Arrays.asList(DefaultMarkupType.getAnnotations(packageDefinition, PackageManager.PACKAGE_ATTRIBUTE_NAME)));
		List<String> excludedPackageNames = new ArrayList<>(Arrays.asList(DefaultMarkupType.getAnnotations(packageDefinition, PackageManager.EXCLUDE_PACKAGE_ATTRIBUTE_NAME)));
		if (packageNames.isEmpty() && !excludedPackageNames.isEmpty()) {
			// when there are only excluded packages defined -> list all packages and...
			packageNames = new ArrayList<>(KnowWEUtils.getPackageManager(packageDefinition.getWeb())
					.getAllPackageNames());
		}
		// ..remove excluded packages
		packageNames.removeAll(excludedPackageNames);
		return getAllSectionsOfType(packageDefinition.getWeb(), sectionType, packageNames.toArray(new String[0]));
	}

	/**
	 * Returns if the renderer contains no sections.
	 *
	 * @return true if there are no sections, false otherwise
	 */
	public boolean isEmpty() {
		return sections.isEmpty();
	}

	/**
	 * Filters the rows of this list section renderer by the specified filter predicate. If the filter predicate is
	 * null, nothing is done. The method always returns this instance, to allow chained method calls.
	 *
	 * @param filter the filter to be applied
	 * @return this (modified) instance
	 */
	public ListSectionsRenderer<T> filter(Predicate<Section<T>> filter) {
		if (filter != null) this.sections.removeIf(filter.negate());
		return this;
	}

	/**
	 * Use an individual function to grey out lines by overwriting the default grey out function.
	 *
	 * @param greyoutFunction: Function that consumes a section and returns whether the corresponding line is greyed out
	 *                         or not.
	 * @return this (modified) instance
	 */
	public ListSectionsRenderer<T> greyout(@NotNull Function<Section<T>, Boolean> greyoutFunction) {
		this.greyoutFunction = greyoutFunction;
		return this;
	}

	/**
	 * Grey out the lines not compiled by the default compiler of the given compiler class
	 *
	 * @param defaultCompilerClass: the type of compiler to check for, when checking whether the line should be greyed
	 *                              out
	 * @return this (modified) instance
	 */
	public ListSectionsRenderer<T> greyout(Class<? extends Compiler> defaultCompilerClass) {
		this.greyoutFunction = getDefaultCompilerGreyoutFunction(context, defaultCompilerClass);
		return this;
	}

	/**
	 * Provide a default greyout function checking if the section is compiled by the default compiler of the given
	 * compiler class
	 *
	 * @param context              the user context of the render request
	 * @param defaultCompilerClass the class of the compiler to check default compilers agains
	 * @return the default greyout function for the given compiler class
	 */
	public static <T extends Type> Function<Section<T>, Boolean> getDefaultCompilerGreyoutFunction(UserContext context, Class<? extends Compiler> defaultCompilerClass) {
		return line -> {
			Compiler compiler = Compilers.getCompiler(context, line, defaultCompilerClass);
			if (compiler == null) return true;
			return !Compilers.isDefaultCompiler(context, compiler);
		};
	}
}
