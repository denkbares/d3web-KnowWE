package de.d3web.tirex.core.extractionStrategies;

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.tirex.core.OriginalMatchAndStrategy;
import de.d3web.tirex.core.TiRexUtilities;

/**
 * This ExtractionStrategy is trying to match a certain String within a line of
 * text by checking the edit distance between the String and every single word
 * within the text. If the edit distance between the String and any of the words
 * is within predefined boundaries, then the algorithm returns the String.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class EditDistanceMatch extends AbstractExtractionStrategy {

	/**
	 * This ExtractionStrategy is implemented as a Singleton.
	 */
	private static ExtractionStrategy instance;

	private EditDistanceMatch() {
		// empty
	}

	public static ExtractionStrategy getInstance() {
		if (instance == null) {
			instance = new EditDistanceMatch();
		}

		return instance;
	}

	@Override
	public OriginalMatchAndStrategy extractKnowledge(IDObject toMatch,
			String knowledge) {
		String[] words = knowledge.split(" ");

		String qaText = extractQuestionOrAnswerText(toMatch);

		if (qaText != null) {
			Collection<String> chunks = new ArrayList<String>();
			chunks.add(qaText);
			chunks.addAll(TiRexUtilities.getInstance()
					.convertArrayToCollection(qaText.split(" ")));

			for (String chunk : chunks) {
				for (String word : words) {
					if (TiRexUtilities.getInstance().levensteinDistanceIsOK(
							word, chunk)) {
						return new OriginalMatchAndStrategy(knowledge, toMatch,
								word, getInstance());
					}
				}
			}
		}

		return null;
	}

	@Override
	public Annotation getAnnotation() {
		return new Annotation("<font color=#00D000><b>", "</b></font>");
	}

	@Override
	public double getRating() {
		return 0.8;
	}

	@Override
	public String getName() {
		return "Levenstein Match";
	}
}
