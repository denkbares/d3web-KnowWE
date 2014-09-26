package de.knowwe.rdf2go;

import de.knowwe.core.event.Event;

/**
 * Gets fired when a Rdf2GoCore is to be destroyed.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.03.2014
 */
public class Rdf2GoCoreDestroyEvent extends Event {

	private Rdf2GoCore core;

	public Rdf2GoCoreDestroyEvent(Rdf2GoCore rdf2GoCore) {
		this.core = rdf2GoCore;
	}

	public Rdf2GoCore getRdf2GoCore() {
		return this.core;
	}
}
