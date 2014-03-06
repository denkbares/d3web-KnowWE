package de.knowwe.ontology.turtle.lazyRef;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.CompilerFinishedEvent;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;

public class LazyReferenceManager implements EventListener {

	public static LazyReferenceManager instance = null;

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

	private final Map<de.knowwe.core.compile.Compiler, Map<String, Set<Identifier>>> data = new HashMap<de.knowwe.core.compile.Compiler, Map<String, Set<Identifier>>>();

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		Set<Class<? extends Event>> result = new HashSet<Class<? extends Event>>();
		result.add(CompilerFinishedEvent.class);
		return result;
	}

	@Override
	public void notify(Event event) {
		Object compiler = ((CompilerFinishedEvent<?>) event).getCompiler();
		/*
		 * clear cache for this compiler
		 */
		if (data.containsKey(compiler)) {
			data.remove(compiler);
		}
	}

	public Set<Identifier> getData(de.knowwe.core.compile.Compiler c, String lazyTermName) {
		if (c == null) return null;
		if (!data.containsKey(c)) {
			/*
			 * build up cache once
			 */
			createCache(c);
		}
		return data.get(c).get(lazyTermName);
	}

	private void createCache(de.knowwe.core.compile.Compiler c) {
		if (c instanceof TermCompiler) {
			TerminologyManager terminologyManager = ((TermCompiler) c).getTerminologyManager();
			Collection<Identifier> allDefinedTerms = terminologyManager.getAllDefinedTerms();
			Map<String, Set<Identifier>> map = data.get(c);
			if (map == null) {
				map = new HashMap<String, Set<Identifier>>();
				data.put(c, map);
			}

			for (Identifier identifier : allDefinedTerms) {
				if (identifier.getPathElements().length == 2) {
					String suffix = identifier.getPathElementAt(1);
					Set<Identifier> suffixSet = map.get(suffix);
					if (suffixSet == null) {
						suffixSet = new HashSet<Identifier>();
						map.put(suffix, suffixSet);
					}
					suffixSet.add(identifier);
				}
			}
		}
	}

}
