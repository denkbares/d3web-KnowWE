package de.d3web.we.alignment;

public class NumericalIdentity {
	
	public NumericalIdentity() {
		super();
	}

	public boolean equals(Object o) {
		if(o instanceof NumericalIdentity || o instanceof Number) return true;
		return false;
	}
	
	public int hashCode() {
		return NumericalIdentity.class.hashCode();
	}
	
	public String toString() {
		return "NumericalIdentity";
	}
	
}
