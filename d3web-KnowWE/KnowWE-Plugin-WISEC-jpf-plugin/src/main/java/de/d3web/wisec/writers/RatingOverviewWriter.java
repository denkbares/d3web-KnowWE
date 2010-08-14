package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.WISECModel;

public class RatingOverviewWriter extends WISECWriter {

	private String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "OverviewRatings";

	public RatingOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory + FILENAME + ".txt");

		writer.write("!!! Rankings\n\n");
		for (String rating : model.generatedRatings()) {
			writer.write("* [" + rating + " | " + model.wikiFileNameForRating(rating) + "]\n");
		}

		writer.close();
	}

}
