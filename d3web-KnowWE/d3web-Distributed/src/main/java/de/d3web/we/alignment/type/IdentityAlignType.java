package de.d3web.we.alignment.type;

import de.d3web.we.basic.IdentifiableInstance;

public class IdentityAlignType extends AbstractAlignType {
	
	private static IdentityAlignType instance = new IdentityAlignType();

	private IdentityAlignType() {
		super();
	}
	
	public static IdentityAlignType getInstance() {
		return instance;
	}

	@Override
	public Object getAlignedValue(IdentifiableInstance input, IdentifiableInstance output) {
		return output.getValue();
	}

}
