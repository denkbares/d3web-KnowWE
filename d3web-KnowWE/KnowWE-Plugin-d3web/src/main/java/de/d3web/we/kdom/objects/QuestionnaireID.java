package de.d3web.we.kdom.objects;

import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.decisionTree.QClassID;
import de.d3web.we.utils.KnowWEUtils;

public class QuestionnaireID extends D3webID {

	private static String QC_STORE_KEY = "QUESTION_STORE_KEY";

	@Override
	public QContainer getObject(Section<? extends ObjectRef> s) {
		return (QContainer)KnowWEUtils.getStoredObject(s, QC_STORE_KEY);
	}

	public static void storeQuestionnaire(Section<QuestionnaireID> s, QContainer q) {
		KnowWEUtils.storeSectionInfo(s, QC_STORE_KEY, q);
	}

}
