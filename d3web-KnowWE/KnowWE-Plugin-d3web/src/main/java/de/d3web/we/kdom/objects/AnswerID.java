package de.d3web.we.kdom.objects;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public abstract class AnswerID extends D3webID {
	
	private static String ANSWER_STORE_KEY = "ANSWER_STORE_KEY";
	
	public abstract Section<QuestionID> getQuestionSection(Section<? extends AnswerID> s);

	@Override
	public Answer getObject(Section<? extends ObjectRef> s) {
		return (Answer)KnowWEUtils.getStoredObject(s, ANSWER_STORE_KEY);
	}
	
	public void storeAnswer(Section<? extends AnswerID> s, Answer a) {
		KnowWEUtils.storeSectionInfo(s, AnswerID.ANSWER_STORE_KEY, a);
	}
}
