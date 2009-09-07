package de.d3web.we.kdom;

public class IDGeneratorOutput {
	
	private String id;
	
	private boolean idConflict;
	
	public IDGeneratorOutput(String id, boolean idConflict){
		this.id = id;
		this.idConflict = idConflict;
	}

	public String getID() {
		return id;
	}
	
	public boolean isIdConflict() {
		return idConflict;
	}
}
