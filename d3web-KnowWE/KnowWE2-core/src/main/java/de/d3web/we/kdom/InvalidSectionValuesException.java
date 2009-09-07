package de.d3web.we.kdom;


public class InvalidSectionValuesException extends Exception{
	
	private Section sec = null;
	private String s;
	
	public InvalidSectionValuesException (Section sec) {
		this.sec = sec;
	}
	
	public InvalidSectionValuesException (String sec) {
		this.s = sec;
	}

	@Override
	public String getMessage() {
		if(sec != null) {
		return super.getMessage()+" Section:"+sec.toString();
		}
		return s;
	}
	
	
}
