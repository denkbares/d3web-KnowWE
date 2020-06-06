/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.objects;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DestroyScript;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;

/**
 * Script to incrementally register a term definition section to the terminology manager.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 06.06.2020
 */
public abstract class AbstractDefinitionRegistrationScript<C extends TermCompiler, T extends Type> implements CompileScript<C, T>, DestroyScript<C, T> {

	private final Class<C> compilerClass;

	public AbstractDefinitionRegistrationScript(Class<C> compilerClass) {
		this.compilerClass = compilerClass;
	}

	@Override
	public Class<C> getCompilerClass() {
		return compilerClass;
	}

	/**
	 * Creates a new instance of the definition registration script for the specified compiler, term class, and supplier
	 * method for the identifier to be registered.
	 */
	public static <C extends TermCompiler, T extends Type> AbstractDefinitionRegistrationScript<C, T>
	create(Class<C> compilerClass, Class<?> termClass, Function<Section<T>, Identifier> identifierSupplier) {
		return create(compilerClass, termClass, (compiler, section) -> identifierSupplier.apply(section));
	}

	/**
	 * Creates a new instance of the definition registration script for the specified compiler, term class, and supplier
	 * method for the identifier to be registered.
	 */
	public static <C extends TermCompiler, T extends Type> AbstractDefinitionRegistrationScript<C, T>
	create(Class<C> compilerClass, Class<?> termClass, BiFunction<C, Section<T>, Identifier> identifierSupplier) {
		return new AbstractDefinitionRegistrationScript<C, T>(compilerClass) {
			@Override
			@Nullable
			public Identifier getRegisteredIdentifier(C compiler, Section<T> section) {
				return identifierSupplier.apply(compiler, section);
			}

			@Override
			@NotNull
			public Class<?> getRegisteredClass(C compiler, Section<T> section) {
				return termClass;
			}
		};
	}

	/**
	 * Returns the identifier to be registered as a term for the specified section. The method may return null if no
	 * registration should be performed. The method may also throw a compiler message to create an error / warning that
	 * the term registration has failed. In this case no registration is performed.
	 *
	 * @param compiler the compiler to register the term for
	 * @param section  the section to register a term for
	 * @return the term to be registered, or null to skip registering
	 * @throws CompilerMessage if an error should be created instead of registering the term
	 */
	@Nullable
	public abstract Identifier getRegisteredIdentifier(C compiler, Section<T> section) throws CompilerMessage;

	/**
	 * Returns the term class to register the term identifier for the specified section. The method may throw a compiler
	 * message to create an error / warning that the term registration has failed. In this case no registration is
	 * performed.
	 *
	 * @param compiler the compiler to register the term for
	 * @param section  the section to register a term for
	 * @return the term class to be registered
	 * @throws CompilerMessage if an error should be created instead of registering the term
	 */
	@NotNull
	public abstract Class<?> getRegisteredClass(C compiler, Section<T> section) throws CompilerMessage;

	@Override
	public void compile(C compiler, Section<T> section) throws CompilerMessage {
		if (section.get() == null) return;
		Identifier termIdentifier = getRegisteredIdentifier(compiler, section);
		if (termIdentifier != null) {
			Class<?> termObjectClass = getRegisteredClass(compiler, section);
			compile(compiler, section, termIdentifier, termObjectClass);
		}
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
		try {
			Identifier termIdentifier = getRegisteredIdentifier(compiler, section);
			// we assume that also nothing could have been registered without an Identifier -> ergo nothing to unregister
			if (termIdentifier != null) {
				Class<?> termObjectClass = getRegisteredClass(compiler, section);
				destroy(compiler, section, termIdentifier, termObjectClass);
			}
		}
		catch (CompilerMessage ignore) {
			// if an error occurs here, we assume that no registration is performed, so no de-registration is required
		}
	}

	public void destroy(C compiler, Section<T> section, Identifier termIdentifier, Class<?> termObjectClass) {
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		terminologyManager.unregisterTermDefinition(compiler, section, termObjectClass, termIdentifier);

		if (compiler instanceof IncrementalCompiler) {
			Compilers.destroyAndRecompileReferences((IncrementalCompiler) compiler, termIdentifier);
		}
	}
}
