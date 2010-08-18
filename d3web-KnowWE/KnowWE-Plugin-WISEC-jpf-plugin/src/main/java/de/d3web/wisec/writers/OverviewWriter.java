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

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.WISECModel;

public class OverviewWriter extends WISECWriter {

	public static final String FILENANE = WISECExcelConverter.FILE_PRAEFIX + "WISEC.txt";
	private static final String ALL_SUBSTANCES = WISECExcelConverter.FILE_PRAEFIX + "AllSubstances";
	private static final String ALL_SUBSTANCE_LISTS = WISECExcelConverter.FILE_PRAEFIX
			+ "AllSubstanceLists";

	public OverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory + FILENANE);
		writer.write("!!! WISEC Overview\n\n");
		writeGeneralSettings(writer);
		// writeSubstanceListOverview(writer);
		writer.close();

		// writeAllSubstances();
	}

	private void writeGeneralSettings(Writer writer) throws IOException {
		writer.write("* [List of all sources | " + WISECExcelConverter.FILE_PRAEFIX
				+ "All_Sources ] (" + model.sourceLists.size() + " sources)\n");
		writer.write("* [List of all substance lists | " + ALL_SUBSTANCE_LISTS + "] ("
				+ model.substanceLists.size() + " lists)\n");
		writer.write("* [List of all substances | " + ALL_SUBSTANCES + "] ("
				+ model.activeSubstances.size() + " active of " + model.substances.size()
				+ " substances)\n");
		writer.write("\n\n");

	}

}
