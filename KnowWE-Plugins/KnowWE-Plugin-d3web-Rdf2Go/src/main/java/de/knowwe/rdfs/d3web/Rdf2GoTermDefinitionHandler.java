package de.knowwe.rdfs.d3web;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.session.values.Unknown;
import de.d3web.we.object.D3webTermDefinition;
import de.knowwe.core.kdom.objects.HasCompilerDependentTermName;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class Rdf2GoTermDefinitionHandler extends OntologyCompileScript<D3webTermDefinition<NamedObject>> {

	@Override
	public void compile(OntologyCompiler compiler, Section<D3webTermDefinition<NamedObject>> section) {

		IRI termIdentifierURI = Rdf2GoD3webUtils.registerTermDefinition(compiler, section);
		Class<?> termObjectClass = section.get().getTermObjectClass(compiler, section);

		Rdf2GoCore core = compiler.getRdf2GoCore();
		List<Statement> statements = new ArrayList<>();

		// lns:TermIdentifier rdf:type lns:TermObjectClass
		Rdf2GoUtils.addStatement(core, termIdentifierURI, RDF.TYPE, termObjectClass.getSimpleName(), statements);
		String termName;
		if (section.get() instanceof HasCompilerDependentTermName) {
			termName = Sections.cast(section, HasCompilerDependentTermName.class).get().getTermName(compiler, section);
		}
		else {
			termName = section.get().getTermName(section);
		}
		Rdf2GoUtils.addStatement(core, termIdentifierURI, RDFS.LABEL, core.createLiteral(termName), statements);

		// make sure to generate some implicit statements for d3web objects that exist, but might not have a section
		// if they do have a question, the statements would be duplicates, but that does not matter for rdf...
		if (Question.class.isAssignableFrom(termObjectClass)) {

			Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);

			createChoiceStatements(compiler, core, statements, termIdentifier, Unknown.getInstance()
					.getValue()
					.toString());

			if (termObjectClass.equals(QuestionYN.class)) {
				createChoiceStatements(compiler, core, statements, termIdentifier, QuestionYN.YES_STRING);
				createChoiceStatements(compiler, core, statements, termIdentifier, QuestionYN.NO_STRING);
			}
		}

		core.addStatements(section, statements);
	}

	public void createChoiceStatements(OntologyCompiler compiler, Rdf2GoCore core, List<Statement> statements, Identifier termIdentifier, String label) {
		IRI choiceIri = Rdf2GoD3webUtils.getTermIRI(compiler, termIdentifier.append(label));
		Rdf2GoUtils.addStatement(compiler.getRdf2GoCore(), choiceIri, RDF.TYPE, Choice.class.getSimpleName(), statements);
		Rdf2GoUtils.addStatement(compiler.getRdf2GoCore(), choiceIri, RDFS.LABEL, core.createLiteral(label), statements);
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<D3webTermDefinition<NamedObject>> section) {
		compiler.getRdf2GoCore().removeStatements(section);
		Rdf2GoD3webUtils.unregisterTermDefinition(compiler, section);
	}
}
