package de.d3web.we.terminology.term;

public interface TermUpdater<E> {

	public void updateTerm(Term term, E object);
	
}
