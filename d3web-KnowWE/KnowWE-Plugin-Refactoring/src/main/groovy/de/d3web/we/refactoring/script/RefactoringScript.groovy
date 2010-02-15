package de.d3web.we.refactoring.script

import de.d3web.we.refactoring.session.RefactoringSession

public abstract class RefactoringScript {
	def RefactoringSession refactoringSession
	def storage = [:]
	def propertyMissing(String name) {
		storage[name]
	}
	def propertyMissing(String name, value) {
		storage[name] = value
	}
	
	public RefactoringScript(RefactoringSession refactoringSession){
		this.refactoringSession = refactoringSession
	}
	public abstract void run() 
}
