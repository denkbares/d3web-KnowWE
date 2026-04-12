package de.knowwe.rdfs.d3web;

import java.util.Collection;

import org.eclipse.rdf4j.model.IRI;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.we.object.D3webTermReference;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class Rdf2GoD3webUtils {

	public static IRI getSessionIdIRI(Rdf2GoCore core, String sessionId) {
		return core.createLocalIRI(Strings.encodeURL(sessionId));
	}

	public static IRI getFactIRI(Rdf2GoCore core) {
		return core.createLocalIRI("Fact");
	}

	public static IRI getHasFactIRI(Rdf2GoCore core) {
		return core.createLocalIRI("hasFact");
	}

	public static IRI getHasValueIRI(Rdf2GoCore core) {
		return core.createLocalIRI("hasValue");
	}

	public static IRI getHasFormattedValueIRI(Rdf2GoCore core) {
		return core.createLocalIRI("hasFormattedValue");
	}

	public static IRI getHasTerminologyObjectIRI(Rdf2GoCore core) {
		return core.createLocalIRI("hasTerminologyObject");
	}

	public static boolean hasParentDashTreeElement(OntologyCompiler compiler, Identifier parentIdentifier) {
		boolean hasParent = false;
		Collection<Section<?>> termDefiningSections = compiler.getTerminologyManager()
				.getTermDefiningSections(parentIdentifier);
		for (Section<?> termDefiningSection : termDefiningSections) {
			Section<? extends DashTreeElement> fatherDashTreeElement = DashTreeUtils.getParentDashTreeElement(termDefiningSection);
			if (fatherDashTreeElement != null) {
				hasParent = true;
				break;
			}
		}
		return hasParent;
	}

	/**
	 * Returns the local (lns) uri used for that term in the compilers the
	 * core.
	 *
	 * @param compiler    the compiler the term is (or will be) registered for
	 * @param termSection the section referencing or defining of the term
	 * @return the uri for the term
	 */
	public static IRI getTermIRI(Rdf2GoCompiler compiler, Section<? extends Term> termSection) {
		return getTermIRI(compiler, termSection.get().getTermIdentifier(compiler, termSection));
	}

	/**
	 * Returns the local (lns) uri used for that term in the compilers the
	 * core.
	 *
	 * @param compiler       the compiler the term is (or will be) registered for
	 * @param termIdentifier the identifier of the term
	 * @return the uri for the term
	 */
	public static IRI getTermIRI(Rdf2GoCompiler compiler, Identifier termIdentifier) {
		String externalForm = Rdf2GoUtils.getCleanedExternalForm(termIdentifier);
		return compiler.getRdf2GoCore().createLocalIRI(externalForm);
	}

	/**
	 * Registers the term definition of the specified defining section into the terminology manager
	 * of the compiler. Both, the lns-uri-identifier and the 'normal' identifier will be registered.
	 * The method also returns the local (lns) uri created for that term using the compilers the
	 * core.
	 *
	 * @param compiler the compiler to register the term definition for
	 * @param section  the section defining the term
	 * @return the lns uri of the registered term
	 */
	public static IRI registerTermDefinition(OntologyCompiler compiler, Section<? extends TermDefinition> section) {
		Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
		Class<?> termClass = section.get().getTermObjectClass(compiler, section);
		return registerTermDefinition(compiler, section, termIdentifier, termClass);
	}

	/**
	 * Registers the term definition of the specified defining section into the terminology manager
	 * of the compiler. Both, the lns-uri-identifier and the 'normal' identifier will be registered.
	 * The method also returns the local (lns) uri created for that term using the compilers the
	 * core.
	 *
	 * @param compiler       the compiler to register the term definition for
	 * @param section        the section defining the term
	 * @param termIdentifier the term identifier
	 * @param termClass      the class to register the object to
	 * @return the lns uri of the registered term
	 */
	public static IRI registerTermDefinition(OntologyCompiler compiler, Section<?> section, Identifier termIdentifier, Class<?> termClass) {
		Identifier lnsIdentifier = getLnsIdentifier(termIdentifier);
		compiler.getTerminologyManager().registerTermDefinition(compiler, section, Resource.class, lnsIdentifier);
		compiler.getTerminologyManager().registerTermDefinition(compiler, section, termClass, termIdentifier);

		return getTermIRI(compiler, termIdentifier);
	}

	public static void registerTermReference(OntologyCompiler compiler, Section<D3webTermReference<NamedObject>> section) {
		Identifier termIdentifier = section.get((namedObjectD3webTermReference, section1) -> namedObjectD3webTermReference
				.getTermIdentifier(compiler, section1));
		compiler.getTerminologyManager()
				.registerTermReference(compiler, section, Resource.class, getLnsIdentifier(termIdentifier));
		compiler.getTerminologyManager()
				.registerTermReference(compiler, section, section.get(
						(D3webTermReference<NamedObject> namedObjectD3webTermReference, Section<D3webTermReference<NamedObject>> section1)
								-> namedObjectD3webTermReference.getTermObjectClass(compiler, section1)), termIdentifier);
	}

	/**
	 * Unregisters the term definitions previously registered by the {#registerTermDefinition} method.
	 *
	 * @param compiler the compiler to unregister the term definition for
	 * @param section  the section originally defining the term
	 */
	public static void unregisterTermDefinition(OntologyCompiler compiler, Section<? extends TermDefinition> section) {
		Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
		Class<?> termClass = section.get().getTermObjectClass(compiler, section);
		unregisterTermDefinition(compiler, section, termIdentifier, termClass);
	}

	public static void unregisterTermReference(OntologyCompiler compiler, Section<D3webTermReference<NamedObject>> section) {
		Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
		compiler.getTerminologyManager()
				.unregisterTermReference(compiler, section, Resource.class, Rdf2GoD3webUtils.getLnsIdentifier(termIdentifier));
		compiler.getTerminologyManager()
				.unregisterTermReference(compiler, section, section.get((D3webTermReference<NamedObject> namedObjectD3webTermReference, Section<D3webTermReference<NamedObject>> section1) -> namedObjectD3webTermReference
						.getTermObjectClass(compiler, section1)), termIdentifier);
	}

	/**
	 * Unregisters the term definitions previously registered by the {#registerTermDefinition} method.
	 *
	 * @param compiler       the compiler to unregister the term definition for
	 * @param section        the section originally defining the term
	 * @param termIdentifier the term identifier
	 * @param termClass      the class the object was registered to
	 */
	public static void unregisterTermDefinition(OntologyCompiler compiler, Section<?> section, Identifier termIdentifier, Class<?> termClass) {
		Identifier lnsIdentifier = Rdf2GoD3webUtils.getLnsIdentifier(termIdentifier);
		compiler.getTerminologyManager().unregisterTermDefinition(
				compiler, section, Resource.class, lnsIdentifier);
		compiler.getTerminologyManager().unregisterTermDefinition(
				compiler, section, termClass, termIdentifier);
	}

	public static Identifier getLnsIdentifier(Identifier termIdentifier) {
		return new Identifier("lns").append(termIdentifier);
	}

}
