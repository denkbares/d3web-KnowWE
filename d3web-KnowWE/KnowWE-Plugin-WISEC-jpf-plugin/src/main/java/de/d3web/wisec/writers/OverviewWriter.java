package de.d3web.wisec.writers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class OverviewWriter extends WISECWriter {
	public static final String FILENANE = "WI_WISEC.txt";
	private static final String ALL_SUBSTANCES = "WI_SUB_ALL_SUBSTANCES"; 
	
	public OverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = new FileWriter(new File(this.outputDirectory+FILENANE));
		writer.write("!!! WISEC Overview\n\n");
		writeGeneralSettings(writer);
		writeSubstanceListOverview(writer);
		writer.close();
		
		writeAllSubstances();
	}


	private void writeAllSubstances() throws IOException {
		Writer writer = new FileWriter(new File(this.outputDirectory+ALL_SUBSTANCES+".txt"));
		StringBuffer b = new StringBuffer(); 
		
		b.append("!!! All used substances\n\n");
		
		b.append("|| Substance || Occurrences \n");
		List<Substance> sortedSubstances = sortSubstances();
		for (Substance substance : sortedSubstances) {
			b.append("| "+SubstanceWriter.asWikiMarkup(substance)+" | "+model.usesInLists(substance)+"\n");
		}
		
		writer.write(b.toString());
		writer.close();
		
	}

	private List<Substance> sortSubstances() {
		List<Substance> sorted = new ArrayList<Substance>();
		for (Substance substance : model.getSubstances()) {
			if (model.usesInLists(substance) >= model.SUBSTANCE_OCCURRENCE_THRESHOLD) {
				sorted.add(substance);
			}
		}
		
		Collections.sort(sorted, new Comparator<Substance>() {
			@Override
			public int compare(Substance o1, Substance o2) {
				return model.usesInLists(o2) - model.usesInLists(o1);
			}
		});
		return sorted;
	}

	private void writeGeneralSettings(Writer writer) throws IOException {
		writer.write("!!! General \n");
		writer.write("* Set occurrence threshold: " + model.SUBSTANCE_OCCURRENCE_THRESHOLD + "\n");
		writer.write("* Number of substances used: " + computeNumberOfUsedSubstances() + "\n");
		writer.write("* Total use of substances in lists: " + computeNumberOfTotalUse() + "\n");
		writer.write("* [List of all substances | "+ALL_SUBSTANCES+"]");
		writer.write("\n\n");
		
		writer.write("!! Ratings\n\n");
		for (String rating : model.generatedRatings()) {
			writer.write("* [" + rating + " | " + model.wikiFileNameForRating(rating) + "]\n");
		}
		
	}
	
	private int computeNumberOfTotalUse() {
		int totalUse = 0;
		for (SubstanceList list : model.getSubstanceLists()) {
			for (Substance substance : list.substances) {
				Integer uses = model.usesInLists(substance);
				if (uses >= model.SUBSTANCE_OCCURRENCE_THRESHOLD) {
					totalUse++;
				}				
			}
		}
		return totalUse;
	}

	private int computeNumberOfUsedSubstances() {
		int usedSubstances = 0;
		for (Substance substance : model.getSubstances()) {
			Integer uses = model.usesInLists(substance);
			if (uses >= model.SUBSTANCE_OCCURRENCE_THRESHOLD) {
				usedSubstances++;
			}
		}
		return usedSubstances;
	}

	private void writeSubstanceListOverview(Writer writer) throws IOException {
		writer.write("!!! Substance lists \n");
		for (SubstanceList list : model.getSubstanceLists()) {
			String filename = list.filename;
			writer.write("* [" + list.name +" | " +filename+ "] "+
					computeConsideredString(list) +
					"("+SubstanceListWriter.getCriteriaString(list) + ")"+
					" \n");
		}
		writer.write("\n\n");
	}
	
	private String computeConsideredString(SubstanceList list) {
		int notused=0;
		int used = 0;
		for (Substance substance : list.substances) {
			if (model.usesInLists(substance) >= model.SUBSTANCE_OCCURRENCE_THRESHOLD) {
				used++;
			} else {
				notused++;
			}
		}
		return " (CS: " + used + ", NCS: " + notused + ") ";
	}

}
