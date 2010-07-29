package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class SubstanceListsOverviewWriter extends WISECWriter {
	private static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "AllSubstanceLists";
	
	public final static String[] LIST_ATTRIBUTES = new String[] {
			"Source_ID", "ID", "Name", "Criteria_Code", "List_allocation", "Number_of_substances",
			"CMR", "Persistence", "Bioakumulation_Potential", "Aqua_Tox", "PBT", "vPvB", "EDC",
			"Multiple_Tox", "LRT", "Climatic_Change", "drinking_water", " surface_water", "sea",
			"groundwater", "Risk_related", "Exposure", "compartment", "Market_Volume",
			"Wide_d_use", "Political", "SVHC_regulated", "Regulated", "ecological_concerns" };
	
	
	public SubstanceListsOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory+FILENAME+".txt");

		writeHeader(writer);
		for (SubstanceList list : model.getSubstanceLists()) {
			
			String listLine = generateOverviewLineFor(list);
			writer.append(listLine + "\n");
			
		}
		
		writeFooter(writer);
		writer.close();

	}


	
	public static String generateOverviewLineFor(SubstanceList list) {
		String filename = SubstanceListWriter.getWikiFileNameFor(list.getId());
		StringBuffer buffy = new StringBuffer();
		for (String attribute : LIST_ATTRIBUTES) {
			if (attribute.equalsIgnoreCase("Name")) {
				buffy.append("| [" + clean(list.getName()) + " | " + filename + "] "); // Name
																					// of
																					// the
																					// List
			}
			else {
				String value = list.info.get(attribute);
				if (value == null) {
					value = "";
				}
				buffy.append("| " + clean(value) + " ");
			}
		}
		// buffy.append("| " + getSourceListNumber(list)); // UpperListName
		// buffy.append(" | " + getUpperListName(list)); // UperListNumber
		// buffy.append(" | "+ computeConsideredString(list)); // How much
		// considered?
		// buffy.append(" | "+ SubstanceListWriter.getCriteriaString(list)); //
		// Which criteria used?

		return buffy.toString();
	}

	private static String clean(String string) {
		return ConverterUtils.clean(string);
	}

	// private String getSourceListNumber(SubstanceList list) {
	// if (list.upperList == null) {
	// return "";
	// }
	// else {
	// return
	// ConverterUtils.toShoppedString(list.upperList.get(WISECExcelConverter.NUMBER_KEY));
	// }
	// }
	//
	// private String getUpperListName(SubstanceList list) {
	// if (list.upperList == null) {
	// return "";
	// }
	// else {
	// String linked = "["+clean(list.upperList.getName())+" | " +
	// SourceListWriter.getWikiFilename(list.upperList.getName()) + "]";
	// return linked;
	// }
	// }

	private String computeConsideredString(SubstanceList list) {
		int notused=0;
		int used = 0;
		for (Substance substance : list.substances) {
			if (model.activeSubstances.contains(substance.getName())) {
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
		writer.append("!!! Lists\n\n");
		// open the zebra and the sortable table
		writer.append("%%zebra-table\n%%sortable\n");
		// write all header names
		writer.append(writeTableHeader() + "\n");
		// writer.append("|| No || Upper List || List || Used || Unused || Criteria \n");
	}

	public static String writeTableHeader() {
		StringBuffer b = new StringBuffer();
		for (String header : LIST_ATTRIBUTES) {
			b.append("|| " + header + " ");
		}
		return b.toString();
	}
}
