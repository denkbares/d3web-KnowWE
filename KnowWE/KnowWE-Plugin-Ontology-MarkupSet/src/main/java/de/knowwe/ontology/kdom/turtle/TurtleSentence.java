package de.knowwe.ontology.kdom.turtle;

import java.util.ArrayList;
import java.util.List;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.sectionFinder.SplitSectionFinderUnquoted;

public class TurtleSentence extends AbstractType {

	public TurtleSentence() {
		setSectionFinder(new TurtleSentenceFinder());

		this.addChildType(new TurtleSubjectSection());
		this.addChildType(new TurtlePredSentence());
	}

	class TurtleSentenceFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			List<SectionFinderResult> splitResult = new SplitSectionFinderUnquoted(".", new char[] {
					'\'', '"' }).lookForSections(text, father, type);

			for (SectionFinderResult sectionFinderResult : splitResult) {
				String foundText = SectionFinderResult.getFoundText(sectionFinderResult, father);
				if (foundText.endsWith(".")) {
					result.add(new SectionFinderResult(sectionFinderResult.getStart(),
							sectionFinderResult.getEnd() - 1));
				}
				else if (!Strings.isBlank(foundText)) {
					result.add(sectionFinderResult);
				}
			}

			return result;
		}

	}

}
