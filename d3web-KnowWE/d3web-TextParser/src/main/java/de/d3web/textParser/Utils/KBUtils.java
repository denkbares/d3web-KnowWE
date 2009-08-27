package de.d3web.textParser.Utils;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.QuestionYN;

public class KBUtils {
	/**
	 * Searches for the AnswerChoice of a YN-question
	 * This method is provided because YN-questions may have user-defined answer names
	 * instead of the standard answers YES or NO.
	 * @param question the question
	 * @param answerName answer name to search for
	 * @return the AnswerChoice represented by the given answer name
	 */
	public static AnswerChoice findAnswerYN(KnowledgeBaseManagement kbm, QuestionYN question, String answerName) {
		if (answerName.equals("yes") || answerName.equals("ja") || answerName.equals("true"))
			return kbm.findAnswerChoice(question, "Yes");
		else if (answerName.equals("no") || answerName.equals("nein") || answerName.equals("false"))
			return kbm.findAnswerChoice(question, "No");
		else
			return null;
	}
	
	/**
	 * Searches for a NamedObject with text <CODE>objectName</CODE>.
	 * Searches QContainers, Questions and Diagnoses.
	 * @param objectName name of the object to search for
	 * @return NamedObject with given text
	 */
	public static NamedObject findNamedObject(KnowledgeBaseManagement kbm, String objectName) {
		try {
			NamedObject object = kbm.findQContainer(objectName);
			if (object==null) object = kbm.findQuestion(objectName);
			if (object==null) object = kbm.findDiagnosis(objectName);
			return object;
		}
		catch (NullPointerException n) { return null; }
	}
}
