package de.knowwe.jspwiki;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static com.denkbares.strings.Strings.*;

/**
 * @author Tim Abler
 * @created 10.10.2018
 */
public class PackageArticlesMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	public static final String ANNOTATION_EXCLUDE = "exclude";

	public static final String ANNOTATION_TEMPLATE = "template";

	static {
		MARKUP = new DefaultMarkup("PackageArticles");
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(ANNOTATION_TEMPLATE);
		MARKUP.addAnnotation(ANNOTATION_EXCLUDE);
		MARKUP.getAnnotation(ANNOTATION_TEMPLATE)
				.setDocumentation("Set a template how each Link-Label should be adapted, e.g.\n"
						+ "@template: * link.replace(\"Prefix\", \"Other Prefix\")");
		MARKUP.getAnnotation(ANNOTATION_EXCLUDE).setDocumentation("Specify a regex regarding the article names to " +
				"exclude from this list, even if they are part of the package.");
		MARKUP.setTemplate("%%PackageArticles \n" +
				"@package: \u00ABpackage-name\u00BB\n" +
				"@exclude: \u00ABarticle-name-regex\u00BB\n" +
				"%");
	}

	public PackageArticlesMarkup() {
		super(MARKUP);
		this.setRenderer((section, user, string) -> {
			Collection<Section<?>> packageSections = KnowWEUtils.getPackageManager(section)
					.getSectionsOfPackage(section.getPackageNames().toArray(new String[0]));

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
			String exclude = DefaultMarkupType.getAnnotation(section, ANNOTATION_EXCLUDE);
			String[] excludeRegex = splitUnquotedToArray(exclude, ",");
			for (int i = 0; i < excludeRegex.length; i++) {
				excludeRegex[i] = unquote(trim(excludeRegex[i]));
			}

			packageSections.stream()
					.filter(s -> {
						assert s.getTitle() != null;
						for (String regex : excludeRegex) {
							if (s.getTitle().matches(regex)) {
								return false;
							}
						}
						return true;
					})
					.filter(section1 -> {
						assert section.getTitle() != null;
						return !section.getTitle().equals(section1.getTitle());
					})
					.filter(distinctByKey(Section::getTitle))

					.forEach(s -> {
						assert s.getTitle() != null;
						string.append(prefixFinal + "[" + s.getTitle()
								.replace(oldStringFinal, newStringFinal) + "|" + s.getTitle() + "]\n" + suffixFinal);
					});

			string.appendHtml("</div>");
		});
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
