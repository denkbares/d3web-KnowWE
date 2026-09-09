package de.knowwe.ontology.turtle.lazyRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.compile.CompilerRemovedEvent;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TermDefinitionRegisteredEvent;
import de.knowwe.core.compile.terminology.TermDefinitionUnregisteredEvent;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.event.CompilerEvent;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;

public class LazyReferenceManager implements EventListener, AutoCloseable {

	private final Environment environment;

	private final Map<TermCompiler, Map<String, Set<Identifier>>> cache = new WeakHashMap<>();

	private LazyReferenceManager(@NotNull Environment environment) {
		this.environment = environment;
	}

	/**
	 * Returns the lazy reference manager of the specified environment, created on first access and closed together
	 * with the environment.
	 */
	@NotNull
	public static LazyReferenceManager of(@NotNull Environment environment) {
		return environment.getService(LazyReferenceManager.class, () -> {
			LazyReferenceManager manager = new LazyReferenceManager(environment);
			environment.getEventManager().registerListener(manager);
			return manager;
		});
	}

	/**
	 * Returns the lazy reference manager responsible for the specified compiler, i.e. the one of the compiler's
	 * environment.
	 */
	@NotNull
	public static LazyReferenceManager of(@NotNull Compiler compiler) {
		return of(KnowWEUtils.getEnvironment(compiler));
	}

	/**
	 * Returns the lazy reference manager responsible for the specified section, i.e. the one of the section's
	 * environment.
	 */
	@NotNull
	public static LazyReferenceManager of(@NotNull Section<?> section) {
		return of(KnowWEUtils.getEnvironment(section));
	}

	/**
	 * @deprecated use {@link #of(Environment)} or its siblings with the environment at hand
	 */
	@Deprecated
	public static LazyReferenceManager getInstance() {
		return of(Environment.requireInstance());
	}

	@Override
	public void close() {
		environment.getEventManager().unregister(this);
		synchronized (this) {
			cache.clear();
		}
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		Collection<Class<? extends Event>> result = new ArrayList<>();
		result.add(CompilerRemovedEvent.class);
		result.add(TermDefinitionRegisteredEvent.class);
		result.add(TermDefinitionUnregisteredEvent.class);
		return result;
	}

	@Override
	public synchronized void notify(Event event) {
		if (event instanceof CompilerEvent) {
			Compiler compiler = ((CompilerEvent) event).getCompiler();
			if (compiler instanceof TermCompiler) {
				if (cache.remove(compiler) != null) {
					if (!(compiler instanceof IncrementalCompiler)) return;
					if (event instanceof TermDefinitionRegisteredEvent) {
						handleRegistration((TermDefinitionRegisteredEvent) event, (IncrementalCompiler) compiler);
					}
					if (event instanceof TermDefinitionUnregisteredEvent) {
						handleUnregistration((TermDefinitionUnregisteredEvent) event, (IncrementalCompiler) compiler);
					}
				}
			}
		}
	}

	public void handleUnregistration(TermDefinitionUnregisteredEvent event, IncrementalCompiler compiler) {
		Identifier identifier = toLazyIdentifier(event.getIdentifier());
		if (identifier != null) {
			Compilers.destroyAndRecompileReferences(compiler, identifier);
		}
	}

	public void handleRegistration(TermDefinitionRegisteredEvent event, IncrementalCompiler compiler) {
		Identifier identifier = toLazyIdentifier(event.getIdentifier());
		if (identifier != null) {
			Sections.references((TermCompiler) compiler, identifier).forEach(compiler::addSectionToCompile);
		}
	}

	@Nullable
	private Identifier toLazyIdentifier(Identifier identifier) {
		String[] pathElements = identifier.getPathElements();
		if (pathElements.length != 2) return null;
		return new Identifier(pathElements[1]);
	}

	public synchronized Set<Identifier> getPotentialMatches(TermCompiler termCompiler, String lazyTermName) {
		if (termCompiler == null) return Collections.emptySet();
		Map<String, Set<Identifier>> suffixMapping = cache.get(termCompiler);
		if (suffixMapping == null) {
			suffixMapping = new HashMap<>();
			cache.put(termCompiler, suffixMapping);
			createCache(termCompiler, suffixMapping);
		}
		Set<Identifier> identifiers = suffixMapping.get(lazyTermName);
		if (identifiers == null) identifiers = Collections.emptySet();
		return identifiers;
	}

	private void createCache(TermCompiler termCompiler, Map<String, Set<Identifier>> identifierMapping) {
		TerminologyManager terminologyManager = termCompiler.getTerminologyManager();
		Collection<Identifier> allDefinedTerms = terminologyManager.getAllDefinedTerms();
		for (Identifier identifier : allDefinedTerms) {
			if (identifier.getPathElements().length == 2) {
				insertTerm(identifierMapping, identifier);
			}
		}
	}

	private void insertTerm(Map<String, Set<Identifier>> suffixMapping, Identifier identifier) {
		suffixMapping.computeIfAbsent(identifier.getPathElementAt(1), k -> new HashSet<>(4)).add(identifier);
	}
}
