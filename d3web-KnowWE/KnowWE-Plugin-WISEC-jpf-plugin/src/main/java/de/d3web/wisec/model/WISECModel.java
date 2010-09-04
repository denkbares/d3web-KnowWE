/*
 * Copyright (C) 2010 denkbares GmbH
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.wisec.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	Map<String, String> substanceRatings;

	// new model from here

	// the CAS no. of all known substances
	public Collection<String> substancesByCAS = new HashSet<String>();
	// the names of all active substances (subset of substances)
	public Collection<String> activeSubstances = new HashSet<String>();
	// all imported source lists
	public Collection<SourceList> sourceLists = new HashSet<SourceList>();
	// all imported substance lists
	public Collection<SubstanceList> substanceLists = new HashSet<SubstanceList>();
	// a map to store, in which substance lists the particular substances are
	// included
	Map<String, Collection<SubstanceList>> substanceInList = new HashMap<String, Collection<SubstanceList>>();

	// relation: group name -> List of substance ids stored in this group
	public Map<String, Group> groups = new HashMap<String, Group>();

	// EC_no for the given CAS
	Map<String, Collection<String>> CAS2EC;
	// Chemical_name for the given CAS
	Map<String, Collection<String>> CAS2ChemNames;
	// CAS2IUPAC_name for the given CAS
	Map<String, Collection<String>> CAS2IUPACname;

	public Map<String, List<String>> teamSubstances = new HashMap<String, List<String>>();
	public Map<String, List<String>> teamGroups = new HashMap<String, List<String>>();

	public WISECModel() {
		substanceRatings = new LinkedHashMap<String, String>();
		CAS2EC = new HashMap<String, Collection<String>>();
		CAS2ChemNames = new HashMap<String, Collection<String>>();
		CAS2IUPACname = new HashMap<String, Collection<String>>();
		activeSubstances = new HashSet<String>();
		groups = new HashMap<String, Group>();
	}

	public void addToGroup(String groupName, String substanceID) {
		Group group = groups.get(groupName);
		if (group == null) {
			group = new Group(groupName);
			groups.put(groupName, group);
		}
		group.addSubstance(substanceID);
	}

	public void addSubstanceToTeam(String teamName, String substance) {
		List<String> tsubstances = teamSubstances.get(teamName);
		if (tsubstances == null) {
			tsubstances = new ArrayList<String>();
		}
		if (!tsubstances.contains(substance)) {
			tsubstances.add(substance);
			teamSubstances.put(teamName, tsubstances);
		}
	}

	public void addGroupToTeam(String teamName, String group) {
		List<String> tgroups = teamGroups.get(teamName);
		if (tgroups == null) {
			tgroups = new ArrayList<String>();
		}
		if (!tgroups.contains(group)) {
			tgroups.add(group);
			teamGroups.put(teamName, tgroups);
		}
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
		this.substanceLists.add(substanceList);
		updateSubstanceOccurences(substanceList);
	}

	private void updateSubstanceOccurences(SubstanceList substanceList) {
		for (Substance substance : substanceList.substances) {
			String casNo = substance.getName();
			substancesByCAS.add(casNo);
			Collection<SubstanceList> lists = substanceInList.get(casNo);
			if (lists == null) {
				lists = new HashSet<SubstanceList>();
			}
			lists.add(substanceList);
			substanceInList.put(casNo, lists);
			update("EC_No", CAS2EC, substance);
			update("Chemical_name", CAS2ChemNames, substance);
			update("IUPAC_name", CAS2IUPACname, substance);
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

	public Collection<SubstanceList> getSubstanceLists() {
		return this.substanceLists;
	}

	public void add(SourceList sourceList) {
		this.sourceLists.add(sourceList);
		// TODO update relation with substances
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

	public Collection<SubstanceList> getSubstanceListsContaining(String substancename) {
		Collection<SubstanceList> lists = substanceInList.get(substancename);
		if (lists == null) {
			return Collections.emptySet();
		}
		else {
			return lists;
		}
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

	public SubstanceList getSubstanceListWithID(String listID) {
		for (SubstanceList list : this.substanceLists) {
			if (list.getId().equals(listID)) {
				return list;
			}
		}
		return null;
	}

	public Collection<String> getListsWithSource(String name) {
		Collection<String> lists = new HashSet<String>();
		for (SubstanceList list : this.substanceLists) {
			if (list.info.get("Source_ID").equalsIgnoreCase(name)) {
				lists.add(list.getName());
			}
		}
		return lists;
	}

	public SubstanceList getListWithName(String listname) {
		for (SubstanceList list : this.substanceLists) {
			if (list.getName().equals(listname)) {
				return list;
			}
		}
		return null;
	}

	public String getSourceListNameForID(String sourceID) {
		for (SourceList sourceList : this.sourceLists) {
			if (sourceList.getId().equals(sourceID)) {
				return sourceList.getName();
			}
		}
		return "NO_ID";
	}

	public SourceList getSourceListForID(String sourceID) {
		for (SourceList sourceList : this.sourceLists) {
			if (sourceList.getId().equals(sourceID)) {
				return sourceList;
			}
		}
		return null;
	}

	/**
	 * 
	 * @created 06.08.2010
	 * @return
	 */
	public Collection<String> getAllTeamNames() {
		Set<String> teams = new HashSet<String>();
		teams.addAll(this.teamGroups.keySet());
		teams.addAll(this.teamSubstances.keySet());
		return teams;
	}

}
