package de.d3web.we.refactoring.script;

import java.util.List;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.rules.RulesSectionContent;


			//1 Coveringlist-Section mit der id holen
			Section<?> knowledgeSection = ra.findKnowledgeSection();
			//2 Alle Finding 's dieser Section holen
			List<Section<Finding>> findingSections = ra.findFindings(knowledgeSection);
			//3 SolutionID holen
			String solutionID = ra.findSolutionID(knowledgeSection);
			//4 Pro Finding eine Regel bauen
			StringBuilder sb = new StringBuilder("");
			for (Section<Finding> sec : findingSections) {
				ra.createRulesText(solutionID, sb, sec);
			}
			sb.append("\n");
			//5 Lösche entsprechende XCList
			ra.deleteXCList(knowledgeSection);
			//6 Füge Regel ein und 7 speichere Artikel
			Section<RulesSectionContent> rulesSectionContent = ra.findRulesSectionContent();
			ra.saveArticle(sb, rulesSectionContent);
			
			System.out.println("Article saved per Groovy!");

