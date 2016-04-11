package de.d3web.we.kdom.condition.helper;

import de.d3web.strings.Strings;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.NonTerminalCondition;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;

import java.util.List;

/**
 * @author Jochen
 * 
 *         Any expression enclosed with brackets is a BracedCondition each has a
 *         child of type BracedConditionContent
 * 
 */
public class BracedCondition extends NonTerminalCondition {

	public BracedCondition() {
		this.setSectionFinder(EmbracedExpressionFinder.createEmbracedExpressionFinder());
	}
}

/**
 * 
 * creates EmbracedExpressions if expression starts with a opening bracket and
 * concludes with a closing brackets AND these two correspond to each other
 * 
 * @author Jochen
 * 
 */
class EmbracedExpressionFinder implements SectionFinder {

	public static SectionFinder createEmbracedExpressionFinder() {
		ConstraintSectionFinder sectionFinder = new ConstraintSectionFinder(
				new EmbracedExpressionFinder());
		return sectionFinder;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		String trimmed = text.trim();
		if (trimmed.isEmpty()) return null;
		int leadingSpaces = text.indexOf(trimmed);
		int followingSpaces = text.length() - trimmed.length() - leadingSpaces;
		boolean startsWithOpen = trimmed.startsWith(Character.toString(CompositeCondition.BRACE_OPEN));
		int closingBracket = Strings.indexOfClosingBracket(trimmed, 0,
				CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED);

        // if it does not start with an opening bracket
        if (!startsWithOpen) {
			// its not an embraced expression for sure => return null
			return null;
		}

		// throw error if no corresponding closing bracket can be found
		if (closingBracket == -1) {
			Messages.storeMessage(father, this.getClass(),
					Messages.syntaxError("missing \")\""));
			return null;
		}
		else {
			Messages.clearMessages(father, this.getClass());
		}

		// an embracedExpression needs to to start and end with '(' and ')'
		if (startsWithOpen
				&& trimmed.endsWith(Character.toString(CompositeCondition.BRACE_CLOSED))) {
			// and the ending ')' needs to close the opening
			if (closingBracket == trimmed.length() - 1) {
				return SectionFinderResult.singleItemList(new SectionFinderResult(
						leadingSpaces, text.length() - followingSpaces));
			}

		}

		// OR an embracedExpression can be concluded with a lineEnd-comment
		int lastEndLineCommentSymbol = Strings.lastIndexOfUnquoted(text, "//");
        // so has to start with '(' and have a line-end-comment-sign after
        // the closing bracket but nothing in between!
		if (trimmed.startsWith(Character.toString(CompositeCondition.BRACE_OPEN))) {
			if (lastEndLineCommentSymbol > -1
					&& !CompositeCondition.hasLineBreakAfterComment(trimmed)) {
				// TODO fix: < 3 is inaccurate
				// better check that there is no other expression in between
				if (lastEndLineCommentSymbol - closingBracket < 3) {
					return SectionFinderResult.singleItemList(new SectionFinderResult(
							leadingSpaces, text.length()));
				}
			}

		}

		return null;
	}

}
