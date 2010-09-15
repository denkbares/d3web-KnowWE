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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.WISECModel;

/**
 * 
 * @author joba
 * @created 05.08.2010
 */
public class AllSubstancesOverviewWriter extends WISECWriter {

	public static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "Substances";

	public AllSubstancesOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory + FILENAME + ".txt");
		writeBreadcrumb(writer);
		List<String> substances = new ArrayList<String>(model.substancesByCAS);
		Collections.sort(substances);

		StringBuffer buffy = new StringBuffer();
		buffy.append("!!!List of all substances %%(font-size:small;font-weight:normal)- Collection of all considered substances. For clearer overview divided into parts.%% \n\n");
		writeChapterLinks(AllSubstancesChapterWriter.CHAPTERNUMBERS, "Substances by CAS", buffy);
		writeChapterLinks(AllSubstancesChapterWriter.CHAPTERLETTERS, "Substances by chemical name",
				buffy);

		writer.write(buffy.toString());
		writer.close();
	}

	private void writeChapterLinks(String chapterSymbols, String headline, StringBuffer buffy) {

		// Write headline
		buffy.append("! ");
		buffy.append(headline);
		buffy.append("\n");

		// Write links
		String currentChapterSymbol = "";
		for (int i = 0; i < chapterSymbols.length(); i++) {
			currentChapterSymbol = chapterSymbols.substring(i, i + 1);
			buffy.append("~[ [");
			buffy.append(currentChapterSymbol);
			buffy.append("|");
			buffy.append(ConverterUtils.cleanWikiLinkSpaces(AllSubstancesChapterWriter.FILENAME));
			buffy.append(currentChapterSymbol);
			buffy.append("] ]");
		}

		if (chapterSymbols.startsWith("1")) {
			buffy.append("~[ [Sonstige|");
			buffy.append(ConverterUtils.cleanWikiLinkSpaces(AllSubstancesChapterWriter.FILENAME));
			buffy.append("OtherCAS] ]");
		}
		else {
			buffy.append("~[ [0-9, Sonderzeichen|");
			buffy.append(ConverterUtils.cleanWikiLinkSpaces(AllSubstancesChapterWriter.FILENAME));
			buffy.append("OtherNames] ]");
		}
		buffy.append("\n\n");

	}

	@Override
	protected void writeBreadcrumb(Writer writer) throws IOException {
		super.writeBreadcrumb(writer);
		writer.write(" > [Index of Lists|"
				+ ConverterUtils.cleanWikiLinkSpaces(SubstanceListsOverviewWriter.FILENAME)
				+ "] > "
				+ "All Substances\n\n");
	}
}
