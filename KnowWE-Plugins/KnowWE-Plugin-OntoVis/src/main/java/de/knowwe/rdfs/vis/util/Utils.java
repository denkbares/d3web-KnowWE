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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.collections.PartialHierarchyTree;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.preview.PreviewManager;
import de.knowwe.core.report.Messages;
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
		return getLabel(concept, repo, languageTag, "<http://www.w3.org/2004/02/skos/core#prefLabel>", "rdfs:label");
	}

	public static String getLabel(Value concept, Rdf2GoCore repo, String languageTag, String... properties) {
		if (properties.length == 0) {
			throw new IllegalArgumentException("Property definition requred here!");
		}

		// try to find language specific label
		String label = getLanguageSpecificLabel(concept, repo, languageTag, properties);

		// otherwise use non-language specific label according to priority
		if (label == null) {

			for (String property : properties) {
				String query = "SELECT ?x WHERE { <" + concept + "> " + property.trim() + " ?x.}";
				TupleQueryResult resultTable = repo.sparqlSelect(query);
				for (BindingSet queryRow : resultTable) {
					Value node = queryRow.getValue("x");
					return node.stringValue();
				}
			}

		}
		return label;
	}

	private static String getLanguageSpecificLabel(Value concept, Rdf2GoCore repo, String languageTag, String... properties) {
		if (languageTag == null) return null;

		for (String property : properties) {
			String query = "SELECT ?x WHERE { <" + concept
					+ "> " + property.trim() + " ?x. FILTER(LANGMATCHES(LANG(?x), \"" + languageTag + "\"))}";
			TupleQueryResult resultTable = repo.sparqlSelect(query);
			for (BindingSet queryRow : resultTable) {
				Value node = queryRow.getValue("x");
				String label = node.stringValue();
				if (label.charAt(label.length() - 3) == '@') {
					label = label.substring(0, label.length() - 3);
				}
				return label;
			}
		}
		return null;
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
			label = literalToLabel(toLiteral);

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
			URI uri = (URI) toURI;
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
				label = fetchLabel(config, toURI, rdfRepository);

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
				if (clazz != null && !clazz.isEmpty()) {
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

	@NotNull
	private static String literalToLabel(Literal toLiteral) {
		String label;
		label = toLiteral.toString();
		if (label.contains("@")) {
			String lang = label.substring(label.indexOf('@') + 1);
			label = label.substring(0, label.indexOf('@')) + " (" + lang + ")";
		}
		else if (label.contains("^^")) {
			if (label.contains("#")) {
				String dataType = label.substring(label.lastIndexOf('#') + 1, label.length() - 1);
				label = Strings.unquote(label.substring(0, label.indexOf("^^"))) + ("string".equals(dataType) ? "" : " (" + dataType + ")");
			}
			else {
				label = label.substring(0, label.indexOf("^^"));
			}
		}
		else {
			label = Strings.quote(label);
		}
		return label;
	}

	@Nullable
	public static String fetchLabel(Config config, Value toURI, Rdf2GoCore rdf2GoCore) {
		String showLabels = config.getShowLabels();
		String label = null;
		if (!Strings.isBlank(showLabels) && !"false".equals(showLabels.toLowerCase())) {
			if ("true".equals(showLabels.toLowerCase())) {
				label = Utils.getRDFSLabel(toURI, rdf2GoCore, config.getLanguage());
			}
			else {
				label = Utils.getLabel(toURI, rdf2GoCore, config.getLanguage(), showLabels.split(","));
			}
			if (label != null && label.charAt(label.length() - 3) == '@') {
				// do not show language tag of relation labels
				label = label.substring(0, label.length() - 3);
			}
		}
		return label;
	}

	private static String getIdentifierLiteral(Literal toLiteral) {
		return toLiteral.stringValue().replace("\"", "") + "ONTOVIS-LITERAL";
	}

	private static String getIdentifierBValue(BNode bValue) {
		return bValue.toString();
	}

	private static RenderingStyle setClassColorCoding(Value node, RenderingStyle style, Config config, Rdf2GoCore rdfRepository) {
		Map<String, String> classColorScheme = config.getClassColors();
		if (classColorScheme != null && !classColorScheme.isEmpty()) {
			String shortURI = Rdf2GoUtils.reduceNamespace(rdfRepository, node.stringValue());
			if (Rdf2GoUtils.isClass(rdfRepository, (URI) node)) {
				String color = classColorScheme.get(shortURI);
				if (color != null) {
					style.setFillcolor(color);
				}
			}
			else {
//				Collection<URI> classURIs = Rdf2GoUtils.getClasses(rdfRepository, (URI) value);
//				for (URI classURI : classURIs) {
//					String shortURIClass = Rdf2GoUtils.reduceNamespace(rdfRepository, classURI.stringValue());
//					String color = findColor(shortURIClass, classColorScheme);
//					if (color != null) {
//						style.setFillcolor(color);
//						break;
//					}
				// We fetch the class hierarchy of this concept
				PartialHierarchyTree<URI> classHierarchy = Rdf2GoUtils.getClassHierarchy(rdfRepository, (URI) node);
				// we then remove from this hierarchy all classes that do not have a color assignment
				List<URI> allClasses = classHierarchy.getNodesDFSOrder();
				for (URI clazz : allClasses) {
					if (!classColorScheme.containsKey(Rdf2GoUtils.reduceNamespace(rdfRepository, clazz.toString()))) {
						classHierarchy.remove(clazz);
					}
				}
				URI clazzToBeColored = Rdf2GoUtils.findMostSpecificClass(classHierarchy);
				if (clazzToBeColored != null) {
					String color = classColorScheme.get(Rdf2GoUtils.reduceNamespace(rdfRepository, clazzToBeColored.toString()));
					if (color != null) {
						style.setFillcolor(color);
					}
				}
			}

		}
		return style;
	}

	public static String createConceptURL(String to, Config config, Section<?> section, LinkToTermDefinitionProvider uriProvider, String uri) {
		// TODO: 21.11.16 find better solution for this, after successor for deprecated LinkMode annotation has been found
		if (section != null) {
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
					uri.stringValue());
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

	public static boolean isBlankNode(Value n) {
		return n instanceof BNode;
	}

	public static boolean isLiteral(Value n) {
		return n instanceof Literal;
	}

	public static String getConceptName(Value uri, Rdf2GoCore repo) {
		/*
		handle string/literal
		 */
		if (isLiteral(uri)) {
			return getIdentifierLiteral((Literal) uri);
		}

        /*
		handle BNodes
		 */
		if (isBlankNode(uri)) {
			return getIdentifierBValue((BNode) uri);
		}

		/*
		handle URI
		 */
		try {
			URI uriValue = (URI) uri;
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
		if (Utils.isBlankNode(conceptURI)) {
			return GraphDataBuilder.NODE_TYPE.BLANKNODE;
		}

		GraphDataBuilder.NODE_TYPE result = GraphDataBuilder.NODE_TYPE.UNDEFINED;

		if (Rdf2GoUtils.isClass(rdfRepository, (URI) conceptURI)) return GraphDataBuilder.NODE_TYPE.CLASS;

		if (Rdf2GoUtils.isProperty(rdfRepository, (URI) conceptURI)) return GraphDataBuilder.NODE_TYPE.PROPERTY;

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

	public static Map<String, String> createColorCodings(Section<?> section, String relationName, Rdf2GoCore core, String entityName) {
		Messages.clearMessages(section, Utils.class);

		String query = "SELECT ?entity ?color WHERE {" +
				"?entity <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + entityName + " ." +
				"?entity " + relationName + " ?color" +
				"}";
//		TupleQueryResult resultTable = core.sparqlSelect(query);
//		for (BindingSet row : resultTable) {
//			Value entity = row.getValue("entity");
//			String color = row.getValue("color").stringValue();
		Map<String, String> colorCodings = new HashMap<>();
		try {
			TupleQueryResult resultTable = core.sparqlSelect(query);
			for (BindingSet row : resultTable) {
				Value entity = row.getValue("entity");
				String color = row.getValue("color").stringValue();
				String shortURI = Rdf2GoUtils.reduceNamespace(core, entity.toString());
				colorCodings.put(shortURI, color);
			}
			return colorCodings;
		}
		catch (Exception e) {
			Log.severe("Exception while looking up color codes", e);
			Messages.storeMessage(section, Utils.class, Messages.error("Unable to find color " + relationName + "."));
		}
		return null;
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

	public static String getConceptFromRequest(UserContext user) {
		if (user != null) {
			return user.getParameter("concept");
		}
		return null;
	}

	public static String createRelationLabel(Config config, Rdf2GoCore rdfRepository, Value relationURI, String relation) {
		// is the node a literal ? edit: can a predicate node ever be a literal ??
		Literal toLiteral = null;
		try {
			toLiteral = (Literal) relationURI;
		}
		catch (ClassCastException e) {
			// do nothing
		}

		String relationName = relation;
		if (toLiteral != null) {
			relationName = literalToLabel(toLiteral);
		}
		else {
			// if it is no literal look for label for the URI
			String relationLabel = Utils.fetchLabel(config,
					relationURI, rdfRepository);
			if (relationLabel != null) {
				relationName = relationLabel;
			}
		}
		return relationName;
	}

	public static String getFileID(Section<?> section, UserContext user) {
		String conceptFromRequest = Utils.getConceptFromRequest(user);
		String fileID = "Visualization_" + section.getID();

		if (conceptFromRequest != null) {
			fileID += "_" + conceptFromRequest.replaceAll("\\W", "_").replaceAll(":","");
		}

		OntologyCompiler compiler = Compilers.getCompiler(section, OntologyCompiler.class);
		if (compiler == null) return fileID;

		String result = fileID + "_" + Integer.toHexString(compiler.hashCode());

		// jspwiki rendering pipeline kills us for double underscores...
		result = result.replaceAll("__", "_");
		return result;
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

	@NotNull
	public static MultiMap<String, String> getSubPropertyMap(Rdf2GoCore rdfRepository) {
		MultiMap<String, String> subPropertiesMap = new DefaultMultiMap<>();

		// Get all  SubProperties and add all non-recursive to an ArrayList
		String subPropertyQuery = "SELECT ?Property ?SubProperty WHERE {\n" +
				"\t  ?SubProperty rdfs:subPropertyOf ?Property\n" +
				"  }\n";
		CachedTupleQueryResult propertyRelations = rdfRepository.sparqlSelect(subPropertyQuery);
		for (BindingSet propertyRelation : propertyRelations) {
			String subProperty = propertyRelation.getValue("SubProperty").stringValue();
			String property = propertyRelation.getValue("Property").stringValue();

			// if SubProperty is not same as Property
			if (!property.equals(subProperty)) {
				subPropertiesMap.put(property, subProperty);
			}

		}
		return subPropertiesMap;
	}

	@SuppressWarnings("Duplicates")
	public static MultiMap<String, String> getInverseRelationsMap(Rdf2GoCore rdfRepository) {
		MultiMap<String, String> inversePropertiesMap = new DefaultMultiMap<>();
		// find all inverse Relations
		String query = "SELECT ?Property ?InverseProperty WHERE {\n" +
				"\t  ?Property owl:inverseOf ?InverseProperty\n" +
				"  }\n";
		CachedTupleQueryResult relations = rdfRepository.sparqlSelect(query);
		for (BindingSet relation : relations) {
			String Property = relation.getValue("Property").stringValue();
			String inverseProperty = relation.getValue("InverseProperty").stringValue();

			// if SubProperty is not same as Property
			//if (!inverseProperty.equals(Property)) {
			inversePropertiesMap.put(inverseProperty, Property);
			//}

		}
		return inversePropertiesMap;
	}
}
