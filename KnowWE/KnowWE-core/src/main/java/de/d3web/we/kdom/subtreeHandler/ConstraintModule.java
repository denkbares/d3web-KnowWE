package de.d3web.we.kdom.subtreeHandler;

import de.d3web.we.kdom.KnowWEObjectType;



public abstract class ConstraintModule<T extends KnowWEObjectType> implements IncrementalConstraint<T> {

	public Operator OPERATOR;

	public Purpose PURPOSE;

	public enum Operator {
		DONT_COMPILE_IF_VIOLATED, COMPILE_IF_VIOLATED
	}

	public enum Purpose {
		CREATE, DESTROY, CREATE_AND_DESTROY
	}
	
	public ConstraintModule() {
		this(Operator.COMPILE_IF_VIOLATED, Purpose.CREATE_AND_DESTROY);
	}
	
	public ConstraintModule(Operator o, Purpose p) {
		if (o != null) this.OPERATOR = o;
		if (p != null) this.PURPOSE = p;
	}

}
