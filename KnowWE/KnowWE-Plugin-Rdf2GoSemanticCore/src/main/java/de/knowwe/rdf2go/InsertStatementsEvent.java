package de.knowwe.rdf2go;

import java.util.Collection;

import org.ontoware.rdf2go.model.Statement;


public class InsertStatementsEvent extends ModifiedCoreDataEvent {

	public InsertStatementsEvent(Collection<Statement> actual, Collection<Statement> implicit, Rdf2GoCore core) {
		super(actual, implicit, core);
	}

}


