package de.d3web.we.refactoring.script

import de.d3web.we.refactoring.dialog.RefactoringScript 

public abstract class RefactoringScriptGroovy extends RefactoringScript {

	def storage = [:]
	def propertyMissing(String name) {
		storage[name]
	}
	def propertyMissing(String name, value) {
		storage[name] = value
	}
	
	public abstract void run() 
}
