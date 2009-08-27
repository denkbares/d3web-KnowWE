package de.d3web.we.alignment.aligner;

import java.util.List;

import de.d3web.we.alignment.LocalAlignment;
import de.d3web.we.terminology.local.LocalTerminologyStorage;

public interface LocalAligner<E> {

	public List<LocalAlignment> align(LocalTerminologyStorage storage, E object, String idString);
		
	
}
