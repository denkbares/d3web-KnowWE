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

import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.TerminologyHandler;
import de.knowwe.core.correction.CorrectionProvider;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;


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
		Section<AnswerReference> refSec = ((Section<AnswerReference>)section);

		Collection<Section<? extends TermDefinition>> localTermMatches = terminologyHandler.getAllLocalTermsDefsOfType(
				article.getTitle(),
				termReference.getTermObjectClass()
		);

		String originalText = section.getOriginalText();
		List<CorrectionProvider.Suggestion> suggestions = new LinkedList<CorrectionProvider.Suggestion>();
		Levenstein l = new Levenstein();

		for (Section<? extends TermDefinition> match : localTermMatches) {

			AnswerReference answerReference = (AnswerReference) termReference;
			Section<QuestionReference> questionSection = answerReference.getQuestionSection((Section<? extends AnswerReference>) section);
			String question = questionSection.get().getTermIdentifier(questionSection);
			// Special case: AnswerReference: Also check that the defining Question matches
			if (! (refSec.get().getTermIdentifier(refSec).startsWith(question))) {
				continue;
			}
			
			double score = l.score(originalText, match.get().getTermName(match));
			if (score >= -threshold) {
				

				suggestions.add(new CorrectionProvider.Suggestion(match.get().getTermName(match), (int)score));
			}
		}

		return suggestions;
	}


}
