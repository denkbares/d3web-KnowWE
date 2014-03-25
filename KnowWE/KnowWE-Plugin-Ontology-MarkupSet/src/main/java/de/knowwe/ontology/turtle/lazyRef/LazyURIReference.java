package de.knowwe.ontology.turtle.lazyRef;

import java.util.Collection;

import org.ontoware.rdf2go.model.node.Node;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.renderer.CompositeRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.ontology.turtle.TermDragSurroundingRenderer;
import de.knowwe.ontology.turtle.TurtleURI;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;

public class LazyURIReference extends SimpleReference implements NodeProvider<LazyURIReference> {

	private static final String IDENTIFIER_KEY = "identifierKey";

	public LazyURIReference() {
		super(OntologyCompiler.class, Resource.class);
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.setRenderer(new CompositeRenderer(StyleRenderer.Question, new TermDragSurroundingRenderer()));
		this.removeCompileScript(OntologyCompiler.class, SimpleReferenceRegistrationScript.class);
		this.addCompileScript(new LazyURIReferenceHandler());
	}

	@Override
	public Node getNode(Section<LazyURIReference> section, Rdf2GoCompiler compiler) {
		Identifier identifier = (Identifier) section.getSectionStore().getObject(compiler, IDENTIFIER_KEY);
		if (identifier == null) {
			throw new IllegalStateException("Cannot get identifier before compilation");
		}
		return TurtleURI.getNodeForIdentifier(compiler.getRdf2GoCore(), identifier);
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		return new Identifier(getTermName(section));

	}

	public static Collection<Identifier> getPotentiallyMatchingIdentifiers(TermCompiler termCompiler, Section<?> section) {

		Section<LazyURIReference> uriReference = Sections.cast(section, LazyURIReference.class);
		String lazyRefText = uriReference.get().getTermName(uriReference);

		return LazyReferenceManager.getInstance().getPotentialMatches(termCompiler, lazyRefText);
	}

	private class LazyURIReferenceHandler extends OntologyCompileScript<LazyURIReference> {

		@Override
		public void compile(OntologyCompiler compiler, Section<LazyURIReference> section) throws CompilerMessage {
			Collection<Identifier> potentiallyMatchingIdentifiers = getPotentiallyMatchingIdentifiers(
					compiler, section);

			String termName = getTermName(section);
			Identifier identifier = null;
			String message = null;

			if (potentiallyMatchingIdentifiers.isEmpty()) {
				message = "Resource '" + termName
						+ "' not found. A resource either needs a namespace or to be defined unambiguously.";
			}
			else if (potentiallyMatchingIdentifiers.size() == 1) {
				identifier = potentiallyMatchingIdentifiers.iterator().next();
			}
			else {
				message = "Resource '" + termName
						+ "' is ambiguous: " + Strings.concat(", ", potentiallyMatchingIdentifiers);
			}
			if (identifier == null) {
				// as a fail save we use the lns
				identifier = new Identifier("lns", termName);
			}
			section.getSectionStore().storeObject(compiler, IDENTIFIER_KEY, identifier);

			TerminologyManager manager = compiler.getTerminologyManager();
			// we always also register the section as a reference to the name to be able to update if new identifiers with
			// the same name are registered
			manager.registerTermReference(compiler, section, getTermObjectClass(section), new Identifier(termName));

			if (message == null) {
				// we overwrite existing messages
				throw new CompilerMessage();
			}
			else {
				throw CompilerMessage.error(message);
			}
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<LazyURIReference> section) {
			compiler.getTerminologyManager().unregisterTermReference(compiler,
					section, getTermObjectClass(section), new Identifier(getTermName(section)));
			section.getSectionStore().removeObject(compiler, IDENTIFIER_KEY);
		}
	}

}
