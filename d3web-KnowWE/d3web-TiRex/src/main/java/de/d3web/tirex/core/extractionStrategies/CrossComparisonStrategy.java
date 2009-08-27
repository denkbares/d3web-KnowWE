package de.d3web.tirex.core.extractionStrategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.tirex.core.OriginalMatchAndStrategy;
import de.d3web.tirex.core.TiRexUtilities;

public class CrossComparisonStrategy extends AbstractExtractionStrategy {

	/**
	 * This ExtractionStrategy is implemented as a Singleton.
	 */
	private static CrossComparisonStrategy instance;

	private static Map<String, List<List<String>>> chunkedQuestions = null;

	private CrossComparisonStrategy() {
		chunkedQuestions = new HashMap<String, List<List<String>>>();
	}

	public static CrossComparisonStrategy getInstance() {
		if (instance == null) {
			instance = new CrossComparisonStrategy();
		}

		return instance;
	}

	@Override
	public OriginalMatchAndStrategy extractKnowledge(IDObject toMatch,
			String knowledge) {
		String qaText = extractQuestionOrAnswerText(toMatch);
		if (qaText == null) {
			return null;
		}

		int maxChunks = getMaxChunks(qaText, knowledge);

		List<List<String>> qaChunks = chunkedQuestions.get(qaText);
		if (qaChunks == null) {
			// A full sized list is created and saved
			qaChunks = getChunks(qaText, qaText.split(" ").length);
			chunkedQuestions.put(qaText, qaChunks);
		}

		// Necessary for all those cases where maxChunks < qaChunks.size()
		List<List<String>> trimmedQAChunks = new ArrayList<List<String>>();
		for (int i = qaChunks.size() - maxChunks; i < qaChunks.size(); i++) {
			trimmedQAChunks.add(qaChunks.get(i));
		}

		List<List<String>> kwChunks = getChunks(knowledge, maxChunks);

		for (int i = 0; i < maxChunks; i++) {
			List<String> qaParts = trimmedQAChunks.get(i);
			List<String> kwParts = kwChunks.get(i);

			for (int j = 0; j < qaParts.size(); j++) {
				for (int k = 0; k < kwParts.size(); k++) {
					if (!qaParts.get(j).equals("")
							&& TiRexUtilities.getInstance()
									.levensteinDistanceIsOK(qaParts.get(j),
											kwParts.get(k))) {
						return new OriginalMatchAndStrategy(knowledge, toMatch,
								kwParts.get(k), getInstance());
					}
				}
			}
		}

		return null;
	}

	private int getMaxChunks(String s1, String s2) {
		String[] s1split = s1.split(" ");
		String[] s2split = s2.split(" ");

		return (s1split.length < s2split.length) ? s1split.length
				: s2split.length;
	}

	private List<List<String>> getChunks(String text, int maxChunks) {
		List<List<String>> result = new ArrayList<List<String>>();

		String[] chunks = text.split(" ");

		StringBuffer sb = new StringBuffer("");
		for (int i = 1; i <= maxChunks; i++) {
			List<String> list = new ArrayList<String>();
			for (int j = 0; j < chunks.length; j++) {
				sb = new StringBuffer("");
				for (int k = j; k < j + i && k < chunks.length
						&& j + i < chunks.length + 1; k++) {
					sb.append(((k != j) ? " " : "") + chunks[k]);
				}
				list.add(sb.toString());
			}

			// the lists with the biggest chunks are put at the beginning
			result.add(0, list);
		}

		return result;
	}

	@Override
	public Annotation getAnnotation() {
		return new Annotation("<cross comparison strategy>",
				"</cross comparison strategy>");
	}

	@Override
	public String getName() {
		return "Cross Comparison Strategy";
	}

	@Override
	public double getRating() {
		return 0.9;
	}

	// for testing purposes
	// public static void main(String[] args) {
	// CrossComparisonStrategy strategy = CrossComparisonStrategy
	// .getInstance();
	// String qaText = "Initial costs (in Euro)";
	// String knowledge = "medium initial costs";
	//
	// if (qaText == null) {
	// System.out.println(qaText = null);
	// }
	//
	// int maxChunks = strategy.getMaxChunks(qaText, knowledge);
	// System.out.println(maxChunks);
	//
	// List<List<String>> qaChunks = strategy.getChunks(qaText, maxChunks);
	// // printChunks(qaChunks);
	//
	// List<List<String>> kwChunks = strategy.getChunks(knowledge, maxChunks);
	// // printChunks(kwChunks);
	//
	// for (int i = 0; i < maxChunks; i++) {
	// List<String> qaParts = qaChunks.get(i);
	// List<String> kwParts = kwChunks.get(i);
	//
	// for (int j = 0; j < qaParts.size(); j++) {
	// for (int k = 0; k < kwParts.size(); k++) {
	// if (!qaParts.get(j).equals("")
	// && TiRexUtilities.getInstance()
	// .levensteinDistanceIsOK(qaParts.get(j),
	// kwParts.get(k))) {
	// System.out.println("Knowledge: " + knowledge
	// + ", Match: " + kwParts.get(k));
	// return;
	// }
	// }
	// }
	// }
	// }

	// for testing purposes
	// public static void printChunks(List<List<String>> chunks) {
	// for (List<String> sameSizedChunks : chunks) {
	// for (String chunk : sameSizedChunks) {
	// System.out.println(chunk + "; ");
	// }
	// System.out.println("\n");
	// }
	// System.out.println("\n");
	// }
}
