package de.knowwe.core.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.collections.PriorityList;
import de.knowwe.core.kdom.Type;
import de.knowwe.event.InitEvent;

/**
 * Class to manage all the compile scripts of a specific compiler type.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 30.10.2013
 */
public class ScriptManager<C extends Compiler> implements EventListener {

	private static class ScriptList<C extends Compiler, T extends Type>
			extends PriorityList<Priority, CompileScript<C, T>> {

		public ScriptList() {
			super(Priority.DEFAULT);
		}
	}

	@SuppressWarnings("rawtypes")
	private static final ScriptList EMPTY_SCRIPT_LIST = new ScriptList();

	private final Class<C> compilerClass;

	@SuppressWarnings("rawtypes")
	private final Map<Type, ScriptList> scripts = Collections.synchronizedMap(new HashMap<>());

	private final Set<Type> subtreeTypesWithScripts = Collections.synchronizedSet(new HashSet<>());

	private boolean initialized = false;

	public ScriptManager(Class<C> compilerClass) {
		this.compilerClass = compilerClass;
		EventManager.getInstance().registerListener(this);
	}

	/**
	 * Adds a script for a specific type with a specific priority.
	 * 
	 * @created 30.10.2013
	 * @param priority the priority of the script
	 * @param type the type the script is intended for
	 * @param script the script to be added
	 */
	@SuppressWarnings({
			"rawtypes", "unchecked" })
	public <T extends Type> void addScript(Priority priority, T type, CompileScript<C, T> script) {
		if (initialized) {
			// adding scripts after initialization is a very bad idea, it can cause memory leaks (e.g. adding
			// a new script with every compilation?), and also destroy various optimizations for KDOM operation
			throw new UnsupportedOperationException("Adding scripts after initialization is not supported!");
		}
		// find map and create lazy if not exists
		ScriptList list = scripts.computeIfAbsent(type, k -> new ScriptList());

		// add script to list
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (list) {
			list.add(priority, script);
		}
	}


	private void addSubtreeTypesWithScripts(Type type) {
		if (!subtreeTypesWithScripts.add(type)) return;
		for (Type parent : type.getParentTypes()) {
			addSubtreeTypesWithScripts(parent);
		}
	}

	@SuppressWarnings({
			"rawtypes", "unchecked" })
	public <T extends Type> Map<Priority, List<CompileScript<C, T>>> getScripts(T type) {
		ScriptList list = scripts.get(type);
		if (list == null) list = EMPTY_SCRIPT_LIST;
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (list) {
			return list.getPriorityMap();
		}
	}

	public Collection<Type> getTypes() {
		return Collections.unmodifiableSet(scripts.keySet());
	}

	public boolean hasScriptsForSubtree(Type type) {
		return subtreeTypesWithScripts.contains(type);
	}

	public <T extends Type> void removeScript(T type, Class<? extends CompileScript<C, T>> clazz) {
		@SuppressWarnings("unchecked")
		ScriptList<C, T> scriptsOfType = scripts.get(type);
		List<CompileScript<C, T>> remove = new ArrayList<>();
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (scriptsOfType) {
			for (CompileScript<C, T> compileScript : scriptsOfType) {
				if (compileScript.getClass().equals(clazz)) {
					remove.add(compileScript);
				}
			}
			scriptsOfType.removeAll(remove);
		}
	}

	public <T extends Type> void removeAllScript(T type) {
		scripts.remove(type);
	}

	public Class<C> getCompilerClass() {
		return compilerClass;
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		List<Class<? extends Event>> events = new ArrayList<>();
		events.add(InitEvent.class);
		return events;
	}

	@Override
	public void notify(Event event) {
		if (event instanceof InitEvent) {
			// we have to do this after type are initialized, because while
			// scripts are added to the manager, not all parent-child-links may
			// be established
			initialized = true;
			for (Type type : getTypes()) {
				addSubtreeTypesWithScripts(type);
			}
		}

	}

}
