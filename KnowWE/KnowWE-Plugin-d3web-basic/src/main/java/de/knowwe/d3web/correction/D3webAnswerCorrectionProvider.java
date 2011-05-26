/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.d3web.correction;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.wcohen.ss.Levenstein;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermReference;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.terminology.TerminologyHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.knowwe.core.correction.CorrectionProvider;


/**
 * A Correction Provider for AnswerReference objects
 * 
 * @author Alex Legler
 * @created 04.03.2011 
 */
public class D3webAnswerCorrectionProvider implements CorrectionProvider {

	@Override
	@SuppressWarnings("unchecked")
	public List<CorrectionProvider.Suggestion> getSuggestions(KnowWEArticle article, Section<?> section, int threshold) {
		if (!(section.get() instanceof TermReference)) {
			return null;
		}

		if (!section.hasErrorInSubtree(article)) {
			return null;
		}

		TerminologyHandler terminologyHandler = KnowWEUtils.getTerminologyHandler(KnowWEEnvironment.DEFAULT_WEB);
		TermReference<?> termReference = ((TermReference<?>) section.get());		

		Collection<String> localTermMatches = terminologyHandler.getAllLocalTermsOfType(
				article.getTitle(),
				termReference.getTermObjectClass()
		);

		String originalText = section.getOriginalText();
		List<CorrectionProvider.Suggestion> suggestions = new LinkedList<CorrectionProvider.Suggestion>();
		Levenstein l = new Levenstein();

		for (String match : localTermMatches) {
			String name;
			String type;

			String[] parts = match.split(" ");

			name = parts.length == 1 ? parts[0] : parts[1];
			type = parts.length == 2 ? parts[0] : null;

			double score = l.score(originalText, name);
			if (score >= -threshold) {
				// Special case: AnswerReference: Also check that the defining Question matches
				AnswerReference answerReference = (AnswerReference) termReference;
				String question = answerReference.getQuestionSection((Section<? extends AnswerReference>) section).getOriginalText(); // TODO: -> getTermName()

				if (!question.equals(type)) {
					continue;
				}

				suggestions.add(new CorrectionProvider.Suggestion(name, (int)score));
			}
		}

		return suggestions;
	}


}
