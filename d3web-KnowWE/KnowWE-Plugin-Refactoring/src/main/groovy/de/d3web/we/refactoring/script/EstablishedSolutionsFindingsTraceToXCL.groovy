package de.d3web.we.refactoring.script

import de.d3web.we.refactoring.session.RefactoringSession

public class EstablishedSolutionsFindingsTraceToXCL extends RefactoringScript {

	public EstablishedSolutionsFindingsTraceToXCL(RefactoringSession refactoringSession) {
		super(refactoringSession)
	}

	@Override
	public void run() {
		refactoringSession.identity {
			type = getTypeFromString('KnowWEArticle')
			objectID = findObjectID(type)
			establishedSolutions = findSolutions('(!)', objectID)
			establishedSolutions.each{
				createXCLFromFindingsTrace it
				deleteSolutionOccurrences it
			}
		}
	}
}
