package de.knowwe.ontology.turtle.lazyRef;

import java.util.Collection;
import java.util.Set;

import org.ontoware.rdf2go.model.node.Node;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.ontology.turtle.TurtleURI;
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
		this.addCompileScript(Priority.LOWER, new LazyURIReferenceHandler(OntologyCompiler.class));
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
		OntologyCompiler c = Compilers.getCompiler(section, OntologyCompiler.class);
		Identifier identifier = (Identifier) section.getSectionStore().getObject(c, IDENTIFIER_KEY);
		if (identifier == null) {
			Collection<Identifier> potentiallyMatchingIdentifiers = getPotentiallyMatchingIdentifiers(c, section);
			if (potentiallyMatchingIdentifiers != null && potentiallyMatchingIdentifiers.size() == 1) {
				identifier = potentiallyMatchingIdentifiers.iterator().next();
				section.getSectionStore().storeObject(c, IDENTIFIER_KEY, identifier);
			}
		}
		return identifier;
	}

	public static Collection<Identifier> getPotentiallyMatchingIdentifiers(de.knowwe.core.compile.Compiler c, Section<?> section) {

		String lazyRefText = section.getText();

		Set<Identifier> potentialMatches = LazyReferenceManager.getInstance().getData(c,
				lazyRefText);

		return potentialMatches;
	}

	private class LazyURIReferenceHandler extends SimpleReferenceRegistrationScript<OntologyCompiler> {

		public LazyURIReferenceHandler(Class<OntologyCompiler> compilerClass) {
			super(compilerClass);
		}

		@Override
		public void compile(OntologyCompiler compiler, Section<Term> section) throws CompilerMessage {
			Collection<Identifier> potentiallyMatchingIdentifiers = getPotentiallyMatchingIdentifiers(
					compiler, section);

			if (potentiallyMatchingIdentifiers == null
					|| potentiallyMatchingIdentifiers.size() == 0) {
				throw CompilerMessage.error("Term '"
						+ section.get().getTermName(section)
						+ "' not found. A term either needs a namespace or to be defined unambiguously.");
			}
			else if (potentiallyMatchingIdentifiers.size() == 1) {
				section.getSectionStore().storeObject(compiler, IDENTIFIER_KEY,
						potentiallyMatchingIdentifiers.iterator().next());
				super.compile(compiler, section);
			}
			else {
				throw CompilerMessage.error("Term '" + section.get().getTermName(section)
						+ "' is ambiguous: " + Strings.concat(", ", potentiallyMatchingIdentifiers));
			}
		}
	}

}
