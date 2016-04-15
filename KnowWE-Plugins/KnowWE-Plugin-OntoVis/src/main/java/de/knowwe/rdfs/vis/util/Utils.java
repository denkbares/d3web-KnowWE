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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.preview.PreviewManager;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdfs.vis.OntoGraphDataBuilder;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Config;
import de.knowwe.visualization.GraphDataBuilder;
import de.knowwe.visualization.SubGraphData;
import de.knowwe.visualization.dot.RenderingStyle;

/**
 * @author Jochen Reutelshöfer
 * @created 29.11.2012
 */
public class Utils {

	public static String getRDFSLabel(Value concept, Rdf2GoCore repo, String languageTag) {

		// try to find language specific label
		String label = getLanguageSpecificLabel(concept, repo, languageTag);

		// otherwise use standard label
		if (label == null) {

			String query = "SELECT ?x WHERE { <" + concept.toString() + "> rdfs:label ?x.}";
			Rdf2GoCore.QueryResultTable resultTable = repo.sparqlSelect(query);
			for (BindingSet BindingSet : resultTable) {
				Value Value = BindingSet.getValue("x");
				label = Value.stringValue()
				break; // we assume there is only one label

			}
		}
		return label;
	}

	private static String getLanguageSpecificLabel(Value concept, Rdf2GoCore repo, String languageTag) {
		if (languageTag == null) return null;
		String label = null;

		String query = "SELECT ?x WHERE { <" + concept.toString()
				+ "> rdfs:label ?x. FILTER(LANGMATCHES(LANG(?x), \"" + languageTag + "\"))}";
		Rdf2GoCore.QueryResultTable resultTable = repo.sparqlSelect(query);
		for (BindingSet BindingSet : resultTable) {
			Value Value = BindingSet.getValue("x");
			label = Value.stringValue();
			if (label.charAt(label.length() - 3) == '@') {
				label = label.substring(0, label.length() - 3);
			}
			break; // we assume there is only one label

		}
		return label;
	}

	public static ConceptNode createValue(Config config, Rdf2GoCore rdfRepository, LinkToTermDefinitionProvider uriProvider, Section<?> section, SubGraphData data, Value toURI, boolean insertNewValue) {
		return createValue(config, rdfRepository, uriProvider, section, data, toURI, insertNewValue, null);
	}

	public static ConceptNode createValue(Config config, Rdf2GoCore rdfRepository, LinkToTermDefinitionProvider uriProvider, Section<?> section, SubGraphData data, Value toURI, boolean insertNewValue, String clazz) {
		ConceptNode visValue;

		GraphDataBuilder.NODE_TYPE type;
		Literal toLiteral;
		String label = null;
		String identifier;

		/*
		1. case: Value is Literal
		 */
		try {
			toLiteral = (Literal) toURI;
			//add a key to identifier to have distinguish between concepts and literals, e.g., <lns:Q> and "Q"
			identifier = getIdentifierLiteral(toLiteral);
			type = GraphDataBuilder.NODE_TYPE.LITERAL;
			label = toLiteral.toString();
			if (label.contains("@")) {
				String lang = label.substring(label.indexOf('@') + 1);
				label = "\"" + label.substring(0, label.indexOf('@')) + "\"" + " (" + lang + ")";
			}
			else if (label.contains("^^")) {
				if (label.contains("#")) {
					String dataType = label.substring(label.lastIndexOf('#') + 1);
					label = "\"" + label.substring(0, label.indexOf("^^")) + "\"" + " (" + dataType + ")";
				}
				else {
					label = "\"" + label.substring(0, label.indexOf("^^")) + "\"";
				}
			}
			else {
				label = Strings.quote(label);
			}

			RenderingStyle style = Utils.getStyle(type);
			visValue = new ConceptNode(identifier, type, null, label, style);
			if (insertNewValue) {
				data.addConcept(visValue);
			}
			return visValue;
		}
		catch (ClassCastException e) {
			// do nothing as this is just a type check
		}

		/*
		2. case: Value is BNode
		 */
		BNode bValue;
		try {
			bValue = (BNode) toURI;
			identifier = getIdentifierBValue(bValue);

			visValue = data.getConcept(identifier);
			if (visValue == null) {
				type = GraphDataBuilder.NODE_TYPE.BLANKNODE;
				label = getIdentifierBValue(bValue);
				RenderingStyle style = Utils.getStyle(type);

				visValue = new ConceptNode(identifier, type, null, label, style);
				if (insertNewValue) {
					data.addConcept(visValue);
				}
			}
			return visValue;

		}
		catch (ClassCastException e) {
			// do nothing as this is just a type check
		}


		/*
		3. case: Value is URI-Resource
		 */
		try {
			URI uri = toURI.asURI();
			identifier = getConceptName(toURI, rdfRepository);
			visValue = data.getConcept(identifier);

			if (visValue == null) {

				type = GraphDataBuilder.NODE_TYPE.UNDEFINED;
				if (Rdf2GoUtils.isClass(rdfRepository, uri)) {
					type = GraphDataBuilder.NODE_TYPE.CLASS;
				}
				if (Rdf2GoUtils.isProperty(rdfRepository, uri)) {
					type = GraphDataBuilder.NODE_TYPE.PROPERTY;
				}
				if (config.isShowLabels()) {
					label = Utils.getRDFSLabel(
							toURI, rdfRepository,
							config.getLanguage());
				}
				if (label == null) {
					label = identifier;
				}
				RenderingStyle style = Utils.getStyle(type);
				Utils.setClassColorCoding(toURI, style, config, rdfRepository);
				visValue = new ConceptNode(identifier, type, createConceptURL(identifier, config,
						section,
						uriProvider, uri.toString()), label, clazz, style);
				if (insertNewValue) {
					data.addConcept(visValue);
				}
			}
			else {
				if (clazz != null && clazz.length() > 0) {
					// we found a type-triple and add the clazz attribute to the already existing Value
					visValue.setClazz(clazz);
					// re-color according to newly found clazz
					RenderingStyle style = Utils.getStyle(visValue.getType());
					Utils.setClassColorCoding(toURI, style, config, rdfRepository);
					visValue.setStyle(style);

				}
			}
			return visValue;
		}
		catch (ClassCastException e) {
			// do nothing as this is just a type check
		}

		// this case should/can never happen!
		Log.severe("No valid Value type!");
		return null;
	}

