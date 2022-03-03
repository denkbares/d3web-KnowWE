package de.knowwe.jspwiki;

import java.text.ParseException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.PartialHierarchyException;
import com.denkbares.collections.PartialHierarchyTree;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.StringFragment;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Predicates;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Scope;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.util.Icon;

import static com.denkbares.strings.Strings.*;

/**
 * The markup takes a subset of articles and renders the links to the articles as a list. You may specify multiple
 * "selectors" for the articles, then the subset of articles is listed that matches all the selectors. The selectors are
 * specified as annotations. If there are multiple definition of the same selector, they are combined by "OR", using the
 * union-set of articles.
 * <p>
 * the selectors are:
 * <ul>
 * <li>article: regular expression for the article title</li>
 * <li>exclude: regular expression to exclude articles by their title</li>
 * <li>package: package-name, to include all articles that are in a specified package</li>
 * <li>markup: {@link Scope}-definition of the markup to be searched, only articles are listed that contains the
 * denoted markup</li>
 * </ul>
 *
 * @author Tim Abler, Volker Belli (denkbares GmbH)
 * @created 10.10.2018
 */
public class LinkArticlesMarkup extends DefaultMarkupType {
	private static final Logger LOGGER = LoggerFactory.getLogger(LinkArticlesMarkup.class);

	private static final DefaultMarkup MARKUP;

	public static final String ANNOTATION_ARTICLE = "article";
	public static final String ANNOTATION_EXCLUDE = "exclude";
	public static final String ANNOTATION_TEMPLATE = "template";
	public static final String ANNOTATION_MARKUP = "markup";
	private static final String ANNOTATION_FRAME = "frame";
	private static final String ANNOTATION_HEADER = "header";
	private static final String ANNOTATION_PREFIX_REDUCTION = "prefixReduction";

	static {
		MARKUP = new DefaultMarkup("LinkArticles");
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(ANNOTATION_TEMPLATE);
		MARKUP.addAnnotation(ANNOTATION_EXCLUDE);
		MARKUP.addAnnotationRenderer(ANNOTATION_EXCLUDE, StyleRenderer.ANNOTATION);
		MARKUP.addAnnotation(ANNOTATION_MARKUP);
		MARKUP.addAnnotation(ANNOTATION_ARTICLE);
		MARKUP.addAnnotation(ANNOTATION_PREFIX_REDUCTION, false, "true", "false");
		MARKUP.getAnnotation(ANNOTATION_PREFIX_REDUCTION)
				.setDocumentation("Shortens the displayed page title if the title form a prefix hierarchy, that is, the title of the child article has the title of its parent article as prefix.");
		MARKUP.addAnnotation(ANNOTATION_FRAME);
		MARKUP.addAnnotationRenderer(ANNOTATION_FRAME, NothingRenderer.getInstance());
		MARKUP.addAnnotation(ANNOTATION_HEADER);
		MARKUP.addAnnotationRenderer(ANNOTATION_HEADER, NothingRenderer.getInstance());
		MARKUP.getAnnotation(PackageManager.PACKAGE_ATTRIBUTE_NAME)
				.setDocumentation("Specify a package name to show all articles which declare this package.");
		MARKUP.getAnnotation(ANNOTATION_ARTICLE)
				.setDocumentation("Specify a regex regarding the article names to show in this list.");
		MARKUP.getAnnotation(ANNOTATION_EXCLUDE).setDocumentation("Specify a regex regarding the article names to " +
				"exclude from this list, even if they are part of the package, the markup or the mentioned titles.");
		MARKUP.getAnnotation(ANNOTATION_MARKUP)
				.setDocumentation("Specify the name of a markup to show all articles it's used in.");
		MARKUP.getAnnotation(ANNOTATION_TEMPLATE)
				.setDocumentation("Set a template how each Link-Label should be adapted, e.g.\n"
						+ "@template: * link.replace(\"\u00ABregex\u00BB\", \"\u00ABreplacement\u00BB\")");
		MARKUP.getAnnotation(ANNOTATION_FRAME)
				.setDocumentation("Specify whether the markup has a frame that turns grey when mouse is over.");
		MARKUP.getAnnotation(ANNOTATION_HEADER)
				.setDocumentation("Specify whether the header (containing the tools etc.) is rendered or not.");
		MARKUP.setTemplate("%%LinkArticles \n" +
				"@package: \u00ABpackage-name\u00BB\n" +
				"@markup: \u00ABmarkup-name\u00BB\n" +
				"@article: \u00ABarticle-name-regex\u00BB\n" +
				"@exclude: \u00ABarticle-name-regex\u00BB\n" +
				"%");
	}

	public LinkArticlesMarkup() {
		super(MARKUP);
		this.setRenderer((section, user, out) ->
				new RenderWorker(Sections.cast(section, LinkArticlesMarkup.class)).render(Sections.cast(section, LinkArticlesMarkup.class), user, out));
	}

