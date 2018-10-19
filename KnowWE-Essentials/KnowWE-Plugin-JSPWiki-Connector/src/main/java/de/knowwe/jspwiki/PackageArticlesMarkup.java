package de.knowwe.jspwiki;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static com.denkbares.strings.Strings.splitUnquotedToArray;
import static com.denkbares.strings.Strings.unquote;

/**
 * @author Tim Abler
 * @created 10.10.2018
 */
public class PackageArticlesMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("PackageArticles");
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation("template");
		MARKUP.addAnnotation("exclude");
	}

	public PackageArticlesMarkup() {
		super(MARKUP);
		this.setRenderer(new Renderer() {
			@Override
			public void render(Section<?> section, UserContext user, RenderResult string) {
				Collection<Section<?>> packageSections = KnowWEUtils.getPackageManager(section)
						.getSectionsOfPackage(section.getPackageNames().toArray(new String[0]));

				//annotation template
				String prefix = "*";
				String suffix = "";
				boolean replace = false;
				String oldString = "";
				String newString = "";

				String template = DefaultMarkupType.getAnnotation(section, "template");
				if (template != null) {
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
							replace = true;

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
				}

				final String prefixFinal = prefix;
				final String suffixFinal = suffix;
				final String oldStringFinal;
				final String newStringFinal;

				if (replace) {
					oldStringFinal = oldString;
					newStringFinal = newString;
				}
				else {
					oldStringFinal = "";
					newStringFinal = "";
				}

				packageSections.stream()
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
			}
		});
	}

	private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
