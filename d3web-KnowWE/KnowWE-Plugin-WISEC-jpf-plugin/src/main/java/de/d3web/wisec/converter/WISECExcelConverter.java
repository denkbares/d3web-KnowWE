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
package de.d3web.wisec.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import de.d3web.wisec.model.WISECModel;
import de.d3web.wisec.readers.ActiveSubstancesReader;
import de.d3web.wisec.readers.GroupsReader;
import de.d3web.wisec.readers.ListsReader;
import de.d3web.wisec.readers.SourceListReader;
import de.d3web.wisec.readers.SubstanceListsReader;
import de.d3web.wisec.readers.TeamsReader;
import de.d3web.wisec.readers.WISECReader;
import de.d3web.wisec.scoring.ScoringWeightsConfiguration;
import de.d3web.wisec.writers.ActiveSubstancesWriter;
import de.d3web.wisec.writers.AllSubstancesChapterWriter;
import de.d3web.wisec.writers.AllSubstancesOverviewWriter;
import de.d3web.wisec.writers.GroupInfoWriter;
import de.d3web.wisec.writers.GroupsWriter;
import de.d3web.wisec.writers.OverviewWriter;
import de.d3web.wisec.writers.SourceListOverviewWriter;
import de.d3web.wisec.writers.SourceListWriter;
import de.d3web.wisec.writers.SubstanceInfoWriter;
import de.d3web.wisec.writers.SubstanceListWriter;
import de.d3web.wisec.writers.SubstanceListsOverviewWriter;
import de.d3web.wisec.writers.SubstanceRatingListWriter;
import de.d3web.wisec.writers.TeamInfoWriter;
import de.d3web.wisec.writers.TeamsWriter;
import de.d3web.wisec.writers.WISECWriter;

/**
 * Converts the WISEC database (provided as Excel file) into a collection of
 * KnowWE wiki articles (provided as text files).
 * 
 * Current Todos: - no "+" preleading the rating of a substance (should be done,
 * check!)
 * 
 * Later Todos: - Semantic alignment of lists and upper lists
 * 
 * @author joba
 * 
 */
public class WISECExcelConverter {

	// The master database file, that is the input of all knowledge
	public static String WISEC_FILE = "20100820_WISEC_v1.xls";
	public final static String EXCEL_ENCODING = "cp1252";
	// The directory of the master database file
	public static String workspace = "/Users/sebastian/Projekte/Temp/KnowWE/WISEC/";
	// Destination directory, where the generated files are put
	public static String wikiworkspace = "/Users/sebastian/Projekte/Temp/KnowWE/WISEC/wikicontent/content/";
	// "/Users/joba/Documents/Projekte/Temp/KnowWE/WISEC/wikicontent_gen/";
	// public static String wikiworkspace =
	// "/Users/sebastian/Projekte/Temp/KnowWE/WISEC/wikicontent/Treshold-40/";
	// Praefix of most of the generated files
	public static final String FILE_PRAEFIX = "WI_";

	// Name of the column that identifies the name of a substance
	public static String SUBSTANCE_IDENTIFIER = "CAS_No";
	// Include semantic annotations etc. in the generation process
	public static boolean GENERATE_WITH_KNOWLEDGE = true;

	// The generation of lists is limited by the maxListsToConvert threshold
	// public static final int maxListsToConvert = 10;
	public static final int maxListsToConvert = 10000000;

	// Excel identifier for the numbers
	public static final String NUMBER_KEY = "ID";

	public static void main(String[] args) throws BiffException, IOException {

		Stopwatch timer = new Stopwatch();
		timer.start();
		System.out.println("Conversion started: " + new Date());

		new WISECExcelConverter().convert();

		timer.stop();
		System.out.println("Time taken: " + timer.getElapsedTime() / 60000 + "min.");

	}

	private void convert() throws BiffException, IOException {

		WorkbookSettings ws = new WorkbookSettings();
		ws.setEncoding(EXCEL_ENCODING);
		ws.setCharacterSet(0);

		Workbook workbook = Workbook.getWorkbook(new File(workspace + WISEC_FILE), ws);
		WISECModel model = new WISECModel();

		List<? extends WISECReader> readers = Arrays.asList(
					new SourceListReader(workbook),
					new ListsReader(workbook),
					new SubstanceListsReader(workbook),
					new ActiveSubstancesReader(workbook),
					new GroupsReader(workbook),
					new TeamsReader(workbook)
				);
		for (WISECReader wisecReader : readers) {
			wisecReader.read(model);
		}

		List<? extends WISECWriter> writers = configureWriters(model,
				wikiworkspace);
		for (WISECWriter wisecWriter : writers) {
			wisecWriter.write();
		}
	}

