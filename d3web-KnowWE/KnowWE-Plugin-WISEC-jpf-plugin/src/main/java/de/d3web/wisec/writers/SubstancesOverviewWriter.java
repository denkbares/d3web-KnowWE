package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.WISECModel;

public class SubstancesOverviewWriter extends WISECWriter {
	private static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX+"AllSubstances";

	public SubstancesOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	
	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory+FILENAME +".txt");
		StringBuffer b = new StringBuffer(); 
		b.append("!!! All Considered Substances\n\n");
		
		// open the zebra and the sortable table
		b.append("%%zebra-table\n%%sortable\n");
		
		// write the data
//		Spalte 1 SGN
//		Spalte 2 Bei SGN = CAS den Namen sonst leer
//		Spalte 3 EU number
//		Spalte 4 Occurences
		b.append("|| SGN || CAS || EU number || Occurences \n");
		List<Substance> sortedSubstances = sortSubstances();
		for (Substance substance : sortedSubstances) {
			String euNumber = "";
			String casName  = substance.getCAS();
			b.append("| "+SubstanceWriter.asWikiMarkup(substance)+" | "+casName  +" | "+euNumber +"| "+model.usesInLists(substance)+"\n");
		}
		
		writer.write(b.toString());
		// close the zebra and the sortable table
		writer.append("/%\n/%\n");
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

}
