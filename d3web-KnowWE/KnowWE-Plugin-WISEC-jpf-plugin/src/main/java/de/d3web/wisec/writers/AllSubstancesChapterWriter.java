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
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

/**
 * 
 * @author Sebastian Furth
 * @created 04.09.2010
 */
public class AllSubstancesChapterWriter extends WISECWriter {

	public static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "Substances_";
	public static final String CHAPTERNUMBERS = "123456789";
	public static final String CHAPTERLETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private String currentChapterSymbol = "";

	public AllSubstancesChapterWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {

		List<String> substancesByCas = new ArrayList<String>(model.substancesByCAS);
		Collections.sort(substancesByCas);

		// Chapters by CAS
		List<String> addedSubstances = new ArrayList<String>();
		writeChapters(CHAPTERNUMBERS, substancesByCas, addedSubstances, false);

		// Chapters by Chemical name
		addedSubstances = new ArrayList<String>();
		writeChapters(CHAPTERLETTERS, substancesByCas, addedSubstances, true);
	}

	private void writeChapters(String chapterSymbols, List<String> substances,
			List<String> addedSubstances, boolean byName) throws IOException {

		for (int i = 0; i < chapterSymbols.length(); i++) {
			currentChapterSymbol = chapterSymbols.substring(i, i + 1);
			writeChapter(substances, addedSubstances, byName, false);
		}

		// Write remaining substances
		currentChapterSymbol = byName ? "OtherNames" : "OtherCAS";
		writeChapter(substances, addedSubstances, byName, true);

	}

	private void writeChapter(List<String> substances, List<String> addedSubstances, boolean byName, boolean remainingMode) throws IOException {

		Writer writer = ConverterUtils.createWriter(this.outputDirectory + FILENAME
				+ currentChapterSymbol + ".txt");
		writeBreadcrumb(writer);

		StringBuffer buffy = new StringBuffer();
		buffy.append("%%zebra-table \n%%sortable\n");
		if (byName) {
			buffy.append("|| No. || Chemical_name|| Action || EC_No || IUPAC_name || CAS_no  || Lists \n");
		}
		else {
			buffy.append("|| No. || CAS_no || Action || EC_No || IUPAC_name || Chemical_name || Lists \n");
		}

		long counter = 1;
		// Write substances which doesn't fit in any chapter (special chars)
		if (remainingMode) {
			List<String> remaining = new ArrayList<String>(substances);
			remaining.removeAll(addedSubstances);
			for (String substance : remaining) {
				appendTableLineFor(substance, buffy, counter, byName);
				counter++;
			}
		}
		else { // Write substances in their designated chapter
			for (String substance : substances) {
				// Chapters by CAS
				if (!byName) {
					if (substance.startsWith(currentChapterSymbol)
							|| substance.startsWith(currentChapterSymbol.toLowerCase())) {
						appendTableLineFor(substance, buffy, counter, byName);
						counter++;
						addedSubstances.add(substance);
					}
				}
				// Chapters by Chemical_name
				else {
					if (ConverterUtils.clean(model.getChemNamesFor(substance).toString())
								.startsWith("[[" + currentChapterSymbol)
							|| ConverterUtils.clean(model.getChemNamesFor(substance).toString())
									.startsWith("[[" + currentChapterSymbol.toLowerCase())) {
					appendTableLineFor(substance, buffy, counter, byName);
					counter++;
					addedSubstances.add(substance);
					}
				}
			}
		}
		buffy.append("\n");
		buffy.append("/%\n/%\n");

		writer.write(buffy.toString());
		writer.close();
	}

	private void appendTableLineFor(String substance, StringBuffer buffy, long counter, boolean byName) {

		buffy.append("| " + counter);
		if (byName) {
			buffy.append("| " + ConverterUtils.clean(model.getChemNamesFor(substance).toString())); // Chemical_name
		}
		else {
			buffy.append("| " + substance); // CAS
		}

		if (model.activeSubstances.contains(substance)) {
			buffy.append("| A ");
		}
		else {
			buffy.append("| + ");
		}

		buffy.append("| " + ConverterUtils.clean(model.getECNamesFor(substance).toString()));
		buffy.append("| " + ConverterUtils.clean(model.getIUPACFor(substance).toString()));
		if (byName) {
			buffy.append("| " + substance); // CAS
		}
		else {
			buffy.append("| " + ConverterUtils.clean(model.getChemNamesFor(substance).toString())); // Chemical_name
		}

		buffy.append("| <ul>");
		for (SubstanceList list : model.getSubstanceListsContaining(substance)) {
			buffy.append("<li><a href=\"Wiki.jsp?page=");
			buffy.append(SubstanceListWriter.getWikiFileNameFor(list.getId()));
			buffy.append("\"> ");
			buffy.append(list.getName());
			buffy.append(" </a></li> \\\\");
		}
		buffy.append("</ul>\n");
	}

	@Override
	protected void writeBreadcrumb(Writer writer) throws IOException {
		super.writeBreadcrumb(writer);
		writer.write(" > [Index of Lists|" + SubstanceListsOverviewWriter.FILENAME + "] > "
				+ "[All Substances|" + AllSubstancesOverviewWriter.FILENAME + "] > "
				+ "Substances " + currentChapterSymbol + "\n\n");
	}
}
