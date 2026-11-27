/*
 * Copyright (C) 2021 denkbares GmbH, Germany
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

package de.knowwe.include;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.apache.wiki.util.CsrfProtectionAllowList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.knowwe.textdiff.DiffHtmlRenderer;
import com.denkbares.knowwe.textdiff.DiffRenderOptions;
import com.denkbares.knowwe.textdiff.DiffView;
import com.denkbares.knowwe.textdiff.TextDiff;
import com.denkbares.plugin.PluginManager;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import com.denkbares.utils.Streams;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.variables.InterWikiVariableMarkup;
import de.knowwe.core.compile.variables.InterWikiVariableReference;
import de.knowwe.core.compile.variables.VariableIndex;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.elements.A;
import de.knowwe.core.kdom.rendering.elements.Div;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.HtmlNode;
import de.knowwe.core.kdom.rendering.elements.HtmlProvider;
import de.knowwe.core.kdom.rendering.elements.P;
import de.knowwe.core.kdom.rendering.elements.PlainTextNode;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.kdom.rendering.elements.TextNode;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.tools.HelpToolProvider;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.jspwiki.ActionAllowListChecker;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsyncPreviewRenderer;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.tools.Tool;
import de.knowwe.util.Color;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Allows including pages a from other wikis by specifying domain, page and optionally title
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 06.12.21
 */
public class InterWikiImportMarkup extends AttachmentUpdateMarkup implements AttachmentCompileType, IncludingType {

	private static final String WIKI_ANNOTATION = "wiki";
	private static final String WIKI_VARIABLE_ANNOTATION = "wikiVariable";
	private static final String PAGE_ANNOTATION = "page";
	private static final String SECTION_ANNOTATION = "section";
	private static final String COMPILE_ANNOTATION = "compile";
	private static final String MODE_ANNOTATION = "mode";
	private static final String VALIDATION_MODE_ANNOTATION = "validationMode";
	private static final String LATEST_CHANGE_ANNOTATION = "latestChange";
	private static final String TRACKING_ACCEPTED_AT_ANNOTATION = "trackingAcceptedAt";

	/**
	 * Separates the key of the source wiki from the name of the attachment, for imports using a variable. These have
	 * one attachment per source wiki.
	 */
	private static final String SOURCE_SEPARATOR = "@";

	/** Maximum number of entries of the "Compare with source" selection, including the complete comparison. */
	private static final int MAX_DIFF_OPTIONS = 10;

	/**
	 * The update service of the environment the section belongs to.
	 */
	private static InterWikiImportUpdateService updateService(Section<?> section) {
		return InterWikiImportUpdateService.of(section.getEnvironment());
	}

	private static final DefaultMarkup MARKUP = new DefaultMarkup("InterWikiImport");

	/**
	 * Last sync-failure message per wiki attachment path, so the warning survives article
	 * recompiles (which discard section messages and rerun the compile scripts). Keyed by the
	 * stable attachment path, analogous to {@link AttachmentUpdateMarkup}'s last-run tracking.
	 */
	private static final Map<String, String> LAST_SYNC_ERRORS = new ConcurrentHashMap<>();

	static {
		MARKUP.addAnnotation(INTERVAL_ANNOTATION, false);
		MARKUP.addAnnotationContentType(INTERVAL_ANNOTATION, () -> new TimeStampType());
		MARKUP.addAnnotation(COMPILE_ANNOTATION, false, "true", "false");
		MARKUP.addAnnotation(MODE_ANNOTATION, false, "import", "tracking");
		MARKUP.addAnnotation(REPLACEMENT, false, Pattern.compile(".+->(.|[\r\n])*"));
		MARKUP.addAnnotation(REGEX_REPLACEMENT, false, Pattern.compile(".+->(.|[\r\n])*"));
		MARKUP.addAnnotation(WIKI_ANNOTATION, false);
		MARKUP.addAnnotation(WIKI_VARIABLE_ANNOTATION, false, Pattern.compile("[\\w.-]+"));
		MARKUP.addAnnotationContentType(WIKI_VARIABLE_ANNOTATION, InterWikiVariableReference::new);
		MARKUP.addAnnotation(PAGE_ANNOTATION, true);
		MARKUP.addAnnotation(SECTION_ANNOTATION, false);
		MARKUP.addAnnotation(VALIDATION_MODE_ANNOTATION, false, TermCompiler.ReferenceValidationMode.class);
		MARKUP.addAnnotation(LATEST_CHANGE_ANNOTATION, false);
		MARKUP.addAnnotation(TRACKING_ACCEPTED_AT_ANNOTATION, false);
		PackageManager.addPackageAnnotation(MARKUP);
		// the source wiki must accept the change polls of other wikis, also if it has no imports of its own and
		// therefore never starts an update service; the allowlist is global, so register it once with the type;
		// without plugins there are no actions to be called (e.g. in tests running below the plugin layer)
		if (PluginManager.isInitialized()) {
			CsrfProtectionAllowList.register(new ActionAllowListChecker(GetWikiChangesSinceAction.class));
		}
	}

	public InterWikiImportMarkup() {
		super(MARKUP);
		addCompileScript(new SourceAnnotationCheck());
		addCompileScript(new RegistrationScript());
		setRenderer(new AsynchronousRenderer(new InterWikiImportRenderer()));
	}

	@Nullable
	@Override
	public WikiAttachment getWikiAttachment(Section<? extends AttachmentUpdateMarkup> section) throws IOException {
		if (section == null) return null;
		Section<InterWikiImportMarkup> interWikiSection = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (interWikiSection == null) return null;
		String path = getWikiAttachmentPath(interWikiSection);
		if (path == null) return null;
		return section.getEnvironment().getWikiConnector().getAttachment(path);
	}

	/**
	 * Returns the attachment with the content imported from the given wiki, or null if there is none (yet).
	 */
	@Nullable
	WikiAttachment getWikiAttachment(Section<InterWikiImportMarkup> section, @Nullable String wiki) throws IOException {
		String path = getWikiAttachmentPath(section, wiki);
		if (path == null) return null;
		return section.getEnvironment().getWikiConnector().getAttachment(path);
	}

	/**
	 * Whether there is an attachment with content imported from the given wiki. If this cannot be found out, the
	 * attachment is assumed to be there.
	 */
	boolean hasWikiAttachment(Section<InterWikiImportMarkup> section, @Nullable String wiki) {
		try {
			return getWikiAttachment(section, wiki) != null;
		}
		catch (IOException e) {
			return true;
		}
	}

	/**
	 * Returns the path of the attachment of the given import, if there is exactly one. An import using a variable has
	 * one attachment per source wiki. If its variable has several values, or none, null is returned; use
	 * {@link #getWikiAttachmentPath(Section, String)} or {@link #getWikiAttachmentPath(Section, UserContext)} then.
	 */
	@Override
	public String getWikiAttachmentPath(Section<? extends AttachmentUpdateMarkup> section) {
		Section<InterWikiImportMarkup> interWikiSection = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (interWikiSection == null) return null;
		if (getWikiVariable(interWikiSection) == null) return getAttachmentBasePath(interWikiSection) + ".txt";
		return getWikiAttachmentPath(interWikiSection, getWiki(interWikiSection));
	}

	/**
	 * Returns the path of the attachment with the content imported from the given wiki. For imports with
	 * <code>@wiki</code> the path does not depend on the wiki; for imports using a variable null is returned if no
	 * wiki is given.
	 */
	@Nullable
	String getWikiAttachmentPath(Section<InterWikiImportMarkup> section, @Nullable String wiki) {
		String basePath = getAttachmentBasePath(section);
		if (getWikiVariable(section) == null) return basePath + ".txt";
		if (Strings.isBlank(wiki)) return null;
		return basePath + SOURCE_SEPARATOR + InterWikiImportAnnotationFormat.toSourceKey(wiki) + ".txt";
	}

	/**
	 * Returns the path of the attachment with the content imported for the variant currently selected by the user,
	 * or null if nothing is imported for that variant.
	 */
	@Nullable
	String getWikiAttachmentPath(Section<InterWikiImportMarkup> section, UserContext user) {
		if (getWikiVariable(section) == null) return getAttachmentBasePath(section) + ".txt";
		return getWikiAttachmentPath(section, getWiki(section, user));
	}

	/**
	 * The path of the attachment without its extension and, for imports using a variable, without the source wiki.
	 */
	private String getAttachmentBasePath(Section<InterWikiImportMarkup> section) {
		String pageName = "-" + getPageName(section);
		String sectionName = getSectionName(section);
		sectionName = sectionName == null ? "" : "-" + sectionName;
		return section.getTitle() + PATH_SEPARATOR + "WikiImport" + pageName + sectionName;
	}

	/**
	 * The start of the paths of all attachments of the given import using a variable, whatever the source wiki is.
	 */
	String getSourceAttachmentPathPrefix(Section<InterWikiImportMarkup> section) {
		return getAttachmentBasePath(section) + SOURCE_SEPARATOR;
	}

	@Override
	public TermCompiler.ReferenceValidationMode getReferenceValidationMode(Section<? extends AttachmentCompileType> section) {
		return $(section).closest(DefaultMarkupType.class)
				.map(s -> DefaultMarkupType.getAnnotation(s, VALIDATION_MODE_ANNOTATION))
				.filter(Objects::nonNull)
				.findFirst().map(TermCompiler.ReferenceValidationMode::valueOf)
				.orElse(TermCompiler.ReferenceValidationMode.error);
	}

