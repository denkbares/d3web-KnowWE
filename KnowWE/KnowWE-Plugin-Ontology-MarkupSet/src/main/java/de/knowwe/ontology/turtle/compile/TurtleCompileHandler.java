package de.knowwe.ontology.turtle.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.ontology.turtle.TurtleSentence;

public class TurtleCompileHandler extends OntologyHandler<TurtleSentence> {

	@SuppressWarnings({
			"unchecked", "rawtypes" })
	@Override
	public Collection<Message> create(OntologyCompiler compiler, Section<TurtleSentence> section) {

		List<Message> messages = new ArrayList<Message>();

		List<Section<StatementProvider>> statementProviders = Sections.successors(
				section, StatementProvider.class);
		for (Section<StatementProvider> statementSection : statementProviders) {

			StatementProviderResult providerResult = statementSection.get().getStatements(
					statementSection, compiler);
			if (providerResult != null) {
				compiler.getRdf2GoCore().addStatements(section, providerResult.getStatments());
				messages.addAll(providerResult.getMessages());
			}

		}
		return messages;
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<TurtleSentence> section) {
		compiler.getRdf2GoCore().removeStatements(section);
	}

}
