package de.d3web.we.terminology.global;

import java.util.Collection;
import java.util.TreeSet;

import de.d3web.utilities.ISetMap;
import de.d3web.we.alignment.AlignmentUtilRepository;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.local.LocalTerminologyAccess;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermFactory;
import de.d3web.we.terminology.term.TermInfoType;

public class GlobalTerminology {

	private TerminologyType type;
	private Term realRoot;
	private Collection<Term> terms;
	
	
	public GlobalTerminology(TerminologyType type) {
		super();
		this.type = type;
		this.realRoot = new Term(type);
		realRoot.setInfo(TermInfoType.TERM_NAME, "ROOT");
		terms = new TreeSet<Term>();
	}
	
	public TerminologyType getType() {
		return type;
	}


	public ISetMap<Object, Term> addTerminology(LocalTerminologyAccess terminology, String idString) {
		TermFactory termFactory = AlignmentUtilRepository.getInstance().getTermFactory(terminology.getContext());
		return termFactory.addTerminology(terminology, idString, this);
	}
	
	public Collection<Term> getRoots() {
		return realRoot.getChildren();
	}
	
	public void addRoot(Term term) {
		realRoot.addChild(term);
	}

	public Term getRoot() {
		return realRoot;
	}
	
	public Term getTerm(String name, Object value) {
		GlobalTerminologyHandler globalHandler = AlignmentUtilRepository.getInstance().getGlobalTerminogyHandler(this.getClass());
		globalHandler.setTerminology(this);
		for (Term term : globalHandler) {
			if(term.getInfo(TermInfoType.TERM_NAME).equals(name)) {
				if(value == null) {
					if(term.getInfo(TermInfoType.TERM_VALUE) == null) {
						return term;
					}
				} else {
					if(term.getInfo(TermInfoType.TERM_VALUE) != null 
							&& term.getInfo(TermInfoType.TERM_VALUE).equals(value)) {
						return term;
					}
				}
			}
		}
		return null;
	}
		
	public final Collection<Term> getAllTerms() {	
		return terms;
	}
	
	public void setTerms(Collection<Term> terms) {
		terms = new TreeSet<Term>(terms);
	}
	
	public void addTerm(Term term) {
		terms.add(term);
	}
	
	
}
