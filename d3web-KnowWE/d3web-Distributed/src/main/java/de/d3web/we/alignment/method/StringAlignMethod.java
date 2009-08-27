package de.d3web.we.alignment.method;

import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.alignment.type.IdentityAlignType;
import de.d3web.we.alignment.type.NoAlignType;


public class StringAlignMethod implements AlignMethod<String> {

	public AbstractAlignType align(String string1, String string2) {
		if(string1.equals(string2)) {
			return IdentityAlignType.getInstance();
		}
		return NoAlignType.getInstance();
	}

	

	
}
