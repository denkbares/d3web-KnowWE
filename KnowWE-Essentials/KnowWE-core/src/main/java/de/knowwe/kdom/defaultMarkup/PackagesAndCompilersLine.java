/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.ServiceKey;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.GroupingCompiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.elements.Div;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.HtmlProvider;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.util.Icon;

/**
 * The line below a markup with its packages and its compilers, each with its icon. The line never wraps (cut with an
 * ellipsis, see KnowWE-Plugin-Core.css), the full text is its tooltip (tooltipster), one line for each part, with the
 * packages and compilers in their colors and the labels and operators in grey. Other elements can be put in front of
 * the packages and compilers, e.g. a link to the markup.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 29.10.2026
 */
public class PackagesAndCompilersLine {

	public static final String CSS_CLASS = "markupPackagesLine";
	public static final String ICON_TEXT_CSS_CLASS = "markupIconText";
	public static final String COMPILERS_CSS_CLASS = "markupCompilers";

	/**
	 * The operators, brackets and separators of package rules (see PredicateParser), shown in grey in the tooltip.
	 */
	private static final Pattern PACKAGE_SYNTAX = Pattern.compile("\\s+|[()!,]|\\|\\|?|&&?|\\b(?:AND|OR|NOT)\\b");

	/**
	 * The label of the compilers in the tooltip, one per environment: "Compilers", unless a plugin sets another one,
	 * e.g. "Variants" (see {@link #setCompilersLabel(Environment, String)}).
	 */
	public static final class CompilersLabel {

		public static final ServiceKey<CompilersLabel> SERVICE = ServiceKey.of(CompilersLabel.class, environment -> new CompilersLabel());

		private volatile String label = "Compilers";
	}

	private final List<HtmlProvider> children = new ArrayList<>();
	private final List<String> tooltipLines = new ArrayList<>();
	private boolean empty = true;

	/**
	 * Sets the label of the compilers in the tooltip of the lines of the given environment, e.g. "Variants" if the
	 * compilers are the variants of a product.
	 */
	public static void setCompilersLabel(@NotNull Environment environment, @NotNull String label) {
		environment.getService(CompilersLabel.SERVICE).label = label;
	}

	private static String getCompilersLabel(Section<?> section) {
		Environment environment = section.findEnvironment();
		return environment == null ? "Compilers" : environment.getService(CompilersLabel.SERVICE).label;
	}

	/**
	 * Adds an element in front of the packages and compilers, with its line of the tooltip (if any), shown as a label.
	 */
	public PackagesAndCompilersLine add(@NotNull HtmlProvider element, @Nullable String tooltipLabel) {
		return addElement(element, Strings.isBlank(tooltipLabel) ? null : renderTooltipLabel(tooltipLabel));
	}

	/**
	 * Adds an element in front of the packages and compilers, with its line of the tooltip: the label and the value.
	 */
	public PackagesAndCompilersLine add(@NotNull HtmlProvider element, @NotNull String tooltipLabel, @NotNull String tooltipValue) {
		return addElement(element, renderTooltipLabel(tooltipLabel) + " " + Strings.encodeHtml(tooltipValue));
	}

	private PackagesAndCompilersLine addElement(HtmlProvider element, @Nullable String tooltipLineHtml) {
		children.add(element);
		if (tooltipLineHtml != null) tooltipLines.add(tooltipLineHtml);
		return this;
	}

	/**
	 * Adds the given packages as plain text, if there are any.
	 */
	public PackagesAndCompilersLine packages(@Nullable String packages) {
		if (Strings.isBlank(packages)) return this;
		return packages(packages, new Span().clazz("packageOpacity").plainText(packages));
	}

	/**
	 * Adds the packages rendered by the given elements (e.g. the package rules with their renderers), with the given
	 * text as tooltip.
	 */
	public PackagesAndCompilersLine packages(@NotNull String packagesText, HtmlProvider... rendered) {
		empty = false;
		return addElement(renderIconText(Icon.PACKAGE, rendered), renderPackagesTooltip(packagesText));
	}

	/**
	 * Adds the packages rendered by the given element, which already shows the icon of the packages (e.g. the package
	 * annotation of a markup), with the given text as tooltip.
	 */
	public PackagesAndCompilersLine packagesWithIcon(@NotNull String packagesText, HtmlProvider rendered) {
		empty = false;
		return addElement(rendered, renderPackagesTooltip(packagesText));
	}

