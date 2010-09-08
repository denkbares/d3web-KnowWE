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
package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.d3web.we.wisec.util.Criteria;
import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SourceList;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class SubstanceInfoWriter extends WISECWriter {

	public static final String FILE_PRAEFIX = WISECExcelConverter.FILE_PRAEFIX + "SUB_";
	private static final int MAXLENGTH = 20;
	private static Map<String, String> filenameMap = new HashMap<String, String>();

	public SubstanceInfoWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		for (String substanceName : model.getActiveSubstances()) {
			// for (Substance substance : model.getSubstances()) {
			String filename = getWikiFileNameFor(substanceName);
			Writer writer = ConverterUtils.createWriter(this.outputDirectory + filename + ".txt");
			String substance = findSubstanceWithName(substanceName, model);
			if (substance != null) write(substance, writer);
			writer.close();
		}
	}

	private String findSubstanceWithName(String substanceName, WISECModel model) {
		for (String substance : model.substancesByCAS) {
			if (substance.equalsIgnoreCase(substanceName)) {
				return substance;
			}
		}
		return null;
	}

	protected void writeBreadcrumb(Writer writer, String substance) throws IOException {
		super.writeBreadcrumb(writer);
		writer.write(" > [List of Substances|" + AllSubstancesOverviewWriter.FILENAME + "] > "
				+ "[Active Substances|" + ActiveSubstancesWriter.FILENAME + "] > " + substance
				+ "\n\n");
	}

	private void write(String substance, Writer writer) throws IOException {
		writeBreadcrumb(writer, substance);
		StringBuffer b = new StringBuffer();
		b.append("!!! Substance: " + substance + "\n\n");
		// Kopfbereich zur Identifizierung
		writeCASReferences(substance, b);

		// Write groups that include this substance
		writeAdjacentGroups(substance, b);

		// Write all lists that include this substance
		writeAdjacentLists(substance, b);

		writeCriteriaScoring(substance, b);

		writer.append(b.toString());
	}

	private void writeAdjacentLists(String substance, StringBuffer b) {
		Collection<SubstanceList> lists = this.model.getSubstanceListsContaining(substance);
		List<String> usedCriteria = criteriaContainedInAtLeastOneList(lists);

		b.append("!! On Lists\n\n");
		// write header of table
		b.append("%%zebra-table\n%%sortable\n");
		// write all header names
		for (String header : SubstanceListsOverviewWriter.WRITEABLE_ATTR) {
			b.append("|| " + header + " ");
		}
		for (String criteria : usedCriteria) {
			b.append("|| " + criteria + " ");
		}
		b.append("\n");

		// write the content of the table
		for (SubstanceList substanceList : lists) {
			SourceList sourceList = model.getSourceListForID(substanceList.info.get("Source_ID"));
			String row = SubstanceListsOverviewWriter.getInfoRowForSubstanceList(substanceList,
					sourceList);
			b.append(row);
			for (String criteria : usedCriteria) {
				String criteriaValue = substanceList.criteria.get(criteria);
				if (criteriaValue == null) criteriaValue = "";
				b.append("| " + criteriaValue + " ");
			}
			b.append("\n");
		}
		b.append("/%\n/%\n");
	}

	public static List<String> criteriaContainedInAtLeastOneList(Collection<SubstanceList> lists) {
		List<String> criteria = new ArrayList<String>();
		for (String crit : SubstanceListsOverviewWriter.CRITERIA_ATTRIBUTES) {
			if (isInOneList(crit, lists)) {
				criteria.add(crit);
			}
		}
		return criteria;
	}

	private static boolean isInOneList(String crit, Collection<SubstanceList> lists) {
		for (SubstanceList substanceList : lists) {
			String value = substanceList.criteria.get(crit);
			if (value != null && !value.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private void writeAdjacentGroups(String substance, StringBuffer b) {
		List<String> includedGroups = new ArrayList<String>();
		for (String group : this.model.groups.keySet()) {
			List<String> substances = this.model.groups.get(group).getSubstances();
			if (substances.contains(substance)) {
				includedGroups.add(group);
			}
		}
		if (!includedGroups.isEmpty()) {
			b.append("* Groups: " + ConverterUtils.asStringNoBraces(includedGroups) + "\n\n");
		}

	}

	private void writeCriteriaScoring(String substance, StringBuffer b) {

		double totalScore = 0;
		DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(
				new Locale("en", "US")));
		Collection<SubstanceList> lists = this.model.getSubstanceListsContaining(substance);

		b.append("!! Criteria Scoring\n\n");
		for (String criteriaGroup : Criteria.CRITERIAS.keySet()) {

			b.append("!" + criteriaGroup + "\n");
			b.append("%%zebra-table\n%%sortable\n");
			b.append("|| Criteria || Lists || Scoring || On Lists\n");

			for (String criteria : Criteria.CRITERIAS.get(criteriaGroup)) {
				int count = 0;
				double sum = 0;
				b.append("| " + criteria + " | ");
				for (SubstanceList list : lists) {
					String value = list.criteria.get(criteria);
					if (value != null && !value.isEmpty()) {
						count++;
						sum += Double.valueOf(value);
						b.append(" [" + value + "|"
								+ SubstanceListWriter.getWikiFileNameFor(list.getId()) + "]");
					}
				}
				if (count > 0) {
					double score = sum / count;
					totalScore += score;
					b.append("| " + ConverterUtils.colorizeText(score));
				}
				else {
					b.append("| 0 ");
				}
				// number of lists
				b.append("| " + count);
				b.append("\n");
			}
			b.append("/%\n/%\n");

		}

		if (totalScore != 0) {
			b.append("\n!Total score: " + df.format(totalScore));
		}

	}

	private void writeCASReferences(String substance, StringBuffer b) {
		b.append("* Chemical names: ");
		if (model.getChemNamesFor(substance).size() > 1) {
			b.append("\n" + ConverterUtils.asBulletList(model.getChemNamesFor(substance), 2)
					+ " \n");
		}
		else {
			b.append(model.getChemNamesFor(substance).toArray()[0] + "\n");
		}
		b.append("* EC_No: ");
		if (model.getECNamesFor(substance).isEmpty()) {
			b.append(" - \n");
		}
		else {
			b.append(ConverterUtils.asString(model.getECNamesFor(substance)) + " \n");
		}

		b.append("* IUPAC:  ");
		if (model.getIUPACFor(substance).isEmpty()) {
			b.append(" - \n");
		}
		else {
			b.append(ConverterUtils.asString(model.getIUPACFor(substance)) + " \n");
		}

		b.append("\n");
	}

	public static String getWikiFileNameFor(String name) {
		String realName = name;
		if (name.length() > MAXLENGTH) {
			String choppedName = name.substring(0, MAXLENGTH);
			realName = filenameMap.get(name);
			if (realName == null) {
				realName = ConverterUtils.cleanForFilename(choppedName);
				filenameMap.put(name, realName);
			}
		}
		return FILE_PRAEFIX + ConverterUtils.cleanForFilename(realName);
	}

	public static String asWikiMarkup(String substanceName) {
		return "[ " + ConverterUtils.clean(substanceName) + " | "
				+ SubstanceInfoWriter.getWikiFileNameFor(substanceName) + "]";
	}

}
