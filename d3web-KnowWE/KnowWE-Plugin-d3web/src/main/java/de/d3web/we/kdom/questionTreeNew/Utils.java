package de.d3web.we.kdom.questionTreeNew;

import de.d3web.core.inference.condition.AbstractCondition;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.terminology.Answer;
import de.d3web.core.terminology.Question;
import de.d3web.core.terminology.QuestionChoice;
import de.d3web.core.terminology.QuestionNum;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.FindingToConditionBuilder;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionID;
import de.d3web.we.kdom.objects.QuestionTreeAnswerID;

public class Utils {

	public static AbstractCondition createCondition(
			Section<DashTreeElement> element) {

		// get dashTree-father
		Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
				.getDashTreeFather(element);

		Section<QuestionTreeAnswerID> answerSec = dashTreeFather
				.findSuccessor(new QuestionTreeAnswerID());
		// get question-element
		Section<? extends DashTreeElement> qelement = DashTreeElement
				.getDashTreeFather(dashTreeFather);

		Section<QuestionID> qSec = qelement.findSuccessor(new QuestionID());

		Question q = qSec.get().getObject(qSec);

		if (answerSec != null && q instanceof QuestionChoice) {

			Answer a = answerSec.get().getObject(answerSec);

			

			CondEqual c = new CondEqual((QuestionChoice) q, a);

			return c;
		}
		
		Section<NumericCondLine> numCondSec = dashTreeFather
		.findSuccessor(new NumericCondLine());

		if(numCondSec != null && q instanceof QuestionNum) {
			Double d = NumericCondLine.getValue(numCondSec);
			String comp = NumericCondLine.getComparator(numCondSec);
			AbstractCondition condNum = FindingToConditionBuilder.createCondNum(element.getArticle(), numCondSec, comp, d, (QuestionNum)q);
			return condNum;
		}
		return null;
	}

}
