package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class SubstanceListWriter extends WISECWriter {

	private static String FILE_PRAEFIX = WISECExcelConverter.FILE_PRAEFIX+"SL_";
	private boolean withKnowledge;

	public SubstanceListWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		for (SubstanceList list : model.getSubstanceLists()) {
			list.filename = getWikiFileNameFor(list.id);
			Writer writer = ConverterUtils.createWriter(this.outputDirectory + list.filename
					+ ".txt");
			write(list, writer);
			writer.close();
		}
	}

	public static String getWikiFileNameFor(String name) {
		return FILE_PRAEFIX + clean(name);
	}

	private void write(SubstanceList list, Writer writer) throws IOException {
		writer.write("!!! " + list.name + "\n\n");

		// WRITE THE SUBSTANCE LIST CRITERIA
		writer.write("!! Criteria \n\n");

		writeKnowledge(writer, "%%ListCriteria\n");

		for (String key : list.criteria.keySet()) {
			String value = clean(list.criteria.get(key));
			if (value == null) value = "";
			writer.write("| " + key + " | " + value + " \n");
		}
		writeKnowledge(writer, "-\n" +
				"@ListID: " + list.name + "\n" +
				// TODO: Replace WISECUPPER with list.upperlist.getName() but
				// atm the id is not recognized in the upperlist
				"@UpperlistID: WISECUPPER \n" +
				"%\n");

		writer.write("\n __Upper list:__ ");
		if (list.upperList == null) {
			writer.write(" - none - ");
		}
		else {
			writer.write("[ " + list.upperList.getName() + " | "
					+ UpperListWriter.getWikiFilename(list.upperList.getName()) + "]");
		}

		writer.write("\n\n");

		// WRITE THE LIST OF SUBSTANCES
		writeSubstanceTables(writer, list);
	}

	private void writeKnowledge(Writer writer, String knowledge) throws IOException {
		if (withKnowledge) {
			writer.write(knowledge);
		}
	}

	private static String clean(String string) {
		string = string.replaceAll("&", "_AND_");
		string = string.replaceAll("ä", "ae");
		string = string.replaceAll("ö", "oe");
		string = string.replaceAll("ü", "ue");
		string = string.replaceAll("ß", "ss");
		string = string.replaceAll("\n", " ");
		string = ConverterUtils.clean(string);
		return string;
	}

	private void writeSubstanceTables(Writer writer, SubstanceList list) throws IOException {
		// First divide into substances to print and substances that are not
		// considered for knowledge generation
		List<Substance> consideredSubstances = new ArrayList<Substance>();
		List<Substance> notConsideredSubstances = new ArrayList<Substance>();
		for (Substance substance : list.substances) {
			if (substanceExceedsThreshold(substance)) {
				consideredSubstances.add(substance);
			}
			else {
				notConsideredSubstances.add(substance);
			}
		}

		if (consideredSubstances.size() > 0) {
			writer.write("!! Substances \n\n");
			writeKnowledge(writer, "%%ListSubstances\n");
			printSubstanceTable(writer, list, consideredSubstances, true);
			writeKnowledge(writer, "-\n");
			writeKnowledge(writer, "@ListID: " + list.name + "  \n");
			writeKnowledge(writer, "%\n");
		}
		// writerStatistics.substanceListConsideredSubstances.put(list.name,
		// consideredSubstances.size());

		if (notConsideredSubstances.size() > 0) {
			writer.write("\n\n!! Further Substances (not considered for knowledge generation): \n\n");
			printSubstanceTable(writer, list, notConsideredSubstances, false);
		}
	}

	private void printSubstanceTable(Writer writer, SubstanceList list,
			List<Substance> substances, boolean trackStatistics) throws IOException {
		for (String attribute : list.attributes) {
			writer.write("|| " + attribute + " ");
		}
		writer.write("\n");
		for (Substance substance : substances) {
			for (String attribute : list.attributes) {
				String value = clean(substance.values.get(attribute));
				writer.write("| " + value + " ");
				if (attribute.equals(WISECExcelConverter.SUBSTANCE_IDENTIFIER)) {
					writer.write(" [ > | " + SubstanceWriter.getWikiFileNameFor(value) + "]");
				}
			}
			writer.write("\n");
		}
		writer.write("\n");
	}

	private boolean substanceExceedsThreshold(Substance substance) {
		return model.usesInLists(substance) >= model.SUBSTANCE_OCCURRENCE_THRESHOLD;
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
		return "[ " + list.name + " | " + SubstanceListWriter.getWikiFileNameFor(list.id) + "]";
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