	/**
	 * Adds the compilers of the given markup, if there are any. The compilers of a grouping compiler are left out, the
	 * grouping compiler is shown. The names are shortened by the parts shared by all compilers of their class (e.g.
	 * "ssm-"), the tooltip has the full names.
	 */
	public PackagesAndCompilersLine compilers(@NotNull Section<?> markupSection) {
		return compilers(getCompilers(markupSection), getCompilersLabel(markupSection));
	}

	/**
	 * Adds the given compilers, if there are any, with the given label in the tooltip, see {@link
	 * #compilers(Section)}.
	 */
	public PackagesAndCompilersLine compilers(@NotNull Collection<? extends PackageCompiler> compilers, @NotNull String tooltipLabel) {
		if (compilers.isEmpty()) return this;
		empty = false;
		return addElement(renderIconText(ICON_TEXT_CSS_CLASS + " " + COMPILERS_CSS_CLASS, Icon.COMPILER,
						new Span().clazz("packageOpacity").plainText(compilers.stream()
								.map(Compilers::getShortCompilerName).distinct().collect(Collectors.joining(", ")))),
				renderTooltipLabel(tooltipLabel + ":") + " " + compilers.stream()
						.map(Compilers::getCompilerName).distinct().map(PackagesAndCompilersLine::renderTooltipName)
						.collect(Collectors.joining(renderTooltipSyntax(", "))));
	}

	/**
	 * Whether neither packages nor compilers were added.
	 */
	public boolean isEmpty() {
		return empty;
	}

	/**
	 * The line as a div with the given additional css classes. The tooltip is shown with tooltipster (its content is
	 * html), one line for each part.
	 */
	public HtmlElement toHtmlElement(String... cssClasses) {
		String classes = String.join(" ", cssClasses);
		return new Div().clazz(CSS_CLASS + " tooltipster" + (Strings.isBlank(classes) ? "" : " " + classes))
				.title(String.join("<br>", tooltipLines))
				.children(children.toArray(HtmlProvider[]::new));
	}

	/**
	 * The line of the tooltip for the given packages (a package rule or package names): the package names in the color
	 * of the packages, the operators and separators in grey.
	 */
	private static String renderPackagesTooltip(String packagesText) {
		StringBuilder html = new StringBuilder(renderTooltipLabel("Packages:")).append(" ");
		Matcher matcher = PACKAGE_SYNTAX.matcher(packagesText);
		int start = 0;
		while (matcher.find()) {
			if (matcher.start() > start) html.append(renderTooltipName(packagesText.substring(start, matcher.start())));
			String syntax = matcher.group();
			html.append(Strings.isBlank(syntax) ? Strings.encodeHtml(syntax) : renderTooltipSyntax(syntax));
			start = matcher.end();
		}
		if (start < packagesText.length()) html.append(renderTooltipName(packagesText.substring(start)));
		return html.toString();
	}

	private static String renderTooltipLabel(String label) {
		return "<span class=\"markupPackagesTooltipLabel\">" + Strings.encodeHtml(label) + "</span>";
	}

	private static String renderTooltipSyntax(String syntax) {
		return "<span class=\"markupPackagesTooltipSyntax\">" + Strings.encodeHtml(syntax) + "</span>";
	}

	private static String renderTooltipName(String name) {
		return "<span class=\"packageOpacity\">" + Strings.encodeHtml(name) + "</span>";
	}

	/**
	 * The given text with the given icon in front, in the color of the packages.
	 */
	public static HtmlElement renderIconText(Icon icon, String text) {
		return renderIconText(icon, new Span().clazz("packageOpacity").plainText(text));
	}

	/**
	 * The given elements with the given icon in front, in the color of the packages.
	 */
	public static HtmlElement renderIconText(Icon icon, HtmlProvider... content) {
		return renderIconText(ICON_TEXT_CSS_CLASS, icon, content);
	}

	private static HtmlElement renderIconText(String cssClasses, Icon icon, HtmlProvider... content) {
		return new Span().clazz(cssClasses)
				.children(icon.addClasses("packageOpacity").addStyle("margin-right: .3em").toHtmlElement())
				.children(content);
	}

	/**
	 * The compilers of the given markup without the compilers of its grouping compilers (the grouping compiler stands
	 * for them).
	 */
	public static Collection<PackageCompiler> getCompilers(Section<?> markupSection) {
		Collection<PackageCompiler> compilers = new ArrayList<>(Compilers.getCompilers(markupSection, PackageCompiler.class));
		compilers.stream()
				.filter(c -> c instanceof GroupingCompiler)
				.map(c -> (GroupingCompiler) c)
				.toList()
				.forEach(grouping -> compilers.removeAll(grouping.getChildCompilers()));
		return compilers;
	}
}
