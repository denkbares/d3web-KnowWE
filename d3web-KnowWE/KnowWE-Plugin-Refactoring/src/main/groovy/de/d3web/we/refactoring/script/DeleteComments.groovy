package de.d3web.we.refactoring.script

import de.d3web.we.refactoring.session.RefactoringSession

public class DeleteComments extends RefactoringScript{
	
	public DeleteComments(RefactoringSession refactoringSession){
		super(refactoringSession)
	}

	@Override
	public void run() {
		refactoringSession.identity {
			type = getTypeFromString('KnowWEArticle')
			objectID = findObjectID(type)
			deleteComments(objectID)
		}
	}
}