package de.d3web.we.flow.testcase;

import java.util.List;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.table.Table;

public class TestcaseUtils {
	public static String getKnowledge(Section<?> s) {
		
		D3webKnowledgeService knowledgeService = D3webModule.getAD3webKnowledgeServiceInTopic(s.getWeb(), s.getTitle());
	
		List<Question> qlist = knowledgeService.getBase().getQuestions();
		
		StringBuffer buffy = new StringBuffer();

		for (Question q : qlist) {
			buffy.append(q + ",");

		}
		return buffy.substring(0, buffy.length() -1);
	}
}
