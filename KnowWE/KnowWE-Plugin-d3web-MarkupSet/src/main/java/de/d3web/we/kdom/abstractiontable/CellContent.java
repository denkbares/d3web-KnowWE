package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.kdom.AnonymousType;

public class CellContent extends AbstractType {

	public CellContent() {

		this.setSectionFinder(AllTextFinder.getInstance());
		this.addCompileScript(new CellTypeHandler());

		AnonymousType anonymousType = new AnonymousType("CellContent");
		anonymousType.setSectionFinder(AllTextFinder.getInstance());
		this.addChildType(anonymousType);

	}
}
