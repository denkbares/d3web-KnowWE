package de.d3web.we.refactoring.script

import de.d3web.we.refactoring.session.RefactoringSession

public class XCLToRules extends RefactoringScript {

	public XCLToRules(RefactoringSession refactoringSession) {
		super(refactoringSession)
	}

	@Override
	public void run() {
		refactoringSession.identity {
			xcList = findXCList()
			solutionID = findSolutionID(xcList)
			rulesText = new StringBuffer()
			findFindings(xcList).each{createRulesText it, solutionID, rulesText}
			deleteXCList xcList
			addRulesText rulesText, findRulesSectionContent(xcList)
		}
	}
}
