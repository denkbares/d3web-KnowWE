package de.knowwe.rdfs.vis.markup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.core.utils.PackageCompileLinkToTermDefinitionProvider;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdfs.vis.OntoGraphDataBuilder;
import de.knowwe.rdfs.vis.PreRenderWorker;
import de.knowwe.rdfs.vis.markup.sparql.SparqlVisualizationType;
import de.knowwe.rdfs.vis.util.Utils;

public class OntoVisTypeRenderer extends DefaultMarkupRenderer implements PreRenderer {

	private Rdf2GoCore rdfRepository;
	private LinkToTermDefinitionProvider uriProvider;
	private String realPath;

	private String format;

	@Override
	public void renderContents(Section<?> section, UserContext user, RenderResult string) {
        PreRenderWorker.getInstance().preRenderSectionAndWait(this, section, user, string);
	}

    private boolean hasConceptParameter(UserContext user) {
        if (user != null) {

            String parameter = user.getParameter("concept");
            if (parameter != null) {
                return true;
            }
        }
        return false;
    }

	/**
	 * @created 13.07.2014
	 */
	private void findAndReadConfig(String configName, ArticleManager am, Map<String, String> parameterMap) {
		Collection<Section<VisualizationConfigType>> sections = Sections.successors(am, VisualizationConfigType.class);
		for (Section<VisualizationConfigType> section : sections) {
			String name = VisualizationConfigType.getAnnotation(section, VisualizationConfigType.ANNOTATION_NAME);
			if (name.equals(configName)) {
				readConfig(section, parameterMap);
			}
		}
	}

	/**
	 * @created 13.07.2014
	 */
	private void readConfig(Section<VisualizationConfigType> section, Map<String, String> parameterMap) {
		// size
		parameterMap.put(OntoGraphDataBuilder.GRAPH_SIZE, VisualizationConfigType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_SIZE));

