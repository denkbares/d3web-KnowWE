/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.rdfs.vis.util;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdfs.vis.OntoGraphDataBuilder;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.GraphDataBuilder;
import de.knowwe.visualization.SubGraphData;
import de.knowwe.visualization.dot.RenderingStyle;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Literal;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.URI;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;

/**
 * @author Jochen Reutelshöfer
 * @created 29.11.2012
 */
public class Utils {

    public static final String LINE_BREAK = "\\n";

    public static String getRDFSLabel(Node concept, Rdf2GoCore repo, String languageTag) {

        // try to find language specific label
        String label = getLanguageSpecificLabel(concept, repo, languageTag);

        // otherwise use standard label
        if (label == null) {

            String query = "SELECT ?x WHERE { <" + concept.toString() + "> rdfs:label ?x.}";
            QueryResultTable resultTable = repo.sparqlSelect(query);
            for (QueryRow queryRow : resultTable) {
                Node node = queryRow.getValue("x");
                String value = node.asLiteral().toString();
                label = value;
                break; // we assume there is only one label

            }
        }
        return label;
    }

    /**
     * @param concept
     * @param repo
     * @param languageTag
     * @return
     * @created 29.04.2013
     */
    private static String getLanguageSpecificLabel(Node concept, Rdf2GoCore repo, String languageTag) {
        if (languageTag == null) return null;
        String label = null;

        String query = "SELECT ?x WHERE { <" + concept.toString()
                + "> rdfs:label ?x. FILTER(LANGMATCHES(LANG(?x), \"" + languageTag + "\"))}";
        QueryResultTable resultTable = repo.sparqlSelect(query);
        for (QueryRow queryRow : resultTable) {
            Node node = queryRow.getValue("x");
            String value = node.asLiteral().toString();
            label = value;
            if (label.charAt(label.length() - 3) == '@') {
                label = label.substring(0, label.length() - 3);
            }
            break; // we assume there is only one label

        }
        return label;
    }

    public static ConceptNode createNode(Map<String, String> parameters, Rdf2GoCore rdfRepository, LinkToTermDefinitionProvider uriProvider, Section<?> section, SubGraphData data, Node toURI, boolean insertNewNode) {
        return createNode(parameters, rdfRepository, uriProvider, section, data, toURI, insertNewNode, null);
    }

