package de.d3web.we.kdom.questionTreeNew;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.FindingToConditionBuilder;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionDef;
import de.d3web.we.kdom.objects.QuestionTreeAnswerDef;

public class Utils {

	/**
	 * Creates a condition from a List of DashTreeElements. The List is supposed
	 * to start with a Section containing a QuestionTreeAnswerID, followed by a
	 * Section containing a QuestionID. After that it starts again with a
	 * QuestionTreeAnswerID and so forth. If thats not the case, <tt>null</tt>
	 * will be returned. Such a List of DashTreeElements can be created with
	 * <tt>DashTreeElement.getDashTreeAncestors(Section s)</tt>, if <tt>s</tt>
	 * is the child Section of an answer in a valid DashTree.
	 */
	public static Condition createCondition(
			List<Section<? extends DashTreeElement>> ancestors) {

		List<Condition> simpleConds = new ArrayList<Condition>();

		for (int i = 0; i + 2 < ancestors.size(); i += 2) {
			Condition simpleCond = createSimpleCondition(ancestors
					.get(i), ancestors.get(i + 1));
			if (simpleCond != null) {
				simpleConds.add(simpleCond);
			}
		}

		if (simpleConds.isEmpty()) {
			return null;
		} else if (simpleConds.size() == 1) {
			return simpleConds.get(0);
		} else {
			return new CondAnd(simpleConds);
		}

	}

	/**
	 * Creates a condition from the two DashTreeElements father an grandfather.
	 * Father is supposed to contain a QuestionTreeAnswerID, the grandfather a
	 * QuestionID. If thats not the case, <tt>null</tt> will be returned.
	 */
	public static Condition createSimpleCondition(
			Section<? extends DashTreeElement> father,
			Section<? extends DashTreeElement> grandFather) {

		Section<QuestionTreeAnswerDef> answerSec = father
				.findSuccessor(QuestionTreeAnswerDef.class);
		Section<QuestionDef> qSec = grandFather.findSuccessor(QuestionDef.class);

		Question q = qSec.get().getObject(qSec);

		if (answerSec != null && q instanceof QuestionChoice) {
			Answer a = answerSec.get().getObject(answerSec);
			if (a != null) {
				CondEqual c = new CondEqual((QuestionChoice) q, a);
				return c;
			}
		}

		Section<NumericCondLine> numCondSec = father
				.findSuccessor(NumericCondLine.class);

		if (numCondSec != null && q instanceof QuestionNum) {
			Double d = NumericCondLine.getValue(numCondSec);
			String comp = NumericCondLine.getComparator(numCondSec);
			Condition condNum = FindingToConditionBuilder
					.createCondNum(father.getArticle(), numCondSec, comp, d,
							(QuestionNum) q);
			return condNum;
		}

		return null;

	}

}
