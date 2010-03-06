package de.d3web.we.refactoring.script;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.objects.QuestionnaireID;

class RenameQuestionnaireT extends Rename {
	@Override
	public Class<? extends KnowWEObjectType> findRenamingType() {
		return QuestionnaireID.class;
	}
	
	@Override
	public <T extends KnowWEObjectType> String findObjectID(Class<T> clazz) {
		return "RenameObject/RootType/QuestionTree/QuestionTree@content/QuestionDashTree/SubTree/SubTree/" +
		"SubTree/SubTree/DashTreeElement/QuestionDashTreeElementContent/IndicationLine/QuestionnaireID";
	}
	
	@Override
	public String findNewName() {
		return "Fragebogen Umbenannt";
	}
}
