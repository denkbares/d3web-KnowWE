package de.knowwe.ontology.kdom.individual;

import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceDefinition;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.rdf2go.Rdf2GoCore;

public class AbbreviatedIndividualDefinition extends AbbreviatedResourceDefinition {

	public AbbreviatedIndividualDefinition() {
		this.addCompileScript(new AbbreviatedIndividualHandler());
	}

	private static class AbbreviatedIndividualHandler extends OntologyHandler<AbbreviatedResourceDefinition> {

		@Override
		public Collection<Message> create(OntologyCompiler compiler, Section<AbbreviatedResourceDefinition> section) {

			Rdf2GoCore core = Rdf2GoCore.getInstance(compiler);

			IRI resourceURI = section.get().getResourceURI(core, section);

			Section<DefaultMarkupType> individualMarkup = Sections.ancestor(section,
					DefaultMarkupType.class);
			List<Section<? extends AnnotationContentType>> contentTypeSections = DefaultMarkupType.getAnnotationContentSections(
					individualMarkup, IndividualType.TYPE_ANNOTATION_NAME);

			if (contentTypeSections.isEmpty()) {
				core.addStatements(section, core.createStatement(resourceURI, RDF.TYPE, OWL.THING));
			}
			else {
				for (Section<? extends AnnotationContentType> contentTypeSection : contentTypeSections) {
					Section<AbbreviatedResourceReference> resourceSection = Sections.child(
							contentTypeSection, AbbreviatedResourceReference.class);
					if (resourceSection.hasErrorInSubtree()) return Messages.noMessage();
					IRI typeURI = resourceSection.get().getResourceIRI(core, resourceSection);
					core.addStatements(section,
							core.createStatement(resourceURI, RDF.TYPE, typeURI));
				}
			}
			return Messages.noMessage();
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<AbbreviatedResourceDefinition> section) {
			Rdf2GoCore.getInstance(compiler).removeStatements(section);
		}
	}
}
