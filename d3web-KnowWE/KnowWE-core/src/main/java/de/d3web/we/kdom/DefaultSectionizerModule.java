package de.d3web.we.kdom;

import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class DefaultSectionizerModule implements SectionizerModule {

	@Override
	public Section<?> createSection(KnowWEArticle article, KnowWEObjectType ob, Section<?> father, Section<?> thisSection, String secText, SectionFinderResult result) {
		return Section.createTypedSection(
				thisSection.getOriginalText().substring(result.getStart(), result.getEnd()),
				ob,
				father,
				thisSection.getOffSetFromFatherText() + result.getStart(),
				article,
				result.getId(),
				false);

	}

}
