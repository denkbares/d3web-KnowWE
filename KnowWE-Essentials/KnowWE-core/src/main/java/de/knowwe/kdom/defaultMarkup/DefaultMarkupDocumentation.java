/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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
package de.knowwe.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.elements.A;
import de.knowwe.core.kdom.rendering.elements.Div;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.HtmlNode;
import de.knowwe.core.kdom.rendering.elements.Li;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.kdom.rendering.elements.Ul;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup.Annotation;

/**
 * Renders the documentation of a {@link DefaultMarkup} or one of its annotations as an HTML fragment: the description,
 * examples, the annotations with their allowed values and links to help pages. The editor shows it when hovering a
 * markup or annotation name, and the completion shows it next to the suggested markups and annotations, so both read
 * the same. Examples are rendered as <code>pre.knowwe-doc-example</code> with the wiki text, which the editor
 * highlights.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.11.2026
 */
public final class DefaultMarkupDocumentation {

	/**
	 * Property of the wiki or the system that holds the URL prefix of an external wiki with help pages, the page name
	 * "Doc &lt;markup name&gt;" is appended.
	 */
	public static final String HELP_WIKI_PROPERTY = "knowwe.helpTool.externalWiki";

	private static final Pattern ENUM_PATTERN = Pattern.compile("\\(\\?<enum>([^)]*)\\)");
	private static final Pattern SENTENCE_END = Pattern.compile("(?<=[.!?:])\\s");

	private DefaultMarkupDocumentation() {
	}

	/**
	 * Returns the markup type of the environment with the given markup name, ignoring case, or null. Deprecated
	 * markups are found as well, since existing pages may still use them.
	 */
	@Nullable
	public static DefaultMarkupType findMarkupType(Environment environment, String markupName) {
		if (Strings.isBlank(markupName)) return null;
		Collection<DefaultMarkupType> types = Types.successors(environment.getRootType(), DefaultMarkupType.class);
		for (DefaultMarkupType type : types) {
			if (type.getMarkup().getName().equalsIgnoreCase(markupName)) return type;
		}
		return null;
	}

