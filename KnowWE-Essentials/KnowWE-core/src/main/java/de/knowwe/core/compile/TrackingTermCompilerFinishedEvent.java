package de.knowwe.core.compile;

import java.util.Set;

import com.denkbares.events.Event;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TermRegistrationEvent;
import de.knowwe.core.event.CompilerEvent;

public interface TrackingTermCompilerFinishedEvent<C extends TermCompiler> extends CompilerEvent<C> {

	boolean artifactChanged();

	boolean terminologyChanged();

	Set<TermRegistrationEvent<C>> getRemovedTerms();

	Set<TermRegistrationEvent<C>> getAddedTerms();
}
