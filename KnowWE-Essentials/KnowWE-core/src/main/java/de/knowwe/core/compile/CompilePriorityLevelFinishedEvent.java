package de.knowwe.core.compile;

import java.util.Collection;

import de.knowwe.core.event.CompilerEvent;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Event that is thrown when a compiler has finished a certain stage,
 * i.e. all registered compile scripts of that particular priority level are finished
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 23.10.20.
 */
public class CompilePriorityLevelFinishedEvent extends CompilerEvent<Compiler> {
	private final Priority priority;

	private final Collection<Section> compiledSections;

	public CompilePriorityLevelFinishedEvent(Compiler compiler, Priority currentPriority, Collection<Section> compiledSections) {
		super(compiler);
		priority = currentPriority;
		this.compiledSections = compiledSections;
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
	public Collection<Section> getCompiledSections() {
		return compiledSections;
	}
}
