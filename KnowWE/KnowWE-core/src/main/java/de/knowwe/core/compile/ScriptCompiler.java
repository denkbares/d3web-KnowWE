package de.knowwe.core.compile;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;

import de.d3web.utils.Log;
import de.d3web.utils.Pair;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;

/**
 * For the given {@link Section}s and {@link Compiler} it gets all
 * {@link CompileScript}s and then compiles all Sections in the order, given by
 * Priority of the scripts and position in the KDOM (Article).
 *
 * @param <C> the Compiler for which we want to compile
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class ScriptCompiler<C extends Compiler> {

	private final TreeMap<Priority, LinkedHashSet<CompilePair>> priorityMap;

	private Priority currentPriority;

	private Iterator<CompilePair> currentIterator = null;

	@SuppressWarnings("rawtypes")
	private final Class[] typeFilter;

	private final C compiler;

	public ScriptCompiler(C compiler) {
		this(compiler, null);

	}

	@SuppressWarnings("unchecked")
	public ScriptCompiler(C compiler, Class<? extends Type>... filter) {
		this.compiler = compiler;
		this.typeFilter = filter;
		if (this.typeFilter == null) {
			// if no typeFilter is given, we create one automatically by using the
			// types compiled by the given compiler
			Collection<Type> types = CompilerManager.getScriptManager(compiler).getTypes();
			// my guess is that, if we have more than 20 types, the filtering is
			// no longer efficient and we can just traverse the complete KDOM
			if (types.size() < 20) {
				filter = new Class[types.size()];
				int i = 0;
				for (Type type : types) {
					filter[i++] = type.getClass();
				}
			}
		}
		priorityMap = new TreeMap<Priority, LinkedHashSet<CompilePair>>();
		for (Priority p : Priority.getRegisteredPriorities()) {
			priorityMap.put(p, new LinkedHashSet<CompilePair>());
		}
		currentPriority = Priority.getRegisteredPriorities().first();
	}

	/**
	 * Adds a single section to the iterator. Successors of the section are not
	 * added.<br/>
	 * Optionally you can add a filter to only add scripts of the given classes. If no filter is given, all scripts
	 * available for the type of the section and the compiler are added.<p>
	 * You can use this method while the article of this iterator is compiled.
	 * The iterator will continue iterating over its sections in the correct
	 * order also considering the newly added.
	 *
	 * @param section      the section you want to add
	 * @param scriptFilter the classes of the scripts you want to add
	 * @created 27.07.2012
	 */
	public void addSection(Section<?> section, Class<? extends CompileScript>... scriptFilter) {
		@SuppressWarnings("unchecked")
		ScriptManager<C> scriptManager = CompilerManager.getScriptManager((Class<C>) compiler.getClass());
		Map<Priority, List<CompileScript<C, Type>>> scripts =
				scriptManager.getScripts(Sections.cast(section, Type.class).get());
		for (Entry<Priority, List<CompileScript<C, Type>>> entry : scripts.entrySet()) {
			Priority priority = entry.getKey();
			LinkedHashSet<CompilePair> prioritySet = priorityMap.get(priority);
			for (CompileScript<C, Type> script : entry.getValue()) {
				if (scriptFilter.length > 0 && !ArrayUtils.contains(scriptFilter, script.getClass())) continue;
				CompilePair pair = new CompilePair(Sections.cast(section, Type.class), script);
				if (prioritySet.add(pair)) {
					currentIterator = null;
					if (priority.compareTo(currentPriority) > 0) currentPriority = priority;
				}
			}
		}
	}

	/**
	 * Adds the given root section and all its successors to the iterator.<br/>
	 * You can use this method while the article of this iterator is compiled.
	 * The iterator will continue iterating over its sections in the correct
	 * order also considering the newly added.<p>
	 * Optionally you can add a filter to only add scripts of the given classes. If no filter is given, all scripts
	 * available for the type of the sections and the compiler are added.<p>
	 *
	 * @param section the section and its successors you want to add
	 * @param scriptFilter the classes of the scripts you want to add
	 */
	public void addSubtree(Section<?> section, Class<? extends CompileScript>... scriptFilter) {
		@SuppressWarnings("unchecked")
		ScriptManager<C> scriptManager = CompilerManager.getScriptManager((Class<C>) compiler.getClass());
		if (scriptManager.hasScriptsForSubtree(section.get())) {
			// do we still need the typeFilter if we already check if there are
			// scripts for this compiler?
			if (typeFilter == null || Sections.canHaveSuccessorOfType(section, typeFilter)) {
				for (Section<?> child : section.getChildren()) {
					addSubtree(child, scriptFilter);
				}
			}
			addSection(section, scriptFilter);
		}
	}

	private boolean hasNext() {
		if (priorityMap.isEmpty()) return false;
		if (!priorityMap.get(currentPriority).isEmpty()) return true;

		// switch to lower priority if possible
		while (Priority.decrement(currentPriority) != null) {
			currentPriority = Priority.decrement(currentPriority);
			currentIterator = null;
			if (!priorityMap.get(currentPriority).isEmpty()) return true;
		}

		return false;
	}

	private CompilePair next() {
		if (currentIterator == null) {
			currentIterator = priorityMap.get(currentPriority).iterator();
		}
		CompilePair next = currentIterator.next();
		currentIterator.remove();
		return next;
	}

	public Priority getCurrentPriority() {
		return Priority.getPriority(currentPriority.intValue());
	}

	public void compile() {
		while (hasNext()) {
			CompilePair pair = next();
			try {
				pair.getB().compile(compiler, pair.getA());
			}
			catch (CompilerMessage cm) {
				Messages.storeMessages(compiler, pair.getA(), pair.getB().getClass(),
						cm.getMessages());
			}
			catch (Exception e) {
				String msg = "Unexpected internal exception while compiling with script "
						+ pair.getB() + ": " + e.getMessage();
				Messages.storeMessage(pair.getA(), pair.getB().getClass(), Messages.error(msg));
				Log.severe(msg, e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void destroy() {
		while (hasNext()) {
			CompilePair pair = next();
			if (pair.getB() instanceof DestroyScript) {
				try {
					((DestroyScript<C, Type>) pair.getB()).destroy(compiler, pair.getA());
				}
				catch (Exception e) {
					String msg = "Unexpected internal exception while destroying with script "
							+ pair.getB();
					Log.severe(msg, e);
				}
			}
		}
	}

	private class CompilePair extends Pair<Section<Type>, CompileScript<C, Type>> {

		public CompilePair(Section<Type> a, CompileScript<C, Type> b) {
			super(a, b);
		}

	}

}
