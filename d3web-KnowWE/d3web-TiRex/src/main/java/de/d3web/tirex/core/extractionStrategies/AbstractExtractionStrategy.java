/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.tirex.core.extractionStrategies;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.session.values.AnswerChoice;
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
			qaText = ((NamedObject) toMatch).getName();
		} else if (toMatch instanceof AnswerChoice) {
			qaText = ((AnswerChoice) toMatch).getName();
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
		return ((double) s1.length() / (double) s2.length()) >= (TiRexSettings
				.getInstance().getMinimumMatchPercentage()) / 100;
	}

	@Override
	public String toString() {
		return getName() + "\n";
	}
}
