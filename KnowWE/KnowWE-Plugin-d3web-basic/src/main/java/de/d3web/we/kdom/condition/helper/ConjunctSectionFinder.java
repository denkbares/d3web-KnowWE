package de.d3web.we.kdom.condition.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;

public class ConjunctSectionFinder implements SectionFinder {

	private final String[] signs;

	public static SectionFinder createConjunctFinder(String[] signs) {
		ConstraintSectionFinder csf = new ConstraintSectionFinder(
				new ConjunctSectionFinder(signs));
		return csf;
	}

	private ConjunctSectionFinder(String[] signs) {
		this.signs = Arrays.copyOf(signs, signs.length);

	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		Map<Integer, Integer> allFoundOps = new HashMap<Integer, Integer>();
		List<SectionFinderResult> results = new ArrayList<SectionFinderResult>();
		for (String symbol : signs) {
			List<Integer> indicesOfUnbraced = SplitUtility.findIndicesOfUnbraced(text,
					symbol,
					CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED);
			// store all found operator sign oc indices and its length
			for (Integer integer : indicesOfUnbraced) {

				// when (binary) operator has index 0, no valid first operand is
				// possible
				// thus not a valid operator
				// in this case skip finding
				if (integer == 0) continue;

				// same if binary is at the end of the expression
				if (integer == text.length() - symbol.length()) continue;

				allFoundOps.put(integer, symbol.length());
			}

		}

		// without any found conj-sings we dont create any conjuncts
		if (allFoundOps.size() == 0) return null;

		Integer[] keys = allFoundOps.keySet().toArray(
				new Integer[allFoundOps.keySet().size()]);
		Arrays.sort(keys);
		int lastBeginIndex = 0;
		// TODO: caution works only for OP signs with same length!! (e.g., not
		// with 'OR' and 'ODER')
		for (Integer integer : keys) {
			results.add(new SectionFinderResult(lastBeginIndex, integer));
			lastBeginIndex = integer + allFoundOps.get(integer);
		}

		results.add(new SectionFinderResult(lastBeginIndex, text.length()));

		return results;
	}

}
