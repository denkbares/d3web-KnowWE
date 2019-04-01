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

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;

public abstract class SimpleDefinition extends AbstractType implements TermDefinition, RenamableTerm {

	private final Class<?> termObjectClass;

	public SimpleDefinition(Class<? extends TermCompiler> compilerClass, Class<?> termObjectClass) {
		this(compilerClass, termObjectClass, Priority.HIGHER);
	}

	@SuppressWarnings("unchecked")
	public SimpleDefinition(Class<? extends TermCompiler> compilerClass, Class<?> termObjectClass, Priority handlerPriority) {
		this.termObjectClass = termObjectClass;
		this.addCompileScript(handlerPriority,
				new SimpleDefinitionRegistrationScript<>(
						(Class<TermCompiler>) compilerClass));
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return termObjectClass;
	}

	/*
	 * Override {@link Term#getTermIdentifier(TermCompiler, Section} instead
	 */
	@Override
	public final Identifier getTermIdentifier(Section<? extends Term> section) {
		return TermDefinition.super.getTermIdentifier(section);
	}
}
