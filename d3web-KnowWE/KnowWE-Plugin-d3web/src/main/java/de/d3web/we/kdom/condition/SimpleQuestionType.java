package de.d3web.we.kdom.condition;

import de.d3web.we.kdom.Annotation.FindingQuestion;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;

public class SimpleQuestionType extends FindingQuestion {
	
	
	@Override
	protected void init() {
		setSectionFinder(new AllTextFinderTrimmed());
	}
	

}
