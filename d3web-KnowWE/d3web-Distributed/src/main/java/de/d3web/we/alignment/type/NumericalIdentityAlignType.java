package de.d3web.we.alignment.type;

import de.d3web.we.basic.IdentifiableInstance;

public class NumericalIdentityAlignType extends AbstractAlignType {

	private static NumericalIdentityAlignType instance = new NumericalIdentityAlignType();

	private NumericalIdentityAlignType() {
		super();
	}
	
	public static NumericalIdentityAlignType getInstance() {
		return instance;
	}

	@Override
	public Object getAlignedValue(IdentifiableInstance input, IdentifiableInstance output) {
		return input.getValue();
	}

}
