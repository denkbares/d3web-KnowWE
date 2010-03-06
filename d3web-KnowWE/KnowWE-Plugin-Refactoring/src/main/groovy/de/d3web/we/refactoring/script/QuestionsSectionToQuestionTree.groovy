package de.d3web.we.refactoring.script

public class QuestionsSectionToQuestionTree extends RefactoringScriptGroovy{

	@Override
	public void run() {
		type = getTypeFromString('KnowWEArticle')
		objectID = findObjectID(type)
		transformToQuestionTree objectID
	}
}