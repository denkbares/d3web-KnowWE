package de.d3web.we.alignment.type;

import de.d3web.we.basic.IdentifiableInstance;

public class SolutionIdentityAlignType extends AbstractAlignType {

	private static SolutionIdentityAlignType instance = new SolutionIdentityAlignType();

	private SolutionIdentityAlignType() {
		super();
	}
	
	public static SolutionIdentityAlignType getInstance() {
		return instance;
	}

	@Override
	public Object getAlignedValue(IdentifiableInstance input, IdentifiableInstance output) {
		return input.getValue();
	}

}
