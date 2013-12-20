package de.knowwe.ontology.edit;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.wcohen.ss.Levenstein;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.correction.CorrectionProvider;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.ontology.kdom.resource.ResourceReference;

public class URITermCorrectionProvider implements CorrectionProvider {

	@Override
	public List<CorrectionProvider.Suggestion> getSuggestions(Article article, Section<?> section, int threshold) {
		Section<ResourceReference> ref = null;

		if (!(section.get() instanceof ResourceReference)) {
			section = Sections.findSuccessor(section,
					ResourceReference.class);
			if (section == null) return null;
		}
		else {
			ref = (Section<ResourceReference>) section;
		}


		TerminologyManager terminologyManager = KnowWEUtils.getTerminologyManager(article);
		if (terminologyManager.isDefinedTerm(ref.get().getTermIdentifier(ref))) {
			// no suggestions if term is correct
			return null;
		}
		Term termReference = ((Term) section.get());

		Collection<Identifier> localTermMatches = terminologyManager.getAllDefinedTermsOfType(
				termReference.getTermObjectClass(Sections.cast(section, Term.class))
				);

		String originalText = ref.get().getTermIdentifier(ref).toExternalForm();
		List<CorrectionProvider.Suggestion> suggestions = new LinkedList<CorrectionProvider.Suggestion>();
		Levenstein l = new Levenstein();

		for (Identifier match : localTermMatches) {
			double score = l.score(originalText, match.toExternalForm());
			if (score >= -threshold) {
				suggestions.add(new CorrectionProvider.Suggestion(match.getLastPathElement(),
						(int) score));
			}
		}

		return suggestions;
	}
}
