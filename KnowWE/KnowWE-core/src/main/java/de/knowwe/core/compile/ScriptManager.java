package de.knowwe.core.compile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.collections.PriorityList;
import de.knowwe.core.kdom.Type;

/**
 * Class to manage all the compile scripts of a specific compiler type.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 30.10.2013
 */
public class ScriptManager<C extends Compiler> {

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
	private final Map<Type, ScriptList> scripts = new HashMap<Type, ScriptList>();

	public ScriptManager(Class<C> compilerClass) {
		this.compilerClass = compilerClass;
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
		// find map and create lazy if not exists
		ScriptList list = scripts.get(type);
		if (list == null) {
			list = new ScriptList();
			scripts.put(type, list);
		}

		// add script to list
		list.add(priority, script);
	}

	@SuppressWarnings({
			"rawtypes", "unchecked" })
	public <T extends Type> Map<Priority, List<CompileScript<C, T>>> getScripts(T type) {
		ScriptList list = scripts.get(type);
		if (list == null) list = EMPTY_SCRIPT_LIST;
		return list.getPriorityMap();
	}

	public Class<C> getCompilerClass() {
		return compilerClass;
	}

}
