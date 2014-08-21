package de.knowwe.rdf2go;

import de.knowwe.core.event.Event;
import org.ontoware.rdf2go.model.Statement;

import java.util.Collection;

public abstract class ModifiedCoreDataEvent extends Event {

    private final Rdf2GoCore core;

    public ModifiedCoreDataEvent(Rdf2GoCore core) {
        this.core = core;
    }

    public Rdf2GoCore getCore() {
        return core;
    }

}
