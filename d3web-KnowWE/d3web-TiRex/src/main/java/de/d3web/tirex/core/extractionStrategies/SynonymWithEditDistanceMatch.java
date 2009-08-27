package de.d3web.tirex.core.extractionStrategies;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.tirex.core.OriginalMatchAndStrategy;
import de.d3web.tirex.core.TiRexSettings;
import de.d3web.tirex.core.TiRexUtilities;

/**
 * This ExtractionStrategy is running through all the words of a given line of
 * text and trying to find a match for any of the words within a set of its
 * synonyms. The match may differ from the word itself by a certain amount of
 * edit operations. The exact allowed edit distance is defined within the
 * TiRexSettings ResourceBundle. If a word is matched, it is wrapped into an
 * OriginalMatchAndStrategy object and returned by the algorithm.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class SynonymWithEditDistanceMatch extends AbstractExtractionStrategy {

	/**
	 * This ExtractionStrategy is implemented as a Singleton.
	 */
	private static ExtractionStrategy instance;

	private SynonymWithEditDistanceMatch() {
		// empty
	}

	public static ExtractionStrategy getInstance() {
		if (instance == null) {
			instance = new SynonymWithEditDistanceMatch();
		}

		return instance;
	}

	@Override
	public OriginalMatchAndStrategy extractKnowledge(IDObject toMatch,
			String knowledge) {
		String qaText = extractQuestionOrAnswerText(toMatch);

		if (qaText != null) {
			String[] words = knowledge.split(" ");

			Map<String, Collection<String>> synonymsMap = null;
			try {
				synonymsMap = TiRexSettings.getInstance().getSynonymsMap();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (synonymsMap != null) {
				Collection<String> synonymSet = synonymsMap.get(qaText);

				if (synonymSet != null) {
					for (String synonym : synonymSet) {
						synonym = synonym.replaceAll("\r", "");

						for (String word : words) {
							if (TiRexUtilities.getInstance()
									.levensteinDistanceIsOK(synonym, word)) {
								return new OriginalMatchAndStrategy(knowledge,
										toMatch, word, getInstance());
							}
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public Annotation getAnnotation() {
		return new Annotation("<font color=#00AAD0><b>", "</b></font>");
	}

	@Override
	public double getRating() {
		return 0.4;
	}

	@Override
	public String getName() {
		return "Synonym with EditDistance Match";
	}
}
