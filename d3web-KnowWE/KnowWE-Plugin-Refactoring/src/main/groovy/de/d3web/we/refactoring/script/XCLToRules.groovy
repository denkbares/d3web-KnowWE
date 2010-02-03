package de.d3web.we.refactoring.script;

import java.util.List;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.rules.RulesSectionContent;

@Deprecated
// Coveringlist-Section mit der id holen
Section<?> knowledgeSection = ra.findKnowledgeSection();
// Alle Finding 's dieser Section holen
List<Section<Finding>> findingSections = ra.findFindings(knowledgeSection);
// SolutionID holen
String solutionID = ra.findSolutionID(knowledgeSection);
// Pro Finding eine Regel bauen
StringBuilder sb = new StringBuilder("");
for (Section<Finding> sec : findingSections) {
	ra.createRulesText(solutionID, sb, sec);
}
sb.append("\n");
// Lösche entsprechende XCList
ra.deleteXCList(knowledgeSection);
// Füge Regel ein
Section<RulesSectionContent> rulesSectionContent = ra.findRulesSectionContent();
// speichere Artikel
ra.saveArticle(sb, rulesSectionContent);

System.out.println("Article saved per Groovy!");

//package de.d3web.we.refactoring.script
//
//rs.identity{
//	knowledgeSection = findKnowledgeSection()
//	solutionID = findSolutionID(knowledgeSection)
//	sb = new StringBuilder()
//	findFindings(knowledgeSection).each{createRulesText solutionID, sb, it}
//	deleteXCList knowledgeSection
//	saveArticle sb, findRulesSectionContent(knowledgeSection)
//}