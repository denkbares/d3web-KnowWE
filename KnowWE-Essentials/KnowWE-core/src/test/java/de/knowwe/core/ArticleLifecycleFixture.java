/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.rules.ExternalResource;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.parsing.Parser;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.event.InitializedArticlesEvent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Runs the real article manager, parser, ID registry, attachment manager and asynchronous compiler manager without
 * booting a wiki or loading plugins. Each nonempty line is a section; {@code attachment: Page/file.txt} additionally
 * exercises the real attachment lifecycle. Reflection is confined to bootstrapping/restoring the Environment singleton
 * and closing private executors: article construction, replacement, rollback and ID operations use production APIs.
 * These tests share the Environment singleton and must not run concurrently in the same JVM.
 */
public final class ArticleLifecycleFixture extends ExternalResource {

	DefaultArticleManager manager;
	final RecordingCompiler compiler = new RecordingCompiler();
	final Map<String, String> attachments = new ConcurrentHashMap<>();
	Consumer<Section<?>> afterParse = section -> { };

	private Object previousEnvironment;
	private boolean previouslyInitialized;
	private RootType root;
	private final Set<Section<?>> parsedRoots = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
	private final List<EventListener> listeners = new ArrayList<>();

	@Override
	protected void before() throws Exception {
		previousEnvironment = field(Environment.class, "instance").get(null);
		previouslyInitialized = field(Environment.class, "initialized").getBoolean(null);
		field(Environment.class, "initialized").setBoolean(null, false);
		Constructor<Environment> constructor = Environment.class.getDeclaredConstructor(WikiConnector.class);
		constructor.setAccessible(true);
		WikiConnector connector = (WikiConnector) Proxy.newProxyInstance(WikiConnector.class.getClassLoader(),
				new Class<?>[] { WikiConnector.class }, (proxy, method, args) -> {
					if (method.getName().equals("getAttachment")) return attachment((String) args[0]);
					throw new UnsupportedOperationException("Unexpected wiki access: " + method.getName());
				});
		Environment environment = constructor.newInstance(connector);
		field(Environment.class, "instance").set(null, environment);
		root = new RootType() {
			@Override
			public Parser getParser() {
				Parser parser = super.getParser();
				return (text, parent) -> {
					Section<?> parsed = parser.parse(text, parent);
					parsedRoots.add(parsed);
					afterParse.accept(parsed);
					return parsed;
				};
			}
		};
		root.addChildType(0, new AttachmentLine());
		root.addChildType(1, new Line());
		field(Environment.class, "rootType").set(environment, root);
		manager = (DefaultArticleManager) environment.getArticleManager("lifecycle-" + UUID.randomUUID());
		manager.getCompilerManager().addCompiler(1, compiler);
		manager.setInitialized(true);
		// Only initialize this manager's attachments, not unrelated global listeners/script managers.
		manager.getAttachmentManager().notify(new InitializedArticlesEvent(manager));
		field(Environment.class, "initialized").setBoolean(null, true);
	}

	Article register(String title, String text) throws InterruptedException {
		Article article = manager.registerArticle(title, text);
		assertNotNull("Article creation failed", article);
		awaitCompilation();
		return article;
	}

	Article draft(String title, String text) {
		Article article = Article.createArticle(text, title, manager);
		assertNotNull("Draft creation failed", article);
		return article;
	}

	void awaitCompilation() throws InterruptedException {
		manager.getCompilerManager().awaitTermination();
	}

	<E extends Event> void on(Class<E> eventType, Consumer<E> handler) {
		EventListener listener = new EventListener() {
			@Override
			public Collection<Class<? extends Event>> getEvents() {
				return List.of(eventType);
			}

			@Override
			public void notify(Event event) {
				handler.accept(eventType.cast(event));
			}
		};
		listeners.add(listener); // retain strongly until explicitly unregistered in teardown
		EventManager.getInstance().registerListener(listener);
	}

	static Section<?> line(Article article, int index) {
		return article.getRootSection().getChildren().get(index);
	}

	/** No disk or network access: only operations used by AttachmentManager are supported. */
	private WikiAttachment attachment(String path) {
		if (!attachments.containsKey(path)) return null;
		return (WikiAttachment) Proxy.newProxyInstance(WikiAttachment.class.getClassLoader(),
				new Class<?>[] { WikiAttachment.class }, (proxy, method, args) -> switch (method.getName()) {
					case "getPath" -> path;
					case "getInputStream" -> new ByteArrayInputStream(attachments.get(path).getBytes(StandardCharsets.UTF_8));
					default -> throw new UnsupportedOperationException("Unexpected attachment access: " + method.getName());
				});
	}

