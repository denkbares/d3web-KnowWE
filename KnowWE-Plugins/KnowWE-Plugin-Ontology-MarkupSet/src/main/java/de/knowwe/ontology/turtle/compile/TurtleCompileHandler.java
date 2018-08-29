package de.knowwe.ontology.turtle.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;

public class TurtleCompileHandler<Z extends Type> extends OntologyHandler<Z> {

	@Override
	public Collection<Message> create(OntologyCompiler compiler, Section<Z> section) {
		List<Message> messages = new ArrayList<>();

		for (Section<StatementProvider> statementSection : Sections.successors(section, StatementProvider.class)) {
			StatementProviderResult result = statementSection.get().getStatementsSafe(statementSection, compiler);
			if (result != null) {
				compiler.getRdf2GoCore().addStatements(section, result.getStatements());
				messages.addAll(result.getMessages());
			}
		}
		return messages;
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<Z> section) {
		compiler.getRdf2GoCore().removeStatements(section);
	}
}
