/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile.provider;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.rdf2go.Rdf2GoCompiler;

public interface StatementProvider<T extends Type> extends Type {

	@NotNull
	StatementProviderResult getStatements(Section<? extends T> section, Rdf2GoCompiler compiler) throws CompilerMessage;

	@NotNull
	default StatementProviderResult getStatementsSafe(Section<? extends T> section, Rdf2GoCompiler compiler) {
		try {
			return getStatements(section, compiler);
		}
		catch (CompilerMessage e) {
			StatementProviderResult result = new StatementProviderResult(compiler);
			e.getMessages().forEach(result::addMessage);
			return result;
		}
	}
}
