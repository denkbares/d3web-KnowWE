package de.d3web.we.terminology.local;

import de.d3web.we.terminology.term.TerminologyHandler;



public abstract class LocalTerminologyHandler<T, E> extends TerminologyHandler<T, E> {

	
	public abstract E getTerminologicalObject(String id);
	

}
