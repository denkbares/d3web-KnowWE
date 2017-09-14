package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.sectionFinder.SplitSectionFinderUnquoted;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 14.09.17.
 */
public class CellContent extends AbstractType {

	public CellContent() {
		this.setSectionFinder(new AllTextFinderTrimmed());
		AbstractType value = new CellContentValue();
		value.setSectionFinder(new SplitSectionFinderUnquoted(","));
		this.addChildType(value);
	}
}
