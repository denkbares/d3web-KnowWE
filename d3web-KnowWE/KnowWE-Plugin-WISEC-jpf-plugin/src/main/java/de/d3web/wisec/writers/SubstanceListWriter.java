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
import java.util.Arrays;
import java.util.List;

import de.d3web.we.wisec.util.Criteria;
import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SourceList;
import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class SubstanceListWriter extends WISECWriter {

	private static String FILE_PRAEFIX = WISECExcelConverter.FILE_PRAEFIX + "SUL+";
	private boolean withKnowledge;

	public SubstanceListWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		for (SubstanceList list : model.getSubstanceLists()) {
			Writer writer = ConverterUtils.createWriter(this.outputDirectory
					+ getWikiFileNameFor(list.getId())
					+ ".txt");
			write(list, writer);
			writer.close();
		}
	}

	public static String getWikiFileNameFor(String name) {
		return FILE_PRAEFIX + ConverterUtils.cleanForFilename(name);
	}

	private void write(SubstanceList list, Writer writer) throws IOException {
		writeBreadcrumb(writer, list);
		writer.write("!!! " + list.getName() + "\n\n");

		// WRITE THE IDENTIFICATION HEADER
		writeIdentificationHeader(writer, list);

		
		// WRITE THE SUBSTANCE LIST CRITERIA
		writer.write("!! Criteria \n\n");
		String sourceID = null;
		for (String criteriaGroup : Criteria.getCriteriaGroups()) {
			writer.write("!" + criteriaGroup + "\n");
			writeKnowledge(writer, "%%ListCriteria\n");
			for (String criteria : Criteria.getCriteriasFor(criteriaGroup)) {
				String value = list.criteria.get(criteria);
				if (value == null || value.isEmpty()) value = "0";
				else ConverterUtils.clean(value);
				writer.write("|| " + criteria + " | " + value + " \n");
			}
			writer.write("-");
			sourceID = list.info.get("Source_ID");
			if (sourceID != null) {
				writeKnowledge(writer, "\n" +
						"@ListID: " + list.getId() + "\n" +
						"@SourceID: " + sourceID + "\n" +
						"%\n");
			}

			writer.write("\n");
		}
		
		// WRITE SOURCE LINK
		if (sourceID != null) {
			writer.write("\n __Source:__ ");
			writer.write("[ " + model.getSourceListNameForID(sourceID) + "|"
					+ ConverterUtils.cleanWikiLinkSpaces(SourceListWriter.getWikiFilename(sourceID))
					+ "]");
		}
		else {
			writer.write("__Attention:__ No source list known.");
		}

		writer.write("\n\n");

		// WRITE THE LIST OF SUBSTANCES
		writeSubstanceTable(writer, list);
	}

	private void writeIdentificationHeader(Writer writer, SubstanceList list) throws IOException {
		SourceList sourceList = model.getSourceListForID(list.info.get("Source_ID"));
		StringBuffer buffy = new StringBuffer();
		buffy.append("|| Source_ID   | " + getWikiedValueForAttribute("Source_ID", list)
				+ "\n");
		buffy.append("|| Source_Name | [" + sourceList.get("Name") + "|" +
				ConverterUtils.cleanWikiLinkSpaces(SourceListWriter.getWikiFilename(sourceList.getId()))
				+ "]\n");
		buffy.append("|| List_ID     | " + getWikiedValueForAttribute("ID", list) + "\n");
		buffy.append("|| List_Name   | " + getWikiedValueForAttribute("Name", list)
				+ "\n");
		buffy.append("|| Author      | " + sourceList.get("Author") + "\n");
		buffy.append("|| Country     | " + sourceList.get("Country") + "\n");

		buffy.append("|| Criteria_Code | "
				+ getWikiedValueForAttribute("Criteria_Code", list)
				+ "\n");
		buffy.append("|| List_allocation | "
				+ getWikiedValueForAttribute("List_allocation", list)
				+ "\n");
		buffy.append("|| Number_of_substances | "
				+ getWikiedValueForAttribute("Number_of_substances", list) + "\n");

		buffy.append("\n");
		writer.write(buffy.toString());

	}

	private String getWikiedValueForAttribute(String attribute, SubstanceList list) {
		StringBuffer buffy = new StringBuffer();
		if (attribute.equalsIgnoreCase("Name")) {
			buffy.append("[" + ConverterUtils.clean(list.getName()) + "|"
					+ ConverterUtils.cleanWikiLinkSpaces(SubstanceListWriter.getWikiFileNameFor(list.getId()))
					+ "] ");
		}
		else {
			String value = list.info.get(attribute);
			if (value == null) {
				value = "";
			}
			buffy.append(ConverterUtils.clean(value) + " ");
		}
		return buffy.toString();
	}

	private void writeBreadcrumb(Writer writer, SubstanceList list) throws IOException {
		super.writeBreadcrumb(writer);
		writer.write(" > [Index of Lists|"
				+ ConverterUtils.cleanWikiLinkSpaces(SubstanceListsOverviewWriter.FILENAME)
				+ "] > "
				+ list.getName() + "\n\n");
	}

	private void writeKnowledge(Writer writer, String knowledge) throws IOException {
		if (withKnowledge) {
			writer.write(knowledge);
		}
	}

	private void writeSubstanceTable(Writer writer, SubstanceList list) throws IOException {
		writer.write("!! Substances \n\n");
		writeKnowledge(writer, "%%ListSubstances\n");

		printSubstanceTable(writer, list);

		writeKnowledge(writer, "-\n\n");
		writeKnowledge(writer, "@ListID: " + list.getId() + "  \n");
		writeKnowledge(writer, "%\n");

	}

	private void printSubstanceTable(Writer writer, SubstanceList list) throws IOException {
		StringBuffer buffy = new StringBuffer();

		// buffy.append("%%zebra-table \n%%sortable\n");

		List<String> FIRST_HEADERS = Arrays.asList(new String[] {
				"CAS_No", "Action", "EC_No", "IUPAC_name", "Chemical_name" });

		for (String header : FIRST_HEADERS) {
			buffy.append("|| " + header + " ");
		}
		for (String header : list.substanceAttributes) {
			if (!FIRST_HEADERS.contains(header)) {
				buffy.append("|| " + header + " ");
			}
		}

		buffy.append("\n");
		for (Substance substance : list.substances) {
			for (String header : FIRST_HEADERS) {
				if (header.equals("Action")) {
					if (model.getActiveSubstances().contains(substance.getCAS())) {
						buffy.append("| A ");
					}
					else {
						buffy.append("| [+|dummy] ");
					}
				}
				else {
					buffy.append("| ");
					getCellValue(buffy, substance, header);
				}
			}
			for (String header : list.substanceAttributes) {
				if (!FIRST_HEADERS.contains(header)) {
					buffy.append("| ");
					getCellValue(buffy, substance, header);
				}
			}
			buffy.append("\n");
		}
		buffy.append("\n");
		// buffy.append("/%\n/%\n");
		writer.write(buffy.toString());
	}

	private void getCellValue(StringBuffer buffy, Substance substance, String header) {
		String value = ConverterUtils.clean(substance.values.get(header));
		if (header.equals(WISECExcelConverter.SUBSTANCE_IDENTIFIER)) {
			if (model.getActiveSubstances().contains(substance.getCAS())) {
				buffy.append(" [ " + value + "|"
						+ ConverterUtils.cleanWikiLinkSpaces(SubstanceInfoWriter.getWikiFileNameFor(value))
						+ "] ");
			}
			else {
				buffy.append(value + " ");
			}

		}
		else {
			buffy.append(value + " ");
		}
	}

	public void setFilePraefix(String praefix) {
		FILE_PRAEFIX = praefix;
	}

	public boolean isWithKnowledge() {
		return withKnowledge;
	}

	public void setWithKnowledge(boolean withKnowledge) {
		this.withKnowledge = withKnowledge;
	}

	public static String asWikiMarkup(SubstanceList list) {
		return "[ " + list.getName() + "|"
				+ ConverterUtils.cleanWikiLinkSpaces(SubstanceListWriter.getWikiFileNameFor(list.getId()))
				+ "]";
	}

	public static String getCriteriaString(SubstanceList list) {
		String criteriaString = "";
		for (String criteria : list.criteria.keySet()) {
			String value = list.criteria.get(criteria);
			if (value != null && value.length() > 0) {
				criteriaString += criteria + "=" + value + ", ";
			}
		}
		if (criteriaString.length() < 2) {
			return criteriaString;
		}
		return criteriaString.substring(0, criteriaString.length() - 2);
	}

}
