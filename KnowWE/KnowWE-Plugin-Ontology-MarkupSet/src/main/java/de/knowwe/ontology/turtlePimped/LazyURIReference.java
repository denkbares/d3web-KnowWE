package de.knowwe.ontology.turtlePimped;

import java.util.Collection;
import java.util.HashSet;

import org.ontoware.rdf2go.model.node.Node;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.ontology.turtlePimped.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCore;

public class LazyURIReference extends SimpleReference implements NodeProvider<LazyURIReference> {

	public LazyURIReference() {
		super(TermRegistrationScope.LOCAL, Resource.class);
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.setRenderer(StyleRenderer.Question);
	}

	@Override
	public Node getNode(Section<LazyURIReference> section, Rdf2GoCore core) {
		return TurtleURI.getNodeForIdentifier(core, getIdentifier(section));
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		return Strings.unquote(section.getText());
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		return getIdentifier(section);
	}

	private Identifier getIdentifier(Section<? extends Term> section) {
		if (!(section.get() instanceof LazyURIReference)) return null;

		Collection<Identifier> identifiers = getPotentiallyMatchingIdentifiers(section);
		if (identifiers.size() == 1) {
			return identifiers.iterator().next();
		}
		return null;
	}

	public static Collection<Identifier> getPotentiallyMatchingIdentifiers(Section<?> section) {
		Collection<Identifier> identifiers = new HashSet<Identifier>();
		if (!(section.get() instanceof Term)) return identifiers;
		Collection<Article> compilingArticles = KnowWEUtils.getCompilingArticles(section);
		for (Article article : compilingArticles) {
			TerminologyManager terminologyManager = KnowWEUtils.getTerminologyManager(article);
			Collection<Identifier> allDefinedTerms = terminologyManager.getAllDefinedTerms();
			for (Identifier identifier : allDefinedTerms) {
				String[] pathElements = identifier.getPathElements();
				if (pathElements.length == 2) {
					if (pathElements[1].equals(section.getText())) {
						// found match

						identifiers.add(identifier);
					}
				}
			}
		}
		return identifiers;
	}

}
