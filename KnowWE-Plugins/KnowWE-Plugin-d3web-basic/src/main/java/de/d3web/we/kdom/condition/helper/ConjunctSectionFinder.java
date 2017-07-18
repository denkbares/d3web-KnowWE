package de.d3web.we.kdom.condition.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class ConjunctSectionFinder implements SectionFinder {

	public static final String KEYWORD_SEPARATE_CHARS = "()\"! \t\n\r\u00A0";
	private static final int FLAGS = Strings.UNQUOTED | Strings.UNBRACED | Strings.SKIP_COMMENTS;

	private final String[] signs;

	public static SectionFinder createConjunctFinder(String[] signs) {
		return new ConjunctSectionFinder(signs);
	}

	private ConjunctSectionFinder(String[] signs) {
		this.signs = Arrays.copyOf(signs, signs.length);

	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		Map<Integer, Integer> allFoundOps = new HashMap<>();
		for (String symbol : signs) {
			// separation chars only required if the operator itself contains
			// any word-chars
			boolean isWord = hasWordChars(symbol);

			// store all found operator sign oc indices and its length
			for (int index = Strings.indexOf(text, FLAGS, symbol);
				 index >= 0;
				 index = Strings.indexOf(text, index + symbol.length(), FLAGS, symbol)) {


				// if (binary) operator has index 0,
				// no valid first operand is possible
				// thus not a valid operator
				// in this case skip finding
				if (index == 0) continue;

				// same if binary is at the end of the expression
				if (index == text.length() - symbol.length()) continue;

				// word operators must be separated from question names
				// to avoid bug 511 (greedy operators in question names)
				if (isWord) {
					// skip if operator is part of a word (e.q. question name)
					if (!isSeparatorChar(text.charAt(index - 1))) continue;
					if (!isSeparatorChar(text.charAt(index + symbol.length()))) continue;
				}

				allFoundOps.put(index, symbol.length());
			}

		}

		// without any found conj-sings we do not create any conjuncts
		if (allFoundOps.isEmpty()) return null;

		Integer[] keys = allFoundOps.keySet().toArray(new Integer[0]);
		Arrays.sort(keys);
		List<SectionFinderResult> results = new ArrayList<>();
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
