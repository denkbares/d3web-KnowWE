package de.knowwe.core.compile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;

import de.d3web.utils.Log;
import de.d3web.utils.Pair;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;

/**
 * For the given {@link de.knowwe.core.kdom.parsing.Section}s and {@link de.knowwe.core.compile.Compiler} it gets all {@link de.knowwe.core.compile.CompileScript}s and then
 * compiles all Sections in the order, given by Priority of the scripts and position in the KDOM
 * (Article). The ParallelScriptCompiler acts like a set, so you can add combinations of Sections and
 * CompileScripts multiple times, but after the first time, it no longer has any effect and each
 * combination will only be compiled once to avoid loops.
 * <p/>
 * The ParallelScriptCompiler compiles scripts of the same priority in parallel.
 *
 * @param <C> the Compiler for which we want to compile
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class ParallelScriptCompiler<C extends Compiler> {

	private final TreeMap<Priority, ArrayList<CompilePair>> compileMap =
			new TreeMap<>();

	private final Set<CompilePair> pairSet = new HashSet<>();

	private Priority currentPriority;

	private Iterator<CompilePair> currentIterator = null;

	private ExecutorService threadPool;

	private final Class<?>[] typeFilter;

	private final C compiler;

	private final ScriptManager<C> scriptManager;

	@SuppressWarnings("unchecked")
	public ParallelScriptCompiler(C compiler, Class<?>... typeFilter) {
		this.compiler = compiler;
		scriptManager = CompilerManager.getScriptManager((Class<C>) compiler.getClass());
		this.typeFilter = typeFilter;
		for (Priority p : Priority.getRegisteredPriorities()) {
			compileMap.put(p, new ArrayList<>());
		}
		currentPriority = Priority.getRegisteredPriorities().first();
	}

	/**
	 * Adds a single section to the iterator. Successors of the section are not added.<br/>
	 * Optionally you can add a filter to only add scripts of the given classes. If no filter is
	 * given, all scripts available for the type of the section and the compiler are added.<p> You
	 * can use this method while the article of this iterator is compiled. The iterator will
	 * continue iterating over its sections in the correct order also considering the newly
	 * added.<p> The ScriptCompiler acts like a set, so you can add combinations of Sections and
	 * CompileScripts multiple times, but after the first time, it no longer has any effect and each
	 * combination will only be compiled once to avoid loops.
	 *
	 * @param section      the section you want to add
	 * @param scriptFilter the classes of the scripts you want to add
	 * @created 27.07.2012
	 */
	public void addSection(Section<?> section, Class<?>... scriptFilter) {
		synchronized (compileMap) {
			Map<Priority, List<CompileScript<C, Type>>> scripts =
					scriptManager.getScripts(Sections.cast(section, Type.class).get());
			for (Entry<Priority, List<CompileScript<C, Type>>> entry : scripts.entrySet()) {
				Priority priority = entry.getKey();
				ArrayList<CompilePair> compileSet = compileMap.get(priority);
				for (CompileScript<C, Type> script : entry.getValue()) {
					if (scriptFilter.length > 0 && !ArrayUtils.contains(scriptFilter, script.getClass())) {
						continue;
					}
					CompilePair pair = new CompilePair(Sections.cast(section, Type.class), script);
					// we only add pairs that are not already added before (e.g. during incremental compilation)
					if (pairSet.add(pair)) {
						compileSet.add(pair);
						if (priority.compareTo(currentPriority) > 0) currentPriority = priority;
					}
				}
			}
		}
	}

	/**
	 * Adds the given root section and all its successors to the iterator.<br/> You can use this
	 * method while the article of this iterator is compiled. The iterator will continue iterating
	 * over its sections in the correct order also considering the newly added.<p> Optionally you
	 * can add a filter to only add scripts of the given classes. If no filter is given, all scripts
	 * available for the type of the sections and the compiler are added.<p> The ScriptCompiler acts
	 * like a set, so you can add combinations of Sections and CompileScripts multiple times, but
	 * after the first time, it no longer has any effect and each combination will only be compiled
	 * once to avoid loops.
	 *
	 * @param section      the section and its successors you want to add
	 * @param scriptFilter the classes of the scripts you want to add
	 */
	public void addSubtree(Section<?> section, Class<?>... scriptFilter) {
		if (scriptManager.hasScriptsForSubtree(section.get())) {
			if (typeFilter.length == 0 || Sections.canHaveSuccessor(section, typeFilter)) {
				for (Section<?> child : section.getChildren()) {
					addSubtree(child, scriptFilter);
				}
			}
			addSection(section, scriptFilter);
		}
	}

	private CompilePair next() {
		if (currentIterator == null) {
			// happens only at the start, no synchronizing needed
			setCurrentIterator();
			this.threadPool = CompilerManager.createExecutorService();
		}
		if (!currentIterator.hasNext()) {
			// wait for all scripts to finish
			awaitShutDownOfCurrentScripts();
			// all other threads that could possibly access compileMap are now done
			// so no synchronizing is needed
			// try current priority again, maybe new sections were added during compilation
			setCurrentIterator();
			// switch to lower priority if necessary and possible
			while (!currentIterator.hasNext() && Priority.decrement(currentPriority) != null) {
				currentPriority = Priority.decrement(currentPriority);
				setCurrentIterator();
			}
			// still no new pairs? then we are done....
			if (!currentIterator.hasNext()) {
				return null;
			}
			this.threadPool = CompilerManager.createExecutorService();
		}
		return currentIterator.next();
	}

	private void setCurrentIterator() {
		currentIterator = new ArrayList<>(compileMap.get(currentPriority)).iterator();
		// reset the list, during compilation new sections can be added
		compileMap.put(currentPriority, new ArrayList<>());
	}

	private void awaitShutDownOfCurrentScripts() {
		if (this.threadPool != null) {
			threadPool.shutdown();
			try {
				threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e) {
				Log.severe("Interrupted during script compiler shutdown", e);
			}
		}
	}

	public void compile() {
		while (true) {
			CompilePair pair = next();
			if (pair == null) break;
			threadPool.execute(() -> {
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
			});
		}
	}

	@SuppressWarnings("unchecked")
	public void destroy() {
		while (true) {
			CompilePair pair = next();
			if (pair == null) break;
			if (pair.getB() instanceof DestroyScript) {
				threadPool.execute(() -> {
					try {
						((DestroyScript<C, Type>) pair.getB()).destroy(compiler, pair.getA());
					}
					catch (Exception e) {
						String msg = "Unexpected internal exception while destroying with script "
								+ pair.getB();
						Log.severe(msg, e);
					}
				});
			}
		}
	}

	private class CompilePair extends Pair<Section<Type>, CompileScript<C, Type>> {

		public CompilePair(Section<Type> a, CompileScript<C, Type> b) {
			super(a, b);
		}

	}

}
