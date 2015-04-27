/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.visualization.d3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.d3web.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Edge;
import de.knowwe.visualization.GraphDataBuilder;
import de.knowwe.visualization.SubGraphData;
import de.knowwe.visualization.util.IncludeUtils;
import de.knowwe.visualization.util.IncludeUtils.FILE_TYPE;

/**
 * @author JohannaLatt
 * @created 25.06.2013
 */
public class D3Renderer {

    private static String htmlsource = "";
    private static String jsonSource = "";
    private static String arraySource = "";
    private static String arrayLinks = "";

    private static String context;

    // sectionID in paramter rein schreiben (im markup bekannt) und div dann so
    // benennen (id dann
    // mit ins javascript übergeben)
    public static String createD3HTMLSource(SubGraphData data, Map<String, String> parameters) {
        context = Environment.getInstance().getWikiConnector().getServletContext().getContextPath();

        String visualization = parameters.get(GraphDataBuilder.VISUALIZATION);

        htmlsource = "<div id=\"d3" + parameters.get(GraphDataBuilder.SECTION_ID)
                + "\" style=\"overflow: auto\">\r\n";

        // include the necessary d3.js sources
        htmlsource += IncludeUtils.includeFile(FILE_TYPE.JAVASCRIPT, context
                + "/KnowWEExtension/scripts/d3.v3.js");

        // default visualization: wheel
        if (visualization != null && visualization.equals("force")) {
            drawForce(data, parameters);
        }
        else if (visualization != null && visualization.equals("tree")) {
            try {
                drawCollapsibleTree(data, parameters);
            }
            catch (IllegalArgumentException e) {
                htmlsource += "<div class='error'> No valid root concept specified </div>";
            }
        }
        else {
            drawWheel(data, parameters);
        }

        htmlsource += "</div>";
        return htmlsource;
    }

    private static void drawCollapsibleTree(SubGraphData data, Map<String, String> parameters) throws IllegalArgumentException {

        // uses the same JSON source as the wheel visualization
        String concept = parameters.get(GraphDataBuilder.CONCEPT);
        if (concept == null) return;
        writeJSONWheelSource(data, concept);

        // include all necessary scripts and files
        htmlsource += IncludeUtils.includeFile(FILE_TYPE.JAVASCRIPT, context
                + "/KnowWEExtension/scripts/d3tree.js");
        htmlsource += IncludeUtils.includeFile(FILE_TYPE.CSS, context
                + "/KnowWEExtension/css/d3tree.css");

        // draw the collapsible tree visualization
        htmlsource += "<script>";
        htmlsource += " drawTree("
                + parameters.get(GraphDataBuilder.GRAPH_SIZE)
                + ", " + parameters.get(GraphDataBuilder.GRAPH_WIDTH)
                + ", " + parameters.get(GraphDataBuilder.GRAPH_HEIGHT)
                + ", " + jsonSource
                + ", " + "\"" + parameters.get(GraphDataBuilder.SECTION_ID) + "\""
                + ") ";
        htmlsource += "</script>";

        // implement layout style
        String cssCode = parameters.get(GraphDataBuilder.D3_FORCE_VISUALISATION_STYLE);
        if (cssCode != null) {
            htmlsource += "<style type=\"text/css\">";
            htmlsource += cssCode;
            htmlsource += "</style>";
        }

    }

    private static void drawWheel(SubGraphData data, Map<String, String> parameters) throws IllegalArgumentException {
        // write the JSON source for the wheel-visualization
        String concept = parameters.get(GraphDataBuilder.CONCEPT);
        if (concept == null) return;
        writeJSONWheelSource(data, concept);

        // include all necessary scripts and files
        htmlsource += IncludeUtils.includeFile(FILE_TYPE.JAVASCRIPT, context
                + "/KnowWEExtension/scripts/d3wheel.js");
        htmlsource += IncludeUtils.includeFile(FILE_TYPE.CSS, context
                + "/KnowWEExtension/css/d3wheel.css");

        // draw the wheel-visualization
        htmlsource += "<script>";
        htmlsource += " drawWheel("
                + parameters.get(GraphDataBuilder.GRAPH_SIZE)
                + ", " + parameters.get(GraphDataBuilder.GRAPH_WIDTH)
                + ", " + parameters.get(GraphDataBuilder.GRAPH_HEIGHT)
                + ", " + jsonSource
                + ", " + "\"" + parameters.get(GraphDataBuilder.SECTION_ID) + "\""
                + ") ";
        htmlsource += "</script>";

        // implement layout style
        String cssCode = parameters.get(GraphDataBuilder.D3_FORCE_VISUALISATION_STYLE);
        if (cssCode != null) {
            htmlsource += "<style type=\"text/css\">";
            htmlsource += cssCode;
            htmlsource += "</style>";
        }
    }

    private static void drawForce(SubGraphData data, Map<String, String> parameters) {
        // write the JSON source for the force-visualization
        writeJSONForceSource(data);

        // include all necessary scripts and files
        htmlsource += IncludeUtils.includeFile(FILE_TYPE.JAVASCRIPT, context
                + "/KnowWEExtension/scripts/d3force.js");
        htmlsource += IncludeUtils.includeFile(FILE_TYPE.CSS, context
                + "/KnowWEExtension/css/d3force.css");

        // draw the force-visualization
        htmlsource += "<script>";
        htmlsource += " drawForce("
                + parameters.get(GraphDataBuilder.GRAPH_SIZE)
                + ", " + parameters.get(GraphDataBuilder.GRAPH_WIDTH)
                + ", " + parameters.get(GraphDataBuilder.GRAPH_HEIGHT)
                + ", " + arraySource
                + ", " + arrayLinks
                + ", " + "\"" + GraphDataBuilder.createBaseURL() + "\""
                + ", " + "\"" + parameters.get(GraphDataBuilder.TITLE) + "\""
                + ", " + "\"" + parameters.get(GraphDataBuilder.SECTION_ID) + "\""
                + ")";
        htmlsource += "</script>";

        // implement layout style
        String cssCode = parameters.get(GraphDataBuilder.D3_FORCE_VISUALISATION_STYLE);
        if (cssCode != null) {
            htmlsource += "<style type=\"text/css\">";
            htmlsource += cssCode;
            htmlsource += "</style>";

        }
    }

