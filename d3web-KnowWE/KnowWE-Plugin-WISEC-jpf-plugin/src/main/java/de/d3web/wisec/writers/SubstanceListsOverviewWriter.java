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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SourceList;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class SubstanceListsOverviewWriter extends WISECWriter {

	public static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "All+Substance+Lists";

	// TODO RemoveMe
	// public final static String[] LIST_ATTRIBUTES = new String[] {
	// "Source_ID", "ID", "Name", "Criteria_Code", "List_allocation",
	// "Number_of_substances",
	// "CMR", "Persistence", "Bioakumulation_Potential", "Aqua_Tox", "PBT",
	// "vPvB", "EDC",
	// "Multiple_Tox", "LRT", "Climatic_Change", "drinking_water",
	// " surface_water", "sea",
	// "groundwater", "Risk_related", "Exposure", "compartment",
	// "Market_Volume",
	// "Wide_d_use", "Political", "SVHC_regulated", "Regulated",
	// "ecological_concerns" };

	public final static String[] WRITEABLE_ATTR = new String[] {
			"Source_ID", "Source_Name", "ID", "Name", "Author", "Country", "Criteria_code"
	};

	public SubstanceListsOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory + FILENAME + ".txt");
		writeBreadcrumb(writer);
		writeHeader(writer);

		List<SubstanceList> sortedSubstances = new ArrayList<SubstanceList>(
				model.getSubstanceLists());
		Collections.sort(sortedSubstances, new Comparator<SubstanceList>() {

			@Override
			public int compare(SubstanceList o1, SubstanceList o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});

		StringBuffer buffy = new StringBuffer();
		for (SubstanceList list : sortedSubstances) {
			SourceList sourceList = model.getSourceListForID(list.info.get("Source_ID"));
			buffy.append(getInfoRowForSubstanceList(list, sourceList) + "\n");

		}
		writer.append(buffy.toString());

		writeFooter(writer);
		writer.close();

	}

	public static String getInfoRowForSubstanceList(SubstanceList list, SourceList sourceList) {
		StringBuffer buffy = new StringBuffer();
		buffy.append("| " + getWikiedValueForAttribute("Source_ID", list) + " ");
		buffy.append("| [" + sourceList.get("Name") + " | " +
				ConverterUtils.cleanWikiLinkSpaces(SourceListWriter.getWikiFilename(sourceList.getId()))
				+ "] ");
		buffy.append("| " + getWikiedValueForAttribute("ID", list) + " ");
		buffy.append("| " + getWikiedValueForAttribute("Name", list) + " ");
		buffy.append("| " + sourceList.get("Author") + " ");
		buffy.append("| " + sourceList.get("Country") + " ");
		buffy.append("| " + ConverterUtils.clean(sourceList.get("Criteria_code")) + " ");
		return buffy.toString();
	}

	@Override
	protected void writeBreadcrumb(Writer writer) throws IOException {
		super.writeBreadcrumb(writer);
		writer.append(" > Index of Lists\n\n");
	}

	public static String generateOverviewLineFor(SubstanceList list, String[] listAttr) {
		if (listAttr == null) {
			// listAttr = LIST_ATTRIBUTES; TODO: RemoveMe
			System.out.println("listAttr is null!");
			listAttr = new String[0];
		}
		String filename = ConverterUtils.cleanWikiLinkSpaces(SubstanceListWriter.getWikiFileNameFor(list.getId()));
		StringBuffer buffy = new StringBuffer();
		for (String attribute : listAttr) {
			if (attribute.equalsIgnoreCase("Name")) {
				buffy.append("| [" + clean(list.getName()) + " | " + filename + "] "); // Name
				// of
				// the
				// List
			}
			else {
				String value = list.info.get(attribute);
				if (value == null) {
					value = "";
				}
				buffy.append("| " + clean(value) + " ");
			}
		}

		return buffy.toString();
	}

	private static String getWikiedValueForAttribute(String attribute, SubstanceList list) {
		StringBuffer buffy = new StringBuffer();
		if (attribute.equalsIgnoreCase("Name")) {
			buffy.append("[" + clean(list.getName()) + " | "
					+ ConverterUtils.cleanWikiLinkSpaces(SubstanceListWriter.getWikiFileNameFor(list.getId()))
					+ "] ");
		}
		else {
			String value = list.info.get(attribute);
			if (value == null) {
				value = "";
			}
			buffy.append(clean(value) + " ");
		}
		return buffy.toString();
	}

	private static String clean(String string) {
		return ConverterUtils.clean(string);
	}

	private void writeFooter(Writer writer) throws IOException {
		// close the zebra and the sortable table
		writer.append("/%\n/%\n");
	}

	private void writeHeader(Writer writer) throws IOException {
		writer.append("!!! Lists %%(font-size:small;font-weight:normal)- categorization of parts of the initial sources based upon expertise %% \n\n");
		// open the zebra and the sortable table
		writer.append("%%zebra-table\n%%sortable\n");
		// write all header names
		for (String header : WRITEABLE_ATTR) {
			writer.append("|| " + header + " ");
		}
		writer.append("\n");
	}
}
