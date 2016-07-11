package de.knowwe.rdf2go;

import com.denkbares.events.Event;

public abstract class ModifiedCoreDataEvent implements Event {

	private final Rdf2GoCore core;

	public ModifiedCoreDataEvent(Rdf2GoCore core) {
		this.core = core;
	}

	public Rdf2GoCore getCore() {
		return core;
	}

}
