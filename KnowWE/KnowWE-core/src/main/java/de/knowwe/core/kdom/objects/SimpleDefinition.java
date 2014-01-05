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

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.DestroyScript;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;

public abstract class SimpleDefinition<C extends TermCompiler> extends AbstractType implements TermDefinition, RenamableTerm {

	private final Class<?> termObjectClass;

	private final Class<C> compilerClass;

	public SimpleDefinition(Class<C> compilerClass, Class<?> termObjectClass) {
		this(compilerClass, termObjectClass, Priority.HIGHER);
	}

	public SimpleDefinition(Class<C> compilerClass, Class<?> termObjectClass, Priority handlerPriority) {
		this.termObjectClass = termObjectClass;
		this.compilerClass = compilerClass;
		this.addCompileScript(handlerPriority, new SimpleDefinitionRegistrationScript());
	}

	@Override
	public Class<?> getTermObjectClass(Section<? extends Term> section) {
		return termObjectClass;
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		return section.getText();
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		return new Identifier(getTermName(section));
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		String replacement = newIdentifier.getLastPathElement();
		return TermUtils.quoteIfRequired(replacement);
	}

	private class SimpleDefinitionRegistrationScript implements CompileScript<C, SimpleDefinition<C>>, DestroyScript<C, SimpleDefinition<C>> {

		@Override
		public Class<C> getCompilerClass() {
			return compilerClass;
		}

		@Override
		public void compile(C compiler, Section<SimpleDefinition<C>> section) {

			TerminologyManager terminologyManager = compiler.getTerminologyManager();
			Identifier termIdentifier = section.get().getTermIdentifier(section);

			terminologyManager.registerTermDefinition(compiler,
					section, section.get().getTermObjectClass(section),
					termIdentifier);

			if (compiler instanceof IncrementalCompiler) {
				Collection<Section<?>> termReferenceSections = terminologyManager.getTermReferenceSections(termIdentifier);
				((IncrementalCompiler) compiler).addSectionsToCompile(termReferenceSections);
			}

		}

		@Override
		public void destroy(C compiler, Section<SimpleDefinition<C>> section) {
			TerminologyManager terminologyManager = compiler.getTerminologyManager();
			Identifier termIdentifier = section.get().getTermIdentifier(section);
			terminologyManager.unregisterTermDefinition(compiler,
					section, section.get().getTermObjectClass(section),
					termIdentifier);

			if (compiler instanceof IncrementalCompiler) {
				Collection<Section<?>> termReferenceSections = terminologyManager.getTermReferenceSections(termIdentifier);
				((IncrementalCompiler) compiler).addSectionsToDestroy(termReferenceSections);
			}

		}
	}

}
