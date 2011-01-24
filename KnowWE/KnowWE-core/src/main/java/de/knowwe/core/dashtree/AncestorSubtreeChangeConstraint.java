package de.knowwe.core.dashtree;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.subtreeHandler.ConstraintModule;


public class AncestorSubtreeChangeConstraint<T extends KnowWEObjectType> extends ConstraintModule<T> {

	private int dashLvl = 0;

	public AncestorSubtreeChangeConstraint(int dashLvl) {
		this(dashLvl, null, null);
	}

	public AncestorSubtreeChangeConstraint(int dashLvl, Operator o, Purpose p) {
		super(o, p);
		this.dashLvl = dashLvl;
	}

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<T> s) {
		return DashTreeUtils.isChangeInAncestorSubtree(article, s, dashLvl);
	}

}
