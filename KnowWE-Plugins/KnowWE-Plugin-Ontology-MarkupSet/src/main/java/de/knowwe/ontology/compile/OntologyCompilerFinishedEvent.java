/*
 * Copyright (C) 2013 denkbares GmbH
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.ontology.compile;

import java.util.Collections;
import java.util.Set;

import de.knowwe.core.compile.CompilerFinishedEvent;
import de.knowwe.core.compile.terminology.TermRegistrationEvent;

/**
 * Is fired before the {@link OntologyCompiler} starts compiling.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class OntologyCompilerFinishedEvent extends CompilerFinishedEvent<OntologyCompiler> {

	private final boolean ontologyChanged;

	private final Set<TermRegistrationEvent> removedTerms;

	private final Set<TermRegistrationEvent> addedTerms;

	public OntologyCompilerFinishedEvent(OntologyCompiler compiler, boolean changed) {
		super(compiler);
		this.ontologyChanged = changed;
		removedTerms = Collections.emptySet();
		addedTerms = Collections.emptySet();
	}

	public OntologyCompilerFinishedEvent(OntologyCompiler compiler, boolean changed, Set<TermRegistrationEvent> removedTerms, Set<TermRegistrationEvent> addedTerms) {
		super(compiler);
		this.ontologyChanged = changed;
		this.removedTerms = removedTerms;
		this.addedTerms = addedTerms;
	}

	/**
	 * Signals whether the ontology has changed during the compilation.
	 */
	public boolean isOntologyChanged() {
		return ontologyChanged;
	}

	@Override
	public boolean artifactChanged() {
		return isOntologyChanged();
	}

	@Override
	public Set<TermRegistrationEvent> getRemovedTerms() {
		return removedTerms;
	}

	@Override
	public Set<TermRegistrationEvent> getAddedTerms() {
		return addedTerms;
	}

	@Override
	public boolean terminologyChanged() {
		return ! (removedTerms.isEmpty() && addedTerms.isEmpty());
	}
}