	String getPageName(Section<InterWikiImportMarkup> section) {
		return DefaultMarkupType.getAnnotation(section, PAGE_ANNOTATION);
	}

	String getSectionName(Section<InterWikiImportMarkup> section) {
		return DefaultMarkupType.getAnnotation(section, SECTION_ANNOTATION);
	}

	public Mode getMode(Section<InterWikiImportMarkup> section) {
		String mode = Strings.trim(DefaultMarkupType.getAnnotation(section, MODE_ANNOTATION));
		if ("tracking".equalsIgnoreCase(mode)) {
			return Mode.TRACKING;
		}
		return Mode.IMPORT;
	}

	public boolean isTrackingMode(Section<?> markupOrSuccessor) {
		Section<InterWikiImportMarkup> markup = $(markupOrSuccessor).closest(InterWikiImportMarkup.class).getFirst();
		return markup != null && getMode(markup) == Mode.TRACKING;
	}

	/**
	 * In import mode, the root section of the article imported for the variant of the user, as rendered below the
	 * markup.
	 */
	@Override
	public @NotNull List<Section<?>> getIncludedSections(Section<?> section, UserContext user) {
		Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (markup == null || getMode(markup) == Mode.TRACKING) return List.of();
		String path = getWikiAttachmentPath(markup, user);
		Article article = path == null ? null : user.getArticleManager().getArticle(path);
		return article == null ? List.of() : List.of(article.getRootSection());
	}

	@Override
	public @Nullable WikiAttachment getCompiledAttachment(Section<? extends AttachmentCompileType> section) throws IOException {
		if (isCompilingTheAttachment(section)) {
			return getWikiAttachment($(section).closest(AttachmentUpdateMarkup.class).getFirst());
		}
		return null;
	}

	@Override
	public String getCompiledAttachmentPath(Section<? extends AttachmentCompileType> section) {
		if (isTrackingMode(section)) return null;
		return $(section).closest(AttachmentUpdateMarkup.class).mapFirst(markup -> getWikiAttachmentPath(markup));
	}

	@Override
	public @NotNull Collection<String> getCompiledAttachmentPaths(Section<? extends AttachmentCompileType> section) {
		Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (markup == null || isTrackingMode(markup)) return List.of();
		if (getWikiVariable(markup) == null) return List.of(getAttachmentBasePath(markup) + ".txt");
		// one attachment per source wiki, each compiled by the variants importing from that wiki
		return getWikis(markup).stream().map(wiki -> getWikiAttachmentPath(markup, wiki)).distinct().toList();
	}

	/**
	 * For an import using a variable, the attachment of a source wiki is only compiled by the compilers that also
	 * compile a definition giving the variable that wiki as its value, i.e. by the variants importing from it.
	 */
	@Override
	public @Nullable Collection<? extends Section<?>> getCompileConditionSections(Section<? extends AttachmentCompileType> section, String path) {
		Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (markup == null) return null;
		String variable = getWikiVariable(markup);
		if (variable == null) return null;
		VariableIndex index = VariableIndex.get(markup);
		if (index == null) return List.of();
		List<Section<InterWikiVariableMarkup>> definitions = index.getDefinitions(variable);
		definitions.removeIf(definition -> !path.equals(
				getWikiAttachmentPath(markup, normalizeWiki(InterWikiVariableMarkup.getVariableValue(definition)))));
		return definitions;
	}

	@Override
	public boolean isCompilingTheAttachment(Section<? extends AttachmentCompileType> section) {
		if (isTrackingMode(section)) return false;
		return !"false".equals(DefaultMarkupType.getAnnotation(section, COMPILE_ANNOTATION));
	}

	@Override
	public Icon getImportIcon(Section<? extends AttachmentCompileType> section) {
		return Icon.FILE_IMPORT;
	}

	@Override
	public @Nullable URL getUrl(Section<? extends AttachmentUpdateMarkup> section) {
		return getUrl(section, getActionFragment() + "/GetWikiSectionTextAction?reference=", true);
	}

	@Nullable
	private URL getUrl(Section<? extends AttachmentUpdateMarkup> sec, String command, boolean params) {
		Section<InterWikiImportMarkup> section = $(sec).closest(InterWikiImportMarkup.class).getFirst();
		if (section == null) return null;
		return getUrl(section, getWiki(section), command, params);
	}

