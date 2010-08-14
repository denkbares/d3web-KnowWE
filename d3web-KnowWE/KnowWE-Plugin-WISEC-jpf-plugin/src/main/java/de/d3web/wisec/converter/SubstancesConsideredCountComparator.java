package de.d3web.wisec.converter;

import java.util.Comparator;
import java.util.Map;

/**
 * Sorts according to the specified count of considered substances in a list.
 * 
 * @author joba
 * 
 */
public class SubstancesConsideredCountComparator implements
		Comparator<String> {

	private Map<String, Integer> substanceListConsideredSubstances;

	public SubstancesConsideredCountComparator(
			Map<String, Integer> substanceListConsideredSubstances) {
		this.substanceListConsideredSubstances = substanceListConsideredSubstances;
	}

	@Override
	public int compare(String listname1, String listname2) {
		int count1 = substanceListConsideredSubstances.get(listname1);
		int count2 = substanceListConsideredSubstances.get(listname2);
		return count2 - count1;
	}

}
