/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.objects;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DestroyScript;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;

/**
 * Script to incrementally register a term definition section to the terminology manager.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 01.04.2019
 */
public class SimpleDefinitionRegistrationScript<C extends TermCompiler, T extends TermDefinition> implements CompileScript<C, T>, DestroyScript<C, T> {

	private final Class<C> compilerClass;

	@Override
	public Class<C> getCompilerClass() {
		return compilerClass;
	}

	public SimpleDefinitionRegistrationScript(Class<C> compilerClass) {
		this.compilerClass = compilerClass;
	}

	@Override
	public void compile(C compiler, Section<T> section) throws CompilerMessage {

		if (section.get() == null) return;

		Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
		if (termIdentifier == null) {
			throw new CompilerMessage(Messages.error("Could not determine TermIdentifier"));
		}
		Class<?> termObjectClass = section.get().getTermObjectClass(compiler, section);

		compile(compiler, section, termIdentifier, termObjectClass);
	}

	public void compile(C compiler, Section<T> section, Identifier termIdentifier, Class<?> termObjectClass) {
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		terminologyManager.registerTermDefinition(compiler, section, termObjectClass, termIdentifier);

		if (compiler instanceof IncrementalCompiler) {
			Compilers.recompileRegistrations((IncrementalCompiler) compiler, termIdentifier);
		}
	}

	@Override
	public void destroy(C compiler, Section<T> section) {
		Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
		if (termIdentifier == null) {
			// we assume that also nothing could have been registered without an Identifier -> ergo nothing to unregister
			return;
			//throw CompilerMessage.error( "Could not determine TermIdentifier"));
		}
		Class<?> termObjectClass = section.get().getTermObjectClass(compiler, section);

		destroy(compiler, section, termIdentifier, termObjectClass);
	}

	public void destroy(C compiler, Section<T> section, Identifier termIdentifier, Class<?> termObjectClass) {
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		terminologyManager.unregisterTermDefinition(compiler, section, termObjectClass, termIdentifier);

		if (compiler instanceof IncrementalCompiler) {
			Compilers.destroyAndRecompileReferences((IncrementalCompiler) compiler, termIdentifier);
		}
	}
}
