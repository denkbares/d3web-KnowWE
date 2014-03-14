package de.knowwe.ontology.turtle.lazyRef;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.CompilerFinishedEvent;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TermDefinitionRegisteredEvent;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;

public class LazyReferenceManager implements EventListener {

	public static LazyReferenceManager instance = null;

	private Set<Identifier> addedIdentifiers = Collections.synchronizedSet(new HashSet<Identifier>());

	private LazyReferenceManager() {
		//
	}

	public static LazyReferenceManager getInstance() {
		if (instance == null) {
			instance = new LazyReferenceManager();
			EventManager.getInstance().registerListener(instance);
		}
		return instance;
	}

	private final Map<TermCompiler, Map<String, Set<Identifier>>> data
			= new WeakHashMap<TermCompiler, Map<String, Set<Identifier>>>();

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		Set<Class<? extends Event>> result = new HashSet<Class<? extends Event>>();
		result.add(CompilerFinishedEvent.class);
		result.add(TermDefinitionRegisteredEvent.class);
		return result;
	}

	@Override
	public synchronized void notify(Event event) {
		if (event instanceof CompilerFinishedEvent) {

			Object compiler = ((CompilerFinishedEvent<?>) event).getCompiler();
		/*
		 * clear cache for this compiler
		 */
			if (data.containsKey(compiler)) {
				data.remove(compiler);
			}
		}

		if (event instanceof TermDefinitionRegisteredEvent) {
			invalidateCache(((TermDefinitionRegisteredEvent) event).getIdentifier());
		}
	}

	private void invalidateCache(Identifier identifier) {
		this.addedIdentifiers.add(identifier);
	}

	public synchronized Set<Identifier> getData(TermCompiler termCompiler, String lazyTermName) {
		if (termCompiler == null) return null;
		if (!data.containsKey(termCompiler)) {
			/*
			 * build up cache once
			 */
			createCache(termCompiler);
		}
		else if (this.addedIdentifiers.size() > 0) {
			updateCache(termCompiler);

		}
		return data.get(termCompiler).get(lazyTermName);
	}

	private void updateCache(TermCompiler c) {
		Map<String, Set<Identifier>> termData = data.get(c);
		for (Identifier identifier : addedIdentifiers) {
			if (identifier.getPathElements().length == 2) {
				insertTerm(termData, identifier);
			}
		}
		addedIdentifiers.clear();
	}

	private void createCache(TermCompiler termCompiler) {
		TerminologyManager terminologyManager = termCompiler.getTerminologyManager();
		Collection<Identifier> allDefinedTerms = terminologyManager.getAllDefinedTerms();
		Map<String, Set<Identifier>> map = data.get(termCompiler);
		if (map == null) {
			map = new HashMap<String, Set<Identifier>>();
			data.put(termCompiler, map);
		}
		for (Identifier identifier : allDefinedTerms) {
			if (identifier.getPathElements().length == 2) {
				insertTerm(map, identifier);
			}
		}
	}

	private void insertTerm(Map<String, Set<Identifier>> map, Identifier identifier) {
		String suffix = identifier.getPathElementAt(1);
		Set<Identifier> suffixSet = map.get(suffix);
		if (suffixSet == null) {
			suffixSet = new HashSet<Identifier>();
			map.put(suffix, suffixSet);
		}
		suffixSet.add(identifier);
	}

}
