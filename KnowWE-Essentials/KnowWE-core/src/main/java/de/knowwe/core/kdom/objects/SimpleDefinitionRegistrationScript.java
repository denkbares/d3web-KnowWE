/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;

/**
 * Script to incrementally register a term definition section to the terminology manager.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 01.04.2019
 */
public class SimpleDefinitionRegistrationScript<C extends TermCompiler, T extends TermDefinition> extends AbstractDefinitionRegistrationScript<C, T> {

	public SimpleDefinitionRegistrationScript(Class<C> compilerClass) {
		super(compilerClass);
	}

	@Override
	@Nullable
	public Identifier getRegisteredIdentifier(C compiler, Section<T> section) throws CompilerMessage {
		// for term classes we do not allow null identifiers, creating a generic error message in this case
		Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
		if (termIdentifier != null) return termIdentifier;
		throw new CompilerMessage(Messages.error("Could not determine TermIdentifier"));
	}

	@Override
	@NotNull
	public Class<?> getRegisteredClass(C compiler, Section<T> section) {
		return section.get().getTermObjectClass(compiler, section);
	}
}