    public static ConceptNode createNode(Map<String, String> parameters, Rdf2GoCore rdfRepository, LinkToTermDefinitionProvider uriProvider, Section<?> section, SubGraphData data, Node toURI, boolean insertNewNode, String clazz) {
        ConceptNode visNode = null;

        GraphDataBuilder.NODE_TYPE type = GraphDataBuilder.NODE_TYPE.UNDEFINED;
        Literal toLiteral = null;
        String label = null;
        String identifier = null;

		/*
        1. case: Node is Literal
		 */
        try {
            toLiteral = toURI.asLiteral();
            //add a key to identifier to have distinguish between concepts and literals, e.g., <lns:Q> and "Q"
            identifier = getIdentifierLiteral(toLiteral);
            type = GraphDataBuilder.NODE_TYPE.LITERAL;
            label = toLiteral.toString();
            if (label.contains("@")) {
                String lang = label.substring(label.indexOf('@') + 1);
                label = "\"" + label.substring(0, label.indexOf('@')) + "\"" + " (" + lang + ")";
            } else if (label.contains("^^")) {
                if (label.contains("#")) {
                    String dataType = label.substring(label.lastIndexOf('#') + 1);
                    label = "\"" + label.substring(0, label.indexOf("^^")) + "\"" + " (" + dataType + ")";
                } else {
                    label = "\"" + label.substring(0, label.indexOf("^^")) + "\"";
                }
            } else {
                label = Strings.quote(label);
            }

            RenderingStyle style = Utils.getStyle(type);
            visNode = new ConceptNode(identifier, type, null, label, style);
            if (insertNewNode) {
                data.addConcept(visNode);
            }
            return visNode;
        } catch (ClassCastException e) {
            // do nothing as this is just a type check
        }

		/*
		2. case: Node is BlankNode
		 */
        BlankNode bNode = null;
        try {
            bNode = toURI.asBlankNode();
            identifier = getIdentifierBNode(bNode);

            visNode = data.getConcept(identifier);
            if (visNode == null) {
                type = GraphDataBuilder.NODE_TYPE.BLANKNODE;
                label = getIdentifierBNode(bNode);
                RenderingStyle style = Utils.getStyle(type);

                visNode = new ConceptNode(identifier, type, null, label, style);
                if (insertNewNode) {
                    data.addConcept(visNode);
                }
            }
            return visNode;

        } catch (ClassCastException e) {
            // do nothing as this is just a type check
        }


		/*
		3. case: Node is URI-Resource
		 */
        try {
            URI uri = toURI.asURI();
            identifier = getConceptName(toURI, rdfRepository);
            visNode = data.getConcept(identifier);

            if (visNode == null) {

                type = GraphDataBuilder.NODE_TYPE.UNDEFINED;
                if (Rdf2GoUtils.isClass(rdfRepository, uri)) {
                    type = GraphDataBuilder.NODE_TYPE.CLASS;
                }
                if (Rdf2GoUtils.isProperty(rdfRepository, uri)) {
                    type = GraphDataBuilder.NODE_TYPE.PROPERTY;
                }
                if (parameters.get(GraphDataBuilder.USE_LABELS) != null
                        && parameters.get(GraphDataBuilder.USE_LABELS).equals("true")) {
                    label = Utils.getRDFSLabel(
                            toURI, rdfRepository,
                            parameters.get(OntoGraphDataBuilder.LANGUAGE));
                }
                if (label == null) {
                    label = identifier;
                }
                RenderingStyle style = Utils.getStyle(type);
                Utils.setClassColorCoding(toURI, style, parameters, rdfRepository);
                visNode = new ConceptNode(identifier, type, createConceptURL(identifier, parameters,
                        section,
                        uriProvider, uri.toString()), label, clazz, style);
                if (insertNewNode) {
                    data.addConcept(visNode);
                }
            } else {
                if (clazz != null && clazz.length() > 0) {
                    // we found a type-triple and add the clazz attribute to the already existing node
                    visNode.setClazz(clazz);
                    // re-color according to newly found clazz
                    RenderingStyle style = Utils.getStyle(visNode.getType());
                    Utils.setClassColorCoding(toURI, style, parameters, rdfRepository);
                    visNode.setStyle(style);

                }
            }
            return visNode;
        } catch (ClassCastException e) {
            // do nothing as this is just a type check
        }

        // this case should/can never happen!
        Log.severe("No valid Node type!");
        return null;
    }

    private static String getIdentifierLiteral(Literal toLiteral) {
        return toLiteral.toString().replace("\"", "") + "ONTOVIS-LITERAL";
    }

    private static String getIdentifierBNode(BlankNode bNode) {
        return bNode.toString();
    }

    private static RenderingStyle setClassColorCoding(Node node, RenderingStyle style, Map<String, String> parameters, Rdf2GoCore rdfRepository) {
        String classColorScheme = parameters.get(GraphDataBuilder.CLASS_COLOR_CODES);
        if (classColorScheme != null && !Strings.isBlank(classColorScheme)) {
            String shortURI = Rdf2GoUtils.reduceNamespace(rdfRepository, node.asURI().toString());
            if (Rdf2GoUtils.isClass(rdfRepository, node.asURI())) {
                String color = findColor(shortURI, classColorScheme);
                if (color != null) {
                    style.setFillcolor(color);
                }
            } else {
                Collection<URI> classURIs = Rdf2GoUtils.getClasses(rdfRepository, node.asURI());
                for (URI classURI : classURIs) {
                    String shortURIClass = Rdf2GoUtils.reduceNamespace(rdfRepository, classURI.asURI().toString());
                    String color = findColor(shortURIClass, classColorScheme);
                    if (color != null) {
                        style.setFillcolor(color);
                        break;
                    }
                }
            }

        }
        return style;
    }

