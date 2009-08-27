package de.d3web.we.alignment;

import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.basic.IdentifiableInstance;


public class LocalAlignment extends Alignment {

	protected final IdentifiableInstance local;
	
	public LocalAlignment(IdentifiableInstance local, IdentifiableInstance object, AbstractAlignType type) {
		super(object, type);
		this.local = local;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof LocalAlignment)) return false;
		if(o == this) return true;
		LocalAlignment alignment = (LocalAlignment) o;
		if(!getType().equals(alignment.getType())) return false;
		return reflexiveEquals(alignment);
	}
	
	private boolean reflexiveEquals(LocalAlignment alignment) {
		return (getLocal().equals(alignment.getLocal()) && getObject().equals(alignment.getObject()))
			|| (getLocal().equals(alignment.getObject()) && getObject().equals(alignment.getLocal()));
	}

	public IdentifiableInstance getAligned(IdentifiableInstance ii) {
		if(ii.equals(getLocal())) return getObject();
		else return getLocal();
	}
	
	@Override
	public int hashCode() {
		return type.hashCode() + 37 * object.hashCode()+ 37 * local.hashCode() ;
	}
	
	@Override
	public String toString() {
		return local.toString() + " " + super.toString();
	}

	public IdentifiableInstance getLocal() {
		return local;
	}

	
	
}
