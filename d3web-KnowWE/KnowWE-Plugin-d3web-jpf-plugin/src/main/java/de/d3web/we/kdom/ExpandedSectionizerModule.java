package de.d3web.we.kdom;

import de.d3web.we.kdom.sectionFinder.SectionFinderResult;


public class ExpandedSectionizerModule implements SectionizerModule {

	@Override
	public Section<?> createSection(KnowWEArticle article, KnowWEObjectType ob, Section<?> father, Section<?> thisSection, String secText, SectionFinderResult result) {
		if (result instanceof ExpandedSectionFinderResult) {
			return createExpandedSection(
					(ExpandedSectionFinderResult) result, father);

		}
		return null;
	}

	private Section<?> createExpandedSection(ExpandedSectionFinderResult result, Section<?> father) {
		Section<?> s = Section.createTypedSection(result.getText(),
				result.getObjectType(), father,
				result.getStart(), father.getArticle(), null, true, null,
				result.getObjectType());
		if (s.getOffSetFromFatherText() < 0
				|| s.getOffSetFromFatherText() > father.getOriginalText().length()
				|| !father.getOriginalText().substring(s.getOffSetFromFatherText()).startsWith(
						s.getOriginalText())) {
			s.setOffSetFromFatherText(father.getOriginalText().indexOf(
					s.getOriginalText()));
		}

		for (ExpandedSectionFinderResult childResult : result.getChildren()) {
			createExpandedSection(childResult, s);
		}
		return s;
	}

}
