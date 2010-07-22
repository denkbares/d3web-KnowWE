package de.d3web.wisec.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.d3web.wisec.writers.SubstanceRatingListWriter;

/**
 * The {@link WISECModel} stores information about the
 * <ol>
 * <li>the upper substance lists
 * <li>the substance lists
 * <li>the substances
 * </ol>
 * 
 * @author joba
 * 
 */
public class WISECModel {

	Map<String, UpperList> upperLists;
	Map<String, SubstanceList> substanceLists;
	Map<String, Substance> substances;
	Map<String, Integer> usesInList;
	Map<String, String> substanceRatings;

	// EC_no for the given CAS
	Map<String, Collection<String>> CAS2EC;
	// Chemical_name for the given CAS
	Map<String, Collection<String>> CAS2ChemNames;
	// CAS2IUPAC_name for the given CAS
	Map<String, Collection<String>> CAS2IUPACname;

	public int SUBSTANCE_OCCURRENCE_THRESHOLD = 2;

	// the CAS names of substances that are initially active
	public Collection<String> activeSubstances;

	public WISECModel() {
		upperLists = new LinkedHashMap<String, UpperList>();
		substanceLists = new LinkedHashMap<String, SubstanceList>();
		substances = new HashMap<String, Substance>();
		usesInList = new HashMap<String, Integer>();
		substanceRatings = new LinkedHashMap<String, String>();
		CAS2EC = new HashMap<String, Collection<String>>();
		CAS2ChemNames = new HashMap<String, Collection<String>>();
		CAS2IUPACname = new HashMap<String, Collection<String>>();
		activeSubstances = new HashSet<String>();
	}

	public Collection<String> getECNamesFor(String substanceName) {
		if (CAS2EC.get(substanceName) != null) {
			return CAS2EC.get(substanceName);
		}
		else {
			return Collections.emptySet();
		}
	}

	public Collection<String> getIUPACFor(String name) {
		if (CAS2IUPACname.get(name) != null) {
			return CAS2IUPACname.get(name);
		}
		return Collections.emptySet();
	}

	public Collection<String> getChemNamesFor(String name) {
		if (CAS2ChemNames.get(name) != null) {
			return CAS2ChemNames.get(name);
		}
		return Collections.emptySet();
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
				usesInList.put(substance.getName(), uses);
				// update references in substance
				Substance storedSubstance = substances.get(substance.getName());
				if (storedSubstance == null) {
					storedSubstance = substance;
				}
				storedSubstance.usesInLists.add(substanceList);
				substances.put(storedSubstance.getName(), storedSubstance);
				update("EC_No", CAS2EC, substance);
				update("Chemical_name", CAS2ChemNames, substance);
				update("IUPAC_name", CAS2IUPACname, substance);
			}
		}

	}

	private void update(String idName, Map<String, Collection<String>> infoMap, Substance substance) {
		String theName = substance.get(idName).trim().replaceAll("\\n", " ");
		if (theName != null && !theName.isEmpty()) {
			Collection<String> names = infoMap.get(substance.getName());
			if (names == null) {
				names = new HashSet<String>();
			}
			if (!names.contains(theName)) {
				names.add(theName);
				infoMap.put(substance.getName(), names);
			}
		}
	}

	public Integer usesInLists(Substance substance) {
		Integer uses = usesInList.get(substance.getName());
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
