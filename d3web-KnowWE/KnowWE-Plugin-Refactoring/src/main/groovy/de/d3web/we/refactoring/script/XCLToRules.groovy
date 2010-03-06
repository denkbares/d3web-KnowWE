package de.d3web.we.refactoring.script

public class XCLToRules extends RefactoringScriptGroovy {

	@Override
	public void run() {
		xcList = findXCList()
		solutionID = findSolutionID(xcList)
		rulesText = new StringBuffer()
		findFindings(xcList).each{createRulesText it, solutionID, rulesText}
		deleteXCList xcList
		addRulesText rulesText, findRulesSectionContent(xcList)
	}
}
