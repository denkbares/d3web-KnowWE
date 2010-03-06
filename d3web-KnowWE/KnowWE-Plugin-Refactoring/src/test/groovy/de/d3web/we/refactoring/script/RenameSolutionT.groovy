package de.d3web.we.refactoring.script;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.decisionTree.SolutionID;

class RenameSolutionT extends Rename {
	@Override
	public Class<? extends KnowWEObjectType> findRenamingType() {
		return SolutionID.class;
	}
	
	@Override
	public <T extends KnowWEObjectType> String findObjectID(Class<T> clazz) {
		return "RenameObject/RootType/QuestionTree/QuestionTree@content/QuestionDashTree/" +
		"SubTree/SubTree/SubTree2/SubTree/DashTreeElement/QuestionDashTreeElementContent/SetValueLine/QuestionID";
	}
	
	@Override
	public String findNewName() {
		return "Loesung Umbenannt";
	}
}
