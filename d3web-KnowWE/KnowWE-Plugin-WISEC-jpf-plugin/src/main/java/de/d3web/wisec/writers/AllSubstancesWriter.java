/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;


/**
 * 
 * @author joba
 * @created 05.08.2010 
 */
public class AllSubstancesWriter extends WISECWriter {

	public static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "Substances";

	public AllSubstancesWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory + FILENAME + ".txt");
		writeBreadcrumb(writer);
		List<String> substances = new ArrayList<String>(model.substances);
		Collections.sort(substances);

		StringBuffer buffy = new StringBuffer();
		buffy.append("%%zebra-table \n%%sortable\n");
		buffy.append("|| No. || CAS_no || Action || EC_No || IUPAC_name || Chemical_name || Lists \n");

		long counter = 1;
		for (String substanceName : substances) {
			appendTableLineFor(substanceName, buffy, counter);
			counter++;
		}
		buffy.append("\n");
		buffy.append("/%\n/%\n");

		writer.write(buffy.toString());
		writer.close();
	}

	private void appendTableLineFor(String substanceName, StringBuffer buffy, long counter) {
		buffy.append("| " + counter);
		buffy.append("| " + substanceName); // CAS_no

		if (model.activeSubstances.contains(substanceName)) {
			buffy.append("| A ");
		}
		else {
			buffy.append("| [+ | dummy]");
		}

		buffy.append("| " + ConverterUtils.clean(model.getECNamesFor(substanceName).toString()));
		buffy.append("| " + ConverterUtils.clean(model.getIUPACFor(substanceName).toString()));
		buffy.append("| " + ConverterUtils.clean(model.getChemNamesFor(substanceName).toString()));

		buffy.append("| ");
		for (SubstanceList list : model.getSubstanceListsContaining(substanceName)) {
			buffy.append("[" + list.getName() + "|"
					+ SubstanceListWriter.getWikiFileNameFor(list.getName()) + "]\\");
		}
		buffy.append("\n");
	}

	@Override
	protected void writeBreadcrumb(Writer writer) throws IOException {
		super.writeBreadcrumb(writer);
		writer.write(" > [Index of Lists|" + SubstanceListsOverviewWriter.FILENAME + "] > "
				+ "All Substances\n\n");
	}
}
