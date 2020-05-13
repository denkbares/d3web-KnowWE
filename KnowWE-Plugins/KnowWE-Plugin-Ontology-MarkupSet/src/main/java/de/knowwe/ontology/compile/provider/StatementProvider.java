/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile.provider;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.ontology.compile.OntologyCompiler;

public interface StatementProvider<T extends Type> extends Type {

	@NotNull
	StatementProviderResult getStatements(OntologyCompiler compiler, Section<? extends T> section) throws CompilerMessage;

	@NotNull
	default StatementProviderResult getStatementsSafe(OntologyCompiler compiler, Section<? extends T> section) {
		try {
			return getStatements(compiler, section);
		}
		catch (CompilerMessage e) {
			StatementProviderResult result = new StatementProviderResult(compiler);
			e.getMessages().forEach(result::addMessage);
			return result;
		}
	}
}
