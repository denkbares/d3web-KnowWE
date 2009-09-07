package de.d3web.we.javaEnv;

public class NoParseResultException extends Exception {
	
	public NoParseResultException(String message) {
		super(message);
	}
	public NoParseResultException() {
		super("Article not (yet) parsed!");
	}
	
	

}
