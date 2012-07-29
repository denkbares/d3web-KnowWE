package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.kdom.AnonymousType;

public class CellContent extends AbstractType {

	public CellContent() {

		this.setSectionFinder(new AllTextSectionFinder());
		this.addSubtreeHandler(new CellTypeHandler());

		AnonymousType anonymousType = new AnonymousType("CellContent");
		anonymousType.setSectionFinder(new AllTextSectionFinder());
		this.addChildType(anonymousType);

	}

}