	private static final class RenderWorker extends DefaultMarkupRenderer {
		private static final Logger LOGGER = LoggerFactory.getLogger(RenderWorker.class);

		private final Section<LinkArticlesMarkup> section;
		private final Collection<Message> messages = new LinkedHashSet<>();
		private boolean prefixReduction = false;

		private RenderWorker(Section<LinkArticlesMarkup> section) {
			this.section = section;
			setRenderOptions(section);
		}

		private void setRenderOptions(Section<LinkArticlesMarkup> section) {
			String renderHeader = DefaultMarkupType.getAnnotation(section, ANNOTATION_HEADER);
			setRenderHeader(!"false".equals(renderHeader));
			String renderFrame = DefaultMarkupType.getAnnotation(section, ANNOTATION_FRAME);
			setFramed(!"false".equals(renderFrame));
			String prefixReductionValue = DefaultMarkupType.getAnnotation(section, ANNOTATION_PREFIX_REDUCTION);
			prefixReduction = "true".equals(prefixReductionValue);
		}

		@Override
		public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult result) {
			// prepare articles
			Stream<Article> articles = applyPackage();
			articles = applyArticle(articles);
			articles = applyExclude(articles);

			// prepare linked sections
			List<Section<?>> sections = applyMarkup(articles).collect(Collectors.toList());
			if (sections.isEmpty()) {
				result.appendHtmlTag("span", "style", "color: #888")
						.append("-- (no entries)").appendHtmlTag("/span");
			}

			// prepare template
			String template = DefaultMarkupType.getAnnotation(section, ANNOTATION_TEMPLATE);
			TemplateResult templateResult = new TemplateResult(template, prefixReduction);
			try {
				templateResult.compile();
			}
			catch (ParseException e) {
				messages.add(Messages.warning(e.getMessage()));
			}

			// and finally render messages and links
			result.appendHtml("<div>\n");
			DefaultMarkupRenderer.renderMessageBlock(result, messages);
			sections.sort(Comparator.comparing(Section::getTitle, NumberAwareComparator.CASE_INSENSITIVE));

			// for the prefix reduction mode, we need to create the hierarchy of articles
			final PartialHierarchyTree<String> tree = new PartialHierarchyTree<>(String::startsWith);
			if (this.prefixReduction) {
				// prepare prefix hierarchy
				sections.stream().map(s -> s.getArticle().getTitle()).forEach(t -> {
					try {
						tree.insert(t);
					}
					catch (PartialHierarchyException e) {
						LOGGER.error(e.getMessage());
					}
				});
			}

			// create links
			sections.forEach(sec -> templateResult.appendLink(result, sec, tree));
			result.appendHtml("</div>");
			super.renderContentsAndAnnotations(section, user, result);
		}

		private Stream<Article> applyPackage() {
			// as packages are applied first, we do not need to take care of the existing stream
			String[] packageNames = DefaultMarkupType.getAnnotations(section, PackageManager.PACKAGE_ATTRIBUTE_NAME);
			if (packageNames.length == 0) {
				// if package(s) is not specified, use all articles
				return KnowWEUtils.getArticleManager(section.getWeb()).getArticles().stream();
			}

			// otherwise collect articles from package manager
			return Stream.of(packageNames)
					// map package name(s) to sections
					.flatMap(this::resolvePackage)
					// map to disjoint articles
					.map(Section::getArticle)
					.filter(Predicates.distinctByKey(Article::getTitle));
		}

		private Stream<Section<?>> resolvePackage(String packageName) {
			PackageManager packageManager = KnowWEUtils.getPackageManager(section);
			if (!packageManager.getAllPackageNames().contains(packageName)) {
				messages.add(Messages.error("Package not known: " + packageName));
			}
			return Sections.registrations(Compilers.getPackageRegistrationCompiler(section),
					new Identifier(packageName)).map(s -> (Section<?>) s);
		}

		private Stream<Section<?>> applyMarkup(Stream<Article> source) {
			// check if there are any markups specified
			String[] markups = DefaultMarkupType.getAnnotations(section, ANNOTATION_MARKUP);
			if (markups.length == 0) {
				// if not, use the plain articles
				return source.map(Article::getRootSection);
			}

			// otherwise link the first matched section of each article
			List<Scope> scopes = Stream.of(markups).map(Scope::getScope).collect(Collectors.toList());
			return source.<Section<?>>map(article -> findMarkup(article, scopes)).filter(Objects::nonNull);
		}

		private Section<?> findMarkup(Article article, List<Scope> scopes) {
			return scopes.stream()
					.flatMap(scope -> scope.getMatchingSuccessors(article.getRootSection()).stream())
					.findFirst().orElse(null);
		}