	private static String getIdentifierLiteral(Literal toLiteral) {
		return toLiteral.toString().replace("\"", "") + "ONTOVIS-LITERAL";
	}

	private static String getIdentifierBValue(BNode bValue) {
		return bValue.toString();
	}

	private static RenderingStyle setClassColorCoding(Value Value, RenderingStyle style, Config config, Rdf2GoCore rdfRepository) {
		String classColorScheme = config.getClassColors();
		if (classColorScheme != null && !Strings.isBlank(classColorScheme)) {
			String shortURI = Rdf2GoUtils.reduceNamespace(rdfRepository, Value.asURI().toString());
			if (Rdf2GoUtils.isClass(rdfRepository, Value.asURI())) {
				String color = findColor(shortURI, classColorScheme);
				if (color != null) {
					style.setFillcolor(color);
				}
			}
			else {
				Collection<URI> classURIs = Rdf2GoUtils.getClasses(rdfRepository, Value.asURI());
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
		return de.knowwe.visualization.util.Utils.getColorCode(shortURIClass, classColorScheme);
	}

	public static String createConceptURL(String to, Config config, Section<?> section, LinkToTermDefinitionProvider uriProvider, String uri) {
		if (config.getLinkMode() == Config.LinkMode.BROWSE) {
			final OntologyCompiler compiler = Compilers.getCompiler(section, OntologyCompiler.class);
			final String shortURI = Rdf2GoUtils.reduceNamespace(compiler.getRdf2GoCore(), uri);
			Identifier identifier = new Identifier(shortURI);
			String[] identifierParts = shortURI.split(":");
			if (identifierParts.length == 2) {
				identifier = new Identifier(
						identifierParts[0], Strings.decodeURL(identifierParts[1]));

			}

			final TerminologyManager terminologyManager = compiler.getTerminologyManager();
			final Section<?> termDefiningSection = terminologyManager.getTermDefiningSection(identifier);
			if (termDefiningSection == null) {
				// we have no definition found
				return null;
			}
			// get the closes ancestor that will have an anchor to jump to
			Section<?> anchorAncestor = PreviewManager.getInstance()
					.getPreviewAncestor(termDefiningSection);
			String url = KnowWEUtils.getURLLink(anchorAncestor);
			if (url != null) {
				if (!url.startsWith("http:")) {
					url = Environment.getInstance().getWikiConnector().getBaseUrl() + url;
				}
				return url;
			}
		}
		return OntoGraphDataBuilder.createBaseURL() + "?" + (section == null ? "" : "page="
				+ section.getTitle() + "&") + "concept=" + to;
	}

	public static String getIdentifierURI(Value uri, Rdf2GoCore repo) {
		try {
			String reducedNamespace = Rdf2GoUtils.reduceNamespace(repo,
					uri.asURI().toString());
			String[] splitURI = reducedNamespace.split(":");
			String namespace = splitURI[0];
			String name = splitURI[1];
			if (namespace.equals("lns")) {
				return urlDecode(name);
			}
			else {
				return namespace + ":" + urlDecode(name);
			}

		}
		catch (ClassCastException e) {
			return null;
		}
	}

	public static boolean isBNode(Value n) {
		try {
			n.asBNode();
			return true;
		}
		catch (ClassCastException e) {
			return false;
		}
	}

	public static boolean isLiteral(Value n) {
		try {
			n.asLiteral();
			return true;
		}
		catch (ClassCastException e) {
			return false;
		}
	}

	public static String getConceptName(Value uri, Rdf2GoCore repo) {
		/*
        handle string/literal
		 */
		if (isLiteral(uri)) {
			return getIdentifierLiteral(uri.asLiteral());
		}

        /*
		handle BNodes
		 */
		if (isBNode(uri)) {
			return getIdentifierBValue(uri.asBNode());
		}

		/*
		handle URI
		 */
		try {
			URI uriValue = uri.asURI();
			return getIdentifierURI(uriValue, repo);

		}
		catch (ClassCastException e) {
			return null;
		}
	}

	public static GraphDataBuilder.NODE_TYPE getConceptType(Value conceptURI, Rdf2GoCore rdfRepository) {
		if (Utils.isLiteral(conceptURI)) {
			return GraphDataBuilder.NODE_TYPE.LITERAL;
		}
		if (Utils.isBNode(conceptURI)) {
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
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String createColorCodings(String relationName, Rdf2GoCore core, String entityName) {
		StringBuilder result = new StringBuilder();
		String query = "SELECT ?entity ?color WHERE {" +
				"?entity <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + entityName + " ." +
				"?entity " + relationName + " ?color" +
				"}";
		QueryResultTable resultTable = core.sparqlSelect(query);
		for (BindingSet row : resultTable) {
			Value entity = row.getValue("entity");
			String color = row.getLiteralValue("color");
			String shortURI = Rdf2GoUtils.reduceNamespace(core, entity.toString());
			result.append(shortURI).append(" ").append(color).append(";");
		}
		return result.toString().trim();
	}

	public static RenderingStyle getStyle(GraphDataBuilder.NODE_TYPE type) {
		RenderingStyle style = new RenderingStyle();
		style.setFontcolor("black");

		if (type == GraphDataBuilder.NODE_TYPE.CLASS) {
			style.setShape("box");
			style.setStyle("bold");
		}
		else if (type == GraphDataBuilder.NODE_TYPE.INSTANCE) {
			style.setShape("box");
			style.setStyle("rounded");
		}
		else if (type == GraphDataBuilder.NODE_TYPE.PROPERTY) {
			style.setShape("hexagon");
		}
		else if (type == GraphDataBuilder.NODE_TYPE.BLANKNODE) {
			style.setShape("diamond");
		}
		else if (type == GraphDataBuilder.NODE_TYPE.LITERAL) {
			style.setShape("box");
			style.setStyle("filled");
			style.setFillcolor("lightgray");
		}
		else {
			style.setShape("box");
			style.setStyle("rounded");
		}
		return style;
	}

	public static void getConceptFromRequest(UserContext user, Config config) {
		if (user != null) {
			String parameter = user.getParameter("concept");
			if (parameter != null) {
				config.setConcept(Strings.trim(parameter));
			}
		}
	}

	public static String createRelationLabel(Config config, Rdf2GoCore rdfRepository, Value relationURI, String relation) {
		// is the Value a literal ?
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
					config.getLanguage());
			if (relationLabel != null) {
				relationName = relationLabel;
			}
		}
		return relationName;
	}

	public static String getFileID(Section<?> section) {

		String fileID = "Visualization_" + section.getID();

		OntologyCompiler compiler = Compilers.getCompiler(section, OntologyCompiler.class);
		if (compiler == null) return fileID;

		return fileID + "_" + Integer.toHexString(compiler.hashCode());
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
