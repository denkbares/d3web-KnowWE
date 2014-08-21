package de.knowwe.rdf2go;

import java.util.Collection;
import java.util.TreeSet;

import org.ontoware.rdf2go.model.Statement;


public class InsertStatementsEvent extends ModifiedCoreDataEvent {

    private final Collection<Statement> insertCache;
    private final Collection<Statement> removeCache;


    public InsertStatementsEvent(Collection<Statement> removeCache, Collection<Statement> insertCache, Rdf2GoCore core) {
		super(core);
        this.removeCache = removeCache;
        this.insertCache = insertCache;
	}

    public Collection<Statement> getStatements() {
        return getActuallyAdded();
    }

    public Collection<Statement> getActuallyAdded() {
        Collection<Statement> actuallyAdded = new TreeSet<>(insertCache);
        actuallyAdded.removeAll(removeCache);
        return  actuallyAdded;
    }
}


