/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.tirex.core.extractionStrategies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.core.knowledge.terminology.IDObject;
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
