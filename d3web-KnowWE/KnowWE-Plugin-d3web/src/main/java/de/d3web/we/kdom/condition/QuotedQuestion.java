package de.d3web.we.kdom.condition;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.basic.QuotedType;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;

public class QuotedQuestion extends DefaultAbstractKnowWEObjectType {
	
	@Override
	protected void init() {
		this.childrenTypes.add(new QuotedType(new SimpleQuestionType()));
		this.childrenTypes.add(new SimpleQuestionType());
		this.sectionFinder = AllTextFinderTrimmed.getInstance();
	}

}
