package de.knowwe.rdfs.vis.markup.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.core.utils.PackageCompileLinkToTermDefinitionProvider;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdfs.vis.OntoGraphDataBuilder;
import de.knowwe.rdfs.vis.PreRenderWorker;
import de.knowwe.rdfs.vis.markup.ConceptVisualizationType;
import de.knowwe.rdfs.vis.markup.PreRenderer;
import de.knowwe.rdfs.vis.markup.VisConfigType;
import de.knowwe.rdfs.vis.util.Utils;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Edge;
import de.knowwe.visualization.GraphDataBuilder;
import de.knowwe.visualization.GraphVisualizationRenderer;
import de.knowwe.visualization.SubGraphData;
import de.knowwe.visualization.d3.D3VisualizationRenderer;
import de.knowwe.visualization.dot.DOTVisualizationRenderer;
import de.knowwe.visualization.util.FileUtils;

public class SparqlVisualizationTypeRenderer implements Renderer, PreRenderer {

	private static final boolean SHOW_LABEL_FOR_URI = false;

	private Rdf2GoCore core;

	private String format;

	@Override
	public void render(Section<?> content, UserContext user, RenderResult string) {
		PreRenderWorker.getInstance().preRenderSectionAndWait(this, content, user, string);
	}

	@Override
	public void cacheGraph(Section<?> content, RenderResult string) {
		Section<SparqlVisualizationType> section = Sections.ancestor(content,
				SparqlVisualizationType.class);

		List<Message> messages = new ArrayList<Message>();
		Map<String, String> parameterMap = new HashMap<>();
		setFileID(section, parameterMap);

		String format = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_FORMAT);
		if (format != null) {
			format = format.toLowerCase();
			this.format = format;
			parameterMap.put(OntoGraphDataBuilder.FORMAT, format);
		}

