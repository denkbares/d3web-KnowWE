package de.d3web.we.kdom.objects;

import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;


public class QuestionID extends ObjectRef{
	
	private static String QUESTION_STORE_KEY = "QUESTION_STORE_KEY";

	@Override
	public  Question getObject(Section<? extends ObjectRef> s) {
		return (Question)KnowWEUtils.getStoredObject(s, QUESTION_STORE_KEY);
	}
	
	public static void storeQuestion(Section<QuestionID> s, Question q) {
		KnowWEUtils.storeSectionInfo(s, QuestionID.QUESTION_STORE_KEY, q);
	}

}
