package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class SubstanceListsOverviewWriter extends WISECWriter {
	private static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "AllSubstanceLists";
	
	public SubstanceListsOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory+FILENAME+".txt");

		writeHeader(writer);
		StringBuffer buffy = new StringBuffer();
		for (SubstanceList list : model.getSubstanceLists()) {
			String filename = list.filename;
			
			buffy.append("| " + getUpperListNumber(list)); // UpperListName
			buffy.append(" | " + getUpperListName(list));   // UperListNumber
			
			buffy.append(" | [" + list.name +" | " +filename+ "] "); // Name of the List
			
			buffy.append(" | "+ computeConsideredString(list));      // How much considered?
			buffy.append(" | "+ SubstanceListWriter.getCriteriaString(list)); // Which criteria used?
			buffy.append("\n");
		}
		writer.append(buffy.toString());
		
		writeFooter(writer);
		writer.close();

	}


	
	private String getUpperListNumber(SubstanceList list) {
		if (list.upperList == null) {
			return "";
		}
		else {
			return ConverterUtils.toShoppedString(list.upperList.get(WISECExcelConverter.NUMBER_KEY));
		}
	}

	private String getUpperListName(SubstanceList list) {
		if (list.upperList == null) {
			return "";
		}
		else {
			String linked = "["+list.upperList.getName()+" | " + UpperListWriter.getWikiFilename(list.upperList.getName()) + "]";
			return linked;
		}
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
		return "" + used + " | " + notused;
	}
	
	private void writeFooter(Writer writer) throws IOException {
		// close the zebra and the sortable table
		writer.append("/%\n/%\n");
	}

	private void writeHeader(Writer writer) throws IOException {
		writer.append("!!! Lists with Substances\n\n");
		// open the rebra and the sortable table
		writer.append("%%zebra-table\n%%sortable\n");
		// write all header names
		writer.append("|| No || Upper List || List || Used || Unused || Criteria \n");
	}
}
