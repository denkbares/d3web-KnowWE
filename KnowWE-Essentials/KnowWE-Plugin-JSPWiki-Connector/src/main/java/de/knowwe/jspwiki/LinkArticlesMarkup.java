package de.knowwe.jspwiki;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static com.denkbares.strings.Strings.*;

/**
 * @author Tim Abler
 * @created 10.10.2018
 */
public class LinkArticlesMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	public static final String ANNOTATION_EXCLUDE = "exclude";
	public static final String ANNOTATION_TEMPLATE = "template";
	public static final String ANNOTATION_MARKUP = "markup";
	public static final String ANNOTATION_TITLES = "titles";

	static {
		MARKUP = new DefaultMarkup("LinkArticles");
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(ANNOTATION_TEMPLATE);
		MARKUP.addAnnotation(ANNOTATION_EXCLUDE);
		MARKUP.addAnnotation(ANNOTATION_MARKUP);
		MARKUP.addAnnotation(ANNOTATION_TITLES);
		MARKUP.getAnnotation("package")
				.setDocumentation("Specify a package name to show all articles which use this package.");
		MARKUP.getAnnotation(ANNOTATION_TEMPLATE)
				.setDocumentation("Set a template how each Link-Label should be adapted, e.g.\n"
						+ "@template: * link.replace(\"Prefix\", \"Other Prefix\")");
		MARKUP.getAnnotation(ANNOTATION_EXCLUDE).setDocumentation("Specify a regex regarding the article names to " +
				"exclude from this list, even if they are part of the package, the markup or the mentioned titles.");
		MARKUP.getAnnotation(ANNOTATION_MARKUP)
				.setDocumentation("Specify the name of a markup to show all articles it's used in.");
		MARKUP.getAnnotation(ANNOTATION_TITLES)
				.setDocumentation("Specify a regex regarding the article names to show in this list.");
		MARKUP.setTemplate("%%LinkArticles \n" +
				"@package: \u00ABpackage-name\u00BB\n" +
				"@markup: \u00ABmarkup-name\u00BB\n" +
				"@titles: \u00ABarticle-name-regex\u00BB\n" +
				"@exclude: \u00ABarticle-name-regex\u00BB\n" +
				"%");
	}

	public LinkArticlesMarkup() {
		super(MARKUP);
		this.setRenderer((section, user, string) -> {
			//message displayed if unable to find markups/packages
			Set<String> errorMessages = new HashSet<>();

			//sections which should be shown
			Set<String> shownSections = new HashSet<>();

			string.appendHtml("<div>");
			string.append("\n");

			//annotation template
			String template = DefaultMarkupType.getAnnotation(section, ANNOTATION_TEMPLATE);
			TemplateResult templateResult = new TemplateResult(template).invoke();
			String prefix = templateResult.getPrefix();
			String suffix = templateResult.getSuffix();
			String oldString = templateResult.getOldString();
			String newString = templateResult.getNewString();

			final String prefixFinal = prefix;
			final String suffixFinal = suffix;
			final String oldStringFinal = oldString;
			final String newStringFinal = newString;

			//annotation exclude
			String[] excludeAnnotations = DefaultMarkupType.getAnnotations(section, ANNOTATION_EXCLUDE);
			List<String> excludeRegexes = new LinkedList<>();
			if (excludeAnnotations.length != 0) {
				for (String exclude : excludeAnnotations) {
					String[] excludeRegex = splitUnquotedToArray(exclude, ",");
					for (String anExcludeRegex : excludeRegex) {
						excludeRegexes.add(unquote(trim(anExcludeRegex)));
					}
				}
			}

			//annotation package
			handleAnnotationPackage(section, errorMessages, shownSections, excludeRegexes);

			//handle annotation markup
			handleAnnotationMarkup(section, errorMessages, shownSections);

			//handle annotaion titles
			handleAnnotationTitles(section, errorMessages, shownSections, excludeRegexes);

			//add messages to site
			if (!errorMessages.isEmpty() ) {
				string.appendHtml("<span class=\"error\" style=\"white-space: pre-wrap;\">");
				for (String message : errorMessages) {
					string.appendHtml(message + "<br>");
				}
				string.appendHtml("</span>\n");
			}

			//add result to site
			shownSections.stream()
					.filter(titel -> {
						for (String regex : excludeRegexes) {
							if (titel.matches(regex)) {
								return false;
							}
						}
						return true;
					})
					.forEach(titel -> string.append(prefixFinal + "[" + titel
							.replace(oldStringFinal, newStringFinal) + "|" + titel + "]\n" + suffixFinal));

			string.appendHtml("</div>");
		});
	}

	private void handleAnnotationTitles(Section<?> section, Set<String> errorMessages, Set<String> shownSections, List<String> excludeRegexes) {
		String[] titleAnnotations = DefaultMarkupType.getAnnotations(section, ANNOTATION_TITLES);

		if (titleAnnotations.length > 0) {
			Collection<Article> articles = KnowWEUtils.getArticleManager(section.getWeb()).getArticles();

			for (String title : titleAnnotations) {
				AtomicInteger itemCount = new AtomicInteger(0);
				articles.stream()
						.filter(a -> {
							for (String regex : excludeRegexes) {
								if (title.matches(regex)) {
									errorMessages.add("unable to find articles which match the regex: " + title + ", because they're excluded!");
									return false;
								}
							}
							return true;
						})
						.filter(a -> a.getTitle().matches(title))
						.forEach(a -> {
							shownSections.add(a.getTitle());
							itemCount.getAndIncrement();
						});
				if (itemCount.get() < 1) {
					errorMessages.add("unable to find articles which match the regex: " + title);
				}
			}
		}
	}

	private void handleAnnotationMarkup(Section<?> section, Set<String> errorMessages, Set<String> shownSections) {
		String[] markupAnnotations = DefaultMarkupType.getAnnotations(section, ANNOTATION_MARKUP);
		if (markupAnnotations.length > 0) {
			for (String markup : markupAnnotations) {
				AtomicInteger itemCount = new AtomicInteger(0);
				//Collection for Markups
				Collection<Type> markups = Types.getAllChildrenTypesRecursive(RootType.getInstance());
				markups.stream()
						.filter(t -> t instanceof DefaultMarkupType)
						.filter(t -> t.getName().equalsIgnoreCase(markup) || t.getClass().getSimpleName().equalsIgnoreCase(markup))
						.forEach(t -> Sections.$(KnowWEUtils.getArticleManager(section.getWeb()))
								.successor(t.getClass())
								.stream()
								.forEach(s -> {
									assert s.getTitle() != null;
									if (!section.getArticle().getTitle().equalsIgnoreCase(s.getTitle())) {
										shownSections.add(s.getTitle());
									}
									itemCount.getAndIncrement();
								}));

				//test if found any Sections for this markup
				if (itemCount.get() == 0) {
					errorMessages.add("unable to find markups: " + markup);
				}
			}
		}
	}

	private void handleAnnotationPackage(Section<?> section, Set<String> errorMessages, Set<String> shownSections, List<String> excludeRegexes) {
		String[] mentionedPackages = DefaultMarkupType.getAnnotations(section, "package");
		if (mentionedPackages.length > 0) {

			for (String mentionedPackage : mentionedPackages) {
				Collection<Section<?>> packageSections = KnowWEUtils.getPackageManager(section)
						.getSectionsOfPackage(mentionedPackage);

				AtomicInteger itemCount = new AtomicInteger(0);
				packageSections.stream()
						.filter(s -> {
							assert s.getTitle() != null;
							for (String regex : excludeRegexes) {
								if (s.getTitle().matches(regex)) {
									return false;
								}
							}
							return true;
						})
						.filter(s -> {
							assert section.getTitle() != null;
							return !section.getTitle().equals(s.getTitle());
						})
						.filter(distinctByKey(Section::getTitle))

						.forEach(s -> {
							assert s.getTitle() != null;
							if (!section.getArticle().getTitle().equals(s.getTitle())) {
								shownSections.add(s.getTitle());
							}
							itemCount.getAndIncrement();
						});
				if (itemCount.get() == 0) {
					errorMessages.add("unable to find packages: " + mentionedPackage);
				}
			}
		}
	}

	private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	private static class TemplateResult {
		private String template;
		private String prefix;
		private String suffix;
		private String oldString;
		private String newString;

		TemplateResult(String template) {
			this.template = template;
		}

		public String getPrefix() {
			return prefix;
		}

		public String getSuffix() {
			return suffix;
		}

		String getOldString() {
			return oldString;
		}

		String getNewString() {
			return newString;
		}

		public TemplateResult invoke() {
			//annotation template
			prefix = "*";
			suffix = "";
			oldString = "";
			newString = "";
			if (template == null) return this;
			if (template.contains("link")) {
				if (template.endsWith("\\")) {
					suffix = "\\\\";
				}
				if (template.matches(".* link.*")) {
					prefix = template.substring(0, template.indexOf("link") - 1);
				}
				else if (template.matches("link.*")) {
					prefix = template.substring(0, template.indexOf("link"));
				}
				if (template.matches(".*link\\.replace(.*,.*)")) {

					template = template.substring(template.indexOf("(") + 1, template.lastIndexOf(")"));
					String[] temp = splitUnquotedToArray(template, ",");
					if (temp.length == 2) {
						oldString = unquote(temp[0].trim());
						newString = unquote(temp[1].trim());
					}
					else {
						throw new RuntimeException("The replace methode can only handle two arguments!");
					}
				}
			}
			return this;
		}
	}
}
