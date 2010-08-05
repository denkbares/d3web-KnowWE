package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SourceList;
import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class SubstanceListWriter extends WISECWriter {

	private static String FILE_PRAEFIX = WISECExcelConverter.FILE_PRAEFIX + "SUL_";
	private boolean withKnowledge;

	public SubstanceListWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		for (SubstanceList list : model.getSubstanceLists()) {
			Writer writer = ConverterUtils.createWriter(this.outputDirectory
					+ getWikiFileNameFor(list.getId())
					+ ".txt");
			write(list, writer);
			writer.close();
		}
	}

	public static String getWikiFileNameFor(String name) {
		return FILE_PRAEFIX + ConverterUtils.cleanForFilename(name);
	}

	private void write(SubstanceList list, Writer writer) throws IOException {
		writeBreadcrumb(writer, list);
		writer.write("!!! " + list.getName() + "\n\n");

		// WRITE THE IDENTIFICATION HEADER
		writeIdentificationHeader(writer, list);

		// WRITE THE SUBSTANCE LIST CRITERIA
		writer.write("!! Criteria \n\n");
		writeKnowledge(writer, "%%ListCriteria\n");

		for (String key : list.criteria.keySet()) {
			String value = ConverterUtils.clean(list.criteria.get(key));
			if (value != null && !value.isEmpty()) {
				writer.write("|| " + key + " | " + value + " \n");
			}
		}
		String sourceID = list.info.get("Source_ID");
		if (sourceID != null) {
			writeKnowledge(writer, "-\n\n" +
					"@ListID: " + list.getId() + "\n\n" +
					"@SourceID: " + sourceID + "\n" +
					"%\n");

			writer.write("\n __Source:__ ");
			writer.write("[ " + model.getSourceListNameForID(sourceID) + " | "
						+ SourceListWriter.getWikiFilename(sourceID) + "]");
		}
		else {
			writer.write("__Attention:__ No source list known.");
		}
		writer.write("\n\n");

		// WRITE THE LIST OF SUBSTANCES
		writeSubstanceTable(writer, list);
	}

	private void writeIdentificationHeader(Writer writer, SubstanceList list) throws IOException {
		SourceList sourceList = model.getSourceListForID(list.info.get("Source_ID"));
		StringBuffer buffy = new StringBuffer();
		buffy.append("|| Source_ID   | " + getWikiedValueForAttribute("Source_ID", list) + "\n");
		buffy.append("|| Source_Name | [" + sourceList.get("Name") + " | " +
				SourceListWriter.getWikiFilename(sourceList.getId()) + "]\n");
		buffy.append("|| List_ID     | " + getWikiedValueForAttribute("ID", list) + "\n");
		buffy.append("|| List_Name   | " + getWikiedValueForAttribute("Name", list) + "\n");
		buffy.append("|| Author      | " + sourceList.get("Author") + "\n");
		buffy.append("|| Country     | " + sourceList.get("Country") + "\n");

		buffy.append("|| Criteria_Code | " + getWikiedValueForAttribute("Criteria_Code", list)
				+ "\n");
		buffy.append("|| List_allocation | " + getWikiedValueForAttribute("List_allocation", list)
				+ "\n");
		buffy.append("|| Number_of_substances | "
				+ getWikiedValueForAttribute("Number_of_substances", list) + "\n");

		buffy.append("\n");
		writer.write(buffy.toString());

	}

	private String getWikiedValueForAttribute(String attribute, SubstanceList list) {
		StringBuffer buffy = new StringBuffer();
		if (attribute.equalsIgnoreCase("Name")) {
			buffy.append("[" + ConverterUtils.clean(list.getName()) + " | "
					+ SubstanceListWriter.getWikiFileNameFor(list.getId()) + "] ");
		}
		else {
			String value = list.info.get(attribute);
			if (value == null) {
				value = "";
			}
			buffy.append(ConverterUtils.clean(value) + " ");
		}
		return buffy.toString();
	}

	private void writeBreadcrumb(Writer writer, SubstanceList list) throws IOException {
		super.writeBreadcrumb(writer);
		writer.write(" > [Index of Lists|" + SubstanceListsOverviewWriter.FILENAME + "] > "
				+ list.getName() + "\n\n");
	}

	private void writeKnowledge(Writer writer, String knowledge) throws IOException {
		if (withKnowledge) {
			writer.write(knowledge);
		}
	}

	private void writeSubstanceTable(Writer writer, SubstanceList list) throws IOException {
		writer.write("!! Substances \n\n");
		writeKnowledge(writer, "%%ListSubstances\n");

		printSubstanceTable(writer, list);

		writeKnowledge(writer, "-\n\n");
		writeKnowledge(writer, "@ListID: " + list.getId() + "  \n");
		writeKnowledge(writer, "%\n");

	}

	private void printSubstanceTable(Writer writer, SubstanceList list) throws IOException {
		StringBuffer buffy = new StringBuffer();

		buffy.append("%%zebra-table \n%%sortable\n");

		List<String> FIRST_HEADERS = Arrays.asList(new String[] {
				"CAS_No", "Action", "EC_No", "IUPAC_name", "Chemical_name" });

		for (String header : FIRST_HEADERS) {
			buffy.append("|| " + header + " ");
		}
		for (String header : list.substanceAttributes) {
			if (!FIRST_HEADERS.contains(header)) {
				buffy.append("|| " + header + " ");
			}
		}

		buffy.append("\n");
		for (Substance substance : list.substances) {
			for (String header : FIRST_HEADERS) {
				if (header.equals("Action")) {
					if (model.activeSubstances.contains(substance.getName())) {
						buffy.append("| A ");
					}
					else {
						buffy.append("| [+ | dummy] ");
					}					
				} 
				else {
					buffy.append("| ");
					getCellValue(buffy, substance, header);
				}
			}
			for (String header : list.substanceAttributes) {
				if (!FIRST_HEADERS.contains(header)) {
					buffy.append("| ");
					getCellValue(buffy, substance, header);
				}
			}
			buffy.append("\n");
		}
		buffy.append("\n");
		buffy.append("/%\n/%\n");
		writer.write(buffy.toString());
	}


	private void getCellValue(StringBuffer buffy, Substance substance, String header) {
		String value = ConverterUtils.clean(substance.values.get(header));
		if (header.equals(WISECExcelConverter.SUBSTANCE_IDENTIFIER)) {
			if (model.activeSubstances.contains(substance.getName())) {
				buffy.append(" [ " + value + " | "
						+ SubstanceInfoWriter.getWikiFileNameFor(value) + "] ");
			}
			else {
				buffy.append(value + " ");
			}

		}
		else {
			buffy.append(value + " ");
		}
	}


	public void setFilePraefix(String praefix) {
		FILE_PRAEFIX = praefix;
	}

	public boolean isWithKnowledge() {
		return withKnowledge;
	}

	public void setWithKnowledge(boolean withKnowledge) {
		this.withKnowledge = withKnowledge;
	}

	public static String asWikiMarkup(SubstanceList list) {
		return "[ " + list.getName() + " | " + SubstanceListWriter.getWikiFileNameFor(list.getId())
				+ "]";
	}

	public static String getCriteriaString(SubstanceList list) {
		String criteriaString = "";
		for (String criteria : list.criteria.keySet()) {
			String value = list.criteria.get(criteria);
			if (value != null && value.length() > 0) {
				criteriaString += criteria + "=" + value + ", ";
			}
		}
		if (criteriaString.length() < 2) {
			return criteriaString;
		}
		return criteriaString.substring(0, criteriaString.length() - 2);
	}

}
