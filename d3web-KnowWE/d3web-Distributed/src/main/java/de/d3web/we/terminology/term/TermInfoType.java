package de.d3web.we.terminology.term;

public enum TermInfoType {

	TERM_NAME("termName"),
	TERM_VALUE_TYPE("termValueType"),
	TERM_VALUE("termValue"),
	TERM_SPECIAL_PROPERTIES("termSpecialProperties");
	
	
	private String name;

	private TermInfoType(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

	public static TermInfoType getType(String type) {
		for (TermInfoType each : values()) {
			if(each.getName().equals(type)) {
				return each;
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}
	
	
}
