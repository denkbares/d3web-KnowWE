package de.knowwe.ontology.kdom.individual;

import java.util.Collection;
import java.util.List;

import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.vocabulary.OWL;
import org.ontoware.rdf2go.vocabulary.RDF;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceDefinition;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.rdf2go.Rdf2GoCore;

public class AbbreviatedIndividualDefinition extends AbbreviatedResourceDefinition {

	public AbbreviatedIndividualDefinition() {
		this.addSubtreeHandler(new AbbreviatedIndividualHandler());
	}

	private static class AbbreviatedIndividualHandler extends SubtreeHandler<AbbreviatedResourceDefinition> {

		@Override
		public Collection<Message> create(Article article, Section<AbbreviatedResourceDefinition> section) {
			if (section.hasErrorInSubtree()) return Messages.noMessage();

			Rdf2GoCore core = Rdf2GoCore.getInstance(article);

			URI resourceURI = section.get().getResourceURI(core, section);

			Section<DefaultMarkupType> individualMarkup = Sections.findAncestorOfType(section,
					DefaultMarkupType.class);
			List<Section<? extends AnnotationContentType>> contentTypeSections = DefaultMarkupType.getAnnotationContentSections(
					individualMarkup, IndividualType.TYPE_ANNOTATION_NAME);

			if (contentTypeSections.isEmpty()) {
				core.addStatements(core.createStatement(resourceURI, RDF.type, OWL.Thing));
			}
			else {
				for (Section<? extends AnnotationContentType> contentTypeSection : contentTypeSections) {
					Section<AbbreviatedResourceReference> resourceSection = Sections.findChildOfType(
							contentTypeSection, AbbreviatedResourceReference.class);
					if (resourceSection.hasErrorInSubtree()) return Messages.noMessage();
					URI typeURI = resourceSection.get().getResourceURI(core, resourceSection);
					core.addStatements(core.createStatement(resourceURI, RDF.type, typeURI));
				}
			}
			return Messages.noMessage();
		}
	}
}
