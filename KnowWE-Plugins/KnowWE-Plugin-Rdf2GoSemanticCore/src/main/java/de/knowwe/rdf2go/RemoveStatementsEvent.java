package de.knowwe.rdf2go;

import java.util.Collection;

import org.openrdf.model.Statement;

public class RemoveStatementsEvent extends ModifiedCoreDataEvent {

    private final Collection<Statement> actualStatements;


    public RemoveStatementsEvent(Collection<Statement> actual, Rdf2GoCore core) {
		super(core);
        this.actualStatements = actual;
	}

    public Collection<Statement> getStatements() {
        return actualStatements;
    }
}
