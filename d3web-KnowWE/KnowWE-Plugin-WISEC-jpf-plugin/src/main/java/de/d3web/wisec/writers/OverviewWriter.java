package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.WISECModel;

public class OverviewWriter extends WISECWriter {
	public static final String FILENANE = WISECExcelConverter.FILE_PRAEFIX+"WISEC.txt";
	private static final String ALL_SUBSTANCES = WISECExcelConverter.FILE_PRAEFIX + "AllSubstances";
	private static final String ALL_SUBSTANCE_LISTS = WISECExcelConverter.FILE_PRAEFIX
			+ "AllSubstanceLists";
	
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
