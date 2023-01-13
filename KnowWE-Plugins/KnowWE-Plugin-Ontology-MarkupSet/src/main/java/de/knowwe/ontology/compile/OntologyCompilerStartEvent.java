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

import java.util.Collection;

import de.knowwe.core.compile.CompilerStartEvent;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Is fired before the {@link OntologyCompiler} starts compiling.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class OntologyCompilerStartEvent extends CompilerStartEvent<OntologyCompiler> {

	private final Collection<Section<?>> added;
	private final Collection<Section<?>> removed;

	private final boolean completeCompilation;

	public OntologyCompilerStartEvent(OntologyCompiler compiler, Collection<Section<?>> added, Collection<Section<?>> removed, boolean completeCompilation) {
		super(compiler);
		this.added = added;
		this.removed = removed;
		this.completeCompilation = completeCompilation;
	}

	public boolean isCompleteCompilation() {
		return completeCompilation;
	}

	public Collection<Section<?>> getAdded() {
		return added;
	}

	public Collection<Section<?>> getRemoved() {
		return removed;
	}
}
