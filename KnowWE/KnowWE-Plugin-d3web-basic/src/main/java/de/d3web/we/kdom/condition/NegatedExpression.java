package de.d3web.we.kdom.condition;

import java.util.Arrays;
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.sectionFinder.OneOfStringEnumFinder;

/**
 * @author Jochen
 * 
 *         Type for a negated element in the CompositeCondition
 * 
 *         example: 'NOT b' here 'b' is not nodes of type NegatedExpression
 * 
 */
public class NegatedExpression extends NonTerminalCondition {

	static String[] NEG_SIGNS = null;

	public NegatedExpression(String[] keys) {
		NEG_SIGNS = Arrays.copyOf(keys, keys.length);

		AnonymousType negationSign = new AnonymousType("NegationSign");
		ConstraintSectionFinder finder = new ConstraintSectionFinder(
				new OneOfStringEnumFinder(NEG_SIGNS),
				AtMostOneFindingConstraint.getInstance());
		negationSign.setSectionFinder(finder);
		this.addChildType(negationSign);

		this.setSectionFinder(new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
				String trimmed = text.trim();
				for (String sign : NEG_SIGNS) {
					if (trimmed.startsWith(sign)) {
						return new AllTextFinderTrimmed().lookForSections(text,
								father, type);
					}
				}
				return null;
			}
		});
	}
}