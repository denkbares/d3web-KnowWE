package de.knowwe.ontology.turtle;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class TurtleURIList extends AbstractType {

	public TurtleURIList() {
		this.setSectionFinder((text, father, type) -> SectionFinderResult.resultList(
				Strings.splitUnquoted(text, ",", false, TurtleMarkup.TURTLE_QUOTES)));
		this.addChildType(new TurtleURI());
	}
}
