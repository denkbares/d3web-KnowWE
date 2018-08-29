package de.knowwe.ontology.turtle.compile;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.rdf2go.Rdf2GoCompiler;

public interface StatementProvider<T extends Type> extends Type {

	@NotNull
	StatementProviderResult getStatements(Section<? extends T> section, Rdf2GoCompiler core) throws CompilerMessage;

	@NotNull
	default StatementProviderResult getStatementsSafe(Section<? extends T> section, Rdf2GoCompiler core) {
		try {
			return getStatements(section, core);
		}
		catch (CompilerMessage e) {
			StatementProviderResult result = new StatementProviderResult(core);
			e.getMessages().forEach(result::addMessage);
			return result;
		}
	}
}
