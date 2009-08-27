package de.d3web.tirex.core.extractionStrategies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.tirex.core.OriginalMatchAndStrategy;

/**
 * This ExtractionStrategy is the simplest one can imagine. A piece of text is
 * searched for an exact match.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class DirectMatch extends AbstractExtractionStrategy {

	/**
	 * This ExtractionStrategy is implemented as a Singleton.
	 */
	private static ExtractionStrategy instance;

	private DirectMatch() {
		// empty
	}

	public static ExtractionStrategy getInstance() {
		if (instance == null) {
			instance = new DirectMatch();
		}

		return instance;
	}

	@Override
	public OriginalMatchAndStrategy extractKnowledge(IDObject toMatch,
			String knowledge) {
		String qaText = extractQuestionOrAnswerText(toMatch);

		if (qaText != null) {
			Pattern p = Pattern.compile(qaText);
			Matcher m = p.matcher(knowledge);

			if (m.find()) {
				return new OriginalMatchAndStrategy(knowledge, toMatch, m
						.group(), getInstance());
			}
		}

		return null;
	}

	@Override
	public Annotation getAnnotation() {
		return new Annotation("<font color=#44FF44><b>", "</b></font>");
	}

	@Override
	public double getRating() {
		return 1.0;
	}

	@Override
	public String getName() {
		return "Direct Match";
	}
}
