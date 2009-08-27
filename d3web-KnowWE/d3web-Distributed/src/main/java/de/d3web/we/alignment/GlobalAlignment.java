package de.d3web.we.alignment;

import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.terminology.term.Term;

public class GlobalAlignment extends Alignment {

	protected final Term term;
	
	public GlobalAlignment(Term term, IdentifiableInstance object, AbstractAlignType type) {
		super(object, type);
		this.term = term;
	}

	@Override
	public IdentifiableInstance getAligned(IdentifiableInstance ii) {
		return getObject();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!super.equals(o)) return false;
		if(!(o instanceof GlobalAlignment)) return false;
		GlobalAlignment alignment = (GlobalAlignment) o;
		return alignment.getTerm().equals(term);
	}

	@Override
	public int hashCode() {
		try {
			return super.hashCode() + 19 * term.hashCode();
			
		} catch (Exception e) {
			return 0;// TODO: handle exception
		}
	}
	
	@Override
	public String toString() {
		return term.toString() + " " + super.toString();
	}

	public Term getTerm() {
		return term;
	}

	
}
