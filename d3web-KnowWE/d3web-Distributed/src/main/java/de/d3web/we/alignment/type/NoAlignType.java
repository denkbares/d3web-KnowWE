package de.d3web.we.alignment.type;

import de.d3web.we.basic.IdentifiableInstance;

public class NoAlignType extends AbstractAlignType {

	private static NoAlignType instance = new NoAlignType();

	private NoAlignType() {
		super();
	}
	
	public static NoAlignType getInstance() {
		return instance;
	}

	@Override
	public Object getAlignedValue(IdentifiableInstance input, IdentifiableInstance output) {
		return null;
	}

}
