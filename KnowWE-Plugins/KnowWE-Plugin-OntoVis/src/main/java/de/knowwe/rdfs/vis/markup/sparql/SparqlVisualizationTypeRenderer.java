package de.knowwe.rdfs.vis.markup.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
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
import de.knowwe.visualization.d3.D3VisualizationRenderer;
import de.knowwe.visualization.dot.DOTVisualizationRenderer;

public class SparqlVisualizationTypeRenderer implements Renderer, PreRenderer {

	@Override
	public void render(Section<?> content, UserContext user, RenderResult string) {
		if(user.getParameter("concept") != null) {
			// we have received a concept via url parameter to be visualized
			// hence we need to clear the cached visualization
			PreRenderWorker.getInstance().clearCache(content);
		}
		PreRenderWorker.getInstance().handlePreRendering(content, user, this);
		GraphVisualizationRenderer graphRenderer = (GraphVisualizationRenderer) content.getObject(getKey());
		if (graphRenderer != null) string.appendHtml(graphRenderer.getHTMLIncludeSnipplet());
	}

	protected String getKey() {
		return this.getClass().getName();
	}

	private SubGraphData convertToGraph(QueryResultTable resultSet, Config config, Rdf2GoCore rdfRepository, LinkToTermDefinitionProvider uriProvider, Section<?> section, List<Message> messages) {
		SubGraphData data = new SubGraphData();
		List<String> variables = resultSet.getVariables();
		if (variables.size() < 3) {
			messages.add(new Message(Message.Type.ERROR, "A sparqlvis query requires exactly three variables!"));
			return null;
		}
		for (QueryRow row : resultSet) {
			Node fromURI = row.getValue(variables.get(0));
			Node relationURI = row.getValue(variables.get(1));
			Node toURI = row.getValue(variables.get(2));

			if (fromURI == null || toURI == null || relationURI == null) {
				Log.warning("Incomplete query result row: " + row.toString());
				continue;
			}

			ConceptNode fromNode = Utils.createNode(config, rdfRepository, uriProvider, section, data, fromURI, true);
			String relation = Utils.getConceptName(relationURI, rdfRepository);
			ConceptNode toNode = Utils.createNode(config, rdfRepository, uriProvider, section,
					data, toURI, true);
			String relationLabel = Utils.createRelationLabel(config, rdfRepository, relationURI,
					relation);
			Edge newLineRelationsKey = new Edge(fromNode, relationLabel, toNode);
			data.addEdge(newLineRelationsKey);
		}
		if (data.getConceptDeclarations().size() == 0) {
			messages.add(new Message(Message.Type.ERROR, "The query produced an empty result set!"));
			return null;
		}
		return data;
	}

	@Override
	public void preRender(Section<?> content, UserContext user) {
		Section<SparqlVisualizationType> section = Sections.ancestor(content, SparqlVisualizationType.class);
		if (section == null) return;

		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(section);
		if (core == null) return;

		List<Message> messages = new ArrayList<>();
		Config config = new Config();
		config.setCacheFileID(getCacheFileID(section));
		config.readFromSection(section);

		Utils.getConceptFromRequest(user, config);

		if (!Strings.isBlank(config.getColors())) {
			config.setRelationColors(Utils.createColorCodings(config.getColors(), core, "rdf:Property"));
			config.setClassColors(Utils.createColorCodings(config.getColors(), core, "rdfs:Class"));
		}

		LinkToTermDefinitionProvider uriProvider = new PackageCompileLinkToTermDefinitionProvider();

		String sparqlContentRaw = content.getText();

		if(!Strings.isBlank(DefaultMarkupType.getAnnotation(section, SparqlVisualizationType.VIS_TEMPLATE_CLASS)))  {
			// this is a sparql visualization template
			Collection<String> concepts = config.getConcepts();
			if(concepts.size() > 0 ) {
				// we have a concept set via url parameter to fill template
				String conceptShortURI = concepts.iterator().next();
				sparqlContentRaw = fillSparqlTemplate(sparqlContentRaw, conceptShortURI);
			} else {
				String exampleConcept = DefaultMarkupType.getAnnotation(section, SparqlVisualizationType.VIS_TEMPLATE_EXAMPLE);
				if(Strings.isBlank(exampleConcept)) {
					// we have an incomplete/inconsistent markup definition
				} else {
					sparqlContentRaw = fillSparqlTemplate(sparqlContentRaw, exampleConcept);
				}
			}
		}

		// evaluate sparql query and create graph data
		String sparqlString = Rdf2GoUtils.createSparqlString(core, sparqlContentRaw);

		QueryResultTable resultSet = core.sparqlSelect(sparqlString);
		SubGraphData data = convertToGraph(resultSet, config, core, uriProvider, section, messages);

		// if no concept is specified, finally take first guess
		if (data != null && config.getConcepts().isEmpty() && data.getConceptDeclarations().size() > 0) {

			// if no center concept has explicitly been specified, take any
			config.setConcept(data.getConceptDeclarations().iterator().next().getName());
		}

		if (data != null && !Thread.currentThread().isInterrupted()) {
			GraphVisualizationRenderer graphRenderer;
			if (config.getRenderer() == Config.Renderer.D3) {
				graphRenderer = new D3VisualizationRenderer(data, config);
			}
			else {
				graphRenderer = new DOTVisualizationRenderer(data, config);
			}

			graphRenderer.generateSource();

			content.storeObject(getKey(), graphRenderer);
		}

	}

	private String fillSparqlTemplate(String sparqlContentRaw, String conceptShortURI) {
		return sparqlContentRaw.replace("%1$", conceptShortURI);
	}

	@Override
	public void cleanUp(Section<?> section) {
		GraphVisualizationRenderer graphRenderer = (GraphVisualizationRenderer) section.getObject(getKey());
		if (graphRenderer != null) graphRenderer.cleanUp();
	}

}
