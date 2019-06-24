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
 * Represents a unary function with one of the given names.
 * @author Christoph Müller
 * @created 2019-06-19
 */
public class FormulaFunctionContent extends NonTerminalCondition {
	public FormulaFunctionContent(Set<String> names) {
		if (names.isEmpty()) {
			throw new IllegalArgumentException("At least one function name has to passed in the collection.");
		}
		this.setSectionFinder(new FormulaFunctionContentFinder(names, false));
	}

	static class FormulaFunctionContentFinder implements SectionFinder {
		private final Set<String> names;
		private final boolean includeSurroundings;
		public FormulaFunctionContentFinder(Set<String> names, boolean includeSurroundings) {
			this.names = names;
			this.includeSurroundings = includeSurroundings;
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			String trimmed = text.trim();
			String regexp = "^(" + this.names.stream().collect(Collectors.joining("|")) + ")\\s*\\" + CompositeCondition.BRACE_OPEN + ".*";
			if (trimmed.matches(regexp)) {
				int leadingCharacters = text.indexOf(CompositeCondition.BRACE_OPEN);
				int leadingCharactersTrimmed = trimmed.indexOf(CompositeCondition.BRACE_OPEN);
				int closingBracket = Strings.indexOfClosingBracket(trimmed, leadingCharactersTrimmed,
						CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED);

				if (closingBracket != trimmed.length() - 1) {
					return null;
				}

				if (includeSurroundings) {
					return SectionFinderResult.singleItemList(new SectionFinderResult(
							0, closingBracket + 1));
				} else {
					return SectionFinderResult.singleItemList(new SectionFinderResult(
							leadingCharacters + 1, closingBracket));
				}
			}

			return null;
		}

	}
}
