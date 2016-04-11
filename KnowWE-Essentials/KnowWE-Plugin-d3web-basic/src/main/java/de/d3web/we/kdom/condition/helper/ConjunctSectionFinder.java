package de.d3web.we.kdom.condition.helper;

import de.d3web.strings.Strings;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

import java.util.*;

public class ConjunctSectionFinder implements SectionFinder {

    public static final String KEYWORD_SEPARATE_CHARS = "()\"! \t\n\r\u00A0";

	private final String[] signs;

	public static SectionFinder createConjunctFinder(String[] signs) {
		return new ConjunctSectionFinder(signs);
	}

	private ConjunctSectionFinder(String[] signs) {
		this.signs = Arrays.copyOf(signs, signs.length);

	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		Map<Integer, Integer> allFoundOps = new HashMap<Integer, Integer>();
		List<SectionFinderResult> results = new ArrayList<SectionFinderResult>();
		for (String symbol : signs) {
			// separation chars only required if the operator itself contains
			// any word-chars
			boolean isWord = hasWordChars(symbol);

			List<Integer> indicesOfUnbraced = Strings.indicesOfUnbraced(text,
					symbol,
					CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED);
			// store all found operator sign oc indices and its length
			for (Integer integer : indicesOfUnbraced) {

				// if (binary) operator has index 0,
				// no valid first operand is possible
				// thus not a valid operator
				// in this case skip finding
				if (integer == 0) continue;

				// same if binary is at the end of the expression
				if (integer == text.length() - symbol.length()) continue;

				// word operators must be separated from question names
				// to avoid bug 511 (greedy operators in question names)
				if (isWord) {
					// skip if operator is part of a word (e.q. question name)
					if (!isSeparatorChar(text.charAt(integer - 1))) continue;
					if (!isSeparatorChar(text.charAt(integer + symbol.length()))) continue;
				}

				// if operator is inside quotes, also skip the operator
				if (Strings.isQuoted(text, integer)) continue;

				allFoundOps.put(integer, symbol.length());
			}

		}

        // without any found conj-sings we do not create any conjuncts
        if (allFoundOps.size() == 0) return null;

		Integer[] keys = allFoundOps.keySet().toArray(
				new Integer[allFoundOps.keySet().size()]);
		Arrays.sort(keys);
		int lastBeginIndex = 0;
		for (Integer integer : keys) {
			results.add(new SectionFinderResult(lastBeginIndex, integer));
			lastBeginIndex = integer + allFoundOps.get(integer);
		}

		results.add(new SectionFinderResult(lastBeginIndex, text.length()));

		return results;
	}

	private boolean hasWordChars(String text) {
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (Character.isLetterOrDigit(ch)) return true;
		}
		return false;
	}

	public static boolean isSeparatorChar(char charAt) {
        return KEYWORD_SEPARATE_CHARS.indexOf(charAt) >= 0;
    }
}
