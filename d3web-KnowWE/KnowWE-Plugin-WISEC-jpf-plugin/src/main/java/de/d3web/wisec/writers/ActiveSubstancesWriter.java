package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.WISECModel;

public class ActiveSubstancesWriter extends WISECWriter {

	public static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "ActiveSubstances";

	public ActiveSubstancesWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	
	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory+FILENAME +".txt");
		writeBreadcrumb(writer);

		StringBuffer b = new StringBuffer(); 
		b.append("!!! Active Substances\n\n");
		
		// open the zebra and the sortable table
		b.append("%%zebra-table\n%%sortable\n");
		
		// write the data
		b.append("|| CAS_No || EC_no || IUPAC_name || Chemical_name  \n");
		// List<Substance> sortedSubstances = sortSubstances();
		for (String substanceName : model.activeSubstances) {
			// String casName = substance.getCAS();
			b.append("| [" + substanceName + " | WI_SUB_" + substanceName + "] | "
					+ ConverterUtils.asString(model.getECNamesFor(substanceName)) + "| "
					+ ConverterUtils.asString(model.getIUPACFor(substanceName)) + " | "
					+ ConverterUtils.asString(model.getChemNamesFor(substanceName)) + "\n");
			// + " | " + model.usesInLists(substance) + "\n");
		}
		
		writer.write(b.toString());
		// close the zebra and the sortable table
		writer.append("/%\n/%\n");
		writer.close();
		
	}

	@Override
	protected void writeBreadcrumb(Writer writer) throws IOException {
		super.writeBreadcrumb(writer);
		writer.write(" > [List of Substances|" + AllSubstancesWriter.FILENAME + "] > "
				+ "Active Substances\n\n");
	}


}
