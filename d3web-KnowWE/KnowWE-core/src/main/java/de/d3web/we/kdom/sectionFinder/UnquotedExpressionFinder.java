package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.SplitUtility;

/**
 * This SectionFinder finds the _all_ unquoted occurrence of the 'symbol' in the
 * text and creates a section from it.
 * 
 * @author Jochen
 * 
 */
public class UnquotedExpressionFinder extends SectionFinder {

	private final String symbol;

	public UnquotedExpressionFinder(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {

		int index = SplitUtility.indexOfUnquoted(text, symbol);

		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		int counter = 0;
		// in this loop the text is scanned and cropped for occurrences and the
		// results are created

		while (index != -1) {
			result.add(new SectionFinderResult(
					index + counter, index + counter + symbol.length()));

			text = text.substring(index + 1);
			counter += index + 1;
			index = SplitUtility.indexOfUnquoted(text, symbol);
		}

		return result;
	}

}
