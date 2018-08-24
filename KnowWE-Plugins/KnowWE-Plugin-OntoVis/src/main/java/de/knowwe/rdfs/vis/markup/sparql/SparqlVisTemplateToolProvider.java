package de.knowwe.rdfs.vis.markup.sparql;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import com.denkbares.collections.PartialHierarchyTree;
import com.denkbares.strings.Identifier;
import com.denkbares.utils.Log;
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
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 29.04.16.
 */
public class SparqlVisTemplateToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		List<Section<SparqlVisualizationType>> templateSections = findApplicableTemplate(section);
		assert templateSections != null;
		if (templateSections.isEmpty()) return new Tool[] {};
		List<Tool> tools = new ArrayList<>();
		for (Section<SparqlVisualizationType> templateSection : templateSections) {
			String templateClass = DefaultMarkupType.getAnnotation(templateSection, SparqlVisualizationType.VIS_TEMPLATE_CLASS);
			if (templateClass == null) return new Tool[] {};

			URI uri = getURI(section);
			OntologyCompiler compiler = OntologyUtils
					.getOntologyCompiler(section);
			if (compiler == null || uri == null) return new Tool[] {};

			String reducedConceptURI = Rdf2GoUtils.reduceNamespace(compiler.getRdf2GoCore(), uri.toString());
			String link = KnowWEUtils.getURLLink(templateSection.getTitle());

			try {
				String conceptParameterAppendix = "&concept=" + URLEncoder.encode(reducedConceptURI, "UTF-8");
				link += conceptParameterAppendix;
			}
			catch (UnsupportedEncodingException e) {
				Log.severe("problem encoding vis template link", e);
			}

			tools.add(new DefaultTool(
					Icon.SHOWTRACE,
					"Visualize with SparqlVis template '" + templateClass + "'", "Open SparqlVis template for class " + templateClass,
					link, Tool.ActionType.HREF, Tool.CATEGORY_UTIL));
		}
		return tools.toArray(new Tool[tools.size()]);
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return findApplicableTemplate(section) != null;
	}

	private URI getURI(Section<?> section) {
		OntologyCompiler compiler = OntologyUtils
				.getOntologyCompiler(section);
		if (compiler == null) return null;
		if (section.get() instanceof Term) {
			Identifier termIdentifier = ((Term) section.get()).getTermIdentifier((Section<? extends Term>) section);
			Rdf2GoCore core = compiler.getRdf2GoCore();
			return core.createURI(Rdf2GoUtils.expandNamespace(core, termIdentifier.getPathElementAt(0)), termIdentifier.getPathElementAt(1));
		}
		return null;
	}

	private List<Section<SparqlVisualizationType>> findApplicableTemplate(Section<?> section) {
		Map<String, Section<SparqlVisualizationType>> templates = getClassVisTemplates(section);
		OntologyCompiler compiler = OntologyUtils
				.getOntologyCompiler(section);
		URI uri = getURI(section);
		if (uri == null || compiler == null) return null;
		PartialHierarchyTree<URI> classHierarchy = Rdf2GoUtils.getClassHierarchy(compiler.getRdf2GoCore(), uri);
		List<Section<SparqlVisualizationType>> possibleVisTemplate = new ArrayList<>();
		for (URI clazzURI : classHierarchy.getNodesDFSOrder()) {
			Section<SparqlVisualizationType> sparqlVisualizationTypeSection = templates.get(Rdf2GoUtils.reduceNamespace(compiler
					.getRdf2GoCore(), clazzURI.toString()));
			if (sparqlVisualizationTypeSection != null) {
				// found applicable template
				possibleVisTemplate.add(sparqlVisualizationTypeSection);
			}
		}
		return possibleVisTemplate;
	}

	private Map<String, Section<SparqlVisualizationType>> getClassVisTemplates(Section<?> section) {
		Map<String, Section<SparqlVisualizationType>> visTemplatesForClasses = new HashMap<>();
		Collection<Section<SparqlVisualizationType>> allSparqlVisMarkups = Sections.successors(section.getArticleManager(), SparqlVisualizationType.class);
		for (Section<SparqlVisualizationType> sparqlVisualizationTypeSection : allSparqlVisMarkups) {
			String templateClass = DefaultMarkupType.getAnnotation(sparqlVisualizationTypeSection, SparqlVisualizationType.VIS_TEMPLATE_CLASS);
			if (templateClass != null) {
				visTemplatesForClasses.put(templateClass, sparqlVisualizationTypeSection);
			}
		}
		return visTemplatesForClasses;
	}
}
