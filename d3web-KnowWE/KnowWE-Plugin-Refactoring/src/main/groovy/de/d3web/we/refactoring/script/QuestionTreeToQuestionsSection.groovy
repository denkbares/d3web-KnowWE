package de.d3web.we.refactoring.script

public class QuestionTreeToQuestionsSection extends RefactoringScriptGroovy{

	@Override
	public void run() {
		type = getTypeFromString('KnowWEArticle')
		objectID = findObjectID(type)
		transformToQuestionsSection objectID
	}
}