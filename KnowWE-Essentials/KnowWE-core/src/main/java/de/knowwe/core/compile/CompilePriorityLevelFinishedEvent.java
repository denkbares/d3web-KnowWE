package de.knowwe.core.compile;

import java.util.Collection;

import de.knowwe.core.event.CompilerEvent;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Event that is thrown when a compiler has finished a certain stage,
 * i.e. all registered compile scripts of that particular priority level are finished
 * <p>
 * NOTE: The event is _NOT thrown_ if there were no script registered/executed for a specific Priority.
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 23.10.20.
 */
public class CompilePriorityLevelFinishedEvent implements CompilerEvent<Compiler> {
	private final Compiler compiler;
	private final Priority priority;
	private final Collection<Section<?>> compiledSectionsCurrentPriority;
	private final boolean hasCompiledAnySectionUpToNow;

	public CompilePriorityLevelFinishedEvent(Compiler compiler, Priority currentPriority, Collection<Section<?>> compiledSections, boolean hasCompiledAnySectionUpToNow) {
		priority = currentPriority;
		this.compiler = compiler;
		this.compiledSectionsCurrentPriority = compiledSections;
		this.hasCompiledAnySectionUpToNow = hasCompiledAnySectionUpToNow;
	}

	/**
	 * The priority of the compile level that has been finished triggering this event
	 *
	 * @return finished priority
	 */
	public Priority getPriority() {
		return priority;
	}

	/**
	 * The sections that have been compiled by the compile level that triggered this event
	 *
	 * @return compiled sections
	 */
	public Collection<Section<?>> getCompiledSectionsCurrentPriority() {
		return compiledSectionsCurrentPriority;
	}

	/**
	 * Tells whether there have sections been already compiled up to now
	 *
	 * @return whether sections have been compiled by now
	 */
	public boolean hasCompiledAnySectionUpToNow() {
		return hasCompiledAnySectionUpToNow;
	}

	@Override
	public Compiler getCompiler() {
		return compiler;
	}
}
