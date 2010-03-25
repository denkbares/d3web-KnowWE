package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.Section;
import de.d3web.we.utils.SplitUtility;

/**
 * This SectionFinder finds the _first_ unquoted occurrence of the 'symbol' in
 * the text and creates a section from it.
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
	public List<SectionFinderResult> lookForSections(String text, Section father) {

		int index = SplitUtility.indexOfUnquoted(text, symbol);
		if (index != -1) {
			return SectionFinderResult.createSingleItemList(new SectionFinderResult(
					index, index + symbol.length()));
		}

		return null;
	}

}