		parameterMap.put(OntoGraphDataBuilder.GRAPH_WIDTH, VisualizationConfigType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_WIDTH));

		parameterMap.put(OntoGraphDataBuilder.GRAPH_HEIGHT, VisualizationConfigType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_HEIGHT));

		// format
		format = VisualizationConfigType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_FORMAT);
		if (format != null) {
			format = format.toLowerCase();
		} else {
			format = "svg";
		}
		parameterMap.put(OntoGraphDataBuilder.FORMAT, format);

		// dot app
        SparqlVisualizationType.readParameterFromAnnotation(ConceptVisualizationType.ANNOTATION_DOT_APP, section, OntoGraphDataBuilder.DOT_APP, parameterMap);

		// renderer
        SparqlVisualizationType.readParameterFromAnnotation(ConceptVisualizationType.ANNOTATION_RENDERER, section, OntoGraphDataBuilder.RENDERER, parameterMap);

		// visualization
        SparqlVisualizationType.readParameterFromAnnotation(ConceptVisualizationType.ANNOTATION_VISUALIZATION, section, OntoGraphDataBuilder.VISUALIZATION, parameterMap);

		// master
        SparqlVisualizationType.readParameterFromAnnotation(PackageManager.MASTER_ATTRIBUTE_NAME, section, OntoGraphDataBuilder.MASTER, parameterMap);

		// language
        SparqlVisualizationType.readParameterFromAnnotation(ConceptVisualizationType.ANNOTATION_LANGUAGE, section, OntoGraphDataBuilder.LANGUAGE, parameterMap);

		// excludes
		parameterMap.put(OntoGraphDataBuilder.EXCLUDED_RELATIONS, getExcludedRelations(section));
        SparqlVisualizationType.readParameterFromAnnotation(ConceptVisualizationType.ANNOTATION_EXCLUDENODES, section, OntoGraphDataBuilder.EXCLUDED_NODES, parameterMap);


        SparqlVisualizationType.readParameterFromAnnotation(ConceptVisualizationType.ANNOTATION_FILTERRELATIONS, section, OntoGraphDataBuilder.FILTERED_RELATIONS, parameterMap);

		// outgoing edges
        SparqlVisualizationType.readParameterFromAnnotation(ConceptVisualizationType.ANNOTATION_OUTGOING_EDGES, section, OntoGraphDataBuilder.SHOW_OUTGOING_EDGES, parameterMap);


		// inverse Relations
        SparqlVisualizationType.readParameterFromAnnotation(ConceptVisualizationType.ANNOTATION_SHOWINVERSE, section, OntoGraphDataBuilder.SHOW_INVERSE, parameterMap);

		// show classes
        SparqlVisualizationType.readParameterFromAnnotation(ConceptVisualizationType.ANNOTATION_SHOWCLASSES, section, OntoGraphDataBuilder.SHOW_CLASSES, parameterMap);

		// show properties
        SparqlVisualizationType.readParameterFromAnnotation(ConceptVisualizationType.ANNOTATION_SHOWPROPERTIES, section, OntoGraphDataBuilder.SHOW_PROPERTIES, parameterMap);

		// labels
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_LABELS, section, OntoGraphDataBuilder.USE_LABELS, parameterMap);

		// rank direction
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_RANK_DIR, section, OntoGraphDataBuilder.RANK_DIRECTION, parameterMap);

		// link mode
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_LINK_MODE, section, OntoGraphDataBuilder.LINK_MODE, parameterMap, SparqlVisualizationType.LinkMode.jump
				.name());

		// successors and predecessors
		parameterMap.put(OntoGraphDataBuilder.REQUESTED_DEPTH, getSuccessors(section));
		parameterMap.put(OntoGraphDataBuilder.REQUESTED_HEIGHT, getPredecessors(section));

		// add to dot
		String dotAppPrefix = VisualizationConfigType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_DOT_APP);
		if (dotAppPrefix != null) {
			parameterMap.put(OntoGraphDataBuilder.ADD_TO_DOT, dotAppPrefix + "\n");
		}

		// colors
		String colorRelationName = VisualizationConfigType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_COLORS);
		if (!Strings.isBlank(colorRelationName)) {
			parameterMap.put(OntoGraphDataBuilder.RELATION_COLOR_CODES, Utils.createColorCodings(colorRelationName, rdfRepository, "rdf:Property"));
			parameterMap.put(OntoGraphDataBuilder.CLASS_COLOR_CODES, Utils.createColorCodings(colorRelationName, rdfRepository, "rdfs:Class"));
		}
	}

	private String getMaster(Section<?> section) {
		return ConceptVisualizationType.getAnnotation(section,
				PackageManager.MASTER_ATTRIBUTE_NAME);
	}

	/**
	 * @created 18.08.2012
	 */
	private String getPredecessors(Section<?> section) {
		return ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_PREDECESSORS);
	}

	/**
	 * @created 18.08.2012
	 */
	private String getSuccessors(Section<?> section) {
		return ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_SUCCESSORS);
	}

	/**
	 * @created 18.08.2012
	 */
	private String getConcept(UserContext user, Section<?> section) {
		String concept = getConceptFromRequest(user, section);
        if(concept != null) {
            return concept;
        } else {
            return ConceptVisualizationType.getAnnotation(section, ConceptVisualizationType.ANNOTATION_CONCEPT);
        }
	}

    private String getConceptFromRequest(UserContext user, Section<?> section) {
        if (user != null) {
            String parameter = user.getParameter("concept");
            if (parameter != null) {
                return parameter;
            }
        }
        return null;
    }

	/**
	 * Checks the excluded relations to make sure a namespace is provided.
	 *
	 * @created 09.07.2014
	 */
	private String getExcludedRelations(Section<?> section) {
		String parameter = "";
		String exclude = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_EXCLUDERELATIONS);
		String defaultExclude = "";
		if (this.rdfRepository.getModelType().equals(Rdf2GoCore.Rdf2GoModel.SWIFTOWLIM)) {
			defaultExclude += "onto:_checkChain2,onto:_checkChain1,onto:_checkChain3";
		}

		if (exclude != null) {
			// Check if namespace is provided for each relation
			String[] excludes = exclude.split(",");
			for (String e : excludes) {
				if (e.contains(":")) {
					parameter = parameter + e + ",";
				}
			}
			parameter += defaultExclude;
		} else {
			parameter = defaultExclude;
		}
		return parameter;
	}

	@Override
	public void preRender(Section<?> section, UserContext user, RenderResult string) {
		initialise(section, user);

		Map<String, String> parameterMap = new HashMap<String, String>();

		if (Thread.currentThread().isInterrupted()) return;

		// find and read config file if defined
		String configName = ConceptVisualizationType.getAnnotation(section, ConceptVisualizationType.ANNOTATION_CONFIG);

		if (configName != null) {
			findAndReadConfig(configName.trim(), section.getArticleManager(), parameterMap);
		}

		String size = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_SIZE);
		if (size != null) {
			parameterMap.put(OntoGraphDataBuilder.GRAPH_SIZE, size);
		}

		String width = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_WIDTH);
		if (width != null) {
			parameterMap.put(OntoGraphDataBuilder.GRAPH_WIDTH, width);
		}

		String height = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_HEIGHT);
		if (height != null) {
			parameterMap.put(OntoGraphDataBuilder.GRAPH_HEIGHT, height);
		}

		String format = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_FORMAT);
		if (format != null) {
			format = format.toLowerCase();
			this.format = format;
			parameterMap.put(OntoGraphDataBuilder.FORMAT, format);
		}

		String dotApp = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_DOT_APP);
		if (dotApp != null) {
			parameterMap.put(OntoGraphDataBuilder.DOT_APP, dotApp);
		}

		String rendererType = ConceptVisualizationType.getAnnotation(section, ConceptVisualizationType.ANNOTATION_RENDERER);
		if (rendererType != null) {
			parameterMap.put(OntoGraphDataBuilder.RENDERER, rendererType);
		}

		String visualization = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_VISUALIZATION);
		if (visualization != null) {
			parameterMap.put(OntoGraphDataBuilder.VISUALIZATION, visualization);
		}

		parameterMap.put(OntoGraphDataBuilder.CONCEPT, getConcept(user, section));

		String master = getMaster(section);
		if (master != null) {
			parameterMap.put(OntoGraphDataBuilder.MASTER, master);
		}

		String lang = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_LANGUAGE);
		if (lang != null) {
			parameterMap.put(OntoGraphDataBuilder.LANGUAGE, lang);
		}

		String excludedRelations = getExcludedRelations(section);
		if (excludedRelations != null) {
			String allExcludes;
			String alreadyExcluded = parameterMap.get(OntoGraphDataBuilder.EXCLUDED_RELATIONS);
			if (alreadyExcluded == null) {
				allExcludes = excludedRelations;
			} else {
				if (!alreadyExcluded.trim().endsWith(",")) {
					alreadyExcluded += ", ";
				}
				allExcludes = alreadyExcluded + excludedRelations;
			}
			parameterMap.put(OntoGraphDataBuilder.EXCLUDED_RELATIONS, allExcludes);
		}

		String excludeNodes = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_EXCLUDENODES);
		if (excludeNodes != null) {
			String allExcludes;
			String alreadyExcluded = parameterMap.get(OntoGraphDataBuilder.EXCLUDED_NODES);
			if (alreadyExcluded == null) {
				allExcludes = excludeNodes;
			} else {
				if (!alreadyExcluded.trim().endsWith(",")) {
					alreadyExcluded += ", ";
				}
				allExcludes = alreadyExcluded + excludeNodes;
			}
			parameterMap.put(OntoGraphDataBuilder.EXCLUDED_NODES, allExcludes);
		}

		String filteredRelations = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_FILTERRELATIONS);
		if (filteredRelations != null) {
			String allFilters;
			String alreadyFiltered = parameterMap.get(OntoGraphDataBuilder.FILTERED_RELATIONS);
			if (alreadyFiltered == null) {
				allFilters = filteredRelations;
			} else {
				if (!alreadyFiltered.trim().endsWith(",")) {
					alreadyFiltered += ", ";
				}
				allFilters = alreadyFiltered + filteredRelations;
			}
			parameterMap.put(OntoGraphDataBuilder.FILTERED_CLASSES, allFilters);
			parameterMap.put(OntoGraphDataBuilder.FILTERED_RELATIONS, allFilters);
		}

		String outgoingEdges = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_OUTGOING_EDGES);
		if (outgoingEdges != null) {
			parameterMap.put(OntoGraphDataBuilder.SHOW_OUTGOING_EDGES, outgoingEdges);
		}

		String showInverse = VisualizationConfigType.getAnnotation(section, ConceptVisualizationType.ANNOTATION_SHOWINVERSE);
		if (showInverse != null) {
			parameterMap.put(OntoGraphDataBuilder.SHOW_INVERSE, showInverse);
		}

		String classes = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_SHOWCLASSES);
		if (classes != null) {
			parameterMap.put(OntoGraphDataBuilder.SHOW_CLASSES, classes);
		}

		String props = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_SHOWPROPERTIES);
		if (props != null) {
			parameterMap.put(OntoGraphDataBuilder.SHOW_PROPERTIES, props);
		}

		// set flag for use of labels
		String labelValue = SparqlVisualizationType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_LABELS);
		if (labelValue != null) {
			parameterMap.put(OntoGraphDataBuilder.USE_LABELS, labelValue);
		}

		// set rank direction of graph layout
		String rankDir = SparqlVisualizationType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_RANK_DIR);
		if (rankDir != null) {
			parameterMap.put(OntoGraphDataBuilder.RANK_DIRECTION, rankDir);
		}

		// set link mode
        SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_LINK_MODE, section, OntoGraphDataBuilder.LINK_MODE, parameterMap, SparqlVisualizationType.LinkMode.jump
				.name());


        String successors = getSuccessors(section);
		if (successors != null) {
			parameterMap.put(OntoGraphDataBuilder.REQUESTED_DEPTH, successors);
		}

		String predecessors = getPredecessors(section);
		if (predecessors != null) {
			parameterMap.put(OntoGraphDataBuilder.REQUESTED_HEIGHT, predecessors);
		}

		String dotAppPrefix = SparqlVisualizationType.getAnnotation(section,
				SparqlVisualizationType.ANNOTATION_DOT_APP);
		if (dotAppPrefix != null) {
			parameterMap.put(OntoGraphDataBuilder.ADD_TO_DOT, dotAppPrefix + "\n");
		}

		String colorRelationName = SparqlVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_COLORS);

		if (!Strings.isBlank(colorRelationName)) {
			parameterMap.put(OntoGraphDataBuilder.RELATION_COLOR_CODES, Utils.createColorCodings(colorRelationName, rdfRepository, "rdf:Property"));
			parameterMap.put(OntoGraphDataBuilder.CLASS_COLOR_CODES, Utils.createColorCodings(colorRelationName, rdfRepository, "rdfs:Class"));
		}

		// create file ID
		setFileID(section, parameterMap);

		if (Thread.currentThread().isInterrupted()) return;

		createGraphAndAppendHTMLIncludeSnipplet(string, realPath, section, parameterMap, rdfRepository, uriProvider);
	}

	private void setFileID(Section<?> section, Map<String, String> parameterMap) {
        String fileID = Utils.getFileID(section);
        if (fileID == null) return;

		parameterMap.put(OntoGraphDataBuilder.FILE_ID, fileID);
	}

    private void createGraphAndAppendHTMLIncludeSnipplet(RenderResult string, String realPath, Section<?> section, Map<String, String> parameterMap, Rdf2GoCore rdfRepository, LinkToTermDefinitionProvider uriProvider) {
		OntoGraphDataBuilder builder = new OntoGraphDataBuilder(realPath, section, parameterMap,
				uriProvider,
				rdfRepository);
		builder.render(string);
	}

	@Override
	public void cacheGraph(Section<?> section, RenderResult string) {
		initialise(section, null);

		Map<String, String> parameterMap = new HashMap<>();
		setFileID(section, parameterMap);

		String format = ConceptVisualizationType.getAnnotation(section,
				ConceptVisualizationType.ANNOTATION_FORMAT);
		if (format != null) {
			format = format.toLowerCase();
			this.format = format;
			parameterMap.put(OntoGraphDataBuilder.FORMAT, format);
		}

		createGraphAndAppendHTMLIncludeSnipplet(string, realPath, section, parameterMap, rdfRepository, uriProvider);
	}

	private void initialise(Section<?> section, UserContext user) {
		if (user != null) {
			ServletContext servletContext = user.getServletContext();
			if (servletContext == null) return; // at wiki startup only

			realPath = servletContext.getRealPath("");
		} else {
			realPath = Environment.getInstance().getWikiConnector().getServletContext().getRealPath("");
		}

		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);

		if (compiler == null) {
			rdfRepository = Rdf2GoCore.getInstance();
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
			rdfRepository = compiler.getRdf2GoCore();
			uriProvider = new PackageCompileLinkToTermDefinitionProvider();
		}
	}
}
