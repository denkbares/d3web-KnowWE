package de.d3web.we.refactoring.script

import de.d3web.we.refactoring.session.RefactoringSession

public class Rename extends RefactoringScript {

	public Rename(RefactoringSession refactoringSession) {
		super(refactoringSession)
	}

	@Override
	public void run() {
		refactoringSession.identity {
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
}
