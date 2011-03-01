package de.d3web.we.kdom.condition.helper;

import java.util.List;

import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.NonTerminalCondition;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;

/**
 * @author Jochen
 * 
 *         Content of an EmbracedCondition (without the brackets)
 * @see BracedCondition
 * 
 */
public class BracedConditionContent extends NonTerminalCondition {

	@Override
	protected void init() {
		this.sectionFinder = new BracedConditionContentFinder();
	}

	class BracedConditionContentFinder implements ISectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, Type type) {
			String trimmed = text.trim();
			int leadingSpaces = text.indexOf(trimmed);
			if (trimmed.startsWith(Character.toString(CompositeCondition.BRACE_OPEN))) {
				int closingBracket = SplitUtility.findIndexOfClosingBracket(trimmed, 0,
						CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED);

				return SectionFinderResult.createSingleItemList(new SectionFinderResult(
						leadingSpaces + 1, closingBracket));

			}
			return null;
		}

	}
}
