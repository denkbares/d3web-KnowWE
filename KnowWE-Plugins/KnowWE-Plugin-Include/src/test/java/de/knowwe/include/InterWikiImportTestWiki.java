/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.include;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import connector.DummyConnector;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.ParallelScriptCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.SequentialScriptCompiler;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageSelection;
import de.knowwe.core.compile.variables.InterWikiVariableMarkup;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.packaging.PackageMarkupType;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.event.AttachmentDeletedEvent;
import de.knowwe.event.AttachmentStoredEvent;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A wiki to test <code>%%InterWikiImport</code> without importing from another wiki. It runs the real import markup,
 * update service, attachment manager, variable index and package registration with an in-memory connector, but
 * neither boots a wiki nor loads plugins: the type tree consists of the real <code>%%InterWikiImport</code>,
 * <code>%%InterWikiVariable</code> and <code>%%Package</code>, and two test markups:
 * <ul>
 * <li><code>%%TestVariant</code> creates a package compiler for the packages given with <code>@uses</code>, standing
 * for one variant. The compiler only records the content it compiles.</li>
 * <li><code>%%TestContent</code> is the content that is compiled, which is what the source wikis provide.</li>
 * </ul>
 * The source wikis are replaced by {@link SourceWikis}: the update service asks it instead of sending requests, and
 * it answers like a source wiki does. Everything after that is the usual mechanism: the service stores the
 * attachment, writes <code>@latestChange</code> into the page, the connector fires the attachment event, the
 * attachment manager registers the article of the attachment, and the wiki compiles.
 * <p>
 * The scheduled polls of the service are switched off, the tests poll with {@link #poll()}.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
final class InterWikiImportTestWiki implements AutoCloseable {

	record Page(String title, String text) {
		@Override
		public @NotNull String toString() {
			return title;
		}
	}

	final Environment environment;
	final DefaultArticleManager manager;
	final InterWikiImportUpdateService service;
	final SourceWikis sources;
	private final EventFiringConnector connector;
	private final Map<String, String> pages = new ConcurrentHashMap<>();
	private boolean closed = false;

	InterWikiImportTestWiki() {
		this(new SourceWikis());
	}

	InterWikiImportTestWiki(SourceWikis sources) {
		this.sources = sources;
		RootType root = new RootType();
		// without plugins the check of the variables per variant has to be added to its two types by hand
		InterWikiVariableMarkup variableMarkup = new InterWikiVariableMarkup();
		variableMarkup.addCompileScript(Priority.LOWEST, new TestWikiVariableCheck());
		InterWikiImportMarkup importMarkup = new InterWikiImportMarkup();
		importMarkup.addCompileScript(Priority.LOWEST, new TestWikiVariableCheck());
		root.addChildType(0, variableMarkup);
		root.addChildType(1, importMarkup);
		root.addChildType(2, new PackageMarkupType());
		root.addChildType(3, new TestVariantMarkup());
		root.addChildType(4, new TestContentMarkup());
		Properties properties = new Properties();
		// no scheduled polls and no polls after saving a page: the tests decide when to poll
		properties.setProperty(AttachmentUpdateMarkup.KNOWWE_ATTACHMENTS_AUTO_UPDATE_ACTIVE_KEY, "false");
		connector = new EventFiringConnector(properties);
		// the real environment, without plugins: the type tree is the one built above
		environment = Environment.create(connector, root);
		manager = (DefaultArticleManager) environment.getArticleManager();
		service = InterWikiImportUpdateService.of(environment);
		service.setChangeRequester(sources);
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		try {
			awaitChanges();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Interrupted while closing the wiki", e);
		}
		finally {
			environment.close();
		}
	}

	/* ------------------------------------------------ pages ------------------------------------------------ */

	static Page variable(String title, String name, String value, @Nullable String packageName) {
		return new Page(title, "%%InterWikiVariable\n@name: " + name + "\n@value: " + value + "\n"
				+ packageAnnotation(packageName) + "%\n");
	}

	static Page variableImport(String title, String variable, String page, @Nullable String packageName) {
		return new Page(title, "%%InterWikiImport\n@wikiVariable: " + variable + "\n@page: " + page + "\n"
				+ packageAnnotation(packageName) + "%\n");
	}

	static Page literalImport(String title, String wiki, String page, @Nullable String packageName) {
		return new Page(title, "%%InterWikiImport\n@wiki: " + wiki + "\n@page: " + page + "\n"
				+ packageAnnotation(packageName) + "%\n");
	}

	static Page variant(String title, String... usedPackages) {
		StringBuilder text = new StringBuilder("%%TestVariant\n");
		for (String usedPackage : usedPackages) {
			text.append("@uses: ").append(usedPackage).append("\n");
		}
		return new Page(title, text.append("%\n").toString());
	}

	static Page content(String title, String content, @Nullable String packageName) {
		return new Page(title, contentMarkup(content, packageName));
	}

	static String contentMarkup(String content, @Nullable String packageName) {
		return "%%TestContent\n" + content + "\n" + packageAnnotation(packageName) + "%\n";
	}

	private static String packageAnnotation(@Nullable String packageName) {
		return packageName == null ? "" : "@package: " + packageName + "\n";
	}

	/* --------------------------------------------- registration --------------------------------------------- */

	/**
	 * Registers the given pages like the wiki does on startup: all in one registration frame, in the given order,
	 * followed by one compilation. Afterward the wiki is initialized.
	 */
	void start(List<Page> pages) throws InterruptedException {
		pages.forEach(page -> this.pages.put(page.title(), page.text()));
		manager.open();
		try {
			for (Page page : pages) {
				manager.queueArticle(page.title(), page.text());
			}
		}
		finally {
			manager.commit();
		}
		manager.getCompilerManager().awaitTermination();
		manager.setInitialized(true);
		environment.getEventManager().fireEvent(new InitializedArticlesEvent(manager));
	}

	/**
	 * Registers a new version of a page of the running wiki and waits for its compilation.
	 */
	void save(Page page) throws InterruptedException {
		pages.put(page.title(), page.text());
		manager.registerArticle(page.title(), page.text());
		awaitChanges();
	}

	void delete(String title) throws InterruptedException {
		pages.remove(title);
		manager.deleteArticle(title);
		awaitChanges();
	}

	/**
	 * Polls the source wikis of all imports once, like the periodic poll of the update service does, and waits until
	 * the updates are applied and compiled.
	 */
	void poll() throws InterruptedException {
		service.pollRegisteredMarkups();
		awaitChanges();
	}

	/**
	 * Waits until stored and deleted attachments are handled by the attachment manager and everything is compiled.
	 */
	void awaitChanges() throws InterruptedException {
		assertTrue("Attachment changes were not handled in time",
				manager.getAttachmentManager().awaitPendingChanges(30, TimeUnit.SECONDS));
		manager.getCompilerManager().awaitTermination();
	}

	/* --------------------------------------------- attachments --------------------------------------------- */

	/**
	 * Adds an attachment that is already there when the wiki starts, without any event.
	 */
	void addAttachment(String path, String text) throws IOException {
		connector.fireEvents = false;
		try {
			int separator = path.indexOf('/');
			connector.storeAttachment(path.substring(0, separator), path.substring(separator + 1), "test",
					new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
		}
		finally {
			connector.fireEvents = true;
		}
	}

	/**
	 * The paths of all attachments of the wiki, whether they are compiled or not.
	 */
	Set<String> attachments() {
		Set<String> paths = new TreeSet<>();
		for (WikiAttachment attachment : connector.getAttachments()) {
			paths.add(attachment.getPath());
		}
		return paths;
	}

	@Nullable
	String attachmentText(String path) throws IOException {
		WikiAttachment attachment = connector.getAttachment(path);
		return attachment == null ? null : Strings.readStream(attachment.getInputStream());
	}

	/* ------------------------------------------------ state ------------------------------------------------ */

	/**
	 * What the tests compare: the articles compiled from attachments and the content each variant compiles.
	 */
	record State(Set<String> attachmentArticles, Map<String, Set<String>> compiledByVariant) {
		@Override
		public @NotNull String toString() {
			return "attachments " + attachmentArticles + ", compiled " + compiledByVariant;
		}
	}

	State state() {
		Set<String> attachmentArticles = new TreeSet<>();
		for (Article article : manager.getArticles()) {
			if (manager.getAttachmentManager().isAttachmentArticle(article)) attachmentArticles.add(article.getTitle());
		}
		Map<String, Set<String>> compiledByVariant = new TreeMap<>();
		for (TestVariantCompiler compiler : variants()) {
			Set<String> current = new TreeSet<>();
			for (Article article : manager.getArticles()) {
				for (Section<TestContentMarkup> content : Sections.successors(article, TestContentMarkup.class)) {
					if (compiler.isCompiling(content)) current.add(contentOf(content));
				}
			}
			assertEquals("Variant " + compiler.getName() + " has not compiled what its packages contain",
					current, compiler.lastCompiled());
			compiledByVariant.put(compiler.getName(), current);
		}
		return new State(attachmentArticles, compiledByVariant);
	}

	/**
	 * Creates the expected state: the attachment articles, followed by the compiled content of each variant as
	 * <code>"Variant: content-1, content-2"</code>.
	 */
	static State expect(Collection<String> attachmentArticles, String... compiledByVariant) {
		Map<String, Set<String>> compiled = new TreeMap<>();
		for (String entry : compiledByVariant) {
			String[] split = entry.split(":", 2);
			Set<String> contents = new TreeSet<>();
			for (String content : split[1].split(",")) {
				if (Strings.nonBlank(content)) contents.add(content.trim());
			}
			compiled.put(split[0].trim(), contents);
		}
		return new State(new TreeSet<>(attachmentArticles), compiled);
	}

	/**
	 * Closes this wiki and returns the state of a new wiki that is started with the current pages and attachments of
	 * this one, without any poll. Whatever was changed and imported in the running wiki, it must have led to this
	 * very state.
	 */
	State stateAfterRestart() throws Exception {
		Map<String, String> currentPages = new TreeMap<>();
		for (String title : pages.keySet()) {
			// the import writes @latestChange into the pages, so take them as they are now
			currentPages.put(title, manager.getArticle(title).getText());
		}
		Map<String, String> currentAttachments = new TreeMap<>();
		for (String path : attachments()) {
			currentAttachments.put(path, attachmentText(path));
		}
		close();
		try (InterWikiImportTestWiki restarted = new InterWikiImportTestWiki(sources)) {
			for (Map.Entry<String, String> attachment : currentAttachments.entrySet()) {
				restarted.addAttachment(attachment.getKey(), attachment.getValue());
			}
			List<Page> pagesToStart = new ArrayList<>();
			currentPages.forEach((title, text) -> pagesToStart.add(new Page(title, text)));
			restarted.start(pagesToStart);
			return restarted.state();
		}
	}

	List<TestVariantCompiler> variants() {
		List<TestVariantCompiler> variants = new ArrayList<>();
		for (Compiler compiler : manager.getCompilerManager().getCompilers()) {
			if (compiler instanceof TestVariantCompiler variant) variants.add(variant);
		}
		return variants;
	}

	TestVariantCompiler variant(String name) {
		return variants().stream().filter(variant -> variant.getName().equals(name)).findFirst()
				.orElseThrow(() -> new AssertionError("No variant " + name));
	}

	/**
	 * The messages of the check of the wiki variables at the markups of the given page for the given variant, as
	 * "TYPE: text".
	 */
	List<String> variableMessages(String variant, String title) {
		List<String> messages = new ArrayList<>();
		for (Section<DefaultMarkupType> markup : Sections.successors(manager.getArticle(title), DefaultMarkupType.class)) {
			for (Message message : WikiVariableCheckScript.getMessages(variant(variant), markup)) {
				messages.add(message.getType() + ": " + message.getVerbalization());
			}
		}
		return messages;
	}

	String pageText(String title) {
		return manager.getArticle(title).getText();
	}

	/**
	 * The import markup of the given page.
	 */
	Section<InterWikiImportMarkup> importMarkup(String title) {
		Section<InterWikiImportMarkup> markup = Sections.successor(manager.getArticle(title), InterWikiImportMarkup.class);
		if (markup == null) throw new AssertionError("No import on page " + title);
		return markup;
	}

	/**
	 * The lines of the given page that start with the given text, e.g. "@latestChange".
	 */
	List<String> lines(String title, String start) {
		return pageText(title).lines().filter(line -> line.startsWith(start)).toList();
	}

	/**
	 * The messages of all compilers at the markups of the given page, as plain texts.
	 */
	List<String> messages(String title) {
		List<String> messages = new ArrayList<>();
		for (Section<DefaultMarkupType> markup : Sections.successors(manager.getArticle(title), DefaultMarkupType.class)) {
			for (Collection<Message> messagesOfCompiler : Messages.getMessagesMap(markup).values()) {
				messagesOfCompiler.forEach(message -> messages.add(message.getVerbalization()));
			}
		}
		return messages;
	}

	private static String contentOf(Section<?> content) {
		return DefaultMarkupType.getContent(content).trim();
	}

	/* --------------------------------------------- source wikis --------------------------------------------- */

	/**
	 * Stands for the source wikis. It holds a text and the time of its latest change for pages of wikis, and answers
	 * the requests of the update service like the action of a source wiki does: with the text of each requested page
	 * that has changed after the latest change the import already knows.
	 */
	static final class SourceWikis implements InterWikiImportUpdateService.ChangeRequester {

		private record SourcePage(String text, Instant changed) {
		}

		private final Map<String, Map<String, SourcePage>> pagesByWiki = new ConcurrentHashMap<>();
		private final Set<String> unreachable = ConcurrentHashMap.newKeySet();
		private final Set<String> notAuthorized = ConcurrentHashMap.newKeySet();
		private final List<String> requests = Collections.synchronizedList(new ArrayList<>());

		/**
		 * Sets the text of a page (or <code>page#section</code>) of a source wiki, changed at the given time.
		 */
		void setPage(String wiki, String page, String text, Instant changed) {
			pagesByWiki.computeIfAbsent(InterWikiImportMarkup.normalizeWiki(wiki), k -> new ConcurrentHashMap<>())
					.put(page, new SourcePage(text, changed));
		}

		void setUnreachable(String wiki, boolean unreachable) {
			set(this.unreachable, wiki, unreachable);
		}

		void setNotAuthorized(String wiki, boolean notAuthorized) {
			set(this.notAuthorized, wiki, notAuthorized);
		}

		private static void set(Set<String> wikis, String wiki, boolean add) {
			String normalized = InterWikiImportMarkup.normalizeWiki(wiki);
			if (add) wikis.add(normalized);
			else wikis.remove(normalized);
		}

		/**
		 * The requests received so far, as <code>"wiki page@latestChange"</code> per requested import, sorted. Clears
		 * the list.
		 */
		List<String> takeRequests() {
			synchronized (requests) {
				List<String> taken = new ArrayList<>(requests);
				requests.clear();
				Collections.sort(taken);
				return taken;
			}
		}

		@Override
		public InterWikiChanges requestChanges(String wiki, List<InterWikiChanges.RequestedImport> requestedImports) throws IOException {
			for (InterWikiChanges.RequestedImport requestedImport : requestedImports) {
				requests.add(wiki + " " + requestedImport.reference() + "@" + requestedImport.latestChange());
			}
			if (unreachable.contains(wiki)) throw new IOException("Connection refused");
			if (notAuthorized.contains(wiki)) return InterWikiChanges.notAuthorized();
			List<InterWikiChanges.Update> updates = new ArrayList<>();
			for (InterWikiChanges.RequestedImport requestedImport : requestedImports) {
				SourcePage page = pagesByWiki.getOrDefault(wiki, Map.of()).get(requestedImport.reference());
				if (page == null) continue;
				if (requestedImport.latestChange() == null || page.changed().isAfter(requestedImport.latestChange())) {
					updates.add(new InterWikiChanges.Update(requestedImport.requestingSectionId(), page.changed(), page.text()));
				}
			}
			return InterWikiChanges.ok(updates);
		}
	}

	/* ------------------------------------------------ types ------------------------------------------------ */

	/**
	 * The in-memory connector, firing the attachment events the connector of a running wiki fires.
	 */
	private static final class EventFiringConnector extends DummyConnector {

		private volatile boolean fireEvents = true;

		EventFiringConnector(Properties properties) {
			super(properties);
		}

		@Override
		public WikiAttachment storeAttachment(String title, String filename, String user, InputStream stream) throws IOException {
			WikiAttachment attachment = super.storeAttachment(title, filename, user, stream);
			if (fireEvents) getEnvironment().getEventManager().fireEvent(new AttachmentStoredEvent(title, filename));
			return attachment;
		}

		@Override
		public void deleteAttachment(String title, String fileName, String user) {
			super.deleteAttachment(title, fileName, user);
			if (fireEvents) getEnvironment().getEventManager().fireEvent(new AttachmentDeletedEvent(title, fileName));
		}
	}

	/**
	 * Stands for a variant: a compile markup with <code>@uses</code>, creating a compiler for the used packages.
	 */
	static final class TestVariantMarkup extends DefaultMarkupPackageCompileType {

		private static final DefaultMarkup MARKUP = new DefaultMarkup("TestVariant");

		static {
			MARKUP.addAnnotation(PackageManager.COMPILE_ATTRIBUTE_NAME, false);
			MARKUP.addAnnotationContentType(PackageManager.COMPILE_ATTRIBUTE_NAME, PackageSelection::new);
		}

		TestVariantMarkup() {
			super(MARKUP);
			addCompileScript(Priority.HIGHEST, new PackageRegistrationScript<PackageCompileType>() {
				@Override
				public void compile(PackageRegistrationCompiler compiler, Section<PackageCompileType> section) {
					if (!Sections.isLive(section)) return;
					compiler.getCompilerManager()
							.addCompiler(5, new TestVariantCompiler(compiler.getPackageManager(), section));
				}

				@Override
				public void destroy(PackageRegistrationCompiler compiler, Section<PackageCompileType> section) {
					for (PackageCompiler packageCompiler : section.get().getPackageCompilers(section)) {
						compiler.getCompilerManager().removeCompiler(packageCompiler);
					}
				}
			});
		}
	}

	/**
	 * The check of the wiki variables for the test variants, as CBX has one for its variants.
	 */
	static final class TestWikiVariableCheck extends WikiVariableCheckScript<TestVariantCompiler> {
		@Override
		public Class<TestVariantCompiler> getCompilerClass() {
			return TestVariantCompiler.class;
		}
	}

	/**
	 * The compiler of a variant. It records the content of its packages, each time it has to compile, and runs the
	 * scripts registered for it incrementally, the way the compiler of a real variant does: the scripts of the
	 * sections removed from its packages are destroyed, the ones of the added sections are compiled.
	 */
	static final class TestVariantCompiler extends AbstractPackageCompiler implements IncrementalCompiler {

		private final List<Set<String>> history = new ArrayList<>();
		private final AtomicInteger contentCompilations = new AtomicInteger();
		private ParallelScriptCompiler<TestVariantCompiler> scriptCompiler = new ParallelScriptCompiler<>(this, ParallelScriptCompiler.Mode.compile);
		private SequentialScriptCompiler<TestVariantCompiler> destroyScriptCompiler = null; // none for the first compilation

		TestVariantCompiler(PackageManager packageManager, Section<? extends PackageCompileType> compileSection) {
			super(packageManager, compileSection, TestVariantMarkup.class);
		}

		@Override
		public synchronized void compilePackages(String[] packagesToCompile) {
			try {
				Collection<Section<?>> added;
				if (destroyScriptCompiler != null) {
					added = getPackageManager().getAddedSections(packagesToCompile);
					getPackageManager().getRemovedSections(packagesToCompile).forEach(destroyScriptCompiler::addSubtree);
					destroyScriptCompiler.destroy();
				}
				else {
					added = getPackageManager().getSectionsOfPackage(packagesToCompile);
				}
				getCompilerManager().setCurrentCompilePriority(this, Priority.INIT);
				added.forEach(scriptCompiler::addSubtree);
				scriptCompiler.run();
			}
			finally {
				scriptCompiler.shutDown();
				if (destroyScriptCompiler != null) destroyScriptCompiler.shutDown();
				destroyScriptCompiler = new SequentialScriptCompiler<>(this, true);
				scriptCompiler = new ParallelScriptCompiler<>(this, ParallelScriptCompiler.Mode.compile);
			}

			Set<String> contents = new TreeSet<>();
			for (Section<?> section : getPackageManager().getSectionsOfPackage(packagesToCompile)) {
				if (section.get() instanceof TestContentMarkup) contents.add(contentOf(section));
			}
			history.add(contents);
		}

		@Override
		public boolean addSectionToDestroy(Section<?> section, Class<?>... scriptFilter) {
			return destroyScriptCompiler != null && destroyScriptCompiler.addSection(section, scriptFilter);
		}

		@Override
		public boolean addSectionToCompile(Section<?> section, Class<?>... scriptFilter) {
			return scriptCompiler.addSection(section, scriptFilter);
		}

		@Override
		public Sections<?> addSubtreeToDestroy(Section<?> section, Class<?>... scriptFilter) {
			return destroyScriptCompiler == null ? Sections.empty() : destroyScriptCompiler.addSubtree(section, scriptFilter);
		}

		@Override
		public Sections<?> addSubtreeToCompile(Section<?> section, Class<?>... scriptFilter) {
			return scriptCompiler.addSubtree(section, scriptFilter);
		}

		synchronized Set<String> lastCompiled() {
			return history.isEmpty() ? Set.of() : history.get(history.size() - 1);
		}

		/**
		 * How often this variant has compiled a content section. Unlike the number of its compilations this does
		 * not count the cheap ones, in which only the wiki variables of a changed import are checked again.
		 */
		int contentCompilations() {
			return contentCompilations.get();
		}
	}

	/**
	 * The content compiled by the variants. The script only counts, and tells the compiler of a variant that it
	 * has to compile if such a section is added to or removed from its packages.
	 */
	static final class TestContentMarkup extends DefaultMarkupType {

		private static final DefaultMarkup MARKUP = new DefaultMarkup("TestContent");

		static {
			PackageManager.addPackageAnnotation(MARKUP);
		}

		TestContentMarkup() {
			super(MARKUP);
			addCompileScript(new CompileScript<TestVariantCompiler, TestContentMarkup>() {
				@Override
				public void compile(TestVariantCompiler compiler, Section<TestContentMarkup> section) {
					compiler.contentCompilations.incrementAndGet();
				}

				@Override
				public Class<TestVariantCompiler> getCompilerClass() {
					return TestVariantCompiler.class;
				}
			});
		}
	}
}
