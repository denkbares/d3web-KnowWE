/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.ontology.turtle.TurtleMarkup;
import de.knowwe.rdf2go.ClassAndSectionSource;

import static de.knowwe.core.kdom.parsing.Sections.$;

public class StatementProviderHandler<Z extends Type> extends OntologyHandler<Z> {

	@Override
	public Collection<Message> create(OntologyCompiler compiler, Section<Z> section) {
		List<Message> messages = new ArrayList<>();

		String compile = $(section).ancestor(TurtleMarkup.class)
				.mapFirst(s -> DefaultMarkupType.getAnnotation(s, TurtleMarkup.COMPILE));
		if ("false".equals(compile)) {
			return messages;
		}

		//noinspection rawtypes
		for (Section<StatementProvider> statementSection : Sections.successors(section, StatementProvider.class)) {
			//noinspection unchecked
			StatementProviderResult result = statementSection.get().getStatementsSafe(compiler, statementSection);
			compiler.getRdf2GoCore().addStatements(new ClassAndSectionSource(section, getClass()), result.getStatements());
			messages.addAll(result.getMessages());
		}
		return messages;
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<Z> section) {
		compiler.getRdf2GoCore().removeStatements(new ClassAndSectionSource(section, getClass()));
	}
}
