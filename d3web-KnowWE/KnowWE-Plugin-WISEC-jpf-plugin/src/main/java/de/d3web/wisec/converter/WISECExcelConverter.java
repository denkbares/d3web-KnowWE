package de.d3web.wisec.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import de.d3web.wisec.model.WISECModel;
import de.d3web.wisec.readers.WISECReader;
import de.d3web.wisec.readersnew.SubstanceListsReader;
import de.d3web.wisec.readersnew.UpperListReader;
import de.d3web.wisec.scoring.ScoringWeightsConfiguration;
import de.d3web.wisec.writers.OverviewWriter;
import de.d3web.wisec.writers.RatingOverviewWriter;
import de.d3web.wisec.writers.SubstanceListWriter;
import de.d3web.wisec.writers.SubstanceListsOverviewWriter;
import de.d3web.wisec.writers.SubstanceRatingListWriter;
import de.d3web.wisec.writers.SubstanceWriter;
import de.d3web.wisec.writers.SubstancesOverviewWriter;
import de.d3web.wisec.writers.UpperListOverviewWriter;
import de.d3web.wisec.writers.UpperListWriter;
import de.d3web.wisec.writers.WISECWriter;

/**
 * Converts the WISEC database (provided as Excel file) into
 * a collection of KnowWE wiki articles (provided as text files).
 * 
 * Current Todos:
 * - Sonderzeichen in names of substances?
 * - no "+" preleading the rating of a substance (should be done, check!)
 * 
 * Later Todos:
 * - Semantic alignment of lists and upper lists
 * 
 * @author joba
 *
 */
public class WISECExcelConverter {
	// The master database file, that is the input of all knowledge
	public static String WISEC_FILE = "20100715_WISEC.xls";
	// The directory of the master database file
	public static String workspace = "/Users/sebastian/Projekte/Temp/KnowWE/WISEC/";
	// Destination directory, where the generated files are put
	public static String wikiworkspace = "/Users/sebastian/Projekte/Temp/KnowWE/WISEC/wikicontent/Treshold-40/";
	// Praefix of most of the generated files
	public static final String FILE_PRAEFIX = "WI_";

	// Name of the column that identifies the name of a substance 
	public static String SUBSTANCE_IDENTIFIER     = "SGN";
	// Include semantic annotations etc. in the generation process
	public static boolean GENERATE_WITH_KNOWLEDGE = true;
	// Minimum number of occurrences of a substance, that is required before it is considered for the model
	// public static final int NUMBER_OF_SUBSTANCES_THRESHOLD = 40; // takes
	// 9min
	public static final int NUMBER_OF_SUBSTANCES_THRESHOLD = 10; // takes
	// 30min
	// public static final int NUMBER_OF_SUBSTANCES_THRESHOLD = 5; // takes
	// 110min
	// The generation of lists is limited by the maxListsToConvert threshold 
	// public static final int maxListsToConvert = 10;
	public static final int maxListsToConvert = 1000000;
	
	// Excel identifier for the numbers
	public static final String NUMBER_KEY = "LfdNr";
	
	
	public static void main(String[] args) throws BiffException, IOException {
		Stopwatch timer = new Stopwatch();
		timer.start();
		System.out.println("Conversion started: " + new Date());
		
		
		
		new WISECExcelConverter().convert();
		
		timer.stop();
		System.out.println("Time taken: " + timer.getElapsedTime() / 60000 + "min.");

	}


	private void convert() throws BiffException, IOException {
		Workbook workbook = Workbook.getWorkbook(new File(workspace+WISEC_FILE));
		WISECModel model = new WISECModel();
		model.SUBSTANCE_OCCURRENCE_THRESHOLD = NUMBER_OF_SUBSTANCES_THRESHOLD;

		
		List<? extends WISECReader> readers = Arrays.asList(
				new UpperListReader(workbook),
				new SubstanceListsReader(workbook));
		for (WISECReader wisecReader : readers) {
			wisecReader.read(model);
		}
		
		List<? extends WISECWriter> writers = configureWriters(model, wikiworkspace);
		for (WISECWriter wisecWriter : writers) {
			wisecWriter.write();
		}
	}