    private static String findColor(String shortURIClass, String classColorScheme) {

        String color = de.knowwe.visualization.util.Utils.getColorCode(shortURIClass, classColorScheme);
        return color;
    }

    public static String createConceptURL(String to, Map<String, String> parameters, Section<?> s, LinkToTermDefinitionProvider uriProvider, String uri) {
        if (parameters.get(OntoGraphDataBuilder.LINK_MODE) != null) {
            if (parameters.get(OntoGraphDataBuilder.LINK_MODE).equals(
                    OntoGraphDataBuilder.LINK_MODE_BROWSE)) {
                final OntologyCompiler compiler = Compilers.getCompiler(s, OntologyCompiler.class);
                final String shortURI = Rdf2GoUtils.reduceNamespace(compiler.getRdf2GoCore(), uri);
                Identifier identifier = new Identifier(shortURI);
                String[] identifierParts = shortURI.split(":");
                if (identifierParts.length == 2) {
                    identifier = new Identifier(
                            identifierParts[0], Strings.decodeURL(identifierParts[1]));

                }

                final TerminologyManager terminologyManager = compiler.getTerminologyManager();
                final Section<?> termDefiningSection = terminologyManager.getTermDefiningSection(identifier);
                if(termDefiningSection == null) {
                    // we have no definition found
                    return null;
                }
                String url = KnowWEUtils.getURLLink(termDefiningSection);
                if (url != null) {
                    if (!url.startsWith("http:")) {
                        url = Environment.getInstance().getWikiConnector().getBaseUrl() + url;
                    }
                    return url;
                }
            }
        }
        return OntoGraphDataBuilder.createBaseURL() + "?page="
                + OntoGraphDataBuilder.getSectionTitle(s)
                + "&concept=" + to;
    }

    public static String getIdentifierURI(Node uri, Rdf2GoCore repo) {
        try {
            String reducedNamespace = Rdf2GoUtils.reduceNamespace(repo,
                    uri.asURI().toString());
            String[] splitURI = reducedNamespace.split(":");
            String namespace = splitURI[0];
            String name = splitURI[1];
            if (namespace.equals("lns")) {
                return urlDecode(name);
            } else {
                return namespace + ":" + urlDecode(name);
            }

        } catch (ClassCastException e) {
            return null;
        }
    }

