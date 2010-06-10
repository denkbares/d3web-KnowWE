package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class OverviewWriter extends WISECWriter {
	public static final String FILENANE = WISECExcelConverter.FILE_PRAEFIX+"WISEC.txt";
	private static final String ALL_SUBSTANCES = WISECExcelConverter.FILE_PRAEFIX+"SUB_ALL_SUBSTANCES"; 
	
	public OverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory+FILENANE);
		writer.write("!!! WISEC Overview\n\n");
		writeGeneralSettings(writer);
//		writeSubstanceListOverview(writer);
		writer.close();
		
//		writeAllSubstances();
	}



	private void writeGeneralSettings(Writer writer) throws IOException {
		writer.write("!!! General \n");
		writer.write("* Set occurrence threshold: " + model.SUBSTANCE_OCCURRENCE_THRESHOLD + "\n");
		writer.write("* Number of substances used: " + computeNumberOfUsedSubstances() + "\n");
		writer.write("* Total use of substances in lists: " + computeNumberOfTotalUse() + "\n");
		writer.write("* [List of all substances | "+ALL_SUBSTANCES+"]");
		writer.write("\n\n");
		
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



}
