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
package de.d3web.we.knowledgebase;

import java.util.Collections;
import java.util.Set;

import de.knowwe.core.compile.CompilerFinishedEvent;
import de.knowwe.core.compile.terminology.TermRegistrationEvent;

/**
 * This event is fired when packages compiled by a Section<? extends
 * PackageCompileType> have changed and the compilation by the
 * {@link D3webCompiler} has finished.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 10.12.2013
 */
public class D3webCompilerFinishedEvent extends CompilerFinishedEvent<D3webCompiler> {

	public D3webCompilerFinishedEvent(D3webCompiler compiler) {
		super(compiler);
	}

	@Override
	public boolean artifactChanged() {
		// todo: implement for efficiency reasons
		return true;
	}

	@Override
	public boolean terminologyChanged() {
		// todo: implement for efficiency reasons
		return true;
	}

	@Override
	public Set<TermRegistrationEvent> getRemovedTerms() {
		// todo: implement for efficiency reasons
		return Collections.emptySet();
	}

	@Override
	public Set<TermRegistrationEvent> getAddedTerms() {
		// todo: implement for efficiency reasons
		return Collections.emptySet();
	}
}
