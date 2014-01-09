package de.knowwe.ontology.turtle;

import java.util.Collection;
import java.util.HashSet;

import org.ontoware.rdf2go.model.node.Node;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.OntologyUtils;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCore;

public class LazyURIReference extends SimpleReference implements NodeProvider<LazyURIReference> {

	private static final String IDENTIFIER_KEY = "identifier_key";

	@SuppressWarnings("unchecked")
	public LazyURIReference() {
		super(OntologyCompiler.class, Resource.class);
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.setRenderer(StyleRenderer.Question);
		this.removeCompileScript(OntologyCompiler.class, SimpleReferenceRegistrationScript.class);
		this.addCompileScript(new LazyURIReferenceHandler(OntologyCompiler.class));
	}

	@Override
	public Node getNode(Section<LazyURIReference> section, Rdf2GoCore core) {
		return TurtleURI.getNodeForIdentifier(core, getTermIdentifier(section));
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		return Strings.unquote(section.getText());
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		Identifier identifier = (Identifier) section.getSectionStore().getObject(
				Compilers.getCompiler(section, OntologyCompiler.class),
				IDENTIFIER_KEY);
		return identifier;
	}

	public static Collection<Identifier> getPotentiallyMatchingIdentifiers(Section<?> section) {
		Collection<Identifier> identifiers = new HashSet<Identifier>();
		OntologyCompiler ontologyCompiler = OntologyUtils.getOntologyCompiler(section);
		if (ontologyCompiler != null) {
			TerminologyManager terminologyManager = ontologyCompiler.getTerminologyManager();
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

	private class LazyURIReferenceHandler extends SimpleReferenceRegistrationScript<OntologyCompiler> {

		public LazyURIReferenceHandler(Class<OntologyCompiler> compilerClass) {
			super(compilerClass);
		}

		@Override
		public void compile(OntologyCompiler compiler, Section<Term> section) {
			Collection<Identifier> potentiallyMatchingIdentifiers = getPotentiallyMatchingIdentifiers(section);

			if (potentiallyMatchingIdentifiers.size() == 0) {
				Messages.storeMessage(section, getClass(),
						Messages.error("Term '" + section.get().getTermName(section)
								+ "' is unknown."));
			}
			else if (potentiallyMatchingIdentifiers.size() == 1) {
				section.getSectionStore().storeObject(compiler, IDENTIFIER_KEY,
						potentiallyMatchingIdentifiers.iterator().next());
				super.compile(compiler, section);
			}
			else {
				Messages.storeMessage(section, getClass(),
						Messages.error("Term '" + section.get().getTermName(section)
								+ "' is ambiguous: "
								+ Strings.concat(", ", potentiallyMatchingIdentifiers)));
			}
		}

	}

}
