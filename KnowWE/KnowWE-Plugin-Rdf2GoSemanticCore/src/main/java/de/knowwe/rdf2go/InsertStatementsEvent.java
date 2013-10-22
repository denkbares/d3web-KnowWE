package de.knowwe.rdf2go;

import java.util.Collection;

import org.ontoware.rdf2go.model.Statement;

import de.knowwe.core.event.Event;

public class InsertStatementsEvent extends Event {

	private final Collection<Statement> statements;

	public InsertStatementsEvent(Collection<Statement> statements) {
		this.statements = statements;
	}

	public Collection<Statement> getStatements() {
		return statements;
	}
}
