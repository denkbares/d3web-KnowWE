package de.knowwe.ontology.turtle.lazyRef;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.provider.URIProvider;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.ontology.turtle.TurtleURI;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

public class LazyURIReference extends SimpleReference implements URIProvider<LazyURIReference> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LazyURIReference.class);

	private static final String IDENTIFIER_KEY = "identifierKey";

	public LazyURIReference() {
		super(OntologyCompiler.class, Resource.class);
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.setRenderer(StyleRenderer.QUESTION);
		//noinspection unchecked
		this.removeCompileScript(OntologyCompiler.class, SimpleReferenceRegistrationScript.class);
		this.addCompileScript(Priority.LOWER, new LazyIdentifierGenerator());
		this.addCompileScript(Priority.LOWEST, new LazyURIReferenceHandler());
	}

	@Override
	public Value getNode(OntologyCompiler compiler, Section<? extends LazyURIReference> section) {
		Identifier identifier = getTermIdentifier(compiler, section);
		return TurtleURI.getNodeForIdentifier(compiler.getRdf2GoCore(), identifier);
	}

	@Override
	public IRI getIRI(OntologyCompiler core, Section<LazyURIReference> section) {
		return (IRI) getNode(core, section);
	}

	@NotNull
	private Identifier getTermIdentifier(Rdf2GoCompiler compiler, Section<? extends LazyURIReference> section) {
		Identifier identifier = section.getObject(compiler, IDENTIFIER_KEY);
		if (identifier == null) {
			Collection<Identifier> potentiallyMatchingIdentifiers = getPotentiallyMatchingIdentifiers(compiler, section);

			String termName = getTermName(section);
			String message = null;

			if (potentiallyMatchingIdentifiers.isEmpty()) {
				message = "Resource '" + termName + "' not found.";
			}
			else {
				if (potentiallyMatchingIdentifiers.size() == 1
						|| resolveToEqualNS(compiler, potentiallyMatchingIdentifiers)) {
					identifier = potentiallyMatchingIdentifiers.iterator().next();
				}
				else {
					message = "Resource '" + termName + "' is ambiguous: "
							+ Strings.concat(", ", potentiallyMatchingIdentifiers);
				}
			}
			if (identifier == null) {
				// as a fail save we use the lns
				identifier = new Identifier("lns", termName);
			}

			if (message == null) {
				Messages.clearMessages(compiler, section, this.getClass());
			}
			else {
				Messages.storeMessage(compiler, section, this.getClass(), Messages.error(message));
			}
			section.storeObject(compiler, IDENTIFIER_KEY, identifier);
		}
		return identifier;
	}

	private boolean resolveToEqualNS(Rdf2GoCompiler compiler, Collection<Identifier> potentiallyMatchingIdentifiers) {
		Rdf2GoCore rdf2GoCore = compiler.getRdf2GoCore();
		if (potentiallyMatchingIdentifiers.size() == 2) {
			String localNS = rdf2GoCore.getLocalNamespace();
			String defaultNS = rdf2GoCore.getNamespacesMap().get("");
			Iterator<Identifier> iterator = potentiallyMatchingIdentifiers.iterator();
			Identifier firstIdentifier = iterator.next();
			Identifier secondIdentifier = iterator.next();
			String firstNS = firstIdentifier.getPathElementAt(0);
			String secondNS = secondIdentifier.getPathElementAt(0);
			String full = rdf2GoCore.getNamespacesMap().get(firstNS);
			return full != null && full.equals(rdf2GoCore.getNamespacesMap().get(secondNS));
		}
		return false;
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		// we don't want resource to be quoted by interface's default implementation
		return newIdentifier.getLastPathElement();
	}

	@Override
	public Identifier getTermIdentifier(TermCompiler compiler, Section<? extends Term> section) {
		// we just return the first identifier we can find
		// this should only fail if the section is compiled by different compilers and the lazy uri is resolved
		// differently by the compilers
		Map<Compiler, Object> objects = section.getObjects(IDENTIFIER_KEY);
		Set<Identifier> identifiers = new HashSet<>(objects.size());
		for (Object identifier : objects.values()) {
			if (identifier instanceof Identifier) {
				identifiers.add((Identifier) identifier);
			}
		}
		if (identifiers.size() == 1) return identifiers.iterator().next();
		String termName = getTermName(section);
		if (identifiers.size() > 1) {
			LOGGER.warn("Multiple identifier found for " + LazyURIReference.class + " '" + termName + "'");
		}
		if (identifiers.isEmpty()) {
			LOGGER.warn("No identifier found for " + LazyURIReference.class + " '" + termName + "'");
		}
		return new Identifier(termName);
	}

	public static Collection<Identifier> getPotentiallyMatchingIdentifiers(TermCompiler termCompiler, Section<?> section) {

		Section<LazyURIReference> uriReference = Sections.cast(section, LazyURIReference.class);
		String lazyRefText = uriReference.get().getTermName(uriReference);

		return LazyReferenceManager.getInstance().getPotentialMatches(termCompiler, lazyRefText);
	}

	private class LazyURIReferenceHandler extends OntologyCompileScript<LazyURIReference> {

		@Override
		public void compile(OntologyCompiler compiler, Section<LazyURIReference> section) throws CompilerMessage {

			TerminologyManager manager = compiler.getTerminologyManager();
			// we always also register the section as a reference to the name to be able to update if new identifiers with
			// the same name are registered
			manager.registerTermReference(compiler, section, getTermObjectClass(compiler, section), getTermIdentifier(compiler, section));
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<LazyURIReference> section) {
			Identifier identifier = section.removeObject(compiler, IDENTIFIER_KEY);
			compiler.getTerminologyManager().unregisterTermReference(compiler,
					section, getTermObjectClass(compiler, section), identifier);
			compiler.getTerminologyManager().unregisterTermReference(compiler,
					section, getTermObjectClass(compiler, section), new Identifier(getTermName(section)));
		}
	}

	private class LazyIdentifierGenerator extends OntologyCompileScript<LazyURIReference> {
		@Override
		public void compile(OntologyCompiler compiler, Section<LazyURIReference> section) throws CompilerMessage {
			getTermIdentifier(compiler, section); // just make sure, that the identifier is there
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<LazyURIReference> section) {
			// cleanup will be done by LazyURIReferenceHandler
		}
	}
}
