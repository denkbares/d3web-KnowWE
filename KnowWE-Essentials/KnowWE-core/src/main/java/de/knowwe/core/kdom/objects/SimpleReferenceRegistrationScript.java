/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.DestroyScript;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.02.2012
 */
public class SimpleReferenceRegistrationScript<C extends TermCompiler> implements CompileScript<C, Term>, DestroyScript<C, Term> {

	private final Class<C> compilerClass;

	private final boolean validate;

	/**
	 * Creates a new compile script for the given compiler. If validate is set to false, the script will register
	 * without checking for the validity of the reference (e.g. does a definition exist?).
	 */
	public SimpleReferenceRegistrationScript(Class<C> compilerClass, boolean validate) {
		this.compilerClass = compilerClass;
		this.validate = validate;
	}

	public SimpleReferenceRegistrationScript(Class<C> compilerClass) {
		this(compilerClass, true);
	}

	@Override
	public Class<C> getCompilerClass() {
		return compilerClass;
	}

	@Override
	public void compile(C compiler, Section<Term> section) throws CompilerMessage {

		TerminologyManager tHandler = compiler.getTerminologyManager();
		Identifier termIdentifier = section.get().getTermIdentifier(section);
		tHandler.registerTermReference(compiler, section, section.get().getTermObjectClass(section), termIdentifier);
		if (validate) throw new CompilerMessage(validateReference(compiler, section));
	}

	/**
	 * Validates the reference and returns a collection of error or warning messages if the reference is not correctly
	 * specified. Otherwise it returns an empty collection or a collection of info messages.
	 *
	 * @param compiler the Compiler compiling this reference
	 * @param section  the section identifying the reference
	 * @return result messages of validation
	 * @created 28.02.2012
	 */
	public Collection<Message> validateReference(C compiler, Section<Term> section) {
		TerminologyManager tHandler = compiler.getTerminologyManager();
		Identifier termIdentifier = section.get().getTermIdentifier(section);
		if (!tHandler.isDefinedTerm(termIdentifier)) {
			return Messages.asList(getInvalidTermMessage(section));
		}
		return Messages.noMessage();
	}

	private Message getInvalidTermMessage(Section<Term> section) {
		return Messages.noSuchObjectError(
				section.get().getTermObjectClass(section).getSimpleName(),
				section.get().getTermName(section));
	}

	@Override
	public void destroy(C compiler, Section<Term> section) {
		compiler.getTerminologyManager().unregisterTermReference(compiler,
				section, section.get().getTermObjectClass(section),
				section.get().getTermIdentifier(section));
	}
}
