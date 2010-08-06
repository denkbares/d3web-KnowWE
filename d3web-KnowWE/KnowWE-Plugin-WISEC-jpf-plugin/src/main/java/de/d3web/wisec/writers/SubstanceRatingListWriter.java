package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.WISECModel;
import de.d3web.wisec.scoring.DefaultScoringWeightsConfiguration;
import de.d3web.wisec.scoring.ScoringUtils;
import de.d3web.wisec.scoring.ScoringWeightsConfiguration;

public class SubstanceRatingListWriter extends WISECWriter {

	public static final String FILE_PRAEFIX = WISECExcelConverter.FILE_PRAEFIX + "RATING_";
	ScoringWeightsConfiguration configuration = new DefaultScoringWeightsConfiguration();

	class SubstanceWithRating implements Comparable<SubstanceWithRating> {

		String substanceName;
		Double rating;

		public String asWikiString() {
			return SubstanceInfoWriter.asWikiMarkup(substanceName) + " " + rating;
		}

		@Override
		public int compareTo(SubstanceWithRating o) {
			return (int) Math.signum(o.rating - this.rating);
		}
	}

	public SubstanceRatingListWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory
				+ getFileNameFor(configuration.getName()) + ".txt");
		List<SubstanceWithRating> substances = computeRating();
		Collections.sort(substances);
		if (substances.size() > configuration.MAX_SUBSTANCES_IN_RATING) {
			substances = substances.subList(0, configuration.MAX_SUBSTANCES_IN_RATING);
		}

		StringBuffer b = new StringBuffer();
		b.append("!!! Rating: " + configuration.getName() + " \n\n");

		b.append("!! Criteria Weights\n");
		for (String criteria : configuration.getCriterias()) {
			b.append("* " + criteria + " = " + configuration.weightFor(criteria) + "\n");
		}
		b.append("\n\n");
		b.append("!! Rating\n");
		for (SubstanceWithRating substanceWithRating : substances) {
			b.append("# " + SubstanceInfoWriter.asWikiMarkup(substanceWithRating.substanceName) +
					" - "
					+ ConverterUtils.asString(model.getChemNamesFor(substanceWithRating.substanceName))
					+
					" (" + ScoringUtils.prettyPrint(substanceWithRating.rating) + ")\n");
		}
		b.append("\n");
		writer.append(b.toString());
		writer.close();
		model.addRating(configuration.getName(), FILE_PRAEFIX + configuration.getName());
	}

	public static String getFileNameFor(String name) {
		return FILE_PRAEFIX + clean(name);
	}

	private static String clean(String name) {
		String newname = name.replaceAll("\\s", "_");
		return newname;
	}

	private List<SubstanceWithRating> computeRating() {
		List<SubstanceWithRating> ratings = new ArrayList<SubstanceWithRating>();
		for (String substanceName : model.activeSubstances) {
			SubstanceWithRating swr = new SubstanceWithRating();
			swr.substanceName = substanceName;
			swr.rating = ScoringUtils.computeTotalScoreFor(model, getConfiguration(), substanceName);
			ratings.add(swr);

		}
		return ratings;
	}

	public ScoringWeightsConfiguration getConfiguration() {
		return this.configuration;
	}

	public void setConfiguration(ScoringWeightsConfiguration configuration) {
		this.configuration = configuration;
	}

}
