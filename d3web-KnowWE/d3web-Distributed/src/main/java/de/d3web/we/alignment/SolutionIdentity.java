package de.d3web.we.alignment;

import de.d3web.we.basic.SolutionState;

public class SolutionIdentity {
	
	public SolutionIdentity() {
		super();
	}

	public boolean equals(Object o) {
		if(o instanceof SolutionIdentity || o instanceof SolutionState) return true;
		return false;
	}
	
	public int hashCode() {
		return SolutionIdentity.class.hashCode();
	}
	
	public String toString() {
		return "SolutionIdentity";
	}
	
}
