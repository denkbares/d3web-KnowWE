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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import de.d3web.core.knowledge.terminology.IDObject;
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
