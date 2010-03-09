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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.tirex.core.OriginalMatchAndStrategy;
import de.d3web.tirex.core.TiRexSettings;

/**
 * This ExtractionStrategy is running through all the words of a given line of
 * text and trying to find a direct match for any of the words within a set of
 * its synonyms, if synonyms for the word exist within a predefined synonym
 * file. If a word is matched, it is returned by the algorithm.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class SynonymDirectMatch extends AbstractExtractionStrategy {

	/**
	 * This ExtractionStrategy is implemented as a Singleton.
	 */
	private static ExtractionStrategy instance;

	private SynonymDirectMatch() {
		// empty
	}

	public static ExtractionStrategy getInstance() {
		if (instance == null) {
			instance = new SynonymDirectMatch();
		}

		return instance;
	}

	@Override
	public OriginalMatchAndStrategy extractKnowledge(IDObject toMatch,
			String knowledge) {
		String qaText = extractQuestionOrAnswerText(toMatch);

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

					Pattern p = Pattern.compile(synonym);
					Matcher m = p.matcher(knowledge);
					if (m.find()) {
						return new OriginalMatchAndStrategy(knowledge, toMatch,
								m.group(), getInstance());
					}
				}
			}
		}

		return null;
	}

	@Override
	public double getRating() {
		return 0.5;
	}

	@Override
	public Annotation getAnnotation() {
		return new Annotation("<font color=#00D0FF><b>", "</b></font>");
	}

	@Override
	public String getName() {
		return "Synonym Direct Match";
	}

	public static void main(String[] args) {
		Pattern p = Pattern.compile("trains");
		Matcher m = p.matcher("trains endurance coordination");
		if (m.find()) {
			System.out.println(m.group());
		}
	}
}
