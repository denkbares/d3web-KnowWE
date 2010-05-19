package de.d3web.wisec.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.d3web.wisec.writers.SubstanceRatingListWriter;


/**
 * The {@link WISECModel} stores information about the
 * <ol>
 * <li> the upper substance lists
 * <li> the substance lists
 * <li> the substances
 * </ol>
 * @author joba
 *
 */
public class WISECModel {
	Map<String, UpperList> upperLists;
	Map<String, SubstanceList> substanceLists;
	Map<String, Substance> substances;
	Map<Substance, Integer> usesInList;
	Map<String, String> substanceRatings;

	public int SUBSTANCE_OCCURRENCE_THRESHOLD = 2;
	
	
	public WISECModel() {
		upperLists = new LinkedHashMap<String, UpperList>();
		substanceLists = new LinkedHashMap<String, SubstanceList>();
		substances = new HashMap<String,Substance>();
		usesInList = new HashMap<Substance, Integer>();
		substanceRatings = new LinkedHashMap<String, String>();
	}
	
	public void add(SubstanceList substanceList) {
		this.substanceLists.put(substanceList.name, substanceList);
		updateSubstanceOccurences(substanceList);
	}

	private void updateSubstanceOccurences(SubstanceList substanceList) {
		List<String> doublettes = new ArrayList<String>(substanceList.substances.size());
		for (Substance substance : substanceList.substances) {
			if (!doublettes.contains(substance.getName())) {
				doublettes.add(substance.getName());
				// update uses
				Integer uses = usesInLists(substance) + 1;
				usesInList.put(substance, uses);
				// update references in substance
				Substance storedSubstance = substances.get(substance.getName());
				if (storedSubstance == null) {
					storedSubstance = substance;
				}
				storedSubstance.usesInLists.add(substanceList);
				substances.put(storedSubstance.getName(), storedSubstance);
			}
		}
		
	}

	public Integer usesInLists(Substance substance) {
		Integer uses = usesInList.get(substance);
		if (uses == null) {
			return Integer.valueOf(0);
		}
		else { 
			return uses;
		}
	}
	
	public Collection<SubstanceList> getSubstanceLists() {
		return this.substanceLists.values();
	}

	public Collection<Substance> getSubstances() {
		return substances.values();
	}

	public void add(UpperList upperList) {
		this.upperLists.put(upperList.getName(), upperList);
		// TODO update relation with substances
	}

	public Collection<UpperList> getUpperLists() {
		return upperLists.values();
	}
	
	public Map<SubstanceList, String> listsWithCriteria(String substancename, String criteria) {
		Map<SubstanceList, String> result = new HashMap<SubstanceList, String>();
		for (SubstanceList substanceList : getSubstanceListsContaining(substancename)) {
			String value = substanceList.criteria.get(criteria);
			if (value != null && value.length() > 0) {
				result.put(substanceList, value);
			}
		}
		return result;
	}
	
	public List<SubstanceList> getSubstanceListsContaining(String substancename) {
		List<SubstanceList> result = new ArrayList<SubstanceList>();
		for (SubstanceList list : getSubstanceLists()) {
			if (list.hasSubstanceWithName(substancename)) {
				result.add(list);
			}
		}
		return result;
	}

	public List<SubstanceList> listsWithCriteriaHavingValue(String substancename, String criteriaName, String criteriaValue) {
		List<SubstanceList> result = new ArrayList<SubstanceList>();
		for (SubstanceList substanceList : getSubstanceListsContaining(substancename)) {
			String value = substanceList.criteria.get(criteriaName);
			if (value != null && value.equals(criteriaValue)) {
				result.add(substanceList);
			}
		}
		return result;
	}

	public void addRating(String name, String string) {
		substanceRatings.put(name, string);
	}
	
	public Collection<String> generatedRatings() {
		return substanceRatings.keySet();
	}
	
	public String wikiFileNameForRating(String ratingName) {
		return SubstanceRatingListWriter.getFileNameFor(ratingName);
	}

}
