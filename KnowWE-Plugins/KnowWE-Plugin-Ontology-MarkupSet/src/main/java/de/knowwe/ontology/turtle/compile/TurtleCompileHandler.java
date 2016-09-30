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

		List<Section<StatementProvider>> statementProviders = Sections.successors(
				section, StatementProvider.class);
		for (Section<StatementProvider> statementSection : statementProviders) {

			StatementProviderResult providerResult = statementSection.get().getStatements(
					statementSection, compiler);
			if (providerResult != null) {
				compiler.getRdf2GoCore().addStatements(section, providerResult.getStatements());
				messages.addAll(providerResult.getMessages());
			}

		}
		return messages;
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<Z> section) {
		compiler.getRdf2GoCore().removeStatements(section);
	}
}
