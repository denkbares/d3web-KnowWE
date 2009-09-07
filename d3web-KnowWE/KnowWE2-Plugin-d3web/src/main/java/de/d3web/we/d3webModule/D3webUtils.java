package de.d3web.we.d3webModule;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;

public class D3webUtils {
	
	public static de.d3web.kernel.domainModel.qasets.Question getQuestion(KnowledgeServiceSession kss, String qid) {
		if(kss instanceof D3webKnowledgeServiceSession) {
			D3webKnowledgeServiceSession session = ((D3webKnowledgeServiceSession)kss);
			KnowledgeBase kb = session.getBaseManagement().getKnowledgeBase();
			return session.getBaseManagement().findQuestion(qid);

		}
		
		return null;
	}
	
	public static AnswerChoice getAnswer(KnowledgeServiceSession kss, String aid, String qid) {
		Question q = getQuestion(kss, qid);
		if(q != null) {
			D3webKnowledgeServiceSession session = ((D3webKnowledgeServiceSession)kss);
			KnowledgeBase kb = session.getBaseManagement().getKnowledgeBase();
			return (AnswerChoice)session.getBaseManagement().findAnswer(q, aid);
		}
		return null;
	}

}
