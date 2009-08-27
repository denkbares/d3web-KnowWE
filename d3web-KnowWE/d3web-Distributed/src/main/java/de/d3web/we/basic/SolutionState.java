package de.d3web.we.basic;

public enum SolutionState {

	ESTABLISHED("established"), SUGGESTED("suggested"), UNCLEAR("unclear"), EXCLUDED("excluded"), CONFLICT("conflict");
	
	private final String idString;
	
	private SolutionState(String idString) {
		this.idString = idString;
	}
	
	public String getIdString() {
		return idString;
	}
	
	public static SolutionState getType(String idString) {
		for (SolutionState type : values()) {
			if(type.getIdString().equals(idString)) {
				return type;
			}
		}
		return null;
	}
	
	public String toString() {
		return getIdString();
	}
	
}
