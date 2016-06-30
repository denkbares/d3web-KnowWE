package de.d3web.we.kdom.condition;

import java.util.Arrays;

import de.d3web.we.kdom.condition.helper.ConjunctSectionFinder;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.sectionFinder.OneOfStringFinder;

/**
 * Type for a negated element in the CompositeCondition
 * <p>
 * example: 'NOT b' here 'b' is not nodes of type NegatedExpression
 *
 * @author Jochen
 */
public class NegatedExpression extends NonTerminalCondition {

	private final String[] negationKeywords;

	public NegatedExpression(String[] keys) {
		negationKeywords = Arrays.copyOf(keys, keys.length);

		AnonymousType negationSign = new AnonymousType("NegationSign");
		ConstraintSectionFinder finder = new ConstraintSectionFinder(
				new OneOfStringFinder(negationKeywords),
				AtMostOneFindingConstraint.getInstance());
		negationSign.setSectionFinder(finder);
		this.addChildType(negationSign);

		this.setSectionFinder((text, father, type) -> {
			String trimmed = text.trim();
			for (String sign : negationKeywords) {
				if (trimmed.startsWith(sign)
						&& trimmed.length() > sign.length()
						&& ConjunctSectionFinder.isSeparatorChar(trimmed.charAt(sign.length()))) {
					return new AllTextFinderTrimmed().lookForSections(text, father, type);
				}
			}
			return null;
		});
	}
}