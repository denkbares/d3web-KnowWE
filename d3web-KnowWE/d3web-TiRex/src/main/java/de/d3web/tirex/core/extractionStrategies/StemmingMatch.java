package de.d3web.tirex.core.extractionStrategies;

import java.util.ArrayList;
import java.util.Collection;

import tartarus.snowball.ext.englishStemmer;
import de.d3web.kernel.domainModel.IDObject;
import de.d3web.tirex.core.OriginalMatchAndStrategy;
import de.d3web.tirex.core.TiRexUtilities;

/**
 * An ExtractionStrategy based on stemming. (I use the "snowball" stemmer here).
 * Every single word of the knowledge-String is stemmed and compared to the
 * stemmed words out of the IDObject-text. If an exact match is found, a result
 * is returned.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class StemmingMatch extends AbstractExtractionStrategy {

	/**
	 * This ExtractionStrategy is implemented as a Singleton.
	 */
	private static ExtractionStrategy instance;

	private StemmingMatch() {
		// empty
	}

	public static ExtractionStrategy getInstance() {
		if (instance == null) {
			instance = new StemmingMatch();
		}

		return instance;
	}

	@Override
	public OriginalMatchAndStrategy extractKnowledge(IDObject toMatch,
			String knowledge) {
		String qaText = extractQuestionOrAnswerText(toMatch);

		if (qaText != null) {
			englishStemmer stemmer = new englishStemmer();

			Collection<String> chunks = new ArrayList<String>();
			chunks.add(qaText);
			chunks.addAll(TiRexUtilities.getInstance()
					.convertArrayToCollection(qaText.split(" ")));

			String[] words = knowledge.split(" ");

			for (String word : words) {
				for (String chunk : chunks) {
					stemmer.setCurrent(word);
					stemmer.stem();
					String temp = stemmer.getCurrent();

					stemmer.setCurrent(chunk);
					stemmer.stem();

					if (temp.equals(stemmer.getCurrent())) {
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
		return new Annotation("<font color=#00AA00><b>", "</b></font>");
	}

	@Override
	public double getRating() {
		return 0.6;
	}

	@Override
	public String getName() {
		return "Stemming Match";
	}

}