	@Override
	protected void after() {
		try {
			listeners.forEach(EventManager.getInstance()::unregister);
			awaitCompilation();
			afterParse = section -> { };
			// Empty the manager while its attachment listener is still available to clear attachment mappings as well.
			manager.removeAllArticles();
			awaitCompilation();
			manager.getCompilerManager().removeCompiler(compiler);
			closeManager(manager);
			// Also visit partial parser results and unpublished drafts; the manager does not own all of them.
			parsedRoots.forEach(ArticleLifecycleFixture::unregisterTree);
			root.clearCompileScripts();
		}
		catch (Exception e) {
			throw new AssertionError("Failed to clean up the article lifecycle fixture", e);
		}
		finally {
			try {
				field(Environment.class, "instance").set(null, previousEnvironment);
				field(Environment.class, "initialized").setBoolean(null, previouslyInitialized);
			}
			catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}
	}

	private static void unregisterTree(Section<?> section) {
		Messages.unregisterMessagesSection(section);
		Section.unregisterOrUpdateSectionID(section, null);
		section.getChildren().forEach(ArticleLifecycleFixture::unregisterTree);
	}

	/** Also used by the older frame tests, whose otherwise retained listeners react to later attachment tests. */
	public static void closeManager(DefaultArticleManager manager) throws ReflectiveOperationException, InterruptedException {
		EventManager.getInstance().unregister(manager.getAttachmentManager());
		EventManager.getInstance().unregister(manager.getCompilerManager());
		shutdown((ExecutorService) field(CompilerManager.class, "threadPool").get(manager.getCompilerManager()));
		shutdown((ExecutorService) field(AttachmentManager.class, "executor").get(manager.getAttachmentManager()));
	}

	private static void shutdown(ExecutorService executor) throws InterruptedException {
		executor.shutdownNow();
		assertTrue("Test executor did not terminate", executor.awaitTermination(5, TimeUnit.SECONDS));
	}

	private static Field field(Class<?> type, String name) throws NoSuchFieldException {
		Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	private static class Line extends AbstractType {
		Line() {
			super(new RegexSectionFinder("(?m)^.+(?:\\n|$)"));
		}
	}

	private final class AttachmentLine extends AbstractType implements AttachmentCompileType {
		AttachmentLine() {
			super(new RegexSectionFinder("(?m)^attachment: .+(?:\\n|$)"));
		}

		@Override
		public WikiAttachment getCompiledAttachment(Section<? extends AttachmentCompileType> section) {
			return attachment(getCompiledAttachmentPath(section));
		}

		@Override
		public String getCompiledAttachmentPath(Section<? extends AttachmentCompileType> section) {
			return section.getText().substring("attachment: ".length()).trim();
		}

		@Override
		public boolean isCompilingTheAttachment(Section<? extends AttachmentCompileType> section) {
			return true;
		}
	}

	record Compilation(List<Section<?>> added, List<Section<?>> removed) { }

	/**
	 * Captures actual compile inputs by identity. The small contribution ledger deliberately removes by Section identity,
	 * so stale or missing destroy inputs cannot be hidden by replacing everything with the same article title.
	 */
	static final class RecordingCompiler implements Compiler {
		private CompilerManager manager;
		private final List<Compilation> history = new ArrayList<>();
		private final Map<Section<?>, List<String>> contributions = new IdentityHashMap<>();

		@Override
		public void init(CompilerManager manager) {
			this.manager = manager;
		}

		@Override
		public CompilerManager getCompilerManager() {
			return manager;
		}

		@Override
		public boolean isCompiling(Section<?> section) {
			return true;
		}

		@Override
		public synchronized void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {
			history.add(new Compilation(List.copyOf(added), List.copyOf(removed)));
			removed.forEach(contributions::remove);
			for (Section<?> section : added) {
				contributions.put(section, section.getText().lines().map(String::trim).filter(s -> !s.isEmpty()).toList());
			}
		}

		synchronized Compilation lastCompilation() {
			return history.get(history.size() - 1);
		}

		synchronized int compilationCount() {
			return history.size();
		}

		synchronized Map<String, Long> contributions() {
			return contributions.values().stream().flatMap(Collection::stream)
					.collect(Collectors.groupingBy(s -> s, Collectors.counting()));
		}

		@Override
		public void destroy() {
		}
	}
}
