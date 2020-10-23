package de.knowwe.core.compile;

import com.denkbares.events.Event;

/**
 * Event that is thrown when a compiler has finished a certain stage,
 * i.e. all registered compile scripts of that particular priority level are finished
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 23.10.20.
 */
public class CompilePriorityLevelFinishedEvent implements Event {
	private final Compiler compiler;
	private final Priority priority;

	public CompilePriorityLevelFinishedEvent(Compiler compiler, Priority currentPriority) {
		this.compiler = compiler;
		priority = currentPriority;
	}

	public Compiler getCompiler() {
		return compiler;
	}

	public Priority getPriority() {
		return priority;
	}
}
