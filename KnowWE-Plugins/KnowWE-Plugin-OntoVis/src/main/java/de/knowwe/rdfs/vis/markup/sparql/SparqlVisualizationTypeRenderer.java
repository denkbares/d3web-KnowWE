package de.knowwe.rdfs.vis.markup.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.MultiMap;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.core.utils.PackageCompileLinkToTermDefinitionProvider;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdfs.vis.PreRenderWorker;
import de.knowwe.rdfs.vis.markup.PreRenderer;
import de.knowwe.rdfs.vis.util.Utils;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Config;
import de.knowwe.visualization.Edge;
import de.knowwe.visualization.GraphVisualizationRenderer;
import de.knowwe.visualization.SubGraphData;
import de.knowwe.visualization.dot.DOTVisualizationRenderer;

public class SparqlVisualizationTypeRenderer implements Renderer, PreRenderer<GraphVisualizationRenderer> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SparqlVisualizationTypeRenderer.class);

	@Override
	public void render(Section<?> content, UserContext user, RenderResult string) {
		if (user.getParameter("concept") != null) {
			// we have received a concept via url parameter to be visualized
			// hence we need to clear the cached visualization
			PreRenderWorker.getInstance().clearCache(content);
		}
		GraphVisualizationRenderer graphRenderer = getGraphVisualizationRenderer(content, user);
		if (graphRenderer != null) {
			string.appendHtml(graphRenderer.getHTMLIncludeSnipplet());
		}
		else {
			string.appendHtmlElement("span", "No results for this query", "class", "emptySparqlResult");
		}
	}

	public GraphVisualizationRenderer getGraphVisualizationRenderer(Section<?> content, UserContext user) {
		return PreRenderWorker.getInstance()
				.getPreRenderedArtefact(content, user, this);
	}

	private SubGraphData convertToGraph(CachedTupleQueryResult resultSet, Config config, Rdf2GoCore rdfRepository, LinkToTermDefinitionProvider uriProvider, Section<?> section, List<Message> messages) {
		SubGraphData data;

		MultiMap<String, String> subPropertiesMap = Utils.getSubPropertyMap(rdfRepository);
		MultiMap<String, String> inverseRelationsMap = Utils.getInverseRelationsMap(rdfRepository);

		// Get all

		//data = new SubGraphData(subPropertiesMap);
		data = new SubGraphData(subPropertiesMap, inverseRelationsMap);
		//data = new SubGraphData(Utils.getSubPropertyMap(rdfRepository), Utils.getInverseRelationsMap(rdfRepository));

		List<String> variables = resultSet.getBindingNames();
		if (variables.size() < 3) {
			messages.add(Messages.error("A sparqlvis query requires exactly three variables!"));
			return null;
		}

		for (BindingSet row : resultSet) {
			Value subject = row.getValue(variables.get(0));
			Value predicate = row.getValue(variables.get(1));
			Value object = row.getValue(variables.get(2));

			if (subject == null || object == null || predicate == null) {
				LOGGER.warn("Incomplete query result row: " + row);
				continue;
			}

			ConceptNode fromNode = Utils.createValue(config, rdfRepository, uriProvider, section, data, subject, true,
					determineClassType(rdfRepository, variables.get(0), row, subject));

			String relation = Utils.getConceptName(predicate, rdfRepository);
			String relationLabel = Utils.createRelationLabel(config, rdfRepository, predicate, relation);

			ConceptNode toNode = Utils.createValue(config, rdfRepository, uriProvider, section, data, object, true,
					determineClassType(rdfRepository, variables.get(2), row, object));

			Edge newLineRelationsKey = new Edge(fromNode, relationLabel, predicate.stringValue(), toNode);
			data.addEdge(newLineRelationsKey);
		}
		if (data.getConceptDeclarations().isEmpty()) {
			messages.add(Messages.error("The query produced an empty result set!"));
			return null;
		}

		return data;
	}

	private String determineClassType(Rdf2GoCore rdfRepository, String variable, BindingSet row, Value fromURI) {
		// try to determine the clazz/type of the source concept
		String clazz = null;
		if (fromURI instanceof IRI) {
			IRI mostSpecificClass = Rdf2GoUtils.findMostSpecificClass(rdfRepository, rdfRepository.createIRI(fromURI.stringValue()));
			if (mostSpecificClass != null) {
				clazz = Rdf2GoUtils.reduceNamespace(rdfRepository, mostSpecificClass.getNamespace()) + mostSpecificClass
						.getLocalName();
			}
		}
		return clazz;
	}

	@Override
	public GraphVisualizationRenderer preRender(Section<?> content, UserContext user) {
		Section<SparqlVisualizationType> section = Sections.ancestor(content, SparqlVisualizationType.class);
		if (section == null) return null;

		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(user, section);
		if (core == null) return null;

		List<Message> messages = new ArrayList<>();
		Config config = new Config();
		config.setCacheFileID(Utils.getFileID(section, user));

		Messages.clearMessages(section, this.getClass());
		config.init(section, user);

		config.setConcept(Utils.getConceptFromRequest(user));

		if (!Strings.isBlank(config.getColorsProperty())) {
			config.setColors(Utils.createColorCodings(section, config.getColorsProperty(), core));
		}

		LinkToTermDefinitionProvider uriProvider = new PackageCompileLinkToTermDefinitionProvider();

		String sparqlContentRaw = content.getText();

		if (!Strings.isBlank(DefaultMarkupType.getAnnotation(section, SparqlVisualizationType.VIS_TEMPLATE_CLASS))) {
			// this is a sparql visualization template
			Collection<String> concepts = config.getConcepts();
			if (!concepts.isEmpty()) {
				// we have a concept set via url parameter to fill template
				String conceptShortURI = concepts.iterator().next();
				sparqlContentRaw = fillSparqlTemplate(sparqlContentRaw, conceptShortURI);
			}
			else {
				String exampleConcept = DefaultMarkupType.getAnnotation(section, SparqlVisualizationType.VIS_TEMPLATE_EXAMPLE);
				if (Strings.isBlank(exampleConcept)) {
					// we have an incomplete/inconsistent markup definition
				}
				else {
					sparqlContentRaw = fillSparqlTemplate(sparqlContentRaw, exampleConcept);
				}
			}
		}

		// evaluate sparql query and create graph data
		String sparqlString = Rdf2GoUtils.createSparqlString(core, sparqlContentRaw);

		CachedTupleQueryResult resultSet = (CachedTupleQueryResult) core.sparqlSelect(sparqlString, new Rdf2GoCore.Options(config
				.getTimeout()));
		SubGraphData data = convertToGraph(resultSet, config, core, uriProvider, section, messages);

		// if no concept is specified, finally take first guess
		if (data != null && config.getConcepts().isEmpty() && !data.getConceptDeclarations().isEmpty()) {

			// if no center concept has explicitly been specified, take any
			config.setConcept(data.getConceptDeclarations().iterator().next().getName());
		}

		if (data != null && !Thread.currentThread().isInterrupted()) {
			GraphVisualizationRenderer graphRenderer;
			graphRenderer = new DOTVisualizationRenderer(data, config);
			graphRenderer.generateSource();
			return graphRenderer;
		}
		return null;
	}

	private String fillSparqlTemplate(String sparqlContentRaw, String conceptShortURI) {
		return sparqlContentRaw.replace("%1$", conceptShortURI);
	}
}
