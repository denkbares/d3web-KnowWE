package de.d3web.we.kdom.questionTree;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.subtreeHandler.ConstraintModule;


public class RootQuestionChangeConstraint<T extends KnowWEObjectType> extends ConstraintModule<T> {

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<T> s) {
		return QuestionDashTreeUtils.isChangeInRootQuestionSubtree(article, s);
	}

}
