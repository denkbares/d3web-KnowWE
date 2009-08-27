package de.d3web.tirex.core.extractionStrategies;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.tirex.core.OriginalMatchAndStrategy;
import de.d3web.tirex.core.TiRexSettings;

/**
 * The abstract parent class of all the ExtractionStrategies implemented for
 * TiRex. It is recommended that all the exctraction strategies are implemented
 * as Singletons.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public abstract class AbstractExtractionStrategy implements ExtractionStrategy {
	/**
	 * @param toMatch
	 *            An IDObject (Question or Answer)
	 * @return The text that the Question or Answer contains.
	 */
	public static String extractQuestionOrAnswerText(IDObject toMatch) {
		if (toMatch == null) {
			return null;
		}

		String qaText = null;
		if (toMatch instanceof NamedObject) {
			qaText = ((NamedObject) toMatch).getText();
		} else if (toMatch instanceof AnswerChoice) {
			qaText = ((AnswerChoice) toMatch).getText();
		}

		if (qaText == null)
			return null;

		qaText = qaText.replaceAll("[*][ ]*", "");
		qaText = qaText.replaceAll(" [(]+.*[)]+", "");
		qaText = qaText.replaceAll("\r", "");

		return qaText;
	}

	public OriginalMatchAndStrategy extract(IDObject toMatch, String knowledge) {
		//knowledge = knowledge.replaceFirst("[*][ ]*", "");
		knowledge = knowledge.replaceAll("\r", "");

		OriginalMatchAndStrategy result = extractKnowledge(toMatch, knowledge);
		if (result != null
				&& isLengthRatioOK(result.getMatch(), result.getIDObject()
						.toString())) {
			return extractKnowledge(toMatch, knowledge);
		}

		return null;
	}

	protected abstract OriginalMatchAndStrategy extractKnowledge(
			IDObject toMatch, String knowledge);

	/**
	 * @param s1
	 *            First String
	 * @param s2
	 *            Second String
	 * @return True, if the ratio of the lengths of s1 and s2 is greater than a
	 *         set value.
	 */
	protected boolean isLengthRatioOK(String s1, String s2) {
		return ((double) s1.length() / (double) s2.length()) >= ((double) TiRexSettings
				.getInstance().getMinimumMatchPercentage()) / 100;
	}

	public String toString() {
		return getName() + "\n";
	}
}
