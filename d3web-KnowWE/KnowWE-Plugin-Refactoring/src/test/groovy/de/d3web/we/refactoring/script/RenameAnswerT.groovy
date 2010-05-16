package de.d3web.we.refactoring.script;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.questionTreeNew.QuestionTreeAnswerDef;

class RenameAnswerT extends Rename {
	@Override
	public Class<? extends KnowWEObjectType> findRenamingType() {
		return QuestionTreeAnswerDef.class;
	}
	
	@Override
	public <T extends KnowWEObjectType> String findObjectID(Class<T> clazz) {
		return "RenameObject/RootType/QuestionTree/QuestionTree@content/QuestionDashTree/" +
		"SubTree/SubTree/SubTree/DashTreeElement/QuestionDashTreeElementContent/AnswerLine/QuestionTreeAnswerDef";
	}
	
	@Override
	public String findNewName() {
		return "Antwort Umbenannt";
	}
}
