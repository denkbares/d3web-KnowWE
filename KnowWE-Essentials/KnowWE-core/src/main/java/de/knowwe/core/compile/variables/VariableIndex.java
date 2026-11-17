/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.compile.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.compile.CompilationFinishedEvent;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.event.ArticleRegisteredEvent;

/**
 * Index of the variables defined with {@link InterWikiVariableMarkup} in the articles of an article manager.
 * <p>
 * The index is updated when an article is registered, not when it is compiled, and reads the definitions directly
 * from the KDOM. It therefore already answers while the articles of the wiki are still being registered, before the
 * first compilation. This is required for everything that decides what gets compiled, most notably the attachments
 * compiled by an import markup that uses a variable.
 * <p>
 * There are two views on a variable: all its definitions in the wiki ({@link #getDefinitions(String)}), and the
 * definitions compiled by a certain compiler ({@link #getDefinitions(String, Compiler)}), which are the ones valid for
 * the variant of that compiler. The second view depends on the packages and is only reliable after the packages are
 * registered.
 * <p>
 * Reading is lock-free. After a change of definitions the index calls
 * {@link de.knowwe.core.AttachmentManager#refreshCompiledAttachmentPaths()}, always <b>after</b> the change is
 * visible to readers and without holding a lock of its own. The attachment manager in turn first registers a section
 * and then reads the variables. Because both sides write first and read afterward, the attachments to compile do not
 * depend on the order in which the article with the definition and the article using the variable are registered.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public class VariableIndex implements EventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(VariableIndex.class);

	/** From this time spent since the previous compilation on, the statistics are logged as info instead of debug. */
	private static final long LOG_THRESHOLD_MILLIS = 50;

	private final DefaultArticleManager articleManager;

	// what this index was used for since the last compilation has finished, logged with each compilation
	private final LongAdder updates = new LongAdder();
	private final LongAdder updateNanos = new LongAdder();
	private final LongAdder refreshes = new LongAdder();
	private final LongAdder refreshNanos = new LongAdder();
	private final LongAdder lookups = new LongAdder();
	private final LongAdder lookupNanos = new LongAdder();

	/**
	 * The definitions of each article having some, by the lower case title of the article. The map is sorted and the
	 * definitions of an article are in the order of the article, so the definitions are always found in the same
	 * order without sorting them for each request. The entry remembers the article version it was read from, see
	 * {@link #getDefinitions(String, Definitions)}.
	 */
	private final Map<String, Definitions> definitionsByArticle = new ConcurrentSkipListMap<>();

	/**
	 * A definition with its name and value, read once from the annotations when the article is registered, as they
	 * are asked for very often, e.g. for each markup of an imported article when its packages are registered.
	 */
	private record Definition(Section<InterWikiVariableMarkup> section, String name, String value) {
	}

	private record Definitions(Article article, List<Definition> definitions) {
	}

	public VariableIndex(@NotNull DefaultArticleManager articleManager) {
		this.articleManager = articleManager;
		EventManager eventManager = KnowWEUtils.getEventManager(articleManager);
		synchronized (eventManager) {
			eventManager.registerListener(this, EventManager.RegistrationType.WEAK);
		}
	}

	/**
	 * Returns the variable index of the given article manager, or null if the manager does not provide one.
	 */
	@Nullable
	public static VariableIndex get(@Nullable ArticleManager articleManager) {
		return articleManager instanceof DefaultArticleManager defaultArticleManager
				? defaultArticleManager.getVariableIndex() : null;
	}

	/**
	 * Returns the variable index of the wiki the given section belongs to, or null if there is none.
	 */
	@Nullable
	public static VariableIndex get(@NotNull Section<?> section) {
		return get(section.getArticleManager());
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return List.of(ArticleRegisteredEvent.class, CompilationFinishedEvent.class);
	}

	@Override
	public void notify(Event event) {
		if (event instanceof ArticleRegisteredEvent articleRegisteredEvent) {
			update(articleRegisteredEvent.getArticle());
		}
		else if (event instanceof CompilationFinishedEvent) {
			logStatistics();
		}
	}

	/**
	 * Logs how often this index was updated and asked since the last compilation has finished, and how long that
	 * took in total. The lookups are done by many threads, so their time may exceed the time of the compilation.
	 */
	private void logStatistics() {
		long updateCount = updates.sumThenReset();
		long refreshCount = refreshes.sumThenReset();
		long lookupCount = lookups.sumThenReset();
		long updateMillis = TimeUnit.NANOSECONDS.toMillis(updateNanos.sumThenReset());
		long refreshMillis = TimeUnit.NANOSECONDS.toMillis(refreshNanos.sumThenReset());
		long lookupMillis = TimeUnit.NANOSECONDS.toMillis(lookupNanos.sumThenReset());
		if (refreshCount == 0 && lookupCount == 0) return;
		String message = "Wiki variables since the last compilation: " + updateCount + " articles checked for definitions in "
				+ updateMillis + "ms, " + refreshCount + " refreshes of the compiled attachments in " + refreshMillis
				+ "ms, " + lookupCount + " lookups of definitions in " + lookupMillis + "ms ("
				+ definitionsByArticle.size() + " articles with definitions)";
		if (updateMillis + refreshMillis + lookupMillis >= LOG_THRESHOLD_MILLIS) {
			LOGGER.info(message);
		}
		else {
			LOGGER.debug(message);
		}
	}

	private void update(Article article) {
		long start = System.nanoTime();
		String key = article.getTitle().toLowerCase();
		List<Definition> definitions = readDefinitions(article);
		Definitions previous = definitions.isEmpty()
				? definitionsByArticle.remove(key)
				: definitionsByArticle.put(key, new Definitions(article, definitions));
		updates.increment();
		updateNanos.add(System.nanoTime() - start);
		if (previous == null && definitions.isEmpty()) return;

		// The definitions of this article have changed (or at least their sections, which also decide about the
		// packages of dependent attachments). Readers already see the new state, so we can ask for the refresh now.
		long refreshStart = System.nanoTime();
		articleManager.getAttachmentManager().refreshCompiledAttachmentPaths();
		refreshes.increment();
		refreshNanos.add(System.nanoTime() - refreshStart);
	}

	/**
	 * Reads the definitions of the given article, in the order of the article. Definitions without name or value
	 * define nothing and are left out.
	 */
	@NotNull
	private List<Definition> readDefinitions(Article article) {
		// content of compiled attachments does not define variables: which attachments are compiled depends on the
		// variables, so this would be a cycle
		if (articleManager.getAttachmentManager().isAttachmentArticle(article)) return List.of();
		List<Section<InterWikiVariableMarkup>> sections = Sections.successors(article, InterWikiVariableMarkup.class);
		if (sections.isEmpty()) return List.of();
		List<Definition> definitions = new ArrayList<>(sections.size());
		for (Section<InterWikiVariableMarkup> section : sections) {
			String name = InterWikiVariableMarkup.getVariableName(section);
			String value = InterWikiVariableMarkup.getVariableValue(section);
			if (name != null && value != null) definitions.add(new Definition(section, name, value));
		}
		return List.copyOf(definitions);
	}

	/**
	 * Returns the definitions of the current version of the article with the given key. Usually these are the indexed
	 * ones. If the article was replaced without this index being notified yet (or at all, e.g. after a rollback), the
	 * definitions are read from the current version instead of returning outdated sections.
	 */
	@NotNull
	private List<Definition> getDefinitions(String key, Definitions indexed) {
		Article current = articleManager.getArticle(key);
		if (current == indexed.article()) return indexed.definitions();
		if (current == null) return List.of();
		return readDefinitions(current);
	}

	/**
	 * Returns all definitions of the variable with the given name in the wiki, independent of packages and compilers,
	 * in a stable order: by the title of their article and their position in it.
	 *
	 * @param name the name of the variable
	 * @return the sections defining the variable, empty if it is not defined
	 */
	@NotNull
	public List<Section<InterWikiVariableMarkup>> getDefinitions(@NotNull String name) {
		return getDefinitions(name, (String) null);
	}

	/**
	 * Returns the definitions of the variable with the given name that define the given value.
	 *
	 * @param name  the name of the variable
	 * @param value the value to get the definitions for
	 * @return the sections defining the variable with that value
	 */
	@NotNull
	public List<Section<InterWikiVariableMarkup>> getDefinitions(@NotNull String name, @Nullable String value) {
		long start = System.nanoTime();
		List<Section<InterWikiVariableMarkup>> result = new ArrayList<>();
		for (Map.Entry<String, Definitions> entry : definitionsByArticle.entrySet()) {
			for (Definition definition : getDefinitions(entry.getKey(), entry.getValue())) {
				if (!name.equals(definition.name())) continue;
				if (value != null && !value.equals(definition.value())) continue;
				result.add(definition.section());
			}
		}
		lookups.increment();
		lookupNanos.add(System.nanoTime() - start);
		return result;
	}

	/**
	 * Returns the definitions of the variable with the given name that are compiled by the given compiler, i.e. the
	 * ones valid for the variant of that compiler. Only reliable after the packages are registered.
	 *
	 * @param name     the name of the variable
	 * @param compiler the compiler to get the definitions for
	 * @return the sections defining the variable for the compiler
	 */
	@NotNull
	public List<Section<InterWikiVariableMarkup>> getDefinitions(@NotNull String name, @NotNull Compiler compiler) {
		List<Section<InterWikiVariableMarkup>> definitions = getDefinitions(name);
		definitions.removeIf(definition -> !compiler.isCompiling(definition));
		return definitions;
	}

	/**
	 * Returns the distinct values of the variable with the given name over all its definitions in the wiki, in a
	 * stable order.
	 *
	 * @param name the name of the variable
	 * @return the values of the variable, empty if it is not defined
	 */
	@NotNull
	public Set<String> getValues(@NotNull String name) {
		return toValues(getDefinitions(name));
	}

	/**
	 * Returns the distinct values of the variable with the given name for the given compiler. A properly defined
	 * variable has exactly one value per compiler.
	 *
	 * @param name     the name of the variable
	 * @param compiler the compiler to get the values for
	 * @return the values of the variable for the compiler
	 * @see #getDefinitions(String, Compiler)
	 */
	@NotNull
	public Set<String> getValues(@NotNull String name, @NotNull Compiler compiler) {
		return toValues(getDefinitions(name, compiler));
	}

	@NotNull
	private static Set<String> toValues(List<Section<InterWikiVariableMarkup>> definitions) {
		Set<String> values = new LinkedHashSet<>();
		for (Section<InterWikiVariableMarkup> definition : definitions) {
			values.add(InterWikiVariableMarkup.getVariableValue(definition));
		}
		return values;
	}
}
