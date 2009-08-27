package de.d3web.we.terminology.term;

import java.util.List;

import de.d3web.utilities.ISetMap;
import de.d3web.we.alignment.GlobalAlignment;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.local.LocalTerminologyAccess;

public interface TermFactory<T, E> {

	public List<GlobalAlignment> getAlignableTerms(E obj, String idString, GlobalTerminology gt);
	
	public Term getTerm(E object, TerminologyType type, GlobalTerminology gt);
	
	public ISetMap<E, Term> addTerminology(LocalTerminologyAccess<E> localTerminology, String idString, GlobalTerminology globalTerminology);
}
