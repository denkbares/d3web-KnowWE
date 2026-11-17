/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageSelection;
import de.knowwe.core.compile.variables.InterWikiVariableMarkup;
import de.knowwe.core.compile.variables.VariableIndex;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.event.AttachmentDeletedEvent;
import de.knowwe.event.AttachmentStoredEvent;
import de.knowwe.event.FullParseEvent;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A small wiki to test attachments that are compiled depending on variables and variants. It runs the real article
 * manager, attachment manager, variable index, package registration and compiler manager, but neither boots a wiki nor
 * loads plugins. The type tree consists of the real <code>%%InterWikiVariable</code> and three test markups:
 * <ul>
 * <li><code>%%TestImport</code> compiles attachments of its page. With <code>@wiki</code> it compiles the one
 * attachment <code>Page/file.txt</code>, with <code>@wikiVariable</code> one attachment
 * <code>Page/file@value.txt</code> for each value of the variable, restricted to the compilers that also compile a
 * definition of that value. This is what an import markup using variables is supposed to do.</li>
 * <li><code>%%TestVariant</code> creates a package compiler for the packages given with <code>@uses</code>, standing
 * for one variant. The compiler only records the content it compiles.</li>
 * <li><code>%%TestContent</code> is the content that is compiled, in pages and in attachments.</li>
 * </ul>
 * Attachments are kept in memory. Storing and deleting them fires the events the wiki connector fires in a running
 * wiki, so changes reach the attachment manager the usual way.
 * <p>
 * As long as the deprecated singleton accessors exist, only one of these wikis may be open at a time.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public final class VariantAttachmentWiki implements AutoCloseable {

	public record Page(String title, String text) {
		@Override
		public @NotNull String toString() {
			return title;
		}
	}

	final Environment environment;
	final DefaultArticleManager manager;
	private final Map<String, String> attachments = new ConcurrentHashMap<>();
	private final Map<String, String> pages = new ConcurrentHashMap<>();
	private final AtomicInteger compilations = new AtomicInteger();
	private boolean closed = false;

	public VariantAttachmentWiki() {
		RootType root = new RootType();
		root.addChildType(0, new InterWikiVariableMarkup());
		root.addChildType(1, new TestImportMarkup());
		root.addChildType(2, new TestVariantMarkup());
		root.addChildType(3, new TestContentMarkup());
		Environment[] bound = new Environment[1];
		WikiConnector connector = (WikiConnector) Proxy.newProxyInstance(WikiConnector.class.getClassLoader(),
				new Class<?>[] { WikiConnector.class }, (proxy, method, args) -> switch (method.getName()) {
					case "setEnvironment" -> {
						bound[0] = (Environment) args[0];
						yield null;
					}
					case "getEnvironment" -> bound[0];
					case "getAttachment" -> attachment((String) args[0]);
					// what the environment initialization reads from the wiki configuration
					case "getApplicationName" -> "variant-attachment-test";
					case "getWikiProperty" -> null;
					case "getApplicationRootPath" -> "target/variant-attachment-test";
					default -> throw new UnsupportedOperationException("Unexpected wiki access: " + method.getName());
				});
		// the real environment, without plugins: the type tree is the one built above
		environment = Environment.create(connector, root);
		manager = (DefaultArticleManager) environment.getArticleManager();
		manager.getCompilerManager().addCompiler(1, new CompilationCounter());
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		try {
			awaitCompilation();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Interrupted while closing the wiki", e);
		}
		finally {
			// closing the environment releases everything it owns: articles, compilers, listeners, threads
			environment.close();
		}
	}

	/* ------------------------------------------------ pages ------------------------------------------------ */

	public static Page variable(String title, String name, String value, @Nullable String packageName) {
		return new Page(title, variableMarkup(name, value, packageName));
	}

	public static String variableMarkup(String name, String value, @Nullable String packageName) {
		return "%%InterWikiVariable\n@name: " + name + "\n@value: " + value + "\n" + packageAnnotation(packageName) + "%\n";
	}

	public static Page variableImport(String title, String variable, String file, @Nullable String packageName) {
		return new Page(title, variableImportMarkup(variable, file, packageName));
	}

	public static String variableImportMarkup(String variable, String file, @Nullable String packageName) {
		return "%%TestImport\n@wikiVariable: " + variable + "\n@file: " + file + "\n" + packageAnnotation(packageName) + "%\n";
	}

	public static Page literalImport(String title, String wiki, String file, @Nullable String packageName) {
		return new Page(title, "%%TestImport\n@wiki: " + wiki + "\n@file: " + file + "\n" + packageAnnotation(packageName) + "%\n");
	}

	public static Page variant(String title, String... usedPackages) {
		StringBuilder text = new StringBuilder("%%TestVariant\n");
		for (String usedPackage : usedPackages) {
			text.append("@uses: ").append(usedPackage).append("\n");
		}
		return new Page(title, text.append("%\n").toString());
	}

	public static Page content(String title, String content, @Nullable String packageName) {
		return new Page(title, contentMarkup(content, packageName));
	}

	public static String contentMarkup(String content, @Nullable String packageName) {
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
	public void start(List<Page> pages) throws InterruptedException {
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
		finishStart();
	}

	/**
	 * Registers the given pages like {@link #start(List)}, but each page by a thread of its own, all started at the
	 * same time. The wiki does the same on startup, where the order of the pages is undetermined.
	 */
	public void startInParallel(List<Page> pages) throws Exception {
		pages.forEach(page -> this.pages.put(page.title(), page.text()));
		ExecutorService executor = Executors.newFixedThreadPool(pages.size());
		try {
			CountDownLatch ready = new CountDownLatch(pages.size());
			CountDownLatch go = new CountDownLatch(1);
			manager.open();
			try {
				List<Future<?>> queued = new ArrayList<>();
				for (Page page : pages) {
					queued.add(executor.submit(() -> {
						ready.countDown();
						go.await();
						manager.queueArticle(page.title(), page.text());
						return null;
					}));
				}
				assertTrue(ready.await(10, TimeUnit.SECONDS));
				go.countDown();
				for (Future<?> future : queued) {
					future.get(30, TimeUnit.SECONDS);
				}
			}
			finally {
				manager.commit();
			}
		}
		finally {
			executor.shutdownNow();
		}
		finishStart();
	}

	private void finishStart() throws InterruptedException {
		awaitCompilation();
		manager.setInitialized(true);
		KnowWEUtils.getEventManager(manager).fireEvent(new InitializedArticlesEvent(manager));
	}

	/**
	 * Registers a new version of a page of the running wiki and waits for its compilation.
	 */
	public void save(String title, String text) throws InterruptedException {
		pages.put(title, text);
		manager.registerArticle(title, text);
		awaitCompilation();
	}

	public void save(Page page) throws InterruptedException {
		save(page.title(), page.text());
	}

	/**
	 * Asks for a full parse of the given page like the wiki does on request of a user, which makes the attachment
	 * manager parse the compiled attachments of the page again.
	 */
	public void fullParse(String title) throws InterruptedException {
		manager.open();
		try {
			KnowWEUtils.getEventManager(manager).fireEvent(new FullParseEvent(manager.getArticle(title), "test"));
		}
		finally {
			manager.commit();
		}
		awaitCompilation();
	}

	public void delete(String title) throws InterruptedException {
		pages.remove(title);
		manager.deleteArticle(title);
		awaitCompilation();
	}

	public void awaitCompilation() throws InterruptedException {
		manager.getCompilerManager().awaitTermination();
	}

	/* --------------------------------------------- attachments --------------------------------------------- */

	/**
	 * Adds an attachment that is already there when the wiki starts, without any event.
	 */
	public void addAttachment(String path, String text) {
		attachments.put(path, text);
	}

	/**
	 * Stores an attachment in the running wiki. Like the wiki connector of a real wiki, this fires an event that the
	 * attachment manager handles asynchronously. Use {@link #await(Object, Supplier)} to wait for the outcome.
	 */
	public void storeAttachment(String path, String text) {
		attachments.put(path, text);
		int separator = path.indexOf('/');
		KnowWEUtils.getEventManager(manager)
				.fireEvent(new AttachmentStoredEvent(path.substring(0, separator), path.substring(separator + 1)));
	}

	public void deleteAttachment(String path) {
		attachments.remove(path);
		int separator = path.indexOf('/');
		KnowWEUtils.getEventManager(manager)
				.fireEvent(new AttachmentDeletedEvent(path.substring(0, separator), path.substring(separator + 1)));
	}

	/** No disk or network access: only what the attachment manager needs is supported. */
	private WikiAttachment attachment(String path) {
		if (!attachments.containsKey(path)) return null;
		return (WikiAttachment) Proxy.newProxyInstance(WikiAttachment.class.getClassLoader(),
				new Class<?>[] { WikiAttachment.class }, (proxy, method, args) -> switch (method.getName()) {
					case "getPath" -> path;
					case "getInputStream" -> new ByteArrayInputStream(attachments.get(path).getBytes(StandardCharsets.UTF_8));
					default -> throw new UnsupportedOperationException("Unexpected attachment access: " + method.getName());
				});
	}

	/* ------------------------------------------------ state ------------------------------------------------ */

	/**
	 * What the tests compare: the articles compiled from attachments and the content each variant compiles. The
	 * content is given twice, as last compiled by the compiler of the variant and as the packages say now; both have
	 * to match.
	 */
	public record State(Set<String> attachmentArticles, Map<String, Set<String>> compiledByVariant) {
		@Override
		public @NotNull String toString() {
			return "attachments " + attachmentArticles + ", compiled " + compiledByVariant;
		}
	}

	public State state() {
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
	 * Closes this wiki and returns the state of a new wiki that is started with the current pages and attachments of
	 * this one. Whatever was changed in the running wiki, it must have led to this very state.
	 */
	public State stateAfterRestart() throws InterruptedException {
		close();
		try (VariantAttachmentWiki restarted = new VariantAttachmentWiki()) {
			restarted.attachments.putAll(attachments);
			List<Page> currentPages = new ArrayList<>();
			new TreeMap<>(pages).forEach((title, text) -> currentPages.add(new Page(title, text)));
			restarted.start(currentPages);
			return restarted.state();
		}
	}

	/**
	 * Creates the expected state: the attachment articles, followed by the compiled content of each variant as
	 * <code>"Variant: content-1, content-2"</code>.
	 */
	public static State expect(Collection<String> attachmentArticles, String... compiledByVariant) {
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
	 * Waits until the attachments stored and deleted so far are handled and compiled, which is done asynchronously,
	 * and then asserts that the supplied value equals the expected one.
	 */
	public <T> void await(T expected, Supplier<T> actual) throws InterruptedException {
		assertTrue("Attachment changes were not handled in time",
				manager.getAttachmentManager().awaitPendingChanges(30, TimeUnit.SECONDS));
		awaitCompilation();
		assertEquals(expected, actual.get());
	}

	/**
	 * The number of compilations of the wiki so far.
	 */
	public int compilations() {
		return compilations.get();
	}

	public List<TestVariantCompiler> variants() {
		List<TestVariantCompiler> variants = new ArrayList<>();
		for (Compiler compiler : manager.getCompilerManager().getCompilers()) {
			if (compiler instanceof TestVariantCompiler variant) variants.add(variant);
		}
		return variants;
	}

	public TestVariantCompiler variant(String name) {
		return variants().stream().filter(variant -> variant.getName().equals(name)).findFirst()
				.orElseThrow(() -> new AssertionError("No variant " + name));
	}

	public Environment getEnvironment() {
		return environment;
	}

	public VariableIndex variables() {
		return manager.getVariableIndex();
	}

	/**
	 * The messages of all compilers at the markups of the given page, as plain texts.
	 */
	public List<String> messages(String title) {
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

	/* ------------------------------------------------ types ------------------------------------------------ */

	/**
	 * Stands for an import markup: compiles one attachment for a literal wiki, or one attachment per value of a
	 * variable.
	 */
	static final class TestImportMarkup extends DefaultMarkupType implements AttachmentCompileType {

		private static final DefaultMarkup MARKUP = new DefaultMarkup("TestImport");

		static {
			MARKUP.addAnnotation("wiki", false);
			MARKUP.addAnnotation("wikiVariable", false);
			MARKUP.addAnnotation("file", true);
			PackageManager.addPackageAnnotation(MARKUP);
		}

		TestImportMarkup() {
			super(MARKUP);
		}

		@Nullable
		private static String variable(Section<?> section) {
			return Strings.trim(DefaultMarkupType.getAnnotation(section, "wikiVariable"));
		}

		private static String basePath(Section<?> section) {
			return section.getTitle() + "/" + Strings.trim(DefaultMarkupType.getAnnotation(section, "file"));
		}

		@Override
		public @Nullable WikiAttachment getCompiledAttachment(Section<? extends AttachmentCompileType> section) throws IOException {
			String path = getCompiledAttachmentPath(section);
			return path == null ? null : section.getEnvironment().getWikiConnector().getAttachment(path);
		}

		@Override
		public @Nullable String getCompiledAttachmentPath(Section<? extends AttachmentCompileType> section) {
			return variable(section) == null ? basePath(section) + ".txt" : null;
		}

		@Override
		public @NotNull Collection<String> getCompiledAttachmentPaths(Section<? extends AttachmentCompileType> section) {
			String variable = variable(section);
			if (variable == null) return List.of(basePath(section) + ".txt");
			VariableIndex index = VariableIndex.get(section);
			if (index == null) return List.of();
			return index.getValues(variable).stream().map(value -> basePath(section) + "@" + value + ".txt").toList();
		}

		@Override
		public @Nullable Collection<? extends Section<?>> getCompileConditionSections(Section<? extends AttachmentCompileType> section, String path) {
			String variable = variable(section);
			if (variable == null) return null;
			VariableIndex index = VariableIndex.get(section);
			if (index == null) return List.of();
			String value = path.substring(path.lastIndexOf('@') + 1, path.length() - ".txt".length());
			return index.getDefinitions(variable, value);
		}

		@Override
		public boolean isCompilingTheAttachment(Section<? extends AttachmentCompileType> section) {
			return true;
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
	 * The compiler of a variant. It only records the content of its packages, each time it has to compile.
	 */
	public static final class TestVariantCompiler extends AbstractPackageCompiler {

		private final List<Set<String>> history = new ArrayList<>();

		TestVariantCompiler(PackageManager packageManager, Section<? extends PackageCompileType> compileSection) {
			super(packageManager, compileSection, TestVariantMarkup.class);
		}

		@Override
		public synchronized void compilePackages(String[] packagesToCompile) {
			Set<String> contents = new TreeSet<>();
			for (Section<?> section : getPackageManager().getSectionsOfPackage(packagesToCompile)) {
				if (section.get() instanceof TestContentMarkup) contents.add(contentOf(section));
			}
			history.add(contents);
		}

		public synchronized Set<String> lastCompiled() {
			return history.isEmpty() ? Set.of() : history.get(history.size() - 1);
		}

		/**
		 * How often this variant had something to compile.
		 */
		public synchronized int compilations() {
			return history.size();
		}
	}

	/**
	 * The content compiled by the variants. The script does nothing, but tells the compiler of a variant that it has
	 * to compile if such a section is added to or removed from its packages.
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
					// recorded by the compiler itself
				}

				@Override
				public Class<TestVariantCompiler> getCompilerClass() {
					return TestVariantCompiler.class;
				}
			});
		}
	}

	private final class CompilationCounter implements Compiler {

		private CompilerManager compilerManager;

		@Override
		public void init(CompilerManager compilerManager) {
			this.compilerManager = compilerManager;
		}

		@Override
		public CompilerManager getCompilerManager() {
			return compilerManager;
		}

		@Override
		public boolean isCompiling(Section<?> section) {
			return true;
		}

		@Override
		public void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {
			compilations.incrementAndGet();
		}

		@Override
		public void destroy() {
		}
	}
}