	@Nullable
	private URL getUrl(Section<InterWikiImportMarkup> section, @Nullable String wiki, String command, boolean params) {
		String wikiAnnotation = normalizeWiki(wiki);

		String pageName = section.get().getPageName(section);
		String reference = Strings.encodeURL(pageName);
		String sectionName = section.get().getSectionName(section);
		if (sectionName != null) {
			reference += Strings.encodeURL("#" + sectionName);
		}

		String fromParam = "&" + ImportMarker.REQUEST_FROM + "=" + Strings.encodeURL(getImportSourceLabel(section));
		String linkParam = "&" + ImportMarker.REQUEST_LINK + "=" + Strings.encodeURL(getImportSourceLink(section));

		String url = wikiAnnotation + command + reference + (params ? fromParam + linkParam : "");

		try {
			return new URL(url);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Label identifying this importing wiki and article, shown on the source page's "imported by" marker.
	 * Sent both as the {@code requestFrom} URL parameter (single fetch) and in the bulk poll request body.
	 */
	String getImportSourceLabel(Section<InterWikiImportMarkup> section) {
		String linkLabel = section.getEnvironment().getWikiConnector().getApplicationName();
		if (Strings.isBlank(linkLabel) || "knowwe".equalsIgnoreCase(linkLabel)) {
			linkLabel = "another wiki";
		}
		return linkLabel + ": " + section.getTitle();
	}

	/** Absolute link back to this importing section, shown on the source page's "imported by" marker. */
	String getImportSourceLink(Section<InterWikiImportMarkup> section) {
		return KnowWEUtils.getAsAbsoluteLink(section.getEnvironment(), KnowWEUtils.getURLLink(section));
	}

	/**
	 * Returns the wiki the given import imports from, if there is exactly one: the wiki given with
	 * <code>@wiki</code>, or the value of the variable given with <code>@wikiVariable</code> if it has the same value
	 * in the whole wiki. Returns null if no wiki is given or if the variable has several values or none.
	 *
	 * @see #getWikis(Section)
	 * @see #getWiki(Section, UserContext)
	 */
	@Nullable
	String getWiki(Section<InterWikiImportMarkup> section) {
		if (getWikiVariable(section) == null) {
			return Strings.trim(DefaultMarkupType.getAnnotation(section, WIKI_ANNOTATION));
		}
		List<String> wikis = getWikis(section);
		return wikis.size() == 1 ? wikis.get(0) : null;
	}

	/**
	 * Returns the name of the variable the given import takes its source wiki from, or null if it does not use one.
	 */
	@Nullable
	String getWikiVariable(Section<InterWikiImportMarkup> section) {
		String variable = Strings.trim(DefaultMarkupType.getAnnotation(section, WIKI_VARIABLE_ANNOTATION));
		return Strings.isBlank(variable) ? null : variable;
	}

	/**
	 * Returns all wikis the given import imports from, normalized: the one given with <code>@wiki</code>, or all
	 * values the variable given with <code>@wikiVariable</code> has in this wiki, whatever the variant is. The
	 * values are read from the {@link VariableIndex}, so they are already available while the wiki starts.
	 */
	@NotNull
	List<String> getWikis(Section<InterWikiImportMarkup> section) {
		String variable = getWikiVariable(section);
		if (variable == null) {
			String wiki = normalizeWiki(DefaultMarkupType.getAnnotation(section, WIKI_ANNOTATION));
			return wiki.isBlank() ? List.of() : List.of(wiki);
		}
		VariableIndex index = VariableIndex.get(section);
		if (index == null) return List.of();
		return index.getValues(variable).stream()
				.map(InterWikiImportMarkup::normalizeWiki)
				.filter(wiki -> !wiki.isBlank())
				.distinct().toList();
	}

	/**
	 * Returns the definitions of the variable of the given import that are valid for the variant currently selected
	 * by the user. The variant is given by the compilers selected by the user that compile the import: the
	 * definitions they compile count, so a variant may have none, even if all other variants agree on a value. Only
	 * if the import is not compiled by any package compiler, there are no variants to distinguish and all
	 * definitions of the variable are returned.
	 */
	@NotNull
	List<Section<InterWikiVariableMarkup>> getVariableDefinitions(Section<InterWikiImportMarkup> section, UserContext user) {
		String variable = getWikiVariable(section);
		VariableIndex index = VariableIndex.get(section);
		if (variable == null || index == null) return List.of();
		Collection<PackageCompiler> compilers = Compilers.getCompilers(section, PackageCompiler.class);
		if (compilers.isEmpty()) return index.getDefinitions(variable);

		LinkedHashSet<Section<InterWikiVariableMarkup>> definitions = new LinkedHashSet<>();
		for (PackageCompiler compiler : compilers) {
			if (!Compilers.isDefaultCompiler(user, compiler)) continue;
			definitions.addAll(index.getDefinitions(variable, compiler));
		}
		return List.copyOf(definitions);
	}

	/**
	 * Returns the name to show for the source of the given import in the variant currently selected by the user:
	 * the display name of the definition of the variable that is valid for the variant, or the name of the variable
	 * if the definition has none (or the variant no definition). Null for imports not using a variable.
	 */
	@Nullable
	String getSourceDisplayName(Section<InterWikiImportMarkup> section, UserContext user) {
		String variable = getWikiVariable(section);
		if (variable == null) return null;
		for (Section<InterWikiVariableMarkup> definition : getVariableDefinitions(section, user)) {
			String displayName = InterWikiVariableMarkup.getDisplayName(definition);
			if (displayName != null) return displayName;
		}
		return variable;
	}

	/**
	 * Returns the name to show for the given source wiki of the given import: the display name of a definition
	 * giving the variable of the import this wiki as its value, or the wiki itself without protocol if there is
	 * none.
	 */
	@NotNull
	String getWikiDisplayName(Section<InterWikiImportMarkup> section, String wiki) {
		String variable = getWikiVariable(section);
		VariableIndex index = VariableIndex.get(section);
		if (variable != null && index != null) {
			for (Section<InterWikiVariableMarkup> definition : index.getDefinitions(variable)) {
				if (!wiki.equals(normalizeWiki(InterWikiVariableMarkup.getVariableValue(definition)))) continue;
				String displayName = InterWikiVariableMarkup.getDisplayName(definition);
				if (displayName != null) return displayName;
			}
		}
		return InterWikiImportAnnotationFormat.formatWiki(wiki);
	}

	/**
	 * Returns the wikis the given import imports from for the variant currently selected by the user, normalized.
	 * A properly configured import has exactly one. For an import using a variable these are the values of the
	 * definitions valid for that variant, see {@link #getVariableDefinitions(Section, UserContext)}.
	 */
	@NotNull
	List<String> getWikis(Section<InterWikiImportMarkup> section, UserContext user) {
		if (getWikiVariable(section) == null) return getWikis(section);
		return getVariableDefinitions(section, user).stream()
				.map(definition -> normalizeWiki(InterWikiVariableMarkup.getVariableValue(definition)))
				.filter(wiki -> !wiki.isBlank())
				.distinct().toList();
	}

	/**
	 * Returns the wiki the given import imports from for the variant currently selected by the user, or null if
	 * there is none or if it is ambiguous.
	 */
	@Nullable
	String getWiki(Section<InterWikiImportMarkup> section, UserContext user) {
		if (getWikiVariable(section) == null) return getWiki(section);
		List<String> wikis = getWikis(section, user);
		return wikis.size() == 1 ? wikis.get(0) : null;
	}

	/**
	 * Returns two different source wikis of the given import that would use the same attachment, or null if there
	 * are none, which is the usual case.
	 */
	@Nullable
	String[] getSourceKeyConflict(Section<InterWikiImportMarkup> section) {
		List<String> wikis = getWikis(section);
		for (int i = 0; i < wikis.size(); i++) {
			for (int j = i + 1; j < wikis.size(); j++) {
				if (InterWikiImportAnnotationFormat.toSourceKey(wikis.get(i))
						.equals(InterWikiImportAnnotationFormat.toSourceKey(wikis.get(j)))) {
					return new String[] { wikis.get(i), wikis.get(j) };
				}
			}
		}
		return null;
	}

	/**
	 * Explains why nothing can be imported for the variant currently selected by the user, for an import using a
	 * variable, or returns null if there is a source wiki for that variant.
	 */
	@Nullable
	String getMissingSourceMessage(Section<InterWikiImportMarkup> section, UserContext user) {
		String variable = getWikiVariable(section);
		if (variable == null) return null;
		String[] conflict = getSourceKeyConflict(section);
		if (conflict != null) {
			return "The wikis " + conflict[0] + " and " + conflict[1] + " of the variable '" + variable
					+ "' cannot be distinguished by the import. Nothing is imported until this is changed.";
		}
		if (getWikis(section).isEmpty()) {
			return "The variable '" + variable + "' is not defined, so nothing is imported. "
					+ "Define it with the markup InterWikiVariable.";
		}
		List<String> wikis = getWikis(section, user);
		if (wikis.isEmpty()) {
			return "The variable '" + variable + "' has no value for the currently selected variant, "
					+ "so nothing is imported for it.";
		}
		if (wikis.size() > 1) {
			return "The variable '" + variable + "' has several values for the currently selected variant: "
					+ String.join(", ", wikis) + ". Each variant must have exactly one.";
		}
		return null;
	}

	/**
	 * @see InterWikiImportAnnotationFormat#normalizeWiki(String)
	 */
	static String normalizeWiki(String wiki) {
		return InterWikiImportAnnotationFormat.normalizeWiki(wiki);
	}

	/**
	 * The content of a <code>@latestChange</code> annotation: the time of the latest change in the source and, for
	 * imports using a variable, the (normalized) source wiki it belongs to. These have one annotation per source
	 * wiki, written as <code>@latestChange: 2026-11-16T09:30:00Z https://wiki.example.com/</code>.
	 */
	record LatestChange(Instant instant, @Nullable String wiki) {

		@Nullable
		static LatestChange parse(@Nullable String text) {
			String trimmed = Strings.trim(text);
			if (Strings.isBlank(trimmed)) return null;
			String[] parts = trimmed.split("\\s+", 2);
			Instant instant = InterWikiChanges.parseInstant(parts[0]);
			if (instant == null) return null;
			return new LatestChange(instant, parts.length > 1 ? normalizeWiki(parts[1]) : null);
		}
	}

	/**
	 * Returns the latest change in the given source wiki that was imported by the given import. For imports with
	 * <code>@wiki</code> the wiki is not needed.
	 */
	@Nullable
	Instant getLatestChange(Section<InterWikiImportMarkup> section, @Nullable String wiki) {
		if (getWikiVariable(section) == null) {
			String latestChangeText = DefaultMarkupType.getAnnotation(section, LATEST_CHANGE_ANNOTATION);
			return InterWikiChanges.parseInstant(latestChangeText);
		}
		if (wiki == null) return null;
		for (Section<?> content : DefaultMarkupType.getAnnotationContentSections(section, LATEST_CHANGE_ANNOTATION)) {
			LatestChange latestChange = LatestChange.parse(content.getText());
			if (latestChange != null && wiki.equals(latestChange.wiki())) return latestChange.instant();
		}
		return null;
	}

	/**
	 * Collects the replacements that set <code>@latestChange</code> for the given source wiki. For imports using a
	 * variable this is the annotation of that wiki; annotations of wikis the import no longer imports from are
	 * removed along the way.
	 */
	void collectLatestChangeReplacement(Section<InterWikiImportMarkup> section, @Nullable String wiki, Instant latestChange, Map<String, String> replacements) {
		boolean usesVariable = getWikiVariable(section) != null;
		if (usesVariable && wiki == null) return;
		String text = usesVariable ? latestChange + " " + wiki : latestChange.toString();
		List<String> wikis = usesVariable ? getWikis(section) : List.of();

		Section<?> target = null;
		for (Section<?> content : DefaultMarkupType.getAnnotationContentSections(section, LATEST_CHANGE_ANNOTATION)) {
			LatestChange existing = LatestChange.parse(content.getText());
			String existingWiki = existing == null ? null : existing.wiki();
			boolean isTarget = target == null && (!usesVariable || wiki.equals(existingWiki));
			if (isTarget) {
				target = content;
			}
			else if (!usesVariable || existingWiki == null || wiki.equals(existingWiki) || !wikis.contains(existingWiki)) {
				// outdated: a duplicate, or of a wiki that is no source of this import (any more)
				Section<AnnotationType> annotation = $(content).ancestor(AnnotationType.class).getFirst();
				if (annotation != null) replacements.put(annotation.getID(), "");
			}
		}

		if (target != null) {
			replacements.put(target.getID(), text);
		}
		else {
			Section<?> closingTag = $(section).children().getLast();
			if (closingTag == null || !"%".equals(Strings.trim(closingTag.getText()))) return;
			// merge with a possibly existing replacement for this closing tag, e.g. of another source wiki
			String closingText = replacements.getOrDefault(closingTag.getID(), closingTag.getText());
			replacements.put(closingTag.getID(),
					"\n@" + LATEST_CHANGE_ANNOTATION + ": " + text + "\n" + Strings.trimLeft(closingText));
		}
	}

	boolean collectTrackingAcceptedAtReplacement(Section<InterWikiImportMarkup> section, Instant acceptedAt, Map<String, String> replacements) {
		SectionReplacement replacement = getTrackingAcceptedAtReplacement(section, acceptedAt);
		if (replacement == null) return false;
		replacements.put(replacement.section().getID(), replacement.text());
		return true;
	}

	/**
	 * Returns the replacement that sets {@code @trackingAcceptedAt}: either the content of the existing
	 * annotation, or the closing tag of the markup with the annotation inserted in front of it.
	 */
	@Nullable
	private SectionReplacement getTrackingAcceptedAtReplacement(Section<InterWikiImportMarkup> section, Instant acceptedAt) {
		Section<?> acceptedAtContent = DefaultMarkupType.getAnnotationContentSection(section, TRACKING_ACCEPTED_AT_ANNOTATION);
		if (acceptedAtContent != null) {
			return new SectionReplacement(acceptedAtContent, acceptedAt.toString());
		}

		Section<?> closingTag = $(section).children().getLast();
		if (closingTag == null || !"%".equals(Strings.trim(closingTag.getText()))) return null;
		return new SectionReplacement(closingTag,
				"\n@" + TRACKING_ACCEPTED_AT_ANNOTATION + ": " + acceptedAt
						+ "\n" + Strings.trimLeft(closingTag.getText()));
	}

	private record SectionReplacement(Section<?> section, String text) {
	}

	boolean shouldUpdateLatestChange(Section<InterWikiImportMarkup> section, boolean attachmentChanged) {
		return getSectionName(section) == null || attachmentChanged;
	}

	boolean updateAttachmentWithSourceText(Section<InterWikiImportMarkup> section, @Nullable String wiki, String sourceText) {
		if (section.getArticleManager() == null) return false;

		logLastRun(section);
		Messages.clearMessages(section, getClass());

		String path = getWikiAttachmentPath(section, wiki);
		if (path == null) return false;
		if (path.split("/").length > 2) {
			Messages.storeMessage(section, getClass(), Messages.error("Unable to update entries in zipped attachments!"));
			return false;
		}

		ReentrantLock lock = getLock(section);
		if (!lock.tryLock()) {
			return false;
		}

		try {
			byte[] sourceBytes = Streams.getBytesAndClose(applyReplacements(section,
					new ByteArrayInputStream(sourceText.getBytes(StandardCharsets.UTF_8))));
			WikiAttachment attachment = getWikiAttachment(section, wiki);
			if (attachment != null) {
				byte[] attachmentBytes = Streams.getBytesAndClose(attachment.getInputStream());
				if (java.util.Arrays.equals(sourceBytes, attachmentBytes)) {
					return false;
				}
				// tracking compares trimmed texts, so e.g. a different number of trailing line breaks (the
				// section is no longer the last one of the source page) must not create a new version
				if (isTrackingMode(section) && InterWikiTrackingService.normalizeForComparison(new String(sourceBytes, StandardCharsets.UTF_8))
						.equals(InterWikiTrackingService.normalizeForComparison(new String(attachmentBytes, StandardCharsets.UTF_8)))) {
					return false;
				}
			}

			String parentName = path.substring(0, path.indexOf(PATH_SEPARATOR));
			String fileName = path.substring(path.indexOf(PATH_SEPARATOR) + 1);
			section.getEnvironment().getWikiConnector()
					.storeAttachment(parentName, fileName, "SYSTEM", new ByteArrayInputStream(sourceBytes), isVersioning(section));
			return true;
		}
		catch (IOException e) {
			String message = e.getClass().getSimpleName() + " while trying to update attachment " + path;
			Messages.storeMessage(section, getClass(), Messages.error(message + ": " + e.getMessage()));
			throw new RuntimeException(message, e);
		}
		finally {
			lock.unlock();
		}
	}

	@Nullable
	String getTrackingReferenceText(Section<InterWikiImportMarkup> section) throws IOException {
		WikiAttachment attachment = getWikiAttachment(section);
		if (attachment == null) return null;
		return Streams.getTextAndClose(attachment.getInputStream());
	}

	@Nullable
	Instant getTrackingReferenceLastModified(Section<InterWikiImportMarkup> section) throws IOException {
		WikiAttachment attachment = getWikiAttachment(section);
		if (attachment == null || attachment.getDate() == null) return null;
		return attachment.getDate().toInstant();
	}

	/**
	 * Returns the reference text as it was when the tracking differences were acknowledged, i.e. the
	 * newest attachment version stored not after {@code acceptedAt}. Returns {@code null} if that
	 * version is not available (anymore), e.g. because the attachment was stored without versioning
	 * at that time.
	 */
	@Nullable
	String getTrackingAcceptedReferenceText(Section<InterWikiImportMarkup> section, Instant acceptedAt) throws IOException {
		return InterWikiTrackingService.findReferenceTextAt(getWikiAttachment(section), acceptedAt);
	}

	@Nullable
	Instant getTrackingAcceptedAt(Section<InterWikiImportMarkup> section) {
		String acceptedAt = DefaultMarkupType.getAnnotation(section, TRACKING_ACCEPTED_AT_ANNOTATION);
		return InterWikiChanges.parseInstant(acceptedAt);
	}

	@Nullable
	String getTrackingLocalComparisonText(Section<InterWikiImportMarkup> section) {
		int[] range = getLocalComparisonRange(section);
		if (range == null) return null;
		return section.getArticle().getText().substring(range[0], range[1]);
	}

	private int[] getLocalComparisonRange(Section<InterWikiImportMarkup> section) {
		Article article = section.getArticle();
		if (article == null) return null;
		String articleText = article.getText();
		if (articleText == null) return null;

		int sectionStart = section.getOffsetInArticle();
		int sectionEnd = sectionStart + section.getTextLength();

		OptionalInt nextInterWikiImportStart = $(article).successor(InterWikiImportMarkup.class)
				.stream()
				.mapToInt(Section::getOffsetInArticle)
				.filter(offset -> offset > sectionStart)
				.min();

		int compareStart = Math.max(0, Math.min(sectionEnd, articleText.length()));
		int compareEnd = Math.max(compareStart, Math.min(nextInterWikiImportStart.orElse(articleText.length()), articleText.length()));
		return new int[] { compareStart, compareEnd };
	}

	/**
	 * Replaces the local content below the markup with the given text and, if {@code acceptedAt} is
	 * given, sets {@code @trackingAcceptedAt} (if possible). Both changes are done in one replacement of
	 * the article text, as they cannot be combined with section replacements.
	 */
	boolean collectLocalContentReplacement(Section<InterWikiImportMarkup> section, String localText, @Nullable Instant acceptedAt, Map<String, String> replacements) {
		int[] range = getLocalComparisonRange(section);
		if (range == null) return false;

		Article article = section.getArticle();
		String articleText = article.getText();
		String textUpToLocalContent = articleText.substring(0, range[0]);
		// without a closing tag the annotation cannot be inserted, then replace without acknowledging
		SectionReplacement acceptedAtReplacement = acceptedAt == null ? null : getTrackingAcceptedAtReplacement(section, acceptedAt);
		if (acceptedAtReplacement != null) {
			int acceptedAtStart = acceptedAtReplacement.section().getOffsetInArticle();
			int acceptedAtEnd = acceptedAtStart + acceptedAtReplacement.section().getTextLength();
			textUpToLocalContent = articleText.substring(0, acceptedAtStart)
					+ acceptedAtReplacement.text()
					+ articleText.substring(acceptedAtEnd, range[0]);
		}
		// keep the line breaks around the local content, e.g. the empty line before a following markup
		String oldLocalText = articleText.substring(range[0], range[1]);
		String leading = "\n\n";
		String trailing = "\n";
		if (!Strings.isBlank(oldLocalText)) {
			leading = oldLocalText.substring(0, oldLocalText.length() - Strings.trimLeft(oldLocalText).length());
			trailing = oldLocalText.substring(Strings.trimRight(oldLocalText).length());
		}
		String newArticleText = textUpToLocalContent
				+ leading + Strings.trim(localText) + trailing
				+ articleText.substring(range[1]);
		replacements.put(article.getRootSection().getID(), newArticleText);
		return true;
	}

	void refreshNow(Section<InterWikiImportMarkup> section, boolean force) {
		updateService(section).pollSingleMarkup(section, force);
	}

	static String buildRefreshScript(String sectionId, boolean force) {
		return "(function(){"
				+ "jq$.ajax({"
				+ "url: KNOWWE.core.util.getURL({action:'RefreshInterWikiImportAction',"
				+ Attributes.SECTION_ID + ":'" + sectionId + "',"
				+ "force:'" + force + "'}),"
				+ "type:'post',"
				+ "cache:false"
				+ "}).done(function(){window.location.reload();})"
				+ ".fail(function(xhr){"
				+ "KNOWWE.notification.error(null,"
				+ "KNOWWE.plugin.include.errorMessage(xhr, 'Unable to refresh InterWikiImport.'),"
				+ "'iwii-refresh',5000);"
				+ "});"
				+ "})();";
	}

	void refreshTrackingMessages(Section<InterWikiImportMarkup> section) {
		Messages.clearMessages(section, TrackingMessages.class);
		if (getMode(section) != Mode.TRACKING) return;
		try {
			InterWikiTrackingService.TrackingStatus status = InterWikiTrackingService.getTrackingStatus(section);
			if (status.warningActive()) {
				Messages.storeMessage(section, TrackingMessages.class,
						Messages.warning(status.sourceChanges() == null
								? "InterWikiImport tracking differences are not acknowledged yet."
								: "InterWikiImport source changed since the last acknowledgement."));
			}
		}
		catch (IOException e) {
			Messages.storeMessage(section, TrackingMessages.class,
					Messages.warning("Unable to evaluate InterWikiImport tracking status: " + e.getMessage()));
		}
	}

	/** Marker source so tracking messages can be cleared independently from other markup messages. */
	private static final class TrackingMessages {
	}

	/** Marker source so sync-failure warnings can be cleared independently from other markup messages. */
	private static final class SyncMessages {
	}

	/** Like {@link SyncMessages}, for the warning of one source wiki of an import using a variable. */
	private record SourceSyncMessages(String wiki) {
	}

	private static final String SYNCED_WIKIS_KEY = "syncedWikis";

	private Object syncMessageSource(Section<InterWikiImportMarkup> section, @Nullable String wiki) {
		return getWikiVariable(section) == null || wiki == null ? SyncMessages.class : new SourceSyncMessages(wiki);
	}

	/**
	 * Records the outcome of a remote-change poll of the given source wiki on this markup, called by
	 * {@link InterWikiImportUpdateService} after every attempt (whether or not anything changed).
	 * Always refreshes the "last check" timestamp; on failure ({@code errorMessage != null}) it
	 * stores a warning so the failing sync is visible in the markup instead of silently showing
	 * "No check yet", on success it clears any previous warning. The failure is also remembered
	 * per attachment path so {@link RegistrationScript} can re-apply it after a recompile.
	 */
	void recordSyncOutcome(Section<InterWikiImportMarkup> section, @Nullable String wiki, @Nullable String errorMessage) {
		logLastRun(section);
		String path = getWikiAttachmentPath(section, wiki);
		Object source = syncMessageSource(section, wiki);
		if (source instanceof SourceSyncMessages) {
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (section) {
				Set<String> syncedWikis = section.getObject(SYNCED_WIKIS_KEY);
				if (syncedWikis == null) {
					syncedWikis = ConcurrentHashMap.newKeySet();
					section.storeObject(SYNCED_WIKIS_KEY, syncedWikis);
				}
				syncedWikis.add(wiki);
			}
		}
		if (errorMessage == null) {
			if (path != null) LAST_SYNC_ERRORS.remove(path);
			Messages.clearMessages(section, source);
		}
		else {
			if (path != null) LAST_SYNC_ERRORS.put(path, errorMessage);
			Messages.storeMessage(section, source, Messages.warning(errorMessage));
		}
	}

	/**
	 * Clears the sync warnings of source wikis the given import no longer imports from.
	 */
	void clearObsoleteSyncMessages(Section<InterWikiImportMarkup> section) {
		Set<String> syncedWikis = section.getObject(SYNCED_WIKIS_KEY);
		if (syncedWikis == null) return;
		List<String> wikis = getWikis(section);
		for (String wiki : List.copyOf(syncedWikis)) {
			if (wikis.contains(wiki)) continue;
			syncedWikis.remove(wiki);
			Messages.clearMessages(section, new SourceSyncMessages(wiki));
			String path = getWikiAttachmentPath(section, wiki);
			if (path != null) LAST_SYNC_ERRORS.remove(path);
		}
	}

	/** Re-applies remembered sync-failure warnings after a recompile cleared the section messages. */
	private void restoreSyncMessages(Section<InterWikiImportMarkup> section) {
		if (getWikiVariable(section) == null) {
			String path = getWikiAttachmentPath(section);
			String error = path == null ? null : LAST_SYNC_ERRORS.get(path);
			if (error != null) {
				Messages.storeMessage(section, SyncMessages.class, Messages.warning(error));
			}
			return;
		}
		for (String wiki : getWikis(section)) {
			String path = getWikiAttachmentPath(section, wiki);
			String error = path == null ? null : LAST_SYNC_ERRORS.get(path);
			if (error != null) recordSyncOutcome(section, wiki, error);
		}
	}

	/**
	 * Imports using a variable have no single attachment the time of the last check could be remembered for, so it
	 * is remembered for the import as a whole.
	 */
	@Override
	protected void logLastRun(Section<? extends AttachmentUpdateMarkup> section) {
		Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (markup == null || getWikiVariable(markup) == null) {
			super.logLastRun(section);
			return;
		}
		updateService(markup).logLastCheck(getAttachmentBasePath(markup));
	}

	@Override
	protected long timeSinceLastRun(Section<? extends AttachmentUpdateMarkup> section) {
		if (section == null) return Long.MAX_VALUE;
		Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (markup == null || getWikiVariable(markup) == null) return super.timeSinceLastRun(section);
		Long lastCheck = updateService(markup).getLastCheck(getAttachmentBasePath(markup));
		return lastCheck == null ? Long.MAX_VALUE : System.currentTimeMillis() - lastCheck;
	}

	void collectTrackingInitializationReplacement(Section<InterWikiImportMarkup> section, Map<String, String> replacements) throws IOException {
		if (getMode(section) != Mode.TRACKING) return;

		String referenceText = getTrackingReferenceText(section);
		if (Strings.isBlank(referenceText)) return;

		String localComparisonText = getTrackingLocalComparisonText(section);
		// Initialize only when the local area is still empty to protect user-maintained local content.
		if (localComparisonText == null || Strings.isNotBlank(localComparisonText)) return;

		Section<?> closingTag = $(section).children().getLast();
		if (closingTag == null || !"%".equals(Strings.trim(closingTag.getText()))) return;

		// Merge with a possibly existing replacement for this closing tag (e.g. @latestChange update).
		String existingReplacement = replacements.getOrDefault(closingTag.getID(), closingTag.getText());
		String initializedText = Strings.trimRight(existingReplacement)
				+ "\n\n"
				+ Strings.trimRight(referenceText)
				+ "\n";
		replacements.put(closingTag.getID(), initializedText);
	}

	/**
	 * Polls all given markups together, so their updates change each page only once (see
	 * {@link InterWikiImportUpdateService#pollMarkups(Collection, boolean)}).
	 */
	@Override
	public void performUpdates(Collection<? extends Section<? extends AttachmentUpdateMarkup>> sections, boolean force, boolean allowWaitForOtherDownloads) {
		List<Section<InterWikiImportMarkup>> markups = sections.stream()
				.map(section -> $(section).closest(InterWikiImportMarkup.class).getFirst())
				.filter(Objects::nonNull)
				.toList();
		if (markups.isEmpty()) return;
		updateService(markups.get(0)).pollMarkups(markups, force);
	}

	/**
	 * Routes generic update requests (e.g. from {@link de.knowwe.core.action.RecompileAction} or the
	 * global "Update imports" admin tool) through the central {@code @latestChange}-aware poller
	 * instead of the header/timestamp-based pipeline of {@link AttachmentUpdateMarkup}. The generic
	 * pipeline stores a new attachment version even for unchanged content once the page is newer than
	 * the attachment ({@code IMPORT_SECTION_CHANGED}) — that timestamp bump would re-activate already
	 * acknowledged tracking diffs ({@code @trackingAcceptedAt}) on every forced recompile.
	 */
	@Override
	public void performUpdate(Section<? extends AttachmentUpdateMarkup> section, boolean force, boolean allowWaitForOtherDownloads) {
		Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (markup == null) return;
		updateService(markup).pollSingleMarkup(markup, force);
	}

	/**
	 * Tracking mode keeps the history of the reference attachment, so the reference text at the time
	 * of the last acknowledgement can be restored and only the source changes since then have to be
	 * reviewed.
	 */
	@Override
	protected boolean isVersioning(Section<? extends AttachmentUpdateMarkup> section) {
		return isTrackingMode(section);
	}

	@Override
	protected long getIntervalMillis(Section<? extends AttachmentUpdateMarkup> section) {
		return Long.MAX_VALUE;
	}

	@Override
	protected boolean usesOwnScheduling(Section<? extends AttachmentUpdateMarkup> section) {
		return false;
	}

	/** The context under which the tracking diffs remember the choices of the user. */
	private static final DiffRenderOptions DIFF_OPTIONS = DiffRenderOptions.defaults()
			.withView(DiffView.AUTO)
			.withContext("interwiki-import");

	/**
	 * Renders the diff as HTML, built in the browser, side by side where the page is wide enough.
	 */
	static String renderTrackingDiff(TextDiff diff) {
		return DiffHtmlRenderer.renderTextDiff(diff, DIFF_OPTIONS);
	}

	/**
	 * Renders the diff of the given entry. For the changes since a previous version, these are the
	 * changes in the source, and additionally what applying them changes in the local content (on a
	 * conflict with the source winning), if that differs, e.g. as lines changed by the source do not
	 * exist locally. For all differences to the source, it is what applying them changes in the local
	 * content. The element tells by {@code data-applicable} whether applying changes anything and by
	 * {@code data-conflict} whether local changes are overridden.
	 */
	static HtmlElement renderDiffOption(InterWikiTrackingService.DiffOption option, String referenceText, String localText, UserContext user) {
		String local = InterWikiTrackingService.normalizeForComparison(localText);
		HtmlElement element = new Div().attributes(
				"data-applicable", String.valueOf(isApplicable(option, localText)),
				"data-conflict", String.valueOf(option.conflict()));
		TextDiff localChanges = new TextDiff(local, option.resultText());
		if (option.version() == InterWikiTrackingService.ALL_CHANGES) {
			if (local.equals(option.resultText())) {
				element.children(new P().clazz("note").plainText("No changes to apply, the local content already contains them."));
			}
			else {
				element.children(new HtmlNode(renderTrackingDiff(localChanges)));
			}
			return element;
		}

		TextDiff sourceChanges = new TextDiff(option.baseText(), InterWikiTrackingService.normalizeForComparison(referenceText));
		element.children(new P().clazz("tracking-diff-title").plainText("Changes in the source:"));
		if (sourceChanges.stats().equals(new TextDiff.Stats(0, 0))) {
			element.children(new P().clazz("note").plainText("No changes in the source."));
		}
		else {
			element.children(new HtmlNode(renderTrackingDiff(sourceChanges)));
		}
		if (local.equals(option.resultText())) {
			element.children(new P().clazz("note").plainText(getNothingToApplyText(
					InterWikiTrackingService.getSkipReasons(option.baseText(), referenceText, local))));
		}
		else if (InterWikiTrackingService.isLocalChangesDiffNeeded(sourceChanges, localChanges)) {
			element.children(new P().clazz("tracking-diff-title").plainText("The following changes can be applied to the local content:"));
			element.children(new HtmlNode(renderTrackingDiff(localChanges)));
		}
		return element;
	}

	private static String getNothingToApplyText(Set<InterWikiTrackingService.SkipReason> reasons) {
		boolean contained = reasons.contains(InterWikiTrackingService.SkipReason.CONTAINED);
		boolean notExisting = reasons.contains(InterWikiTrackingService.SkipReason.NOT_EXISTING_LOCALLY);
		if (contained && notExisting) {
			return "Nothing to apply to the local content, it already contains some of these changes, the other changed lines do not exist locally.";
		}
		if (contained) return "Nothing to apply to the local content, it already contains these changes.";
		if (notExisting) return "Nothing to apply to the local content, the changed lines do not exist locally.";
		return "Nothing to apply to the local content.";
	}

	/**
	 * Whether applying the entry changes the local content (on a conflict the source winning).
	 */
	static boolean isApplicable(InterWikiTrackingService.DiffOption option, String localText) {
		return !InterWikiTrackingService.normalizeForComparison(localText).equals(option.resultText());
	}

	private class InterWikiImportRenderer extends DefaultMarkupRenderer implements AsyncPreviewRenderer {

		InterWikiImportRenderer() {
			// like the renderer of %%Include: with the pre-formatted style each blank line of the imported page
			// becomes masked html, and JSPWiki sees no line start after it, so everything that has to start a
			// line (headings, lists, rulers) would stay raw text
			setPreFormattedStyle(false);
		}

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			render(section, user, result, true);
		}

		@Override
		public void renderAsyncPreview(Section<?> section, UserContext user, RenderResult result) {
			render(section, user, result, false);
		}

		@Override
		protected String getDefaultCompilerClassProtected(Section<?> section, UserContext user) {
			// InterWikiImport should never be greyed out.
			// Only the markups within the imported article should be greyed out if they are not part of the default compiler
			return "";
		}

		private void render(Section<?> section, UserContext user, RenderResult result, boolean waitForUpdate) {
			waitForUpdate(section, user, waitForUpdate);
			Collection<String> errors = getMessageStrings(section, Message.Type.ERROR, user);
			Collection<String> warnings = getMessageStrings(section, Message.Type.WARNING, user);
			setFramed(!errors.isEmpty() || !warnings.isEmpty());
			super.render(section, user, result);
		}

		private void waitForUpdate(Section<?> section, UserContext user, boolean waitForUpdate) {
			if (waitForUpdate) {
				Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
				if (markup != null) {
					if (markup.get().isTrackingMode(markup)) return;
					// the attachment of the variant selected by the user; nothing to wait for if there is none
					String path = markup.get().getWikiAttachmentPath(markup, user);
					if (path == null) return;
					ArticleManager articleManager = user.getArticleManager();
					try {
						Stopwatch stopwatch = new Stopwatch();
						while (articleManager.getArticle(path) == null && stopwatch.getTime() < 20000) {
							// busy wait... not great, but ok in this case, since it is just a renderer
							// the attachment compilation is triggered asynchronous, so it would
							// be some hassle to do it event based
							Thread.sleep(50);
						}
					}
					catch (InterruptedException ignore) {
					}
				}
			}
		}

		@Override
		public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult result) {
			Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
			if (markup == null) return;

			renderHeader(markup, user, result);
			renderLastChangesMessage(markup, user, result);
			if (markup.get().isTrackingMode(markup)) {
				if (markup.get().getWikis(markup).size() > 1) {
					// the local text below the markup is the same for all variants, so it can only follow one source
					result.append(new Span().clazz("error").plainText("Tracking is not possible, because the variable '"
							+ markup.get().getWikiVariable(markup) + "' has different values: "
							+ String.join(", ", markup.get().getWikis(markup))
							+ ". Use a separate markup and local text for each of them."));
				}
				else {
					renderTracking(markup, user, result);
				}
			}
			else {
				renderImport(markup, user, result);
			}

			// always available, but collapsed (see renderAnnotations)
			renderAnnotations(markup, $(markup).successor(AnnotationType.class).asList(), user, result);
		}

		/**
		 * Renders the annotations as label/value list, with formatted dates, links and replacements.
		 * Values that cannot be formatted (or carry errors) are rendered as usual.
		 */
		@Override
		protected void renderAnnotations(List<Section<AnnotationType>> annotations, UserContext user, RenderResult result, String parentTag, String elementTag) {
			HtmlElement list = new Div().clazz("markupAnnotations iwi-annotations");
			for (Section<AnnotationType> annotation : annotations) {
				String name = annotation.get().getName();
				list.children(new Div()
						.clazz("markupAnnotation iwi-annotation")
						.attributes("data-name", name)
						.children(
								new Span().clazz("iwi-annotation-label").plainText(InterWikiImportAnnotationFormat.getLabel(name)),
								new Span().clazz("iwi-annotation-value").children(getAnnotationValue(annotation, name, user))));
			}
			// the configuration is rarely of interest, so it is collapsed
			result.append(new HtmlElement("details").clazz("iwi-configuration").children(
					new HtmlElement("summary").plainText("Configuration"),
					list));
		}

		private HtmlProvider[] getAnnotationValue(Section<AnnotationType> annotation, String name, UserContext user) {
			Section<AnnotationContentType> content = $(annotation).successor(AnnotationContentType.class).getFirst();
			Section<InterWikiImportMarkup> markup = $(annotation).closest(InterWikiImportMarkup.class).getFirst();
			HtmlProvider plainValue = result -> {
				if (content != null) result.append(content, user);
			};
			if (content == null || markup == null
					|| !Messages.getMessages(content, Message.Type.ERROR, Message.Type.WARNING).isEmpty()) {
				return new HtmlProvider[] { plainValue };
			}
			String text = Strings.trim(content.getText());
			switch (name) {
				case WIKI_ANNOTATION -> {
					return new HtmlProvider[] {
							new A().attributes("href", normalizeWiki(text)).plainText(InterWikiImportAnnotationFormat.formatWiki(text)) };
				}
				case WIKI_VARIABLE_ANNOTATION -> {
					// the variable and the wikis it stands for, the one of the selected variant first
					List<HtmlProvider> value = new ArrayList<>();
					// rendered as the term it is, with its tools, e.g. to show the definitions and all usages
					value.add(plainValue);
					LinkedHashSet<String> wikis = new LinkedHashSet<>(markup.get().getWikis(markup, user));
					wikis.addAll(markup.get().getWikis(markup));
					String separator = "\u2192";
					for (String wiki : wikis) {
						value.add(new Span().clazz("iwi-annotation-arrow").plainText(separator));
						value.add(new A().attributes("href", wiki, "title", InterWikiImportAnnotationFormat.formatWiki(wiki))
								.plainText(markup.get().getWikiDisplayName(markup, wiki)));
						separator = ",";
					}
					return value.toArray(new HtmlProvider[0]);
				}
				case PAGE_ANNOTATION -> {
					URL url = markup.get().getUrl(markup, "Wiki.jsp?page=", false);
					if (url == null) return new HtmlProvider[] { new PlainTextNode(text) };
					return new HtmlProvider[] {
							new A().attributes("href", url.toString().replaceAll("%23.+$", "")).plainText(text) };
				}
				case MODE_ANNOTATION -> {
					return new HtmlProvider[] { new PlainTextNode(InterWikiImportAnnotationFormat.formatMode(text)) };
				}
				case COMPILE_ANNOTATION -> {
					return new HtmlProvider[] { new PlainTextNode("false".equalsIgnoreCase(text) ? "No" : "Yes") };
				}
				case LATEST_CHANGE_ANNOTATION, TRACKING_ACCEPTED_AT_ANNOTATION -> {
					// the latest change may be followed by the source wiki it belongs to
					LatestChange latestChange = LatestChange.parse(text);
					if (latestChange == null) return new HtmlProvider[] { plainValue };
					Instant instant = latestChange.instant();
					HtmlElement date = new Span().title(instant.toString())
							.plainText(InterWikiImportAnnotationFormat.formatDateTime(instant, getLocale(user), ZoneId.systemDefault()));
					String hint = InterWikiImportAnnotationFormat.formatRelative(instant, Instant.now());
					if (latestChange.wiki() != null) {
						hint += ", " + markup.get().getWikiDisplayName(markup, latestChange.wiki());
					}
					return new HtmlProvider[] { date, new Span().clazz("iwi-annotation-hint").plainText(hint) };
				}
				case REPLACEMENT, REGEX_REPLACEMENT -> {
					String[] parts = InterWikiImportAnnotationFormat.splitReplacement(text);
					if (parts == null) return new HtmlProvider[] { plainValue };
					return new HtmlProvider[] {
							new HtmlElement("code").plainText(parts[0]),
							new Span().clazz("iwi-annotation-arrow").plainText("\u2192"),
							new HtmlElement("code").plainText(parts[1]) };
				}
				default -> {
					return new HtmlProvider[] { plainValue };
				}
			}
		}

		private static Locale getLocale(UserContext user) {
			try {
				return user.getLocale();
			}
			catch (RuntimeException e) {
				// no request available, e.g. for asynchronous rendering
				return Locale.ENGLISH;
			}
		}

		@Override
		public boolean shouldRenderAsynchronous(Section<?> section, UserContext user) {
			Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
			if (markup == null) return true;
			String path = markup.get().isTrackingMode(markup)
					? markup.get().getWikiAttachmentPath(markup)
					: markup.get().getWikiAttachmentPath(markup, user);
			// an import using a variable without (single) source for this variant: there is nothing to wait for
			if (path == null && markup.get().getWikiVariable(markup) != null) return false;
			try {
				if (path == null || markup.getEnvironment().getWikiConnector().getAttachment(path) == null) {
					return true;
				}
			}
			catch (IOException e) {
				return true;
			}
			Article article = user.getArticleManager().getArticle(path);
			if (article == null) return true;
			return getLock(markup).isLocked();
		}

		private void renderTracking(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result) {
			InterWikiTrackingService.TrackingStatus trackingStatus;
			try {
				trackingStatus = InterWikiTrackingService.getTrackingStatus(markup);
			}
			catch (IOException e) {
				result.append(new HtmlElement("p").clazz("warning").content("Unable to read the source content: " + e.getMessage()));
				return;
			}

			if (trackingStatus.state() == InterWikiTrackingService.State.MISSING_REFERENCE) {
				result.append(new HtmlElement("p").clazz("note").content("Source content not (yet) available"));
				return;
			}

			if (trackingStatus.canInitializeFromReference() && KnowWEUtils.canWrite(markup, user)) {
				renderInitializationButton(markup, result);
				return;
			}

			if (trackingStatus.localComparisonAvailable()) {
				renderTrackingComparisonStatus(markup, trackingStatus, user, result);
			}
			else {
				result.append(new HtmlElement("p").clazz("warning")
						.content("Tracking mode: local comparison range is currently not available."));
			}
		}

		private void renderInitializationButton(Section<InterWikiImportMarkup> markup, RenderResult result) {
			result.append(new HtmlElement("p").clazz("note")
					.content("Tracking mode: local copy is empty. Insert the current text of the source below the markup as a starting point."));
			result.append(new HtmlElement("button")
					.attributes("type", "button",
							"class", "tracking-action-button",
							"onclick", buildTrackingActionScript(
									"InitInterWikiTrackingLocalCopyAction", markup.getID(),
									"tracking-init", "Unable to initialize local copy.", null))
					.content("Insert source text below"));
		}

		/**
		 * Renders the differences of a tracking markup: if not acknowledged yet, the changes of the source
		 * to review (and what applying them changes in the local content, see {@link #renderDiffOption})
		 * with the buttons to apply and to acknowledge them, and in any case the collapsed "Compare with
		 * source" to look at the complete comparison or the changes since a previous version.
		 */
		private void renderTrackingDifferences(Section<InterWikiImportMarkup> markup, InterWikiTrackingService.TrackingStatus trackingStatus,
				UserContext user, RenderResult result) {
			String localText = markup.get().getTrackingLocalComparisonText(markup);
			String referenceText;
			List<InterWikiTrackingService.DiffOption> options;
			try {
				WikiAttachment attachment = markup.get().getWikiAttachment(markup);
				referenceText = markup.get().getTrackingReferenceText(markup);
				if (attachment == null || referenceText == null || localText == null) return;
				Instant acceptedAt = trackingStatus.trackingAcceptedAt();
				int acceptedVersion = acceptedAt == null ? -1 : InterWikiTrackingService.findVersionAt(attachment, acceptedAt);
				options = InterWikiTrackingService.getDiffOptions(attachment, referenceText, localText, acceptedVersion, MAX_DIFF_OPTIONS);
			}
			catch (IOException e) {
				result.append(new P().clazz("warning").plainText("Unable to read the versions of the source: " + e.getMessage()));
				return;
			}
			Locale locale = getLocale(user);

			if (trackingStatus.state() == InterWikiTrackingService.State.ACCEPTED_DIFF) {
				result.append(new P().clazz("note").children(
						new PlainTextNode("Tracking mode: local content differs from the source, but changes were acknowledged"),
						new HtmlNode(Icon.CHECKED.addColor(Color.OK).addClasses("tracking-check").toHtml())));
			}
			else if (trackingStatus.state() == InterWikiTrackingService.State.CONTAINED_DIFF) {
				result.append(new P().clazz("note").children(
						new PlainTextNode("Tracking mode: local content differs from the source, but all changes of the source since the last acknowledgement are already contained locally"),
						new HtmlNode(Icon.CHECKED.addColor(Color.OK).addClasses("tracking-check").toHtml())));
			}
			else {
				// the changes since the last acknowledgement, or all differences if that is unknown
				InterWikiTrackingService.DiffOption review = trackingStatus.sourceChanges() == null ? null : options.stream()
						.filter(InterWikiTrackingService.DiffOption::acknowledged)
						.findFirst()
						.orElse(null);
				if (review == null) {
					review = options.get(options.size() - 1);
					result.append(new P().clazz("note").plainText("Tracking mode: local content differs from the source and was not acknowledged yet."));
				}
				else {
					result.append(new P().clazz("note").plainText("Tracking mode: the source changed since the last acknowledgement ("
							+ InterWikiImportAnnotationFormat.formatDateTime(Objects.requireNonNull(trackingStatus.trackingAcceptedAt()), locale, ZoneId.systemDefault())
							+ ")."));
				}
				renderReview(markup, review, referenceText, localText, user, result);
			}
			renderComparison(markup, options, localText, trackingStatus.trackingAcceptedAt(), locale, user, result);
		}

		private void renderReview(Section<InterWikiImportMarkup> markup, InterWikiTrackingService.DiffOption review,
				String referenceText, String localText, UserContext user, RenderResult result) {
			result.append(new Div().clazz("tracking-diff").children(renderDiffOption(review, referenceText, localText, user)));
			if (!KnowWEUtils.canWrite(markup, user)) return;
			HtmlElement buttons = new Div().clazz("tracking-action-buttons");
			String acknowledge = buildTrackingActionScript("AcceptInterWikiTrackingDiffAction", markup.getID(),
					"tracking-accept", "Unable to acknowledge tracking differences.", null);
			if (isApplicable(review, localText)) {
				String label = review.conflict()
						? "Override local changes by applying shown changes from source and acknowledge"
						: "Apply shown changes from source and acknowledge";
				buttons.children(createButton(label, "KNOWWE.plugin.include.applyTrackingChanges('" + markup.getID() + "', "
						+ review.version() + ", true, " + review.conflict() + ")"));
			}
			buttons.children(createButton("Acknowledge differences", acknowledge));
			result.append(buttons);
		}

		/**
		 * Renders the collapsed "Compare with source": the complete comparison with the source or the
		 * changes since a previous version (loaded when expanded), and the button to apply them without
		 * acknowledging.
		 */
		private void renderComparison(Section<InterWikiImportMarkup> markup, List<InterWikiTrackingService.DiffOption> options,
				String localText, @Nullable Instant acceptedAt, Locale locale, UserContext user, RenderResult result) {
			HtmlElement select = new HtmlElement("select").attributes(
					"id", "tracking-compare-select-" + markup.getID(),
					"class", "tracking-diff-select",
					"onchange", "KNOWWE.plugin.include.showTrackingComparison('" + markup.getID() + "')");
			// the changes since the previous versions, newest first (so each entry shows more changes), at last
			// the complete comparison, which is preselected
			for (InterWikiTrackingService.DiffOption option : options.subList(0, options.size() - 1)) {
				// entries without anything to apply are of no interest here
				if (isApplicable(option, localText)) select.children(createComparisonOption(option, acceptedAt, locale));
			}
			InterWikiTrackingService.DiffOption complete = options.get(options.size() - 1);
			select.children(createComparisonOption(complete, acceptedAt, locale).attributes("selected", "selected"));
			HtmlElement controls = new Div().clazz("tracking-action-buttons").children(
					new Span().clazz("tracking-diff-selection").children(new Span().plainText("Show:"), select));
			if (KnowWEUtils.canWrite(markup, user)) {
				controls.children(createButton("Apply shown changes",
						"KNOWWE.plugin.include.applyTrackingComparison('" + markup.getID() + "')")
						.attributes("id", "tracking-compare-apply-" + markup.getID()));
			}
			result.append(new HtmlElement("details")
					.clazz("iwi-comparison")
					.attributes("ontoggle", "KNOWWE.plugin.include.showTrackingComparison('" + markup.getID() + "')")
					.children(
							new HtmlElement("summary").plainText("Compare with source"),
							controls,
							new Div().id("tracking-compare-" + markup.getID()).clazz("tracking-diff")));
		}

		private static HtmlElement createComparisonOption(InterWikiTrackingService.DiffOption option, @Nullable Instant acceptedAt, Locale locale) {
			return new HtmlElement("option")
					.attributes("value", String.valueOf(option.version()))
					.plainText(getComparisonLabel(option, acceptedAt, locale));
		}

		/**
		 * Labels the entry by the date of the version, the one of the last acknowledgement by the date of the
		 * acknowledgement (as in the status message).
		 */
		private static String getComparisonLabel(InterWikiTrackingService.DiffOption option, @Nullable Instant acceptedAt, Locale locale) {
			if (option.version() == InterWikiTrackingService.ALL_CHANGES) return "Complete comparison with source";
			List<String> details = new ArrayList<>();
			String label;
			if (option.acknowledged() && acceptedAt != null) {
				label = "Changes since last acknowledgement";
				details.add(InterWikiImportAnnotationFormat.formatDateTime(acceptedAt, locale, ZoneId.systemDefault()));
			}
			else {
				label = "Changes since " + InterWikiImportAnnotationFormat.formatDateTime(Objects.requireNonNull(option.date()), locale, ZoneId.systemDefault());
			}
			if (option.conflict()) details.add("conflict");
			return details.isEmpty() ? label : label + " (" + String.join(", ", details) + ")";
		}

		private static HtmlElement createButton(String label, String onclick) {
			return new HtmlElement("button")
					.attributes("type", "button", "class", "tracking-action-button", "onclick", onclick)
					.plainText(label);
		}

		private static String buildTrackingActionScript(String action, String sectionId, String notificationKey, String fallbackError, @Nullable String confirmMessage) {
			String prefix = confirmMessage == null
					? ""
					: "if(!confirm('" + confirmMessage.replace("\\", "\\\\").replace("'", "\\'") + "'))return;";
			return "(function(){"
					+ prefix
					+ "jq$.ajax({"
					+ "url: KNOWWE.core.util.getURL({action:'" + action + "',"
					+ Attributes.SECTION_ID + ":'" + sectionId + "'}),"
					+ "type:'post',"
					+ "cache:false"
					+ "}).done(function(){window.location.reload();})"
					+ ".fail(function(xhr){"
					+ "KNOWWE.notification.error(null,"
					+ "KNOWWE.plugin.include.errorMessage(xhr, '" + fallbackError + "'),"
					+ "'" + notificationKey + "',10000);"
					+ "});"
					+ "})();";
		}

		private void renderTrackingComparisonStatus(
				Section<InterWikiImportMarkup> markup,
				InterWikiTrackingService.TrackingStatus trackingStatus,
				UserContext user,
				RenderResult result) {
			switch (trackingStatus.state()) {
				case EQUAL -> result.append(new HtmlElement("p").clazz("success")
						.content("Tracking mode: local content matches the source."));
				case UNACCEPTED_DIFF, ACCEPTED_DIFF, CONTAINED_DIFF -> renderTrackingDifferences(markup, trackingStatus, user, result);
				default -> result.append(new HtmlElement("p").clazz("note")
						.content("Tracking mode: status currently unavailable."));
			}
		}

		private void renderImport(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result) {
			String missingSource = markup.get().getMissingSourceMessage(markup, user);
			if (missingSource != null) {
				// plain text: the message contains wiki URLs and the name of a markup
				result.append(new Span().clazz("warning").plainText(missingSource));
				return;
			}
			// the content imported for the variant selected by the user
			String path = markup.get().getWikiAttachmentPath(markup, user);
			Article article = path == null ? null : user.getArticleManager().getArticle(path);
			if (article == null) {
				result.append(new Span("Included article not (yet) available").clazz("warning"));
			}
			else {
				boolean framed = isFramed();
				// the imported content is set off from the import markup itself
				result.append(new Div().clazz("iwi-imported-content").children(content -> {
					Section<RootType> root = article.getRootSection();
					RenderResult lines = new RenderResult(content);
					lines.addCustomRenderer(section -> section == root, new LineStartRootRenderer());
					// the content itself must start on a new line too, not right behind the surrounding html
					lines.append("\n");
					if (framed) {
						// used the framing renderer
						new FramedIncludedSectionRenderer(true).render(root, user, lines);
					}
					else {
						// or simply render the sections belonging to the header
						FramedIncludedSectionRenderer.renderTargetSections(root, true, user, lines);
					}
					content.append(lines);
				}));
			}
		}

		/**
		 * Renders the root section of an imported article with each of its top-level sections starting on a line of
		 * its own. On a page of the wiki this is given (the edit mode puts its anchors there), but not for the
		 * articles of attachments. A markup section ends with its closing "%" and the line break after it, so e.g. a
		 * heading right after a markup would not be at the start of a line and would stay raw text.
		 */
		private static class LineStartRootRenderer implements Renderer {

			@Override
			public void render(Section<?> section, UserContext user, RenderResult result) {
				// the messages of the article, as the renderer of the root type shows them
				for (Collection<Message> messages : Messages.getMessagesMap(section).values()) {
					for (Message message : messages) {
						String tag = message.getType() == Message.Type.ERROR ? "error"
								: message.getType() == Message.Type.WARNING ? "warning" : "information";
						result.append("\n%%").append(tag).append("\n");
						result.append(message.getVerbalization());
						result.append("\n/%\n\n");
					}
				}
				for (Section<?> child : section.getChildren()) {
					result.append("\n");
					result.append(child, user);
				}
			}
		}

		private void renderLastChangesMessage(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result) {
			if (getLock(markup).isLocked()) {
				result.append(new HtmlElement("p").attributes("style", "color:green").content("Update currently ongoing..."));
			}
			else {
				long lastRun = markup.get().timeSinceLastRun(markup);
				String message;
				if (lastRun < Long.MAX_VALUE) {
					Instant now = Instant.now();
					message = "Last check for changes: "
							+ InterWikiImportAnnotationFormat.formatRelative(now.minusMillis(lastRun), now);
					// the change in the source (@latestChange), not the local update of the attachment
					Instant latestChange = markup.get().getLatestChange(markup, markup.get().getWiki(markup, user));
					if (latestChange != null) {
						message += ", last change in source: " + InterWikiImportAnnotationFormat.formatRelative(latestChange, now);
					}
				}
				else {
					message = "No check yet, click here to check now: ";
				}
				result.append(new HtmlElement("p").children(
						new Span(message).clazz("include-message"),
						new A().attributes(
										"onclick", buildRefreshScript(markup.getID(), false),
										"class", "include-refresh tooltipster",
										"title", "Check for changes")
								.children(new HtmlNode(Icon.REFRESH.toHtml()))
				));
			}

			if (DefaultMarkupType.getAnnotation(markup, INTERVAL_ANNOTATION) != null) {
				result.append(new HtmlElement("p")
						.clazz("warning")
						.content("@interval is deprecated for InterWikiImport and no longer used. Updates are polled immediately after startup and then every "
								+ TimeUnit.MILLISECONDS.toMinutes(updateService(markup).getPollIntervalMillis())
								+ " minutes per source wiki."));
			}
		}

		private void renderHeader(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result) {
			// for an import using a variable, the wiki of the variant selected by the user
			String variable = markup.get().getWikiVariable(markup);
			String wiki = markup.get().getWiki(markup, user);
			URL url = markup.get().getUrl(markup, wiki, "Wiki.jsp?page=", false);
			if ((url != null && wiki != null) || variable != null) {
				String pageName = markup.get().getPageName(markup);
				String sectionName = markup.get().getSectionName(markup);
				String reference = pageName;
				if (sectionName != null) {
					reference += " - " + sectionName;
				}

				HtmlElement header = new HtmlElement("h2");
				if (variable != null) {
					// the display name of the definition valid for the selected variant (or the name of the variable)
					// stands for the wiki, linked to the imported page if the wiki is known for that variant; the
					// definition of the variable is reached by a tool of the markup
					String label = markup.get().getSourceDisplayName(markup, user) + ": " + reference;
					if (url != null && wiki != null) {
						String shortenedUrl = url.toString().replaceAll("%23.+$", "");
						header.children(new TextNode("Import from "), new A()
								.attributes("href", shortenedUrl, "title", InterWikiImportAnnotationFormat.formatWiki(wiki))
								.plainText(label));
					}
					else {
						header.children(new PlainTextNode("Import from " + label));
					}
				}
				else {
					String wikiName = wiki.replaceAll("^https?://", "").replaceAll("/$", "");
					String shortenedUrl = url.toString().replaceAll("%23.+$", "");
					header.children(new TextNode("Import from wiki "), new A(wikiName + ": " + reference, shortenedUrl));
				}

				HelpToolProvider helpToolProvider = new HelpToolProvider();
				if (helpToolProvider.hasTools(markup, user)) {
					Tool[] tools = helpToolProvider.getTools(markup, user);
					for (Tool tool : tools) {
						header.children(new A()
								.attributes("title", tool.getDescription(),
										"class", "tooltipster help-tool",
										tool.getActionType() == Tool.ActionType.ONCLICK ? "onclick" : "href",
										tool.getAction())
								.children(new HtmlNode(tool.getIcon().toHtml())));
					}
				}

				String action = "KNOWWE.core.plugin.setMarkupSectionActivationStatus('" + markup.getID() + "', 'off')";
				header.children(new A()
						.attributes("onclick", action, "class", "include-deactivate tooltipster", "title", "Deactivate import")
						.children(new HtmlNode(Icon.TOGGLE_OFF.toHtml())));
				result.append(header);
			}
		}
	}

