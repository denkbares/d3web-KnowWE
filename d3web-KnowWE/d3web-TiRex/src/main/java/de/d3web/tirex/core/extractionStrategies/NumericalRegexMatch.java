package de.d3web.tirex.core.extractionStrategies;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.tirex.core.OriginalMatchAndStrategy;
import de.d3web.tirex.core.TiRexSettings;

/**
 * WARNING: It is not recommended to apply this ExtractionStrategy when trying
 * to extract a Question from a piece of text. If the text contains a String,
 * which can be matched by any of the regular expressions used by this strategy,
 * then it will be returned immediately, though it's most likely not the
 * Question that was to be found.
 * 
 * This ExtractionStrategy is trying to match a substring within a piece of text
 * by applying regular expressions. The regular expressions currently used can
 * extract german figures of speech that indicate certain quantities of
 * something - thus the "Numerical" within the name of the strategy.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class NumericalRegexMatch extends AbstractExtractionStrategy {

	/**
	 * This ExtractionStrategy is implemented as a Singleton.
	 */
	private static ExtractionStrategy instance;

	private NumericalRegexMatch() {
		// empty
	}

	public static ExtractionStrategy getInstance() {
		if (instance == null) {
			instance = new NumericalRegexMatch();
		}

		return instance;
	}

	@Override
	public OriginalMatchAndStrategy extractKnowledge(IDObject toMatch, String knowledge) {
		Map<String, String> regexKnofficePairs = null;
		
			regexKnofficePairs = TiRexSettings.getInstance()
					.getRegexKnofficePairs();
		if(regexKnofficePairs == null) return null;
		Set<String> regexes = regexKnofficePairs.keySet();

		for (String regex : regexes) {
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(knowledge);

			if (m.find()) {
				if (toMatch == null) {
					toMatch = new AnswerChoice();
					((AnswerChoice) toMatch).setText(m.group());
				}
				return new OriginalMatchAndStrategy(knowledge, toMatch, m
						.group(), getInstance());
			}
		}

		return null;
	}

	@Override
	public Annotation getAnnotation() {
		return new Annotation("<font color=#FFDD00><b>", "</b></font>");
	}
	
	@Override
	public double getRating() {
		return 0.5;
	}

	@Override
	public String getName() {
		return "numerical regex match";
	}

}
