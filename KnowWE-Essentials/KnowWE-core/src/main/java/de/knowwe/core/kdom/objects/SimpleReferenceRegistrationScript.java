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
import java.util.function.BiFunction;

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
 * Default compile script registering a term section in a term compiler
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.02.2012
 */
public class SimpleReferenceRegistrationScript<C extends TermCompiler, T extends TermReference> implements CompileScript<C, T>, DestroyScript<C, T> {

	private final Class<C> compilerClass;

	private final BiFunction<C, Section<T>, Boolean> validate;

	public SimpleReferenceRegistrationScript(Class<C> compilerClass) {
		this(compilerClass, true);
	}

	/**
	 * Creates a new compile script for the given compiler. If validate is set to false, the script will register
	 * without checking for the validity of the reference (e.g. does a definition exist?).
	 */
	public SimpleReferenceRegistrationScript(Class<C> compilerClass, boolean validate) {
		this(compilerClass, (c, t) -> validate);
	}

	/**
	 * Creates a new compile script for the given compiler. If the validate function returns false, the script will
	 * register
	 * without checking for the validity of the reference (e.g. does a definition exist?).
	 */
	public SimpleReferenceRegistrationScript(Class<C> compilerClass, BiFunction<C, Section<T>, Boolean> validate) {
		this.compilerClass = compilerClass;
		this.validate = validate;
	}

	@Override
	public Class<C> getCompilerClass() {
		return compilerClass;
	}

	/**
	 * Returns the terminology manager (of the specified compiler) that should be used for register term references.
	 * Override this method to create a script that registers and validates the term references in an other terminology
	 * manager than the compiling one.
	 *
	 * @param compiler the compiler that is compiling the term reference
	 * @return the terminology manager to find the reference in
	 */
	protected TerminologyManager getTerminologyManager(C compiler) {
		return compiler.getTerminologyManager();
	}

	@Override
	public void compile(C compiler, Section<T> section) throws CompilerMessage {
		TerminologyManager manager = getTerminologyManager(compiler);
		if (manager != null) {
			Class<?> termClass = section.get().getTermObjectClass(compiler, section);
			Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
			manager.registerTermReference(compiler, section, termClass, termIdentifier);
			if (validate.apply(compiler, section)) {
				Collection<Message> messages = validateReference(compiler, section);
				if (messages.isEmpty()) {
					Messages.clearMessages(compiler, section, getClass());
				}
				else {
					throw new CompilerMessage(messages);
				}
			}
		}
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
	public Collection<Message> validateReference(C compiler, Section<T> section) {
		TermCompiler.ReferenceValidationMode validationMode = section.get()
				.getReferenceValidationMode(compiler, section);
		if (validationMode == TermCompiler.ReferenceValidationMode.ignore || validationMode == TermCompiler.ReferenceValidationMode.greyOut) {
			return Messages.noMessage();
		}
		if (!section.get().isDefinedTerm(compiler, section)) {
			return Messages.asList(getInvalidTermMessage(compiler, section,
					validationMode == TermCompiler.ReferenceValidationMode.warn ? Message.Type.WARNING : getMessageLevel(compiler)));
		}
		return Messages.noMessage();
	}

	protected Message.Type getMessageLevel(C compiler) {
		return Message.Type.ERROR;
	}

	protected Message getInvalidTermMessage(C compiler, Section<T> section, Message.Type messageType) {
		return Messages.noSuchObjectError(
				section.get().getTermObjectClass(compiler, section).getSimpleName(),
				section.get().getTermName(section), messageType);
	}

	@Override
	public void destroy(C compiler, Section<T> section) {
		TerminologyManager manager = getTerminologyManager(compiler);
		if (manager != null) {
			Class<?> termClass = section.get().getTermObjectClass(compiler, section);
			Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
			manager.unregisterTermReference(compiler, section, termClass, termIdentifier);
		}
	}
}
