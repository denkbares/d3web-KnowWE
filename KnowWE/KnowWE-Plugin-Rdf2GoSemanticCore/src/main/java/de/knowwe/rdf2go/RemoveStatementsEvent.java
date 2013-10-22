package de.knowwe.rdf2go;

import java.util.Collection;

import org.ontoware.rdf2go.model.Statement;

import de.knowwe.core.event.Event;

public class RemoveStatementsEvent extends Event {

	private final Collection<Statement> statements;

	public RemoveStatementsEvent(Collection<Statement> statements) {
		this.statements = statements;
	}

	public Collection<Statement> getStatements() {
		return statements;
	}

}