	/**
	 * Returns the annotation of the markup with the given name, ignoring case, also matching regex annotations, or
	 * null.
	 */
	@Nullable
	public static Annotation findAnnotation(DefaultMarkup markup, String annotationName) {
		if (Strings.isBlank(annotationName)) return null;
		for (Annotation annotation : markup.getAnnotations()) {
			if (annotation.isRegex()) continue;
			if (annotation.getName().equalsIgnoreCase(annotationName)) return annotation;
		}
		for (Annotation annotation : markup.getAnnotations()) {
			if (!annotation.isRegex()) continue;
			if (Pattern.compile(annotation.getName(), Pattern.CASE_INSENSITIVE).matcher(annotationName).matches()) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Returns the allowed values of the annotation if its pattern enumerates them with a group named "enum", see
	 * {@link DefaultMarkup#addAnnotation(String, boolean, String...)}, otherwise null.
	 */
	@Nullable
	public static String[] getEnumValues(@Nullable Annotation annotation) {
		if (annotation == null) return null;
		Pattern pattern = annotation.getPattern();
		if (pattern == null) return null;
		Matcher enumFinder = ENUM_PATTERN.matcher(pattern.toString());
		if (!enumFinder.find()) return null;
		return Stream.of(enumFinder.group(1).split("\\|"))
				.map(v -> v.replaceAll("^\\\\Q|\\\\E$", "")).toArray(String[]::new);
	}

	/**
	 * Returns whether the annotation accepts several values separated by blanks, commas or semicolons.
	 */
	public static boolean isMultiple(Annotation annotation) {
		Pattern pattern = annotation.getPattern();
		return pattern != null && pattern.toString().contains(DefaultMarkup.REGEX_SEPARATE_MULTIPLE);
	}

	/**
	 * Returns the examples of the markup, or its template as the only example if it has none, without trailing blank
	 * lines.
	 */
	@NotNull
	public static List<String> getExamples(DefaultMarkup markup) {
		List<String> examples = new ArrayList<>(markup.getExamples());
		if (examples.isEmpty() && Strings.isNotBlank(markup.getTemplate())) examples.add(markup.getTemplate());
		examples.replaceAll(Strings::trimRight);
		return examples;
	}

	/**
	 * Returns the links to the help pages of the markup: the wiki page "Doc &lt;name&gt;" or "Doc &lt;name&gt;s" of
	 * this wiki, if the user may view it, and the page of the external help wiki, if configured. Each link is an array
	 * of label and URL.
	 */
	@NotNull
	public static List<String[]> getHelpLinks(UserContext context, String markupName) {
		List<String[]> links = new ArrayList<>();
		Article article = getHelpArticle(context.getEnvironment().getArticleManager(), markupName);
		if (article != null && KnowWEUtils.canView(article, context)) {
			links.add(new String[] { article.getTitle(), KnowWEUtils.getURLLink(article) });
		}
		String external = getExternalHelpLink(context, markupName);
		if (external != null) links.add(new String[] { "Doc " + markupName + " (external)", external });
		return links;
	}

	/**
	 * Returns the help page "Doc &lt;name&gt;" or "Doc &lt;name&gt;s" of the markup in the wiki, or null.
	 */
	@Nullable
	public static Article getHelpArticle(@Nullable ArticleManager articleManager, String markupName) {
		if (articleManager == null) return null;
		Article article = articleManager.getArticle("Doc " + markupName);
		if (article == null) article = articleManager.getArticle("Doc " + markupName + "s");
		return article;
	}

	/**
	 * Returns the URL of the help page of the markup in the external help wiki, see {@link #HELP_WIKI_PROPERTY}, or
	 * null if none is configured.
	 */
	@Nullable
	public static String getExternalHelpLink(UserContext context, String markupName) {
		String wikiProperty = System.getProperty(HELP_WIKI_PROPERTY, context.getEnvironment()
				.getWikiConnector()
				.getWikiProperty(HELP_WIKI_PROPERTY));
		if (wikiProperty == null) return null;
		return wikiProperty + "Doc " + markupName;
	}

	/**
	 * Renders the documentation of the markup: title, description, examples, the annotations with a short description
	 * each and the help links. The annotation with the given name, if any, is marked as the current one.
	 */
	@NotNull
	public static HtmlElement renderMarkup(UserContext context, DefaultMarkupType type, @Nullable String currentAnnotation) {
		DefaultMarkup markup = type.getMarkup();
		Div doc = doc(markup.getName(), null);
		HtmlElement title = new Div().clazz("knowwe-doc-title");
		String icon = getIconPath(type);
		if (icon != null) title.children(new HtmlElement("img").clazz("knowwe-doc-icon").attributes("src", icon, "alt", ""));
		title.children(code("%%" + markup.getName()));
		if (markup.isInline()) title.children(badge("inline"));
		doc.children(title);
		if (markup.isDeprecated()) {
			doc.children(new Div().clazz("knowwe-doc-deprecated")
					.plainText("Deprecated" + (Strings.isBlank(markup.getDeprecatedAlternative())
							? "" : ", use %%" + markup.getDeprecatedAlternative() + " instead")));
		}
		if (Strings.isNotBlank(markup.getDocumentation())) {
			doc.children(new Div().clazz("knowwe-doc-text").children(new HtmlNode(markup.getDocumentation())));
		}

		List<String> examples = getExamples(markup);
		if (!examples.isEmpty()) {
			boolean fromTemplate = markup.getExamples().isEmpty();
			doc.children(heading(examples.size() > 1 ? "Examples" : fromTemplate ? "Template" : "Example"));
			for (String example : examples) {
				doc.children(example(example));
			}
		}

		List<Annotation> annotations = Stream.of(markup.getAnnotations())
				.filter(annotation -> !annotation.isDeprecated() && !annotation.isRegex())
				.toList();
		if (!annotations.isEmpty()) {
			doc.children(heading("Annotations"));
			Ul list = new Ul();
			list.clazz("knowwe-doc-annotations");
			for (Annotation annotation : annotations) {
				Li item = new Li();
				item.attributes("data-annotation", annotation.getName());
				if (annotation.getName().equalsIgnoreCase(currentAnnotation)) item.clazz("knowwe-doc-current");
				item.children(code("@" + annotation.getName()), requirement(annotation));
				String summary = summary(annotation.getDocumentation());
				String[] values = getEnumValues(annotation);
				if (values != null) {
					summary = (summary.isEmpty() ? "" : summary + " ") + "Values: " + String.join(", ", values);
				}
				if (!summary.isEmpty()) item.children(new Span().clazz("knowwe-doc-summary").plainText(summary));
				list.children(item);
			}
			doc.children(list);
		}
		doc.children(links(context, markup.getName()));
		return doc;
	}

	/**
	 * Renders the documentation of an annotation of the markup: title, description, allowed values, example and a
	 * link to the documentation of the whole markup.
	 */
	@NotNull
	public static HtmlElement renderAnnotation(UserContext context, DefaultMarkup markup, Annotation annotation) {
		Div doc = doc(markup.getName(), annotation.getName());
		doc.children(new Div().clazz("knowwe-doc-title").children(
				code("@" + annotation.getName()), requirement(annotation),
				new Span().clazz("knowwe-doc-context").plainText("in "),
				new A().clazz("knowwe-doc-markup-link")
						.attributes("href", "#", "data-markup", markup.getName())
						.children(code("%%" + markup.getName()))));
		if (annotation.isDeprecated()) {
			doc.children(new Div().clazz("knowwe-doc-deprecated").plainText("Deprecated"));
		}
		if (Strings.isNotBlank(annotation.getDocumentation())) {
			doc.children(new Div().clazz("knowwe-doc-text").children(new HtmlNode(annotation.getDocumentation())));
		}
		String[] values = getEnumValues(annotation);
		if (values != null && values.length > 0) {
			doc.children(heading("Allowed values" + (isMultiple(annotation) ? " (several separated by ',')" : "")));
			Ul list = new Ul();
			list.clazz("knowwe-doc-values");
			for (String value : values) {
				Li item = new Li();
				item.children(code(value));
				String valueDoc = annotation.getValueDocumentation(value);
				if (Strings.isNotBlank(valueDoc)) {
					item.children(new Span().clazz("knowwe-doc-summary").children(new HtmlNode(valueDoc)));
				}
				list.children(item);
			}
			doc.children(list);
		}
		if (Strings.isNotBlank(annotation.getExample())) {
			doc.children(heading("Example"), example(Strings.trimRight(annotation.getExample())));
		}
		doc.children(links(context, markup.getName()));
		return doc;
	}

	@Nullable
	private static String getIconPath(DefaultMarkupType type) {
		Renderer renderer = type.getRenderer();
		return renderer instanceof DefaultMarkupRenderer markupRenderer ? markupRenderer.getIconPath() : null;
	}

	/**
	 * Returns the first sentence of the documentation as plain text, without HTML tags.
	 */
	@NotNull
	static String summary(@Nullable String documentation) {
		if (Strings.isBlank(documentation)) return "";
		String text = documentation.replaceAll("(?i)<br\\s*/?>|</p>|</li>|</?ul>", " ").replaceAll("<[^>]*>", "");
		text = Strings.decodeHtml(text).replaceAll("\\s+", " ").trim();
		Matcher end = SENTENCE_END.matcher(text);
		return end.find() ? text.substring(0, end.start()) : text;
	}

	private static Div doc(String markup, @Nullable String annotation) {
		Div doc = new Div();
		doc.clazz("knowwe-doc").attributes("data-markup", markup);
		if (annotation != null) doc.attributes("data-annotation", annotation);
		return doc;
	}

	private static HtmlElement code(String text) {
		return new HtmlElement("code").plainText(text);
	}

	private static HtmlElement badge(String text) {
		return new Span().clazz("knowwe-doc-badge").plainText(text);
	}

	private static HtmlElement requirement(Annotation annotation) {
		return annotation.isMandatory()
				? new Span().clazz("knowwe-doc-badge knowwe-doc-required").plainText("required")
				: badge("optional");
	}

	private static HtmlElement heading(String text) {
		return new Div().clazz("knowwe-doc-heading").plainText(text);
	}

	private static HtmlElement example(String wikiText) {
		return new HtmlElement("pre").clazz("knowwe-doc-example").plainText(wikiText);
	}

	private static HtmlElement links(UserContext context, String markupName) {
		Div links = new Div();
		links.clazz("knowwe-doc-links");
		for (String[] link : getHelpLinks(context, markupName)) {
			links.children(new A().attributes("href", link[1], "target", "_blank").plainText(link[0]));
		}
		return links;
	}
}
