package de.d3web.we.alignment.method;

import de.d3web.we.alignment.type.AbstractAlignType;

public interface AlignMethod<E> {

	public AbstractAlignType align(E object1, E object2);
	
}
