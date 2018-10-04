/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.kdom.objects;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;

/**
 * @author Albrecht
 * @created 16.12.2010
 */
public abstract class SimpleReference extends AbstractType implements TermReference, RenamableTerm {

	private final Class<?> termObjectClass;

	public SimpleReference(Class<? extends TermCompiler> compilerClass, Class<?> termObjectClass) {
		this(compilerClass, termObjectClass, Priority.DEFAULT);
	}

	@SuppressWarnings("unchecked")
	public SimpleReference(Class<? extends TermCompiler> compilerClass, Class<?> termObjectClass, Priority registrationPriority) {
		this(new SimpleReferenceRegistrationScript<>((Class<TermCompiler>) compilerClass), termObjectClass, registrationPriority);
	}

	public SimpleReference(CompileScript<? extends TermCompiler, ?> compileScript, Class<?> termObjectClass) {
		this(compileScript, termObjectClass, Priority.DEFAULT);
	}

	public <C extends TermCompiler> SimpleReference(CompileScript<C, ?> compileScript, Class<?> termObjectClass, Priority registrationPriority) {
		this.termObjectClass = termObjectClass;
		this.addCompileScript(registrationPriority, compileScript);
	}

	@Override
	public Class<?> getTermObjectClass(TermCompiler compiler, Section<? extends Term> section) {
		return termObjectClass;
	}

	/*
	 * Override {@link Term#getTermIdentifier(TermCompiler, Section} instead
	 */
	@Override
	public final Identifier getTermIdentifier(Section<? extends Term> section) {
		return TermReference.super.getTermIdentifier(section);
	}

}