		private Stream<Article> applyArticle(Stream<Article> source) {
			// filter exiting article stream if any title regexes are specified
			List<Pattern> patterns = getPatterns(ANNOTATION_ARTICLE);
			if (patterns.isEmpty()) return source;
			return source.filter(article -> patterns.stream().anyMatch(pattern ->
					pattern.matcher(article.getTitle()).matches()));
		}

		private Stream<Article> applyExclude(Stream<Article> source) {
			// filter exiting article stream if any title exclusions are specified
			List<Pattern> patterns = getPatterns(ANNOTATION_EXCLUDE);
			if (patterns.isEmpty()) return source;
			return source.filter(article -> patterns.stream().noneMatch(pattern ->
					pattern.matcher(article.getTitle()).matches()));
		}

		private List<Pattern> getPatterns(String annotationName) {
			String[] annotations = DefaultMarkupType.getAnnotations(section, annotationName);
			List<Pattern> regexes = new LinkedList<>();
			for (String annotation : annotations) {
				for (StringFragment fragment : splitUnquoted(annotation, ",", false)) {
					String regex = fragment.getContentTrimmed();
					try {
						regexes.add(Pattern.compile(unquote(trim(regex))));
					}
					catch (PatternSyntaxException e) {
						messages.add(Messages.error("Invalid regular expression: " + regex));
					}
				}
			}
			return regexes;
		}
	}

	private static class TemplateResult {

		private String template;
		private String prefix;
		private String oldString;
		private String newString;
		private final boolean prefixReduction;

		TemplateResult(String template, boolean prefixReduction) {
			this.template = template;
			this.prefixReduction = prefixReduction;
		}

		public void compile() throws ParseException {
			//annotation template
			prefix = "*";
			oldString = "";
			newString = "";
			if (Strings.isBlank(template)) return;

			if (template.contains("link")) {
				if (template.matches(".* link.*")) {
					prefix = template.substring(0, template.indexOf(" link"));
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
						throw new ParseException("The replace method can only handle two arguments!", 0);
					}
				}
			}
		}

		void appendLink(RenderResult out, Section<?> currentSection, PartialHierarchyTree<String> tree) {
			String currentTitle = currentSection.getTitle();
			if (currentTitle == null) return;
			boolean off = Strings.startsWithIgnoreCase(currentSection.getText(), "%%off:");

			out.append(prefix);
			out.appendHtmlTag("a", "href", KnowWEUtils.getURLLink(currentSection));
			if (off) out.appendHtmlTag("span", "style", "color: #888; text-decoration: line-through");
			String displayTitleCurrent = getDisplayTitle(currentTitle);

			// handle prefix reduction
			if (this.prefixReduction) {
				// we are in prefix reduction mode, hence we need to find the node, its parent and its depth
				PartialHierarchyTree.Node<String> currentTreeNode = tree.find(currentTitle);
				Collection<PartialHierarchyTree.Node<String>> parents = currentTreeNode.getParents();
				if (parents.size() > 1) {
					throw new IllegalStateException("We may only have one parent here as article names are unique!");
				}
				String parentTitle = ""; // a top level element does not have a parent
				if (parents.size() == 1) {
					PartialHierarchyTree.Node<String> parent = parents.iterator().next();
					parentTitle = parent.getData();
				}
				displayTitleCurrent = createIndentation(tree.getMaxDepthLevel(currentTreeNode))
						+ getDisplayTitle(displayTitleCurrent).substring(getDisplayTitle(parentTitle).length()).trim();
			}

			// actually render the title
			out.append(displayTitleCurrent);
			if (off) out.appendHtmlTag("/span");
			out.appendHtmlTag("/a");

			// render if the section is erroneous
			if (currentSection.get() instanceof DefaultMarkupType) {
				int errors = DefaultMarkupRenderer.getMessageStrings(currentSection, Message.Type.ERROR, null).size();
				int warnings = errors > 0 ? 0 :
						DefaultMarkupRenderer.getMessageStrings(currentSection, Message.Type.WARNING, null).size();
				if (errors > 0) {
					out.append(" (")
							.appendHtml(Icon.ERROR.toHtml())
							.append(" ")
							.append(Strings.pluralOf(errors, "error"))
							.append(")");
				}
				if (warnings > 0) {
					out.append(" (")
							.appendHtml(Icon.WARNING.toHtml())
							.append(" ")
							.append(Strings.pluralOf(warnings, "warning"))
							.append(")");
				}
			}
			out.append("\n");
		}

		private String createIndentation(int indentationLevel) {
			// indent via underscore
			return "-".repeat(Math.max(0, indentationLevel));
		}

		@NotNull
		private String getDisplayTitle(@NotNull String title) {
			return title.replaceAll(oldString, newString);
		}
	}
}
