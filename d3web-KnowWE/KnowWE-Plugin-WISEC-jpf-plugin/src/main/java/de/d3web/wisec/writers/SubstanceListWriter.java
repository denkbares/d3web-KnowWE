package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;

import de.d3web.wisec.converter.WISECExcelConverter;
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
		writer.write("!!! " + list.getName() + "\n\n");

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
					"@UpperlistID: " + sourceID + "\n" +
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
		for (String header : list.substanceAttributes) {
			writer.write("|| " + header + " ");
		}
		writer.write("\n");
		for (Substance substance : list.substances) {
			for (String attribute : list.substanceAttributes) {
				String value = ConverterUtils.clean(substance.values.get(attribute));
				writer.write("| ");
				if (attribute.equals(WISECExcelConverter.SUBSTANCE_IDENTIFIER)) {
					if (model.activeSubstances.contains(substance)) {
						writer.write(" [ " + value + " | "
								+ SubstanceInfoWriter.getWikiFileNameFor(value) + "] ");
					}
					else {
						writer.write(" [activate] " + value + " ");
					}

				}
				else {
					writer.write(value + " ");
				}
			}
			writer.write("\n");
		}
		writer.write("\n");
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
