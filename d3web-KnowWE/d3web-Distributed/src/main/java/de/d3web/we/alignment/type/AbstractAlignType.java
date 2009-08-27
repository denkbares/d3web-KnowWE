package de.d3web.we.alignment.type;

import de.d3web.we.basic.IdentifiableInstance;

public abstract class AbstractAlignType {

	public AbstractAlignType() {
		super();
	}
	
	public abstract Object getAlignedValue(IdentifiableInstance input, IdentifiableInstance output);
	
	public String toString() {
		return getClass().getSimpleName();
	}
	
}
