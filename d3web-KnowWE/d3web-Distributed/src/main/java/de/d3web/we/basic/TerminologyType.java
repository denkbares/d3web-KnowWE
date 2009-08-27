package de.d3web.we.basic;

public enum TerminologyType {

	symptom("symptom"), diagnosis("diagnosis"), local("local");
	
	private final String idString;
	
	private TerminologyType(String idString) {
		this.idString = idString;
	}
	
	public String getIdString() {
		return idString;
	}
	
	public static TerminologyType getType(String idString) {
		for (TerminologyType type : values()) {
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
