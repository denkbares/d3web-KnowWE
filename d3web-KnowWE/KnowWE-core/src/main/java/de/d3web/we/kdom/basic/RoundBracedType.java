package de.d3web.we.kdom.basic;

import de.d3web.we.kdom.KnowWEObjectType;

public class RoundBracedType extends EmbracedType {
	
	public RoundBracedType(KnowWEObjectType bodyType) {
		super(bodyType, "(", ")");
	}
}
