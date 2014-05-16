package de.knowwe.rdf2go;

import java.util.Collection;

import org.ontoware.rdf2go.model.Statement;

import de.knowwe.core.event.Event;

public abstract class ModifiedCoreDataEvent extends Event {

	private final Collection<Statement> implicitStatements;
	private final Collection<Statement> actualStatements;
	private final Rdf2GoCore core;

	public ModifiedCoreDataEvent(Collection<Statement> actualStatements,
								 Collection<Statement> implicitStatements, Rdf2GoCore core) {
		this.actualStatements = actualStatements;
		this.implicitStatements = implicitStatements;
		this.core = core;
	}

	public Collection<Statement> getStatements() {
		return actualStatements;
	}

	public Collection<Statement> getImplicitStatements() {
		return implicitStatements;
	}

	public Rdf2GoCore getCore() {
		return core;
	}
}
