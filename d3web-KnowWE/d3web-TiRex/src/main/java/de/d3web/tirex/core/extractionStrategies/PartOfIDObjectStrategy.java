package de.d3web.tirex.core.extractionStrategies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.tirex.core.OriginalMatchAndStrategy;

/**
 * This ExtractionStrategy splits the "knowledge" into single words and checks
 * if any of the words is the part of the question or answer in the "knowledge
 * base".
 * 
 * @author Dmitrij Frese
 * @date 2008/09/15
 */
public class PartOfIDObjectStrategy extends AbstractExtractionStrategy {

	/**
	 * This ExtractionStrategy is implemented as a Singleton.
	 */
	private static ExtractionStrategy instance;

	private PartOfIDObjectStrategy() {
		//empty
	}

	public static ExtractionStrategy getInstance() {
		if (instance == null) {
			instance = new PartOfIDObjectStrategy();
		}

		return instance;
	}

	@Override
	protected OriginalMatchAndStrategy extractKnowledge(IDObject toMatch,
			String knowledge) {
		String qaText = extractQuestionOrAnswerText(toMatch);

		String[] chunks = knowledge.split(" ");

		String match = "";
		for (String chunk : chunks) {
			if (qaText != null) {
				Pattern p = Pattern.compile(chunk, Pattern.LITERAL);
				Matcher m = p.matcher(qaText);
				
				if (m.find() && m.group().length() > match.length()) {
					match = m.group();
				}
			}
		}

		if (!match.equals("")) {
			return new OriginalMatchAndStrategy(knowledge, toMatch, match,
					getInstance());
		}

		return null;
	}

	@Override
	public Annotation getAnnotation() {
		return new Annotation("<part of id object strategy>",
				"</part of id object strategy>");
	}

	@Override
	public String getName() {
		return "Part of IDObject Strategy";
	}

	@Override
	public double getRating() {
		return 0.6;
	}
}
