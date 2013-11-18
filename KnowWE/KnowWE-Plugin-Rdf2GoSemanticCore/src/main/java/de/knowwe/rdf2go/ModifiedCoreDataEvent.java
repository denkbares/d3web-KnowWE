package de.knowwe.rdf2go;

import java.util.Collection;

import org.ontoware.rdf2go.model.Statement;

import de.knowwe.core.event.Event;

public abstract class ModifiedCoreDataEvent extends Event {
	private final Collection<Statement> statements;
	private final Rdf2GoCore core;

	public ModifiedCoreDataEvent(Collection<Statement> statements, Rdf2GoCore core) {
		this.statements = statements;
		this.core = core;
	}

	public Collection<Statement> getStatements() {
		return statements;
	}

	public Rdf2GoCore getCore() {
		return core;
	}
}
