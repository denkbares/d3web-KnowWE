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

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DestroyScript;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

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

	private class SimpleDefinitionRegistrationScript<C extends TermCompiler> implements CompileScript<C, SimpleDefinition>, DestroyScript<C, SimpleDefinition> {

		private final Class<C> compilerClass;

		@Override
		public Class<C> getCompilerClass() {
			return compilerClass;
		}

		public SimpleDefinitionRegistrationScript(Class<C> compilerClass) {
			this.compilerClass = compilerClass;
		}

		@Override
		public void compile(C compiler, Section<SimpleDefinition> section) throws CompilerMessage {

			if (!verifyDefinition(compiler, section)) return;

			TerminologyManager terminologyManager = compiler.getTerminologyManager();
			Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
			if (termIdentifier == null) {
				throw new CompilerMessage(Messages.error("Could not determine TermIdentifier"));
			}

			terminologyManager.registerTermDefinition(compiler,
					section, section.get().getTermObjectClass(compiler, section),
					termIdentifier);

			if (compiler instanceof IncrementalCompiler) {
				Collection<Section<?>> termDefiningSections = terminologyManager.getTermDefiningSections(termIdentifier);
				IncrementalCompiler incrementalCompiler = (IncrementalCompiler) compiler;
				Compilers.addSectionsToCompile(incrementalCompiler, termDefiningSections);
				Collection<Section<?>> termReferenceSections = terminologyManager.getTermReferenceSections(termIdentifier);
				Compilers.addSectionsToCompile(incrementalCompiler, termReferenceSections);
			}

		}

		@Override
		public void destroy(C compiler, Section<SimpleDefinition> section) {
			TerminologyManager terminologyManager = compiler.getTerminologyManager();
			Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
			if (termIdentifier == null) {
				// we assume that also nothing could have been registered without an Identifier -> ergo nothing to unregister
				return;
				//throw CompilerMessage.error( "Could not determine TermIdentifier"));
			}
			terminologyManager.unregisterTermDefinition(compiler,
					section, section.get().getTermObjectClass(compiler, section),
					termIdentifier);

			if (compiler instanceof IncrementalCompiler) {
				Collection<Section<?>> termDefiningSections = terminologyManager.getTermDefiningSections(termIdentifier);
				IncrementalCompiler incrementalCompiler = (IncrementalCompiler) compiler;
				Compilers.addSectionsToDestroyAndCompile(incrementalCompiler, termDefiningSections);
				Collection<Section<?>> termReferenceSections = terminologyManager.getTermReferenceSections(termIdentifier);
				Compilers.addSectionsToDestroyAndCompile(incrementalCompiler, termReferenceSections);
			}
		}

	}

	/*
	 * Override {@link Term#getTermIdentifier(TermCompiler, Section} instead
	 */
	@Override
	public final Identifier getTermIdentifier(Section<? extends Term> section) {
		return TermDefinition.super.getTermIdentifier(section);
	}

	protected boolean verifyDefinition(Compiler compiler, Section<SimpleDefinition> section) {
		return true;
	}
}
