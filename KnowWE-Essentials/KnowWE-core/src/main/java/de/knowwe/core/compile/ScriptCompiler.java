package de.knowwe.core.compile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;

import com.denkbares.utils.Log;
import com.denkbares.utils.Pair;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;

/**
 * For the given {@link Section}s and {@link Compiler} it gets all {@link CompileScript}s and then compiles all Sections
 * in the order, given by Priority of the scripts and position in the KDOM (Article). The ScriptCompiler acts like a
 * set, so you can add combinations of Sections and CompileScripts multiple times, but after the first time, it no
 * longer has any effect and each combination will only be compiled once to avoid loops.
 *
 * @param <C> the Compiler for which we want to compile
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class ScriptCompiler<C extends Compiler> {

	private final TreeMap<Priority, List<CompilePair>> compileSetMap;
	private final Set<CompilePair> pairSet = new HashSet<>();

	private final C compiler;
	private final ScriptManager<C> scriptManager;
	private final Class<?>[] typeFilter;
	private final boolean reverseOrder;
	private final Comparator<Priority> comparator;
	private Iterator<Priority> priorityIterator;

	private Priority currentPriority;
	private Iterator<CompilePair> currentCompileSetIterator = null;
	@SuppressWarnings("rawtypes")
	private final Set<Class<? extends CompileScript>> compileScriptsNotSupportingIncrementalCompilation = new HashSet<>();

	public ScriptCompiler(C compiler, Class<?>... typeFilter) {
		this(compiler, false, typeFilter);
	}

	public ScriptCompiler(C compiler, boolean reverseOrder, Class<?>... typeFilter) {
		this.compiler = compiler;
		//noinspection unchecked
		this.scriptManager = CompilerManager.getScriptManager((Class<C>) compiler.getClass());
		this.reverseOrder = reverseOrder;
		this.typeFilter = typeFilter;
		// the natural order for priority is from high priority to low priority, which in numbers is from low to high numbers
		this.comparator = reverseOrder ? Comparator.naturalOrder() : Comparator.<Priority>naturalOrder().reversed();
		this.compileSetMap = new TreeMap<>(comparator);
		for (Priority p : Priority.getRegisteredPriorities()) {
			compileSetMap.put(p, reverseOrder ? new LinkedList<>() : new ArrayList<>(100));
		}
		this.priorityIterator = compileSetMap.keySet().iterator();
		this.currentPriority = priorityIterator.next();
	}

	public C getCompiler() {
		return compiler;
	}

	/**
	 * Adds a single section to the iterator. Successors of the section are not added.<br/> Optionally you can add a
	 * filter to only add scripts of the given classes. If no filter is given, all scripts available for the type of the
	 * section and the compiler are added.<p> You can use this method while the article of this iterator is compiled.
	 * The iterator will continue iterating over its sections in the correct order also considering the newly added.<p>
	 * The ScriptCompiler acts like a set, so you can add combinations of Sections and CompileScripts multiple times,
	 * but after the first time, it no longer has any effect and each combination will only be compiled once to avoid
	 * loops.
	 *
	 * @param section      the section you want to add
	 * @param scriptFilter the classes of the scripts you want to add
	 * @created 27.07.2012
	 */
	public void addSection(Section<?> section, Class<?>... scriptFilter) {
		Section<Type> typeSection = Sections.cast(section, Type.class);
		Map<Priority, List<CompileScript<C, Type>>> scripts = scriptManager.getScripts(section.get());
		for (Entry<Priority, List<CompileScript<C, Type>>> entry : scripts.entrySet()) {
			Priority priority = entry.getKey();
			List<CompilePair> compileSet = compileSetMap.get(priority);
			for (CompileScript<C, Type> script : entry.getValue()) {
				if (scriptFilter.length > 0 && !ArrayUtils.contains(scriptFilter, script.getClass())) {
					continue;
				}
				if (!isIncrementalCompilationPossible(typeSection, script)) {
					this.compileScriptsNotSupportingIncrementalCompilation.add(script.getClass());
				}
				CompilePair pair = new CompilePair(typeSection, script);
				// we only add pairs that are not already added before (e.g. during incremental compilation)
				if (pairSet.add(pair)) {
					if (reverseOrder) {
						((LinkedList<CompilePair>) compileSet).addFirst(pair);
					}
					else {
						compileSet.add(pair);
					}
					// we reset the iterator and priority in case we added during compilation
					currentCompileSetIterator = null;
					if (comparator.compare(priority, currentPriority) < 0) {
						priorityIterator = resetPriorityIteratorTo(priority);
						currentPriority = priority;
					}
				}
			}
		}
	}

	private Iterator<Priority> resetPriorityIteratorTo(Priority priority) {
		Iterator<Priority> iterator = compileSetMap.keySet().iterator();
		Priority currentPriority = iterator.next();
		while (iterator.hasNext() && currentPriority != priority) {
			currentPriority = iterator.next();
		}
		return iterator;
	}

	public boolean isIncrementalCompilationPossible() {
		return compileScriptsNotSupportingIncrementalCompilation.isEmpty();
	}

	protected <T extends Type> boolean isIncrementalCompilationPossible(Section<T> section, CompileScript<C, T> script) {
		return script instanceof DestroyScript;
	}

	@SuppressWarnings("rawtypes")
	public Set<Class<? extends CompileScript>> getCompileScriptsNotSupportingIncrementalCompilation() {
		return Collections.unmodifiableSet(this.compileScriptsNotSupportingIncrementalCompilation);
	}

	/**
	 * Adds the given root section and all its successors to the iterator.<br/> You can use this method while the
	 * article of this iterator is compiled. The iterator will continue iterating over its sections in the correct order
	 * also considering the newly added.<p> Optionally you can add a filter to only add scripts of the given classes. If
	 * no filter is given, all scripts available for the type of the sections and the compiler are added.<p> The
	 * ScriptCompiler acts like a set, so you can add combinations of Sections and CompileScripts multiple times, but
	 * after the first time, it no longer has any effect and each combination will only be compiled once to avoid
	 * loops.
	 *
	 * @param section      the section and its successors you want to add
	 * @param scriptFilter the classes of the scripts you want to add
	 */
	public void addSubtree(Section<?> section, Class<?>... scriptFilter) {
		//noinspection DuplicatedCode
		if (scriptManager.hasScriptsForSubtree(section.get())) {
			if (typeFilter.length == 0 || Sections.canHaveSuccessor(section, typeFilter)) {
				for (Section<?> child : section.getChildren()) {
					addSubtree(child, scriptFilter);
				}
			}
			addSection(section, scriptFilter);
		}
	}

	private boolean hasNext() {
		if (compileSetMap.isEmpty()) return false;
		if (!compileSetMap.get(currentPriority).isEmpty()) return true;

		// switch to lower priority if possible
		while (priorityIterator.hasNext()) {
			currentPriority = priorityIterator.next();
			currentCompileSetIterator = null;
			if (!compileSetMap.get(currentPriority).isEmpty()) return true;
		}

		return false;
	}

	private CompilePair next() {
		if (currentCompileSetIterator == null) {
			currentCompileSetIterator = compileSetMap.get(currentPriority).iterator();
		}
		CompilePair next = currentCompileSetIterator.next();
		currentCompileSetIterator.remove();
		return next;
	}

	public void compile() {
		Priority lastPriority = Priority.INIT;
		while (hasNext()) {
			// get next script and section, and update the current compile priority, if required
			CompilePair pair = next();
			if (currentPriority != lastPriority) {
				compiler.getCompilerManager().setCurrentCompilePriority(compiler, currentPriority);
			}

			Section<Type> section = pair.getA();
			CompileScript<C, Type> script = pair.getB();
			try {
				script.compile(compiler, section);
			}
			catch (CompilerMessage cm) {
				Messages.storeMessages(compiler, section, script.getClass(), cm.getMessages());
			}
			catch (Throwable e) {
				String msg = "Unexpected internal exception while compiling with script " + script + ": " + e;
				Messages.storeMessage(compiler, section, script.getClass(), Messages.error(msg));
				Log.severe(msg, e);
			}
		}
		compiler.getCompilerManager().setCurrentCompilePriority(compiler, Priority.DONE);
	}

	public void destroy() {
		while (hasNext()) {
			CompilePair pair = next();
			CompileScript<C, Type> destroyScript = pair.getB();
			if (destroyScript instanceof DestroyScript) {
				try {
					//noinspection unchecked
					((DestroyScript<C, Type>) destroyScript).destroy(compiler, pair.getA());
				}
				catch (Throwable e) {
					String msg = "Unexpected internal exception while destroying with script " + destroyScript;
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
