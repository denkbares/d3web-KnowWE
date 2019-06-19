package de.d3web.we.kdom.condition.helper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.denkbares.strings.Strings;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.NonTerminalCondition;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Finds the embedded function calls with the set of given function names including the name and braces itself.
 * @author Christoph Müller
 * @created 2019-06-19
 */
public class FormulaFunction extends NonTerminalCondition {
	public FormulaFunction(Set<String> names) {
		if (names.isEmpty()) {
			throw new IllegalArgumentException("At least one function name has to passed in the collection.");
		}
		this.setSectionFinder(new FormulaFunctionContent.FormulaFunctionContentFinder(names, true));
	}
}
