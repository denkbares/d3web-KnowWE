package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * Takes the text from the beginning of the match of the given
 * SingleResultFinder until the end of the text (every behind match)
 * 
 * @author Jochen
 * 
 */
public class MatchUntilEndFinder extends SectionFinder {

	private final AbstractSingleResultFinder startFinder;

	public MatchUntilEndFinder(AbstractSingleResultFinder start) {
		this.startFinder = start;
	}


	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {

		SectionFinderResult res = startFinder.lookForSection(text, father,type);
		if (res != null) {
			return SectionFinderResult.createSingleItemResultList(res.getStart(),
					text.length());
		}

		return null;
	}

}