    public static boolean isBlankNode(Node n) {
        try {
            BlankNode bNode = n.asBlankNode();
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public static boolean isLiteral(Node n) {
        try {
            Literal l = n.asLiteral();
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public static String getConceptName(Node uri, Rdf2GoCore repo) {
        /*
        handle string/literal
		 */
        if (isLiteral(uri)) {
            return getIdentifierLiteral(uri.asLiteral());
        }

        /*
        handle BlankNodes
		 */
        if (isBlankNode(uri)) {
            return getIdentifierBNode(uri.asBlankNode());
        }

		/*
		handle URI
		 */
        try {
            URI uriNode = uri.asURI();
            return getIdentifierURI(uriNode, repo);

        } catch (ClassCastException e) {
            return null;
        }
    }

    public static GraphDataBuilder.NODE_TYPE getConceptType(Node conceptURI, Rdf2GoCore rdfRepository) {
        if (Utils.isLiteral(conceptURI)) {
            return GraphDataBuilder.NODE_TYPE.LITERAL;
        }
        if (Utils.isBlankNode(conceptURI)) {
            return GraphDataBuilder.NODE_TYPE.BLANKNODE;
        }

        GraphDataBuilder.NODE_TYPE result = GraphDataBuilder.NODE_TYPE.UNDEFINED;

        if (Rdf2GoUtils.isClass(rdfRepository, conceptURI.asURI())) return GraphDataBuilder.NODE_TYPE.CLASS;

        if (Rdf2GoUtils.isProperty(rdfRepository, conceptURI.asURI())) return GraphDataBuilder.NODE_TYPE.PROPERTY;

        return result;
    }

    public static String urlDecode(String name) {
        try {
            return URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String createColorCodings(String relationName, Rdf2GoCore core, String entityName) {
        StringBuffer result = new StringBuffer();

        String query = "SELECT ?entity ?color WHERE {" +
                "?entity <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + entityName + " ." +
                "?entity " + relationName + " ?color" +
                "}";
        QueryResultTable resultTable = core.sparqlSelect(query);
        ClosableIterator<QueryRow> iterator = resultTable.iterator();
        while (iterator.hasNext()) {
            QueryRow row = iterator.next();
            Node entity = row.getValue("entity");
            String color = row.getLiteralValue("color");
            String shortURI = Rdf2GoUtils.reduceNamespace(core, entity.toString());
            result.append(shortURI + " " + color + ";");
        }

        return result.toString().trim();
    }

    public static RenderingStyle getStyle(GraphDataBuilder.NODE_TYPE type) {
        RenderingStyle style = new RenderingStyle();
        style.setFontcolor("black");

        if (type == GraphDataBuilder.NODE_TYPE.CLASS) {
            style.setShape("box");
            style.setStyle("bold");
        } else if (type == GraphDataBuilder.NODE_TYPE.INSTANCE) {
            style.setShape("box");
            style.setStyle("rounded");
        } else if (type == GraphDataBuilder.NODE_TYPE.PROPERTY) {
            style.setShape("hexagon");
        } else if (type == GraphDataBuilder.NODE_TYPE.BLANKNODE) {
            style.setShape("diamond");
        } else if (type == GraphDataBuilder.NODE_TYPE.LITERAL) {
            style.setShape("box");
            style.setStyle("filled");
            style.setFillcolor("lightgray");
        } else {
            style.setShape("box");
            style.setStyle("rounded");
        }
        return style;
    }

	public static String createRelationLabel(Map<String, String> parameters, Rdf2GoCore rdfRepository, Node relationURI, String relation) {
		// is the node a literal ?
		Literal toLiteral = null;
		try {
			toLiteral = relationURI.asLiteral();
		}
		catch (ClassCastException e) {
			// do nothing
		}

		String relationName = relation;
		if (toLiteral != null) {
			relationName = toLiteral.toString();
			if (relationName.contains("@")) {
				relationName = relationName.substring(0, relationName.indexOf('@'));
			}
		}
		else {
			// if it is no literal look for label for the URI
			String relationLabel = Utils.getRDFSLabel(
					relationURI.asURI(), rdfRepository,
					parameters.get(OntoGraphDataBuilder.LANGUAGE));
			if (relationLabel != null) {
				relationName = relationLabel;
			}
		}
		return relationName;
	}

	public static String getFileID(Section<?> section) {
		String textHash = String.valueOf(section.getText().hashCode());

		OntologyCompiler ontoCompiler = Compilers.getCompiler(section, OntologyCompiler.class);
		if (ontoCompiler == null) return null;
		String compHash = String.valueOf(ontoCompiler.getCompileSection().getTitle().hashCode());

		String fileID = "_" + textHash + "_" + compHash;
		return fileID;
	}

	public static String findNewIDFromRenderResult(Sections.ReplaceResult rr) {
		if (rr != null) {
			Map<String, String> resultMapping = rr.getSectionMapping();
			Collection<String> values = resultMapping.values();
			String newID = "";

			if (values.size() == 1) {
				for (String value : values) {
					newID = value;
				}
			}

			return newID;
		}
		return "";
	}

}
