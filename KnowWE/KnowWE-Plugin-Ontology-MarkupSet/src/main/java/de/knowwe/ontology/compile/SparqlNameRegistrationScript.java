package de.knowwe.ontology.compile;


import de.d3web.strings.Identifier;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.kdom.sparql.SparqlNameDefinition;
import de.knowwe.ontology.sparql.SparqlMarkupType;

/**
 * Register sparql name in terminolgy manager
 * @author Veronika Sehne on 14.05.2014.
 */
public class SparqlNameRegistrationScript extends OntologyCompileScript<SparqlMarkupType> {

	@Override
	public void compile(OntologyCompiler compiler, Section<SparqlMarkupType> section) throws CompilerMessage {
		Identifier nameIdentifier = getIdentifier(section);
		if (nameIdentifier != null) {
			compiler.getTerminologyManager().registerTermDefinition(compiler, section, SparqlMarkupType.class, nameIdentifier);
		}

	}

	private Identifier getIdentifier(Section<SparqlMarkupType> section) {
		String sparqlName = DefaultMarkupType.getAnnotation(section, SparqlMarkupType.NAME);
		if (sparqlName == null) {
			return null;
		}
		else {
			return new Identifier(SparqlNameDefinition.TERM_PREFIX, sparqlName);
		}
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<SparqlMarkupType> section) {
		Identifier nameIdentifier = getIdentifier(section);
		if (nameIdentifier != null) {
			compiler.getTerminologyManager()
					.unregisterTermDefinition(compiler, section, SparqlMarkupType.class, getIdentifier(section));
		}
	}
}