	public enum Mode {
		IMPORT,
		TRACKING
	}

	private static class RegistrationScript extends DefaultGlobalCompiler.DefaultGlobalScript<InterWikiImportMarkup> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<InterWikiImportMarkup> section) {
			// an import using a variable is also registered if the variable has no (single) value at the moment
			if (section.get().getWikiVariable(section) == null && section.get().getUrl(section) == null) {
				updateService(section).deregister(section);
				Messages.clearMessages(section, TrackingMessages.class);
				return;
			}
			updateService(section).register(section);
			section.get().refreshTrackingMessages(section);
			section.get().restoreSyncMessages(section);
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<InterWikiImportMarkup> section) {
			updateService(section).deregister(section);
			Messages.clearMessages(section, TrackingMessages.class);
			Messages.clearMessages(section, SyncMessages.class);
			for (String wiki : section.get().getWikis(section)) {
				Messages.clearMessages(section, new SourceSyncMessages(wiki));
			}
		}
	}

	/**
	 * Checks that the source wiki is given exactly once, either directly or by a variable.
	 */
	private static class SourceAnnotationCheck extends DefaultGlobalCompiler.DefaultGlobalScript<InterWikiImportMarkup> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<InterWikiImportMarkup> section) throws CompilerMessage {
			boolean hasWiki = !Strings.isBlank(DefaultMarkupType.getAnnotation(section, WIKI_ANNOTATION));
			boolean hasVariable = section.get().getWikiVariable(section) != null;
			if (!hasWiki && !hasVariable) {
				throw CompilerMessage.error("Either @" + WIKI_ANNOTATION + " or @" + WIKI_VARIABLE_ANNOTATION
						+ " has to be specified.");
			}
			if (hasWiki && hasVariable) {
				throw CompilerMessage.error("@" + WIKI_ANNOTATION + " and @" + WIKI_VARIABLE_ANNOTATION
						+ " must not be specified together.");
			}
		}
	}
}
