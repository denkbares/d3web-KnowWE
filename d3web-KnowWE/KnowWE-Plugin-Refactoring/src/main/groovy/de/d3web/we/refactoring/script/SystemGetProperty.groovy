package de.d3web.we.refactoring.script

import groovy.lang.GroovyInterceptable;

public class SystemGetProperty extends RefactoringScriptGroovy {
	
	@Override
	public void run() {
		System.getProperty("file.encoding")	
	}
}