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

import tartarus.snowball.ext.englishStemmer;
import de.d3web.core.knowledge.terminology.IDObject;
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
