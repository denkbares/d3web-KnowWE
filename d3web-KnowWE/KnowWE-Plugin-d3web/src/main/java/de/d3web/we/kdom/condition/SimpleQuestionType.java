package de.d3web.we.kdom.condition;

import de.d3web.we.kdom.Annotation.FindingQuestion;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;

public class SimpleQuestionType extends FindingQuestion {
	
	@Override
	public SectionFinder getSectioner() {
		return AllTextFinderTrimmed.getInstance();
	}

}
