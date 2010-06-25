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
public class EmbracedContentFinder extends SectionFinder {

	private final char open;
	private final char close;
	private int chains = -1;

	public EmbracedContentFinder(char open, char close) {
		this.close = close;
		this.open = open;
	}

	public EmbracedContentFinder(char open, char close, int chains) {
		this.close = close;
		this.open = open;
		this.chains = chains;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		int start = SplitUtility.indexOfUnquoted(text, "" + open);
		if (start > -1) {
			int end = SplitUtility.findIndexOfClosingBracket(text, start,
						open, close);

			// if chains restriction uninitialized, take all
			if (chains == -1) {
				return SectionFinderResult.createSingleItemResultList(start,
						end + 1);
			}
			else {
				// else check chain restriction
				String content = text.substring(start,
						end + 1);
				if (SplitUtility.getCharacterChains(content).length == chains) {
					return SectionFinderResult.createSingleItemResultList(start,
							end + 1);
				}
			}

		}
		return null;
	}

}
