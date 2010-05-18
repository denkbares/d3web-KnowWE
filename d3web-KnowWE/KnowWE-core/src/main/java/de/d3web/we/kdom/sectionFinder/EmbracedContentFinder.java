package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.Section;
import de.d3web.we.utils.SplitUtility;

/**
 * Finds stuff embraced (regarding quoted occurrences of the embracing signs)
 *
 * e.g.;
 * somestuff (content-to-find) other  --> using '(' and ')'
 * "some <stuff>" <content-to-find> other --> using '<' and '>'
 *
 * @author Jochen
 *
 */
public class EmbracedContentFinder  extends SectionFinder{

	private final char open;
	private final char close;

	public EmbracedContentFinder(char open, char close) {
		this.close = close;
		this.open = open;
	}


		@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		int start = SplitUtility.indexOfUnquoted(text, "" + open);
			if (start > -1) {
				int end = SplitUtility.findIndexOfClosingBracket(text, start,
						open, close);
				return SectionFinderResult.createSingleItemResultList(start,
						end + 1);
			}
			return null;
		}


}
