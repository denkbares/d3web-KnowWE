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

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.IDObject;
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
