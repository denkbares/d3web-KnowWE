package de.knowwe.rdf2go;

/**
 * Gets fired when a Rdf2GoCore is to be destroyed.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.03.2014
 */
public class Rdf2GoCoreDestroyEvent extends ModifiedCoreDataEvent {

	public Rdf2GoCoreDestroyEvent(Rdf2GoCore core) {
		super(core);
	}
}
