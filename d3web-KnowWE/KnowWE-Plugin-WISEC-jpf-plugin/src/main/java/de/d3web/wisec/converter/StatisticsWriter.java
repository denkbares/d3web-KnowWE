package de.d3web.wisec.converter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.d3web.wisec.model.SubstanceList;


public class StatisticsWriter {

	private static StatisticsWriter  instance = new StatisticsWriter();

	private StatisticsWriter() {
	}
	
	public static StatisticsWriter getInstance() {
		return instance ;
	}

	public void write(WISECStatistics statistics, String filename) throws IOException {
		Writer writer = new FileWriter(new File(filename));
		
		writer.write("!!! WISEC Overview\n\n");
		
		writeGeneralSettings(writer,statistics);
		
		writeSubstanceListOverview(writer, statistics);
		
		writeSubstanceCount(writer, statistics);
		
		
		
		writer.close();
	}

	private void writeGeneralSettings(Writer writer, WISECStatistics statistics) throws IOException {
		writer.write("!!! General \n");
		writer.write(" * Set occurence threshold: " + statistics.occurenceThreshold + "\n");
		writer.write(" * Total use of substances: " + statistics.totalUseOfSubstances()+ "\n");
		writer.write("\n\n");
	}

	private void writeSubstanceListOverview(Writer writer,	WISECStatistics statistics) throws IOException {
		writer.write("!!! Substance lists \n");
		List<String> substanceNames = sortSubstanceList(statistics.substanceInFile.keySet(),statistics);
		for (String listname : substanceNames ) {
			String filename = statistics.substanceInFile.get(listname);
			writer.write("* [" + listname +" | " +filename+ "] "+
					" (CS: " + statistics.substanceListConsideredSubstances.get(listname) + ", " +
					" NCS: " + statistics.substanceListNotConsideredSubstances.get(listname) + ") " +
					printListCriteria(statistics.listName2listInstance.get(listname)) + 
					" \n");
		}
		writer.write("\n\n");
	}

	private String printListCriteria(SubstanceList list) {
		String result = "(";
		for (String criteria : list.criteria.keySet()) {
			String value = list.criteria.get(criteria);
			if (value != null && value.length() > 0) {
				result += " " + criteria + "=" + value;
			}
		}
		return result + ")";
	}

	/**
	 * Sort the substance lists according to the considered substances
	 */
	private List<String> sortSubstanceList(Set<String> keySet, WISECStatistics statistis) {
		List<String> sortedLists = new ArrayList<String>(keySet);
		Collections.sort(sortedLists, new SubstancesConsideredCountComparator(statistis.substanceListConsideredSubstances));
		return sortedLists;
	}

	private void writeSubstanceCount(Writer writer, WISECStatistics statistics) throws IOException {
		writer.write("!!! Substance count\n");
		for (String substancename : statistics.substanceCount.keySet()) {
			Integer count = statistics.substanceCount.get(substancename);
			writer.write("* " + substancename+":  "+count+"\n");
		}
		writer.write("\n\n");
	}

}
