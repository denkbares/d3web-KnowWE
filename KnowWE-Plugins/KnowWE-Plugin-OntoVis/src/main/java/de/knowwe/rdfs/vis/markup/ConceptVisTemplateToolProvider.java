package de.knowwe.rdfs.vis.markup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

import com.denkbares.collections.PartialHierarchyTree;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.OntologyUtils;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * @author Dmitrij Kozlov (denkbares GmbH)
 * @created 22.03.17
 */
public class ConceptVisTemplateToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		Section<ConceptVisualizationType> templateSection = findApplicableTemplate(section);
		if (templateSection == null) return new Tool[] {};
		String templateClass = DefaultMarkupType.getAnnotation(templateSection, ConceptVisualizationType.VIS_TEMPLATE_CLASS);
		if (templateClass == null) return new Tool[] {};

		IRI uri = getIRI(section);
		OntologyCompiler compiler = OntologyUtils
				.getOntologyCompiler(section);
		if (compiler == null || uri == null) return new Tool[] {};

		String reducedConceptIRI = Rdf2GoUtils.reduceNamespace(compiler.getRdf2GoCore(), uri.toString());
		String link = KnowWEUtils.getURLLink(templateSection.getTitle());

		link += "&concept=" + Strings.encodeURL(reducedConceptIRI);

		return new Tool[] { new DefaultTool(
				Icon.SHOWTRACE,
//				derp
				"Visualize with ConceptVis template '" + templateClass + "'", "Open ConceptVis template for class " + templateClass,
				link, Tool.ActionType.HREF, Tool.CATEGORY_UTIL) };
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return findApplicableTemplate(section) != null;
	}

	private IRI getIRI(Section<?> section) {
		OntologyCompiler compiler = OntologyUtils
				.getOntologyCompiler(section);
		if (compiler == null) return null;
		if (section.get() instanceof Term) {
			Identifier termIdentifier = ((Term) section.get()).getTermIdentifier(compiler, Sections.cast(section, Term.class));
			Rdf2GoCore core = compiler.getRdf2GoCore();
			return core.createIRI(Rdf2GoUtils.expandNamespace(core, termIdentifier.getPathElementAt(0)), termIdentifier.getPathElementAt(1));
		}
		return null;
	}

	private Section<ConceptVisualizationType> findApplicableTemplate(Section<?> section) {
		Map<String, Section<ConceptVisualizationType>> templates = getClassVisTemplates(section);
		OntologyCompiler compiler = OntologyUtils
				.getOntologyCompiler(section);
		IRI uri = getIRI(section);
		if (uri == null || compiler == null) return null;
		PartialHierarchyTree<IRI> classHierarchy = Rdf2GoUtils.getClassHierarchy(compiler.getRdf2GoCore(), uri);
		for (IRI clazzIRI : classHierarchy.getNodesDFSOrder()) {
			Section<ConceptVisualizationType> conceptVisualizationTypeSection = templates.get(Rdf2GoUtils.reduceNamespace(compiler
					.getRdf2GoCore(), clazzIRI.toString()));
			if (conceptVisualizationTypeSection != null) {
				// found applicable template
				return conceptVisualizationTypeSection;
			}
		}
		return null;
	}

	private Map<String, Section<ConceptVisualizationType>> getClassVisTemplates(Section<?> section) {
		Map<String, Section<ConceptVisualizationType>> visTemplatesForClasses = new HashMap<>();
		Collection<Section<ConceptVisualizationType>> allConceptVisMarkups = Sections.successors(section.getArticleManager(), ConceptVisualizationType.class);
		for (Section<ConceptVisualizationType> conceptVisualizationTypeSection : allConceptVisMarkups) {
			String templateClass = DefaultMarkupType.getAnnotation(conceptVisualizationTypeSection, ConceptVisType.VIS_TEMPLATE_CLASS);
			if (templateClass != null) {
				visTemplatesForClasses.put(templateClass, conceptVisualizationTypeSection);
			}
		}
		return visTemplatesForClasses;
	}
}
