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

import com.wcohen.ss.Levenstein;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.correction.CorrectionProvider;
import de.knowwe.core.correction.DefaultSuggestion;
import de.knowwe.core.correction.Suggestion;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message.Type;

/**
 * A basic correction provider for d3web term references
 * 
 * @author Alex Legler
 * @created 04.03.2011
 */
public class D3webCorrectionProvider implements CorrectionProvider {

	@Override
	public List<Suggestion> getSuggestions(TermCompiler compiler, Section<?> section, int threshold) {
		if (!(section.get() instanceof Term)) {
			return null;
		}

		if (!section.hasErrorInSubtree(compiler)
				&& !section.hasMessageInSubtree(compiler, Type.WARNING)) {
			return null;
		}

		TerminologyManager terminologyHandler = compiler.getTerminologyManager();
		Term termReference = (Term) section.get();

		List<Suggestion> suggestions = new LinkedList<>();
		Identifier termIdentifier = termReference.getTermIdentifier(Sections.cast(section,
				Term.class));
		Collection<Identifier> otherCaseVersions = terminologyHandler.getAllTermsEqualIgnoreCase(
				termIdentifier);
		// if there are multiple different case versions of the term,
		// we only show these
		if (otherCaseVersions.size() > 1) {
			for (Identifier match : otherCaseVersions) {
				if (match.getLastPathElement().equals(termIdentifier.getLastPathElement())) continue;
				suggestions.add(new DefaultSuggestion(match.getLastPathElement(), 0));
			}
			return suggestions;
		}

		Collection<Identifier> localTermMatches = terminologyHandler.getAllDefinedTermsOfType(
				termReference.getTermObjectClass(Sections.cast(section, Term.class)));

		String originalText = section.getText();
		Levenstein l = new Levenstein();

		for (Identifier match : localTermMatches) {
			double score = l.score(originalText, match.getLastPathElement());
			if (score >= -threshold) {
				suggestions.add(new DefaultSuggestion(match.getLastPathElement(),
						(int) score));
			}
		}

		return suggestions;
	}
}
