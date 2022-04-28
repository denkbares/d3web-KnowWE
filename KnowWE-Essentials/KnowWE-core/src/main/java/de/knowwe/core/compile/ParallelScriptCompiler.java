package de.knowwe.core.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.EventManager;
import com.denkbares.utils.Pair;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;

import static de.knowwe.core.compile.ParallelScriptCompiler.Mode.compile;
import static de.knowwe.core.compile.ParallelScriptCompiler.Mode.destroy;

/**
 * For the given {@link Section}s and {@link Compiler} it gets all
 * {@link CompileScript}s and then compiles all Sections in the order, given by Priority of the
 * scripts and position in the KDOM (Article). The ParallelScriptCompiler acts like a set, so you can add combinations
 * of Sections and CompileScripts multiple times, but after the first time, it no longer has any effect and each
 * combination will only be compiled once to avoid loops.
 * <p/>
 * The ParallelScriptCompiler compiles scripts of the same priority in parallel.
 *
 * @param <C> the Compiler for which we want to compile
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class ParallelScriptCompiler<C extends Compiler> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ParallelScriptCompiler.class);

	public enum Mode {
		compile,
		destroy
	}

	private final TreeMap<Priority, List<CompilePair>> compileMap =
			new TreeMap<>();

	private final TreeMap<Priority, List<CompilePair>> compileLog =
			new TreeMap<>();

	private final Set<CompilePair> pairSet = new HashSet<>();

	private Priority currentPriority;

	private Iterator<CompilePair> currentIterator = null;

	private ExecutorService threadPool;

	private final Mode step;
	private final Class<?>[] typeFilter;

	private final C compiler;

	private final ScriptManager<C> scriptManager;

	/**
	 * Creates a new script compiler that runs scripts of the same priority in parallel.
	 * Every instance calls either compile or destroy an all compile scripts, dependent on the {@link Mode} that
	 * is given in the constructor.
	 *
	 * @param compiler   the parent @link {@link Compiler} this script compiler belongs to
	 * @param mode       the step/mode of the compilation, either <tt>compile</tt> or <tt>destroy</tt>
	 * @param typeFilter optional filters for compile script that should be run
	 */
	@SuppressWarnings("unchecked")
	public ParallelScriptCompiler(C compiler, Mode mode, Class<?>... typeFilter) {
		this.compiler = compiler;
		scriptManager = CompilerManager.getScriptManager((Class<C>) compiler.getClass());
		this.step = mode;
		this.typeFilter = typeFilter;
		for (Priority p : Priority.getRegisteredPriorities()) {
			compileMap.put(p, new ArrayList<>());
		}
		currentPriority = Priority.getRegisteredPriorities().first();
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
		synchronized (compileMap) {
			Map<Priority, List<CompileScript<C, Type>>> scripts =
					scriptManager.getScripts(Sections.cast(section, Type.class).get());
			for (Entry<Priority, List<CompileScript<C, Type>>> entry : scripts.entrySet()) {
				Priority priority = entry.getKey();
				List<CompilePair> compileSet = compileMap.get(priority);
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

	private CompilePair next() {
		if (currentIterator == null) {
			// happens only at the start, no synchronizing needed
			setCurrentIterator();
			this.threadPool = CompilerManager.createExecutorService(Compilers.getCompilerName(this.compiler));
		}
		if (!currentIterator.hasNext()) {
			// wait for all scripts to finish
			awaitShutDownOfCurrentScripts();
			// all other threads that could possibly access compileMap are now done
			// so no synchronizing is needed
			// try current priority again, maybe new sections were added during compilation
			setCurrentIterator();

			// switch to lower priority if necessary and possible
			while (!currentIterator.hasNext() && Priority.decrement(this.currentPriority) != null) {
				firePrioStepFinishedEvent(currentPriority);
				this.currentPriority = Priority.decrement(this.currentPriority);
				setCurrentIterator();
			}
			// still no new pairs? then we are done....
			if (!currentIterator.hasNext()) {
				firePrioStepFinishedEvent(currentPriority);
				return null;
			}
			this.threadPool = CompilerManager.createExecutorService(Compilers.getCompilerName(this.compiler));
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
				LOGGER.error("Interrupted during script compiler shutdown", e);
			}
		}
	}

	public void run() {
		if (step == compile) {
			compile();
		}
		else if (step == destroy) {
			destroy();
		}
		else {
			throw new UnsupportedOperationException("Unknown and unhandled " + Mode.class.getSimpleName() + ": " + step);
		}
	}

	private void compile() {
		Priority lastPriority = Priority.INIT;
		while (true) {
			// get next script and section, and update the current compile priority, if required
			CompilePair pair = next();
			if (pair == null) {
				break;
			}
			if (currentPriority != lastPriority) {
				compiler.getCompilerManager().setCurrentCompilePriority(compiler, currentPriority);
				// we are done with this priority level now -> finished this level by throwing an event and proceed to next level
				lastPriority = currentPriority;
			}

			// add to compile log
			List<CompilePair> prioLog = compileLog.computeIfAbsent(currentPriority, x -> new ArrayList<>());
			prioLog.add(pair);

			threadPool.execute(() -> {
				Section<Type> section = pair.getA();
				CompileScript<C, Type> script = pair.getB();
				try {
					script.compile(compiler, section);
				}
				catch (CompilerMessage cm) {
					Messages.storeMessages(compiler, section, script.getClass(), cm.getMessages());
				}
				catch (Exception e) {
					String msg = "Unexpected internal exception while compiling.\n" +
							"Script: " + script + ", @priority " + currentPriority.intValue() + ":\n"
							+ e.getClass().getSimpleName() + ": " + e.getMessage();
					Messages.storeMessage(compiler, section, ParallelScriptCompiler.class, Messages.error(msg));
					LOGGER.error(msg, e);
				}
			});
		}
		compiler.getCompilerManager().setCurrentCompilePriority(compiler, Priority.DONE);
	}

	private void firePrioStepFinishedEvent(Priority lastPriority) {
		if (step == compile) {
			// we throw an event as this priority level compilation is now completed

			List<Section<?>> sectionListThisPrio = compileLog.getOrDefault(lastPriority, Collections.emptyList())
					.stream().map(Pair::getA).collect(Collectors.toList());
			List<Section<?>> allSectionCompiledUpToNow = compileLog.keySet()
					.stream()
					.map(compileLog::get)
					.flatMap(Collection::stream)
					.map(Pair::getA).collect(Collectors.toList());
			// event needs to be fired _after_ the currentPriority (and Iterator) has been incremented
			EventManager.getInstance()
					.fireEvent(new CompilePriorityLevelFinishedEvent(compiler, lastPriority, sectionListThisPrio, !allSectionCompiledUpToNow.isEmpty()));
		}
		else {
			// we need a destroy finished event one day? -> goes here
		}
	}

	@SuppressWarnings("unchecked")
	private void destroy() {
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
						LOGGER.error(msg, e);
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