	private List<? extends WISECWriter> configureWriters(WISECModel model,
			String outputDirectory) {
		List<WISECWriter> writers = new ArrayList<WISECWriter>();

		writers.add(new UpperListWriter(model, outputDirectory));
		
		// The substance list writer
		SubstanceListWriter w = new SubstanceListWriter(model, outputDirectory);
		w.setWithKnowledge(GENERATE_WITH_KNOWLEDGE);
		writers.add(w);
		
		writers.add(new SubstanceWriter(model, outputDirectory));
		
		writers.add(new UpperListOverviewWriter(model, outputDirectory));
		writers.add(new SubstanceListsOverviewWriter(model, outputDirectory));
		
		writers.add(new SubstancesOverviewWriter(model, outputDirectory));
		
		writers.addAll(configureRatingConfigurations(model, outputDirectory));
		writers.add(new RatingOverviewWriter(model, outputDirectory));

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
				"P", "3", "B", "3", "Aqua_Tox", "3", "Multiple_Tox", "1", "EDC", "-1", "CMR", "-1", 
				"LRT", "2", "Climatic_Change", "0", "Risk_related", "0", "Political", "1", "Exposure", "2"
			});
		SubstanceRatingListWriter pbtSubstances = new SubstanceRatingListWriter(model, outputDirectory);
		pbtSubstances.setConfiguration(config1);
		writers.add(pbtSubstances);
		
		// Config: vPvB Substances
		ScoringWeightsConfiguration config2 = new ScoringWeightsConfiguration();
		config2.setName("vPvB Substances");
		config2.setWeights(new String[] {
				"P", "3", "B", "3", "Aqua_Tox", "0", "Multiple_Tox", "0", "EDC", "-1", "CMR", "-1", 
				"LRT", "3", "Climatic_Change", "0", "Risk_related", "0", "Political", "1", "Exposure", "2"
			});
		SubstanceRatingListWriter vPvB = new SubstanceRatingListWriter(model, outputDirectory);
		vPvB.setConfiguration(config2);
		writers.add(vPvB);
		
		// Config: EDC Substances
		ScoringWeightsConfiguration config3 = new ScoringWeightsConfiguration();
		config3.setName("EDC Substances");
		config3.setWeights(new String[] {
				"P", "0", "B", "0", "Aqua_Tox", "0", "Multiple_Tox", "0", "EDC", "3", "CMR", "-1", 
				"LRT", "0", "Climatic_Change", "0", "Risk_related", "0", "Political", "1", "Exposure", "2"
			});
		SubstanceRatingListWriter edcSubstances = new SubstanceRatingListWriter(model, outputDirectory);
		edcSubstances.setConfiguration(config3);
		writers.add(edcSubstances);
		
		// Config: CMR Substances
		ScoringWeightsConfiguration config4 = new ScoringWeightsConfiguration();
		config4.setName("CMR Substances");
		config4.setWeights(new String[] {
				"P", "0", "B", "0", "Aqua_Tox", "0", "Multiple_Tox", "0", "EDC", "0", "CMR", "3", 
				"LRT", "0", "Climatic_Change", "0", "Risk_related", "0", "Political", "1", "Exposure", "2"
			});
		SubstanceRatingListWriter cmrSubstances = new SubstanceRatingListWriter(model, outputDirectory);
		cmrSubstances.setConfiguration(config4);
		writers.add(cmrSubstances);

		// Config: Toxic Substances
		ScoringWeightsConfiguration config5 = new ScoringWeightsConfiguration();
		config5.setName("Toxic Substances");
		config5.setWeights(new String[] {
				"P", "0", "B", "0", "Aqua_Tox", "3", "Multiple_Tox", "3", "EDC", "2", "CMR", "0", 
				"LRT", "0", "Climatic_Change", "0", "Risk_related", "0", "Political", "1", "Exposure", "2"
			});
		SubstanceRatingListWriter toxicSubstances = new SubstanceRatingListWriter(model, outputDirectory);
		toxicSubstances.setConfiguration(config5);
		writers.add(toxicSubstances);

		return writers;
	}	
}
