package de.d3web.we.kdom.condition.helper;

import java.util.List;

import com.denkbares.strings.Strings;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.NonTerminalCondition;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * @author Jochen
 * 
 *         Content of an EmbracedCondition (without the brackets)
 * @see BracedCondition
 * 
 */
public class BracedConditionContent extends NonTerminalCondition {

	public BracedConditionContent() {
		this.setSectionFinder(new BracedConditionContentFinder());
	}

	static class BracedConditionContentFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			String trimmed = text.trim();
			int leadingSpaces = text.indexOf(trimmed);
			if (trimmed.startsWith(Character.toString(CompositeCondition.BRACE_OPEN))) {
				int closingBracket = Strings.indexOfClosingBracket(trimmed, 0,
						CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED);

				return SectionFinderResult.singleItemList(new SectionFinderResult(
						leadingSpaces + 1, closingBracket));

			}
			return null;
		}

	}
}
