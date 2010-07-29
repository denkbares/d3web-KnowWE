package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SourceList;
import de.d3web.wisec.model.WISECModel;

public class SourceListOverviewWriter extends WISECWriter {
	private static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX
			+ "All_Sources";

	public SourceListOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory
				+ FILENAME + ".txt");

		writeHeader(writer);
		StringBuffer buffy = new StringBuffer();
		for (SourceList list : model.sourceLists) {
			for (String attribute : list.getAttributes()) {
				String value = list.get(attribute);
				if (value != null) {
					value = ConverterUtils.clean(value);
					if (value.equals(list.getName())) {
						value = "[" + value + " | "
								+ SourceListWriter.getWikiFilename(list.getId()) + "]";
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
		for (String attribute : getHeaderNames()) {
			writer.append("|| " + attribute);
		}
		writer.append("\n");
	}

	private Collection<String> getHeaderNames() {
		SourceList list = (SourceList) model.sourceLists.toArray()[0];
		return list.getAttributes();
	}
}