	private List<? extends WISECWriter> configureWriters(WISECModel model,
			String outputDirectory) {
		List<WISECWriter> writers = new ArrayList<WISECWriter>();

		writers.add(new SourceListWriter(model, outputDirectory));

		// The substance list writer
		SubstanceListWriter w = new SubstanceListWriter(model, outputDirectory);
		w.setWithKnowledge(GENERATE_WITH_KNOWLEDGE);
		writers.add(w);

		writers.add(new SubstanceInfoWriter(model, outputDirectory));

		writers.add(new SourceListOverviewWriter(model, outputDirectory));

		writers.add(new SubstanceListsOverviewWriter(model,
				outputDirectory));

		writers.add(new AllSubstancesChapterWriter(model, outputDirectory));
		writers.add(new AllSubstancesOverviewWriter(model, outputDirectory));
		writers.add(new ActiveSubstancesWriter(model, outputDirectory));

		writers.add(new GroupsWriter(model, outputDirectory));
		writers.add(new GroupInfoWriter(model, outputDirectory));
		writers.add(new TeamsWriter(model, outputDirectory));
		writers.add(new TeamInfoWriter(model, outputDirectory));

		// ////// Substance Ratings
		// writers.addAll(configureRatingConfigurations(model,
		// outputDirectory));
		// writers.add(new RatingOverviewWriter(model, outputDirectory));

		// the overview should be the last in the list, since it uses some
		// information generated in previous writers
		writers.add(new OverviewWriter(model, outputDirectory));
		return writers;
	}

	private List<? extends WISECWriter> configureRatingConfigurations(WISECModel model,
			String outputDirectory) {
		List<WISECWriter> writers = new ArrayList<WISECWriter>();

		// Config: PBT Substances
		ScoringWeightsConfiguration config1 = new ScoringWeightsConfiguration();
		config1.setName("PBT Substances");
		config1.setWeights(new String[] {
				"Persistence", "3", "Bioakumulation_Potential", "3", "Aqua_Tox", "3",
				"Multiple_Tox", "1", "EDC", "-1", "CMR", "-1",
				"LRT", "2", "Climatic_Change", "0", "Risk_related", "0", "Political", "1",
				"Exposure", "2"
			});
		SubstanceRatingListWriter pbtSubstances = new SubstanceRatingListWriter(model,
				outputDirectory);
		pbtSubstances.setConfiguration(config1);
		writers.add(pbtSubstances);

		// Config: vPvB Substances
		ScoringWeightsConfiguration config2 = new ScoringWeightsConfiguration();
		config2.setName("vPvB Substances");
		config2.setWeights(new String[] {
				"Persistence", "3", "Bioakumulation_Potential", "3", "Aqua_Tox", "0",
				"Multiple_Tox", "0", "EDC", "-1", "CMR", "-1",
				"LRT", "3", "Climatic_Change", "0", "Risk_related", "0", "Political", "1",
				"Exposure", "2"
			});

		SubstanceRatingListWriter vPvB = new SubstanceRatingListWriter(model, outputDirectory);
		vPvB.setConfiguration(config2);
		writers.add(vPvB);

		// Config: EDC Substances
		ScoringWeightsConfiguration config3 = new ScoringWeightsConfiguration();
		config3.setName("EDC Substances");
		config3.setWeights(new String[] {
				"Persistence", "0", "Bioakumulation_Potential", "0", "Aqua_Tox", "0",
				"Multiple_Tox", "0", "EDC", "3", "CMR", "-1",
				"LRT", "0", "Climatic_Change", "0", "Risk_related", "0", "Political", "1",
				"Exposure", "2"
			});

		SubstanceRatingListWriter edcSubstances = new SubstanceRatingListWriter(model,
				outputDirectory);
		edcSubstances.setConfiguration(config3);
		writers.add(edcSubstances);

		// Config: CMR Substances
		ScoringWeightsConfiguration config4 = new ScoringWeightsConfiguration();
		config4.setName("CMR Substances");
		config4.setWeights(new String[] {
				"Persistence", "0", "Bioakumulation_Potential", "0", "Aqua_Tox", "0",
				"Multiple_Tox", "0", "EDC", "0", "CMR", "3",
				"LRT", "0", "Climatic_Change", "0", "Risk_related", "0", "Political", "1",
				"Exposure", "2"
			});

		SubstanceRatingListWriter cmrSubstances = new SubstanceRatingListWriter(model,
				outputDirectory);
		cmrSubstances.setConfiguration(config4);
		writers.add(cmrSubstances);

		// Config: Toxic Substances
		ScoringWeightsConfiguration config5 = new ScoringWeightsConfiguration();
		config5.setName("Toxic Substances");
		config5.setWeights(new String[] {
				"Persistence", "0", "Bioakumulation_Potential", "0", "Aqua_Tox", "3",
				"Multiple_Tox", "3", "EDC", "2", "CMR", "0",
				"LRT", "0", "Climatic_Change", "0", "Risk_related", "0", "Political", "1",
				"Exposure", "2"
			});

		SubstanceRatingListWriter toxicSubstances = new SubstanceRatingListWriter(model,
				outputDirectory);
		toxicSubstances.setConfiguration(config5);
		writers.add(toxicSubstances);

		return writers;
	}
}
