package de.d3web.we.alignment.aligner;

import java.util.List;

import de.d3web.we.alignment.GlobalAlignment;
import de.d3web.we.terminology.term.Term;

public interface GlobalAligner<E> {

	public List<GlobalAlignment> align(Term term, E object, String idString);
		
	
}