		createGraphAndAppendHTMLIncludeSnipplet(string, new SubGraphData(), parameterMap, messages);
	}

	private void createGraphAndAppendHTMLIncludeSnipplet(RenderResult string, SubGraphData data, Map<String, String> parameterMap, List<Message> messages) {
		String renderedContent = "";

		if (data != null && !Thread.currentThread().isInterrupted()) {

			// current default source renderer is DOT
			GraphVisualizationRenderer graphRenderer = new DOTVisualizationRenderer(data,
					parameterMap);
			String renderer = parameterMap.get(OntoGraphDataBuilder.RENDERER);
			if (renderer != null && renderer.equals(GraphDataBuilder.Renderer.d3.name())) {
				graphRenderer = new D3VisualizationRenderer(data, parameterMap);
			}

			// re-use graph if possible
			if (!FileUtils.filesAlreadyRendered(graphRenderer.getGraphFilePath())) {
				graphRenderer.generateSource();
			}
			renderedContent = graphRenderer.getHTMLIncludeSnipplet();

		}
		if (messages.size() > 0 && string != null) {
			DefaultMarkupRenderer.renderMessagesOfType(Message.Type.WARNING, messages,
					string);
		}

		if (string != null && !Thread.currentThread().isInterrupted()) {
			string.appendHtml(renderedContent);
		}
	}

	private void setFileID(Section<?> section, Map<String, String> parameterMap) {

		String fileID = Utils.getFileID(section);
		if (fileID == null) return;

		parameterMap.put(OntoGraphDataBuilder.FILE_ID, fileID);
	}

	private SubGraphData convertToGraph(QueryResultTable resultSet, Map<String, String> parameters, Rdf2GoCore rdfRepository, LinkToTermDefinitionProvider uriProvider, Section<?> section, List<Message> messages) {
		SubGraphData data = new SubGraphData();
		List<String> variables = resultSet.getVariables();
		if (variables.size() < 3) {
			Message m = new Message(Message.Type.ERROR,
					"A sparqlvis query requires exactly three variables!");
			messages.add(m);
			return null;
		}
		for (QueryRow row : resultSet) {

			Node fromURI = row.getValue(variables.get(0));

			Node relationURI = row.getValue(variables.get(1));

			Node toURI = row.getValue(variables.get(2));

			if (fromURI == null || toURI == null || relationURI == null) {
				Log.warning("incomplete query result row: " + row.toString());
				continue;
			}

			ConceptNode fromNode = Utils.createNode(parameters, rdfRepository, uriProvider,
					section, data, fromURI, true);
			String relation = Utils.getConceptName(relationURI, rdfRepository);

			ConceptNode toNode = Utils.createNode(parameters, rdfRepository, uriProvider, section,
					data, toURI, true);

			String relationLabel = Utils.createRelationLabel(parameters, rdfRepository, relationURI,
					relation);

			Edge newLineRelationsKey = new Edge(fromNode, relationLabel, toNode);

			data.addEdge(newLineRelationsKey);

		}
		if (data.getConceptDeclarations().size() == 0) {
			Message m = new Message(Message.Type.ERROR,
					"The query produced an empty result set!");
			messages.add(m);
			return null;
		}
		return data;
	}

	private String getMaster(Section<?> section) {
		return SparqlVisualizationType.getAnnotation(section,
				PackageManager.MASTER_ATTRIBUTE_NAME);
	}

	/**
	 * @created 13.07.2014
	 */
	private void findAndReadConfig(String configName, ArticleManager am, Map<String, String> parameterMap, List<Message> messages, RenderResult string) {
		Collection<Section<VisConfigType>> sections = Sections.successors(am, VisConfigType.class);
		for (Section<VisConfigType> section : sections) {
			String name = VisConfigType.getAnnotation(section, VisConfigType.ANNOTATION_NAME);
			if (name.equals(configName)) {
				readConfig(section, parameterMap, messages, string);
			}
		}
	}

	/**
	 * @created 13.07.2014
	 */
	private void readConfig(Section<VisConfigType> section, Map<String, String> parameterMap, List<Message> messages, RenderResult string) {
		// set css layout to be used
		String layout = VisConfigType.getAnnotation(section, SparqlVisualizationType.ANNOTATION_DESIGN);
		if (layout != null) {

			String cssText = null;

			ArticleManager articleManager = Environment.getInstance().getArticleManager(
					Environment.DEFAULT_WEB);
			Collection<Article> articles = articleManager.getArticles();

			for (Article article : articles) {
				Section<RootType> rootSection = article.getRootSection();
				// search layouttypes
				List<Section<SparqlVisDesignType>> sparqlVisDesignSections = Sections.successors(
						rootSection, SparqlVisDesignType.class);

				for (Section<SparqlVisDesignType> currentSection : sparqlVisDesignSections) {

					String currentLayout = SparqlVisDesignType.getAnnotation(currentSection,
							SparqlVisDesignType.ANNOTATION_NAME);
					if (currentLayout.equals(layout)) {

						cssText = SparqlVisDesignType.getContentSection(currentSection).getText();

					}

				}
			}

			if (cssText != null) {

				parameterMap.put(OntoGraphDataBuilder.D3_FORCE_VISUALISATION_STYLE, cssText);
			} else {
				Message noSuchLayout = new Message(Message.Type.WARNING,
						"No such layout " + layout + " found!");
				Collection<Message> warnings = new HashSet<Message>();
				messages.add(noSuchLayout);
				if (string != null) {
					DefaultMarkupRenderer.renderMessagesOfType(Message.Type.WARNING, warnings,
							string);
				}

			}

		}

		// size
		parameterMap.put(OntoGraphDataBuilder.GRAPH_SIZE, VisConfigType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_SIZE));

		parameterMap.put(OntoGraphDataBuilder.GRAPH_HEIGHT, VisConfigType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_HEIGHT));

		parameterMap.put(OntoGraphDataBuilder.GRAPH_WIDTH, VisConfigType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_WIDTH));

		// format
		format = VisConfigType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_FORMAT);
		if (format != null) {
			format = format.toLowerCase();
		}else {
			format = "svg";
		}
		parameterMap.put(OntoGraphDataBuilder.FORMAT, format);

		// dotApp
		String dotApp = VisConfigType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_DOT_APP);
		parameterMap.put(OntoGraphDataBuilder.DOT_APP, dotApp);

		// renderer
		String rendererType = VisConfigType.getAnnotation(section, SparqlVisualizationType.ANNOTATION_RENDERER);
		parameterMap.put(OntoGraphDataBuilder.RENDERER, rendererType);

		// visualization
		String visualization = VisConfigType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_VISUALIZATION);
		parameterMap.put(OntoGraphDataBuilder.VISUALIZATION, visualization);

		// master
		String master = VisConfigType.getAnnotation(section,
				PackageManager.MASTER_ATTRIBUTE_NAME);
		if (master != null) {
			parameterMap.put(OntoGraphDataBuilder.MASTER, master);
		}

		// language
		String lang = VisConfigType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_LANGUAGE);
		if (lang != null) {
			parameterMap.put(OntoGraphDataBuilder.LANGUAGE, lang);
		}

		// labels
		String labelValue = VisConfigType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_LABELS);
		parameterMap.put(OntoGraphDataBuilder.USE_LABELS, labelValue);

		// rank direction
		String rankDir = VisConfigType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_RANK_DIR);
		parameterMap.put(OntoGraphDataBuilder.RANK_DIRECTION, rankDir);

		// link mode
		String linkModeValue = VisConfigType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_LINK_MODE);
		if (linkModeValue == null) {
			// default link mode is 'jump'
			linkModeValue = SparqlVisualizationType.LinkMode.jump.name();
		}
		parameterMap.put(OntoGraphDataBuilder.LINK_MODE, linkModeValue);

		// add to dot
		String dotAppPrefix = VisConfigType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_DOT_APP);
		if (dotAppPrefix != null) {
			parameterMap.put(OntoGraphDataBuilder.ADD_TO_DOT, dotAppPrefix + "\n");
		}

		// showInverse flag
		String inverseFlag = VisConfigType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_SHOWINVERSE);
		if(inverseFlag != null) {
			parameterMap.put(OntoGraphDataBuilder.SHOW_INVERSE, inverseFlag);
		}


		// colors
		String colorRelationName = VisConfigType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_COLORS);

		if (!Strings.isBlank(colorRelationName)) {
			parameterMap.put(OntoGraphDataBuilder.RELATION_COLOR_CODES, Utils.createColorCodings(colorRelationName, core, "rdf:Property"));
			parameterMap.put(OntoGraphDataBuilder.CLASS_COLOR_CODES, Utils.createColorCodings(colorRelationName, core, "rdfs:Class"));
		}
	}

	@Override
	public void preRender(Section<?> content, UserContext user, RenderResult string) {
		Section<SparqlVisualizationType> section = Sections.ancestor(content,
				SparqlVisualizationType.class);
		Section<DefaultMarkupType> defMarkupSection = Sections.cast(section,
				DefaultMarkupType.class);

		core = Rdf2GoUtils.getRdf2GoCore(defMarkupSection);
		if (core == null && string != null) {
			string.appendHtmlElement("div", "");
			return;
		}

		List<Message> messages = new ArrayList<Message>();

		String realPath = getRealPath(user);
        if (realPath == null) return; // at wiki startup only

		Map<String, String> parameterMap = new HashMap<String, String>();

		if (Thread.currentThread().isInterrupted()) return;

		// find and read config file if defined
		String configName = SparqlVisualizationType.getAnnotation(section, SparqlVisualizationType.ANNOTATION_CONFIG);

		if (configName != null) {
			findAndReadConfig(configName.trim(), section.getArticleManager(), parameterMap, messages, string);
		}

		
		setCSSLayoutConfig(string, section, messages, parameterMap);


		parameterMap.put(OntoGraphDataBuilder.REAL_PATH, realPath);

		parameterMap.put(OntoGraphDataBuilder.SECTION_ID, section.getID());

		// set panel size
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_SIZE, section, OntoGraphDataBuilder.GRAPH_SIZE, parameterMap);

        // set height
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_HEIGHT, section, OntoGraphDataBuilder.GRAPH_HEIGHT, parameterMap);

		// set width
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_WIDTH, section, OntoGraphDataBuilder.GRAPH_WIDTH, parameterMap);

		// set format (png/svg)
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_FORMAT, section, OntoGraphDataBuilder.FORMAT, parameterMap);

		// additional dot source code
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_DOT_APP, section, OntoGraphDataBuilder.DOT_APP, parameterMap);

		// set rank direction of graph layout
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_RANK_DIR, section, OntoGraphDataBuilder.RANK_DIRECTION, parameterMap);

		// set color codings if existing
		String colorRelationName = SparqlVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_COLORS);
		if (!Strings.isBlank(colorRelationName)) {
			parameterMap.put(OntoGraphDataBuilder.RELATION_COLOR_CODES, Utils.createColorCodings(colorRelationName, core, "rdf:Property"));
			parameterMap.put(OntoGraphDataBuilder.CLASS_COLOR_CODES, Utils.createColorCodings(colorRelationName, core, "rdfs:Class"));
		}

		// set flag for use of labels
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_LABELS, section, OntoGraphDataBuilder.USE_LABELS, parameterMap);

		// set renderer
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_RENDERER, section, OntoGraphDataBuilder.RENDERER, parameterMap);

		// set visualization
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_VISUALIZATION, section, OntoGraphDataBuilder.VISUALIZATION, parameterMap);

		// set master
		String master = getMaster(section);
		if (master != null) {
			parameterMap.put(OntoGraphDataBuilder.MASTER, master);
		}

		// set language
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_LANGUAGE, section, OntoGraphDataBuilder.LANGUAGE, parameterMap);

		// set link mode
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_LINK_MODE, section, OntoGraphDataBuilder.LINK_MODE, parameterMap, SparqlVisualizationType.LinkMode.jump
				.name());

		String addToDOT = "";
		List<Section<? extends AnnotationContentType>> annotationSections =
				DefaultMarkupType.getAnnotationContentSections(section,
						SparqlVisualizationType.ANNOTATION_ADD_TO_DOT);
		for (Section<? extends AnnotationContentType> anno : annotationSections) {
			if (anno != null) addToDOT += anno.getText() + "\n";
		}
		if (!addToDOT.equals("")) {
			String alreadyAdded = parameterMap.get(OntoGraphDataBuilder.ADD_TO_DOT);
			if (alreadyAdded != null) {
				addToDOT = addToDOT + alreadyAdded;
			}
			parameterMap.put(OntoGraphDataBuilder.ADD_TO_DOT, addToDOT);
		}

		if (Thread.currentThread().isInterrupted()) return;

		LinkToTermDefinitionProvider uriProvider;
		String globalAnnotation = DefaultMarkupType.getAnnotation(section, Rdf2GoCore.GLOBAL);
		if (globalAnnotation != null && globalAnnotation.equals("true")) {
			// TODO: completely remove dependency to IncrementalCompiler
			try {
				uriProvider = (LinkToTermDefinitionProvider) Class.forName(
						"de.knowwe.compile.utils.IncrementalCompilerLinkToTermDefinitionProvider")
						.newInstance();
			}
			catch (Exception e) {
				uriProvider = new LinkToTermDefinitionProvider() {
					@Override
					public String getLinkToTermDefinition(Identifier name, String masterArticle) {
						return null;
					}
				};
			}
		} else {
			uriProvider = new PackageCompileLinkToTermDefinitionProvider();
		}

		// evaluate sparql query and create graph data
		String sparqlString = Rdf2GoUtils.createSparqlString(core, content.getText());

		QueryResultTable resultSet = core.sparqlSelect(
				sparqlString);
		SubGraphData data = convertToGraph(resultSet, parameterMap, core, uriProvider,
				section, messages);

		// read passed concept parameter from browser url if existing
		String conceptName = null;
		if (user != null) {
			conceptName = user.getParameter("concept");
		}

		// otherwise use annotation value
		if (conceptName == null) {
			conceptName = ConceptVisualizationType.getAnnotation(section, ConceptVisualizationType.ANNOTATION_CONCEPT);
		}

		// if no concept is specified, finally take first guess
		if (data != null && conceptName == null && data.getConceptDeclarations().size() > 0) {

			// if no center concept has explicitly been specified, take any
			conceptName = data.getConceptDeclarations().iterator().next().getName();
		}
		parameterMap.put(OntoGraphDataBuilder.CONCEPT, conceptName);

		// create file ID
		setFileID(section, parameterMap);

		// render content
		String renderedContent = "";

		if (data != null && !Thread.currentThread().isInterrupted()) {

			// current default source renderer is DOT
			GraphVisualizationRenderer graphRenderer = new DOTVisualizationRenderer(data,
					parameterMap);
			String renderer = parameterMap.get(OntoGraphDataBuilder.RENDERER);
			if (renderer != null && renderer.equals(GraphDataBuilder.Renderer.d3.name())) {
				graphRenderer = new D3VisualizationRenderer(data, parameterMap);
			}

			// re-use graph if possible
			if (!FileUtils.filesAlreadyRendered(graphRenderer.getGraphFilePath())) {
				graphRenderer.generateSource();
			}
			renderedContent = graphRenderer.getHTMLIncludeSnipplet();

		}
		if (messages.size() > 0 && string != null) {
			DefaultMarkupRenderer.renderMessagesOfType(Message.Type.WARNING, messages,
					string);
		}

		if (string != null && !Thread.currentThread().isInterrupted()) {
			string.appendHtml(renderedContent);
		}
	}

	private void setCSSLayoutConfig(RenderResult string, Section<SparqlVisualizationType> section, List<Message> messages, Map<String, String> parameterMap) {
		// set css layout to be used
		String layout = SparqlVisualizationType.getAnnotation(section, SparqlVisualizationType.ANNOTATION_DESIGN);
		if (layout != null) {

			String cssText = null;

			ArticleManager articleManager = Environment.getInstance().getArticleManager(
					Environment.DEFAULT_WEB);
			Collection<Article> articles = articleManager.getArticles();

			for (Article article : articles) {
				Section<RootType> rootSection = article.getRootSection();
				// search layouttypes
				List<Section<SparqlVisDesignType>> sparqlVisDesignSections = Sections.successors(
						rootSection, SparqlVisDesignType.class);

				for (Section<SparqlVisDesignType> currentSection : sparqlVisDesignSections) {

					String currentLayout = SparqlVisDesignType.getAnnotation(currentSection,
							SparqlVisDesignType.ANNOTATION_NAME);
					if (currentLayout.equals(layout)) {

						cssText = SparqlVisDesignType.getContentSection(currentSection).getText();

					}

				}
			}

			if (cssText != null) {

				parameterMap.put(OntoGraphDataBuilder.D3_FORCE_VISUALISATION_STYLE, cssText);
			} else {
				Message noSuchLayout = new Message(Message.Type.WARNING,
						"No such layout " + layout + " found!");
				Collection<Message> warnings = new HashSet<Message>();
				messages.add(noSuchLayout);
				if (string != null) {
					DefaultMarkupRenderer.renderMessagesOfType(Message.Type.WARNING, warnings,
							string);
				}

			}

		}
	}

	public static String getRealPath(UserContext user) {
        String realPath = null;
        if (user != null) {
            ServletContext servletContext = user.getServletContext();
            if (servletContext == null) return null; // at wiki startup only

            realPath = servletContext.getRealPath("");
        } else {
            realPath = Environment.getInstance().getWikiConnector().getServletContext().getRealPath("");
        }
        return realPath;
    }
}
