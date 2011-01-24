package de.d3web.we.kdom.subtreeHandler;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public class SuccessorNotReusedConstraint<T extends KnowWEObjectType> extends ConstraintModule<T> {

	public SuccessorNotReusedConstraint() {
	}

	public SuccessorNotReusedConstraint(Operator o, Purpose p) {
		super(o, p);
	}

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<T> s) {
		return s.isOrHasSuccessorNotReusedBy(article.getTitle());
	}

}
