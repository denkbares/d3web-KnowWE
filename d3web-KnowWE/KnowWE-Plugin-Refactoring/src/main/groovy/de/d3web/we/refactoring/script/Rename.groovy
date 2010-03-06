package de.d3web.we.refactoring.script

import groovy.lang.GroovyInterceptable;

public class Rename extends RefactoringScriptGroovy {

	@Override
	public void run() {
		renamingType = findRenamingType()
		objectID = findObjectID(renamingType)
		newName = findNewName()
		existingElements = findRenamingList(renamingType, objectID, newName)
		if (existingElements.size() == 0) {
			findRenamingList(renamingType, objectID).each{renameElement it, newName, renamingType}
		} else {
			printExistingElements existingElements
		}	
	}
}
