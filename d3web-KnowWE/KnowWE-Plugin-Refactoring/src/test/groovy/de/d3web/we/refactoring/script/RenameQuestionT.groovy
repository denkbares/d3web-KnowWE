package de.d3web.we.refactoring.script;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.objects.QuestionID;

class RenameQuestionT extends Rename {
	@Override
	public Class<? extends KnowWEObjectType> findRenamingType() {
		return QuestionID.class;
	}
	
	@Override
	public <T extends KnowWEObjectType> String findObjectID(Class<T> clazz) {
		return "RenameObject/RootType/QuestionTree/QuestionTree@content/QuestionDashTree/SubTree/" +
		"SubTree/DashTreeElement/QuestionDashTreeElementContent/QuestionLine/QuestionID";
	}
	
	@Override
	public String findNewName() {
		return "Frage Umbenannt";
	}
}
