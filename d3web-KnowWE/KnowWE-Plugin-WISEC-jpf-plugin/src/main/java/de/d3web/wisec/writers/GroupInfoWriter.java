/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SourceList;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class GroupInfoWriter extends WISECWriter {

	private static final String FILENAME_PRAEFIX = WISECExcelConverter.FILE_PRAEFIX + "GR_";

	public GroupInfoWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		for (String groupName : this.model.groups.keySet()) {
			String filename = getWikiFileNameFor(groupName);
			Writer writer = ConverterUtils.createWriter(this.outputDirectory + filename + ".txt");

			writeBreadcrumb(writer, groupName);
			writeGroupInfoPage(writer, groupName);

			writer.close();
		}

	}

	private void writeGroupInfoPage(Writer writer, String groupName) throws IOException {
		StringBuffer buffy = new StringBuffer();
		buffy.append("!!! Group: " + groupName + "\n\n");

		writeIDBlock(buffy, groupName);

		writeAdjacentLists(buffy, groupName);

		writeCriteriaScoring(buffy, groupName);

		writer.write(buffy.toString());
	}

	private void writeCriteriaScoring(StringBuffer b, String groupName) {
		// private void writeCriteriaScoring(String substance, StringBuffer b)
		// String[] criteriaValues = new String[] {
		// "3", "2", "1", "0", "-1", "-2", "-3" };

		b.append("!! Criteria Scoring\n\n");

		DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(
				new Locale("en", "US")));
		Collection<SubstanceList> lists = getAllListWithOneCasOf(groupName);

		List<String> usedCriteria = SubstanceInfoWriter.criteriaContainedInAtLeastOneList(lists);
		double totalScore = 0;
		b.append("|| Criteria || Lists || Scoring\n");
		for (String criteria : usedCriteria) {
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
				b.append("| " + df.format(score));
			}
			else {
				b.append("| 0 ");
			}
			b.append("\n");
		}
		if (totalScore > 0) {
			b.append("Total score: " + df.format(totalScore));
		}

	}

	private void writeAdjacentLists(StringBuffer b, String groupName) {
		// determine all lists that contain at least one cas of the group
		List<SubstanceList> lists = getAllListWithOneCasOf(groupName);
		Collections.sort(lists, new Comparator<SubstanceList>() {

			@Override
			public int compare(SubstanceList o1, SubstanceList o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});

		List<String> usedCriteria = SubstanceInfoWriter.criteriaContainedInAtLeastOneList(lists);

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

	private List<SubstanceList> getAllListWithOneCasOf(String groupName) {
		Collection<SubstanceList> lists = new HashSet<SubstanceList>();
		for (String cas : this.model.groups.get(groupName)) {
			if (this.model.getSubstanceListsContaining(cas) != null) {
				lists.addAll(this.model.getSubstanceListsContaining(cas));
			}
		}
		return new ArrayList<SubstanceList>(lists);
	}

	private void writeIDBlock(StringBuffer b, String groupName) {
		Collection<String> cas_nos = this.model.groups.get(groupName);

		// CAS_no
		b.append("* CAS_no: \n");
		for (String cas : cas_nos) {
			b.append("** " + SubstanceInfoWriter.asWikiMarkup(cas) + "\n");
		}
		b.append("\n");

		// CHEMICAL NAMES
		Collection<String> chemNames = getChemNamesFor(cas_nos);
		b.append("* Chemical names: ");
		if (chemNames.size() == 0) {
			b.append(" -");
		}
		else if (chemNames.size() > 1) {
			b.append("\n" + ConverterUtils.asBulletList(chemNames, 2)
					+ " \n");
		}
		else {
			b.append(chemNames.toArray()[0] + "\n");
		}

		// EC_nos
		Collection<String> ecnos = getECNosFor(cas_nos);
		b.append("* EC_No: ");
		if (ecnos.isEmpty()) {
			b.append(" - \n");
		}
		else {
			b.append(ConverterUtils.asStringNoBraces(ecnos) + " \n");
		}

		// IUPAC names
		Collection<String> iupas = getIUPACsFor(cas_nos);
		b.append("* IUPAC:  ");
		if (iupas.isEmpty()) {
			b.append(" - \n");
		}
		else {
			b.append(ConverterUtils.asStringNoBraces(iupas) + " \n");
		}
		b.append("\n");
	}

	private Collection<String> getIUPACsFor(Collection<String> cas_nos) {
		Collection<String> iupacs = new HashSet<String>();
		for (String cas : cas_nos) {
			Collection<String> iupac = this.model.getIUPACFor(cas);
			if (iupac != null) {
				iupacs.addAll(iupac);
			}
		}
		return iupacs;
	}

	private Collection<String> getECNosFor(Collection<String> cas_nos) {
		Collection<String> ecnos = new HashSet<String>();
		for (String cas : cas_nos) {
			Collection<String> ec = this.model.getECNamesFor(cas);
			if (ec != null) {
				ecnos.addAll(ec);
			}
		}
		return ecnos;
	}

	private Collection<String> getChemNamesFor(Collection<String> cas_nos) {
		Collection<String> chems = new HashSet<String>();
		for (String cas : cas_nos) {
			Collection<String> chem = this.model.getChemNamesFor(cas);
			if (chem != null) {
				chems.addAll(chem);
			}
		}
		return chems;
	}

	public static String getWikiFileNameFor(String groupName) {
		return FILENAME_PRAEFIX + ConverterUtils.cleanForFilename(groupName);
	}

	protected void writeBreadcrumb(Writer writer, String groupName) throws IOException {
		super.writeBreadcrumb(writer);
		writer.write(" > [List of Substances|" + AllSubstancesWriter.FILENAME + "] > "
				+ "[Active Substances|" + ActiveSubstancesWriter.FILENAME + "] > " + groupName
				+ "\n\n");
	}

}
