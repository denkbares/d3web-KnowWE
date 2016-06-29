package de.knowwe.ontology.turtle.lazyRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.CompilerRemovedEvent;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TermDefinitionRegisteredEvent;
import de.knowwe.core.compile.terminology.TermDefinitionUnregisteredEvent;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.event.CompilerEvent;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.parsing.Section;

public class LazyReferenceManager implements EventListener {

	public static LazyReferenceManager instance = null;

	private final Map<TermCompiler, Map<String, Set<Identifier>>> cache = new WeakHashMap<>();

	private LazyReferenceManager() {
	}

	public static LazyReferenceManager getInstance() {
		if (instance == null) {
			instance = new LazyReferenceManager();
			EventManager.getInstance().registerListener(instance);
		}
		return instance;
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
			cache.remove(compiler);
			if (event instanceof TermDefinitionRegisteredEvent) {
				TermDefinitionRegisteredEvent registeredEvent = (TermDefinitionRegisteredEvent) event;
				compileLazyReferences((TermCompiler) compiler, registeredEvent.getIdentifier());
			}
			if (event instanceof TermDefinitionUnregisteredEvent) {
				TermDefinitionUnregisteredEvent unregisteredEvent = (TermDefinitionUnregisteredEvent) event;
				destroyLazyReferences((TermCompiler) compiler, unregisteredEvent.getIdentifier());
			}
		}
	}

	private void destroyLazyReferences(TermCompiler compiler, Identifier identifier) {
		if (compiler instanceof IncrementalCompiler) {
			Collection<Section<?>> termReferenceSections = getTermReferenceSections(compiler, identifier);
			Compilers.addSectionsToDestroyAndCompile((IncrementalCompiler) compiler, termReferenceSections);
		}
	}

	private Collection<Section<?>> getTermReferenceSections(TermCompiler compiler, Identifier identifier) {
		String[] pathElements = identifier.getPathElements();
		if (pathElements.length != 2) return Collections.emptyList();
		return compiler.getTerminologyManager().getTermReferenceSections(new Identifier(pathElements[1]));
	}

	private void compileLazyReferences(TermCompiler compiler, Identifier identifier) {
		if (compiler instanceof IncrementalCompiler) {
			Compilers.addSectionsToCompile((IncrementalCompiler) compiler, getTermReferenceSections(compiler, identifier));
		}
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
		String suffix = identifier.getPathElementAt(1);
		Set<Identifier> suffixSet = suffixMapping.get(suffix);
		if (suffixSet == null) {
			suffixSet = new HashSet<>(4);
			suffixMapping.put(suffix, suffixSet);
		}
		suffixSet.add(identifier);
	}

}
