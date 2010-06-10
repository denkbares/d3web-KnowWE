package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.UpperList;
import de.d3web.wisec.model.WISECModel;

public class UpperListOverviewWriter extends WISECWriter {
	private static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX
			+ "InformationSources";

	private static final String[] PRINTABLE_ATTRIBUTES = new String[] {
			WISECExcelConverter.NUMBER_KEY, "Inhalt", "Institution", "Pefix",
			"Incl" };

	public UpperListOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory
				+ FILENAME + ".txt");

		writeHeader(writer);
		StringBuffer buffy = new StringBuffer();
		for (UpperList list : model.getUpperLists()) {
			for (String attribute : PRINTABLE_ATTRIBUTES) {
				String value = list.get(attribute);
				if (value != null) {
					value = ConverterUtils.clean(value);
					if (value.equals(list.getName())) {
						value = "[" + value + " | "
								+ UpperListWriter.getWikiFilename(value) + "]";
					} else if (attribute.equals(WISECExcelConverter.NUMBER_KEY)) {
						value = ConverterUtils.toShoppedString(value);
					}
				} else {
					value = "";
				}
				buffy.append("| " + value + " ");
			}
			buffy.append("\n");
		}
		writer.append(buffy.toString());

		writeFooter(writer);
		writer.close();
	}

	private void writeFooter(Writer writer) throws IOException {
		// close the zebra and the sortable table
		writer.append("/%\n/%\n");
	}

	private void writeHeader(Writer writer) throws IOException {
		writer.append("!!! Information Sources\n\n");
		// open the rebra and the sortable table
		writer.append("%%zebra-table\n%%sortable\n");
		// write all header names
		for (String attribute : PRINTABLE_ATTRIBUTES) {
			writer.append("|| " + attribute);
		}
		writer.append("\n");
	}
}
