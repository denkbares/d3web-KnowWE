package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.kdom.AnonymousType;

public class CellContent extends AbstractType {

	public CellContent() {

		this.setSectionFinder(AllTextSectionFinder.getInstance());
		this.addSubtreeHandler(new CellTypeHandler());

		AnonymousType anonymousType = new AnonymousType("CellContent");
		anonymousType.setSectionFinder(AllTextSectionFinder.getInstance());
		this.addChildType(anonymousType);

	}
}
