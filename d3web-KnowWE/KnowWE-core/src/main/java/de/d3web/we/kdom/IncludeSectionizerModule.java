package de.d3web.we.kdom;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.include.IncludeSectionFinderResult;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;


public class IncludeSectionizerModule implements SectionizerModule {

	@Override
	@SuppressWarnings("unchecked")
	public Section<?> createSection(KnowWEArticle article, KnowWEObjectType ob, Section<?> father, Section<?> thisSection, String secText, SectionFinderResult result) {
		Section s = null;
		if (result instanceof IncludeSectionFinderResult) {
			s = Section.createTypedSection(
					thisSection.getOriginalText().substring(
							result.getStart(),
							result.getEnd()),
					ob,
					father,
					thisSection.getOffSetFromFatherText()
							+ result.getStart(),
					article,
					result.getId(),
					false,
					((IncludeSectionFinderResult) result).getIncludeAddress(),
					ob);
			KnowWEEnvironment.getInstance().getIncludeManager(
					s.getWeb()).registerInclude(s);

		}
		return s;
	}

}
