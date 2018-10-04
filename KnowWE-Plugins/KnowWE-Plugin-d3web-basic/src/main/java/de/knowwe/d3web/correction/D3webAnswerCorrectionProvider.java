/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.d3web.correction;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.denkbares.strings.Identifier;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.correction.CorrectionProvider;
import de.knowwe.core.correction.DefaultSuggestion;
import de.knowwe.core.correction.Suggestion;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;

/**
 * A Correction Provider for AnswerReference objects
 *
 * @author Alex Legler
 * @created 04.03.2011
 */
public class D3webAnswerCorrectionProvider implements CorrectionProvider {

	@Override
	@SuppressWarnings("unchecked")
	public List<Suggestion> getSuggestions(TermCompiler compiler, Section<?> section, int threshold) {
		if (!(section.get() instanceof Term)) {
			return null;
		}

		if (!section.hasErrorInSubtree(compiler)) {
			return null;
		}

		TerminologyManager terminologyHandler = compiler.getTerminologyManager();
		Term termReference = (Term) section.get();
		Section<AnswerReference> refSec = ((Section<AnswerReference>) section);

		Collection<Identifier> allDefinedLocalTermsOfType = terminologyHandler.getAllDefinedTermsOfType(
				termReference.getTermObjectClass(compiler, refSec));

		String originalText = section.getText();
		List<Suggestion> suggestions = new LinkedList<>();

		for (Identifier matchedIdentifier : allDefinedLocalTermsOfType) {

			AnswerReference answerReference = (AnswerReference) termReference;
			Section<QuestionReference> questionSection = answerReference.getQuestionSection((Section<? extends AnswerReference>) section);
			Identifier question = questionSection.get().getTermIdentifier(compiler, questionSection);
			// Special case: AnswerReference: Also check that the defining
			// Question matches
			if (!(refSec.get().getTermIdentifier(compiler, refSec).startsWith(question))) {
				continue;
			}

			double score = StringUtils.getLevenshteinDistance(originalText, matchedIdentifier.getLastPathElement(), threshold);
			if (score >= 0) {
				suggestions.add(new DefaultSuggestion(matchedIdentifier.getLastPathElement(), (int) score));
			}
		}

		return suggestions;
	}
}
