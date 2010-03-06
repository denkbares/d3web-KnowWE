package de.d3web.we.refactoring.script

public class DeleteComments extends RefactoringScriptGroovy{

	@Override
	public void run() {
		type = getTypeFromString('KnowWEArticle')
		objectID = findObjectID(type)
		deleteComments(objectID)
	}
}