package de.knowwe.ontology.edit;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.wcohen.ss.Levenstein;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.correction.CorrectionProvider;
import de.knowwe.core.correction.DefaultSuggestion;
import de.knowwe.core.correction.Suggestion;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.ontology.kdom.resource.ResourceReference;

public class URITermCorrectionProvider implements CorrectionProvider {

	@SuppressWarnings("unchecked")
	@Override
	public List<Suggestion> getSuggestions(TermCompiler compiler,
			Section<?> section, int threshold) {
		Section<ResourceReference> ref = null;

		if (!(section.get() instanceof ResourceReference)) {
			section = Sections.successor(section, ResourceReference.class);
			if (section == null)
				return null;
		} else {
			ref = (Section<ResourceReference>) section;
		}

		TerminologyManager terminologyManager = compiler
				.getTerminologyManager();

		if(terminologyManager == null) {
			// no terminology manager available, hence no completions are available
			return null;
		}

		Identifier originalTermIdentifier = ref.get().getTermIdentifier(ref);
		if (terminologyManager.isDefinedTerm(originalTermIdentifier)) {
			// no suggestions if term is correct
			return null;
		}
		Term termReference = ((Term) section.get());

		Collection<Identifier> localTermMatches = terminologyManager
				.getAllDefinedTermsOfType(termReference
						.getTermObjectClass(Sections.cast(section, Term.class)));

		String originalText = originalTermIdentifier.toExternalForm();
		List<Suggestion> suggestions = new LinkedList<>();
		Levenstein l = new Levenstein();
		for (Identifier match : localTermMatches) {
			double score = l.score(originalText, match.toExternalForm());
			if (score >= -threshold) {
				suggestions.add(new DefaultSuggestion(match
						.getLastPathElement(), (int) score));
			}/* infix test */
			else if (match.getPathElementAt(0).equals(
					originalTermIdentifier.getPathElementAt(0))
					&& match.getLastPathElement().matches(
							".*" + originalTermIdentifier.getLastPathElement()
									+ ".*")) {

				// prevent trivial infix matches
				if (originalTermIdentifier.getLastPathElement().length() > 4) {
					int infixScore = -1
							* (match.getLastPathElement().length() - originalTermIdentifier
									.getLastPathElement().length());
					suggestions.add(new DefaultSuggestion(match
							.getLastPathElement(), infixScore));

				}
			} else {
				/*
				 * prefix+suffix match 
				 */
				double singleDifferenceScore = singleDifferenceMatchScore(
						match.getLastPathElement(),
						originalTermIdentifier.getLastPathElement());
				if (singleDifferenceScore > 0.6) {
					suggestions.add(new DefaultSuggestion(match
							.getLastPathElement(),
							(int) (singleDifferenceScore * 10)));
				}
			}
		}

		return suggestions;
	}

	private double singleDifferenceMatchScore(String string1, String string2) {
		/*
		 * how long strings match from the beginning
		 */
		int prefixMatchLength = 0;
		for (int i = 0; i < string1.length(); i++) {
			if (i >= string2.length())
				break;
			if (string1.charAt(i) == string2.charAt(i)) {
				prefixMatchLength++;
			} else {
				break;
			}
		}

		/*
		 * how long strings match when starting from the end respectively
		 */
		int suffixMatchLength = 0;
		for (int i = 0; i < string1.length(); i++) {
			if (string2.length() - 1 - i < 0)
				break;
			if (string1.charAt(string1.length() - 1 - i) == string2
					.charAt(string2.length() - 1 - i)) {
				suffixMatchLength++;
			} else {
				break;
			}
		}

		int maxLength = Math.max(string1.length(), string2.length());

		double score = (((double) (prefixMatchLength + suffixMatchLength)) / maxLength);

		return score;
	}

}
