package de.d3web.we.refactoring.script

public class EstablishedSolutionsFindingsTraceToXCL extends RefactoringScriptGroovy {

	@Override
	public void run() {
		type = getTypeFromString('KnowWEArticle')
		objectID = findObjectID(type)
		establishedSolutions = findSolutions('(!)', objectID)
		establishedSolutions.each{
			createXCLFromFindingsTrace it
			deleteSolutionOccurrences it
		}
	}
}