    /**
     * Writes the JSON source for the wheel visualization.
     *
     * @param data
     * @param concept main concept on which the data bases on
     * @created 20.06.2013
     */
    private static void writeJSONWheelSource(SubGraphData data, String concept) throws IllegalArgumentException {
        ConceptNode conceptRoot = data.getConcept(concept);
        if (conceptRoot == null) {
            throw new IllegalArgumentException(
                    "no root concept specified for hierarchy visualization");
        }
        jsonSource = "{\n";

        String conceptLabel = conceptRoot.getConceptLabel();
        if (!conceptLabel.endsWith("\"")) {
            conceptLabel = "\"" + conceptLabel + "\"";
        }
        jsonSource += "\"concept\": " + conceptLabel;

        HierarchyTree tree = new HierarchyTree(conceptRoot, data);
        HierarchyNode root = tree.getRoot();

        String conceptUrl = root.getConceptNode().getConceptUrl();

        jsonSource += ",\n";
        jsonSource += "\"conceptUrl\": \"" + conceptUrl + "\"";
        if (root.hasChildren()) {
            addChildrenToSource(root);
        }

        jsonSource += "\n}";
    }

    private static String getLabel(HierarchyNode node) {
        String label = node.getName();
        if (node.getLabel() != null) {
            String xsdStringAnnotation = "^^http://www.w3.org/2001/XMLSchema#string";
            label = node.getLabel();
            if (label.endsWith(xsdStringAnnotation)) {
                label = label.substring(0, label.length() - xsdStringAnnotation.length());
            }
        }
        return label;
    }

    /**
     * Adds the children of the given HierarchyNode to the jsonSource
     *
     * @param root
     * @created 25.06.2013
     */
    private static void addChildrenToSource(HierarchyNode root) {
        jsonSource += ",\n\"children\": [\n";
        List<HierarchyNode> children = root.getChildren();
        Iterator<HierarchyNode> iterator = children.iterator();
        while (iterator.hasNext()) {
            HierarchyNode next = iterator.next();
            // if the child is not in the source yet: Add it so source and loop
            // through it's children
            String conceptUrl = next.getConceptNode().getConceptUrl();

            String targetURL = "";
            if (conceptUrl != null) {
                targetURL = Strings.encodeURL(conceptUrl);
            }

            String label = getLabel(next);
            if (!next.isInSourceYet()) {
                if (label.endsWith("\"")) {
                    jsonSource += "{\"concept\": " + label;
                } else {
                    jsonSource += "{\"concept\": \"" + label + "\"";
                }
                jsonSource += ",\n\"conceptUrl\": \"" + targetURL
                        + "\" ";
                next.setIsInSourceYet(true);
                if (next.hasChildren()) {
                    addChildrenToSource(next);
                }
                jsonSource += "}";
            }
            // ...otherwise only add the child but don't go further in the tree
            // (-> endless loop)
            else {
                if (label.endsWith("\"")) {
                    jsonSource += "{\"concept\": " + label;
                } else {
                    jsonSource += "{\"concept\": \"" + label + "\"";
                }
                jsonSource += ",\n\"conceptUrl\": \"" + targetURL + "\"\n";
                jsonSource += "}";
            }
            if (iterator.hasNext()) {
                // not last element yet
                jsonSource += ",\n";
            }
        }
        jsonSource += "\n]";
    }

    /**
     * Writes the JSON source for the force visualization.
     *
     * @param data
     * @created 08.07.2013
     */
    private static void writeJSONForceSource(SubGraphData data) {
        List<String> links = new ArrayList<String>();

        // SOURCE
        arraySource = "[\n";
        Iterator<Edge> iterator = data.getAllEdges().iterator();
        while (iterator.hasNext()) {
            Edge next = iterator.next();
            String p = next.getPredicate();

            String subjectLabel = next.getSubject().getConceptLabel();
            if (subjectLabel == null) {
                subjectLabel = next.getSubject().getName();
            }

            String objectLabel = next.getObject().getConceptLabel();
            if (objectLabel == null) {
                objectLabel = next.getObject().getName();
            }

            if (subjectLabel.endsWith("\"")) {
                arraySource += "{source: " + subjectLabel + ", ";
            } else {
                arraySource += "{source: \"" + subjectLabel + "\", ";
            }
            if (objectLabel.endsWith("\"")) {
                arraySource += "target: " + objectLabel + ", ";
            } else {
                arraySource += "target: \"" + objectLabel + "\", ";
            }
            if (p.endsWith("\"")) {
                arraySource += "type: " + p + "}";
            } else {
                arraySource += "type: \"" + p + "\"}";
            }


            if (iterator.hasNext()) {
                arraySource += ",";
            }
            arraySource += "\n";

            if (!links.contains(p)) {
                links.add(p);
            }
        }
        arraySource += "]";

        // LINKS
        arrayLinks = "[";
        Iterator<String> iter = links.iterator();
        while (iter.hasNext()) {
            String next = iter.next();
            arrayLinks += " \"" + next + "\"";
            if (iter.hasNext()) {
                arrayLinks += ",";
            }
        }
        arrayLinks += "]\n";
    }
}
