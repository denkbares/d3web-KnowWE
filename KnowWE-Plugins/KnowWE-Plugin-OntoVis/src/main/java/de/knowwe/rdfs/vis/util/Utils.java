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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.BindingSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.collections.PartialHierarchyException;
import com.denkbares.collections.PartialHierarchyTree;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.semanticcore.utils.Sparqls;
import com.denkbares.strings.Text;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Locales;
import com.denkbares.strings.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	public static String getRDFSLabel(Value concept, Rdf2GoCore repo, Locale... languages) {
		return getLabel(concept, repo, languages, "<http://www.w3.org/2004/02/skos/core#prefLabel>", "rdfs:label");
	}

	public static String getLabel(Value concept, Rdf2GoCore repo, Locale[] languages, String... properties) {
		if (properties.length == 0) {
			throw new IllegalArgumentException("Property definition requred here!");
		}

		// try to find language specific label
		String label = getLanguageSpecificLabel(concept, repo, languages, properties);

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

	private static String getLanguageSpecificLabel(Value concept, Rdf2GoCore repo, Locale[] languages, String... properties) {
		if (languages == null) return null;
		Map<Locale, String> cache = new HashMap<>();
		for (String property : properties) {
			String query = "SELECT ?x WHERE { <" + concept
					+ "> " + property.trim() + " ?x. }";
			TupleQueryResult resultTable = repo.sparqlSelect(query);
			for (BindingSet queryRow : resultTable) {
				Text text = Sparqls.asText(queryRow, "x");
				if (Locales.hasSameLanguage(text.getLanguage(), languages[0])) {
					return text.getString();
				}
				cache.put(text.getLanguage(), text.getString());
			}
		}
		Locale bestLocale = Locales.findBestLocale(Arrays.asList(languages), cache.keySet());
		return cache.get(bestLocale);
	}

	public static ConceptNode createValue(Config config, Rdf2GoCore rdfRepository, LinkToTermDefinitionProvider
			uriProvider, Section<?> section, SubGraphData data, Value toIRI, boolean insertNewValue) {
		return createValue(config, rdfRepository, uriProvider, section, data, toIRI, insertNewValue, null);
	}

	public static ConceptNode createValue(Config config, Rdf2GoCore rdfRepository, LinkToTermDefinitionProvider
			uriProvider, Section<?> section, SubGraphData data, Value value, boolean insertNewValue, String clazz) {
		ConceptNode visValue;

		GraphDataBuilder.NODE_TYPE type;
		Literal toLiteral;
		String label;
		String identifier;

		/*
		1. case: Value is Literal
		 */
		if (value instanceof Literal) {
			toLiteral = (Literal) value;
			//add a key to identifier to have distinguish between concepts and literals, e.g., <lns:Q> and "Q"
			identifier = getIdentifierLiteral(toLiteral);
			type = GraphDataBuilder.NODE_TYPE.LITERAL;
			label = literalToLabel(toLiteral);

			RenderingStyle style = Utils.getStyle(type, config);
			visValue = new ConceptNode(identifier, type, null, label, style);
			if (insertNewValue) {
				data.addConcept(visValue);
			}
			return visValue;
		}

		/*
		2. case: Value is BNode
		 */
		BNode bValue;
		try {
			bValue = (BNode) value;
			identifier = getIdentifierBValue(bValue);

			visValue = data.getConcept(identifier);
			if (visValue == null) {
				type = GraphDataBuilder.NODE_TYPE.BLANKNODE;
				// TODO: find way to retrieve the actual label of the bNode if existing (cannot be sparqled with bNode id)
				label = getIdentifierBValue(bValue);
				RenderingStyle style = Utils.getStyle(type, config);

				visValue = new ConceptNode(identifier, type, null, label, style);
				if (insertNewValue) {
					data.addConcept(visValue);
				}
			}
			return visValue;
		}
		catch (ClassCastException ignore) {
			// do nothing as this is just a type check
		}


		/*
		3. case: Value is IRI-Resource
		 */
		try {
			IRI uri = (IRI) value;
			identifier = getConceptName(value, rdfRepository);
			visValue = data.getConcept(identifier);

			if (visValue == null) {

				type = GraphDataBuilder.NODE_TYPE.UNDEFINED;
				if (Rdf2GoUtils.isClass(rdfRepository, uri)) {
					type = GraphDataBuilder.NODE_TYPE.CLASS;
				}
				if (Rdf2GoUtils.isProperty(rdfRepository, uri)) {
					type = GraphDataBuilder.NODE_TYPE.PROPERTY;
				}
				label = fetchLabel(config, value, rdfRepository);

				if (label == null) {
					label = identifier;
				}
				RenderingStyle style = Utils.getStyle(type, config);
				Utils.setClassColorCoding(value, style, config, rdfRepository);
				String conceptURL = createConceptURL(identifier, config, section, uriProvider, uri.toString());
				visValue = new ConceptNode(identifier, type, conceptURL, label, clazz, style);
				if (insertNewValue) {
					data.addConcept(visValue);
				}
			}
			else {
				if (clazz != null && !clazz.isEmpty()) {
					// we found a type-triple and add the clazz attribute to the already existing Value
					visValue.setClazz(clazz);
					// re-color according to newly found clazz
					RenderingStyle style = Utils.getStyle(visValue.getType(), config);
					Utils.setClassColorCoding(value, style, config, rdfRepository);
					visValue.setStyle(style);
				}
			}
			return visValue;
		}
		catch (ClassCastException ignore) {
			// do nothing as this is just a type check
		}

		// this case should/can never happen!
		LOGGER.error("No valid Value type!");
		return null;
	}

	@NotNull
	private static String literalToLabel(Literal toLiteral) {
		if (toLiteral.getDatatype().equals(XMLSchema.STRING)) {
			return toLiteral.stringValue();
		}
		String label = toLiteral.toString();
		if (label.contains("@")) {
			String lang = label.substring(label.indexOf('@') + 1);
			label = label.substring(0, label.indexOf('@')) + " (" + lang + ")";
		}
		else if (label.contains("^^")) {
			if (label.contains("#")) {
				String dataType = label.substring(label.lastIndexOf('#') + 1, label.length() - 1);
				label = Strings.unquote(label.substring(0, label.indexOf("^^"))) + ("string".equals(dataType) ? "" :
						" (" + dataType + ")");
			}
			else {
				label = label.substring(0, label.indexOf("^^"));
			}
		}
		else if (!Strings.isQuoted(label, '"')) {
			label = Strings.quote(label);
		}
		return label;
	}

	@Nullable
	public static String fetchLabel(Config config, Value toIRI, Rdf2GoCore rdf2GoCore) {
		String showLabels = config.getShowLabels();
		String label = null;
		if (!Strings.isBlank(showLabels) && !"false".equals(showLabels.toLowerCase())) {
			if ("true".equals(showLabels.toLowerCase())) {
				label = Utils.getRDFSLabel(toIRI, rdf2GoCore, config.getLanguages());
			}
			else {
				label = Utils.getLabel(toIRI, rdf2GoCore, config.getLanguages(), showLabels.split(","));
			}
			if (label != null && label.length() >= 3 && label.charAt(label.length() - 3) == '@') {
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

	private static RenderingStyle setClassColorCoding(Value node, RenderingStyle style, Config config, Rdf2GoCore
			rdfRepository) {
		Map<String, String> individualColorsScheme = config.getIndividualColors();
		String shortIRI = Rdf2GoUtils.reduceNamespace(rdfRepository, node.stringValue());
		if (node instanceof IRI) {
			if (individualColorsScheme.containsKey(shortIRI)) {
				style.setFillcolor(individualColorsScheme.get(shortIRI));
			}
		}

		Map<String, String> classColorScheme = config.getClassColors();
		if (classColorScheme != null && !classColorScheme.isEmpty()) {

			if (Rdf2GoUtils.isClass(rdfRepository, (IRI) node)) {
				String color = classColorScheme.get(shortIRI);
				if (color != null) {
					style.setFillcolor(color);
				}
			}
			else {
				// We fetch the class hierarchy of this concept
				PartialHierarchyTree<IRI> classHierarchy = Rdf2GoUtils.getClassHierarchy(rdfRepository, (IRI) node);
				// we then remove from this hierarchy all classes that do not have a color assignment
				List<IRI> allClasses = classHierarchy.getNodesDFSOrder();
				for (IRI clazz : allClasses) {
					if (!classColorScheme.containsKey(Rdf2GoUtils.reduceNamespace(rdfRepository, clazz.toString()))) {
						try {
							classHierarchy.remove(clazz);
						}
						catch (PartialHierarchyException e) {
							LOGGER.error("Unable to remove class " + clazz, e);
						}
					}
				}
				IRI clazzToBeColored = Rdf2GoUtils.findMostSpecificClass(classHierarchy);
				if (clazzToBeColored != null) {
					String color = classColorScheme.get(Rdf2GoUtils.reduceNamespace(rdfRepository, clazzToBeColored
							.toString()));
					if (color != null) {
						style.setFillcolor(color);
					}
				}
			}
		}
		return style;
	}

	public static String createConceptURL(String to, Config config, Section<?> section, LinkToTermDefinitionProvider uriProvider, String uri) {
		if (section != null) {
			final OntologyCompiler compiler = Compilers.getCompiler(section, OntologyCompiler.class);
			final String shortIRI = Rdf2GoUtils.reduceNamespace(compiler.getRdf2GoCore(), uri);
			Identifier identifier = new Identifier(shortIRI);
			String[] identifierParts = shortIRI.split(":");
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
			if (!url.startsWith("http:")) {
				url = Environment.getInstance().getWikiConnector().getBaseUrl() + url;
			}
			return url;
		}
		return OntoGraphDataBuilder.createBaseURL() + "?concept=" + to;
	}

	public static String getIdentifierIRI(Value uri, Rdf2GoCore repo) {
		try {
			String reducedNamespace = Rdf2GoUtils.reduceNamespace(repo,
					uri.stringValue());
			String[] splitIRI = reducedNamespace.split(":");
			String namespace = splitIRI[0];
			String name = splitIRI.length > 1 ? splitIRI[1] : "";
			if (namespace.equals("lns")) {
				return Strings.decodeURL(name);
			}
			else {
				return namespace + ":" + Strings.decodeURL(name);
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
		handle IRI
		 */
		try {
			IRI uriValue = (IRI) uri;
			return getIdentifierIRI(uriValue, repo);
		}
		catch (ClassCastException e) {
			return null;
		}
	}

	public static GraphDataBuilder.NODE_TYPE getConceptType(Value conceptIRI, Rdf2GoCore rdfRepository) {
		if (Utils.isLiteral(conceptIRI)) {
			return GraphDataBuilder.NODE_TYPE.LITERAL;
		}
		if (Utils.isBlankNode(conceptIRI)) {
			return GraphDataBuilder.NODE_TYPE.BLANKNODE;
		}

		GraphDataBuilder.NODE_TYPE result = GraphDataBuilder.NODE_TYPE.UNDEFINED;

		if (Rdf2GoUtils.isClass(rdfRepository, (IRI) conceptIRI)) return GraphDataBuilder.NODE_TYPE.CLASS;

		if (Rdf2GoUtils.isProperty(rdfRepository, (IRI) conceptIRI)) return GraphDataBuilder.NODE_TYPE.PROPERTY;

		return result;
	}

	public static Map<String, String> createColorCodings(Section<?> section, String relationName, Rdf2GoCore core,
														 String entityName) {
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
				String shortIRI = Rdf2GoUtils.reduceNamespace(core, entity.toString());
				colorCodings.put(shortIRI, color);
			}
			return colorCodings;
		}
		catch (Exception e) {
			LOGGER.error("Exception while looking up color codes", e);
			Messages.storeMessage(section, Utils.class, Messages.error("Unable to find color " + relationName + "."));
		}
		return null;
	}

	public static RenderingStyle getStyle(GraphDataBuilder.NODE_TYPE type, Config config) {
		RenderingStyle style = new RenderingStyle();
		style.setFontcolor("black");
		try {
			if (type == GraphDataBuilder.NODE_TYPE.CLASS) {
				style.setShape("box");
				style.setStyle("bold");
				if (config.getClassNodeStyle() != null) {
					makeRenderingStyle(style, config.getClassNodeStyle());
				}
			}
			else if (type == GraphDataBuilder.NODE_TYPE.INSTANCE) {
				style.setShape("box");
				style.setStyle("rounded");
				if (config.getInstanceNodeStyle() != null) {
					makeRenderingStyle(style, config.getInstanceNodeStyle());
				}
			}
			else if (type == GraphDataBuilder.NODE_TYPE.PROPERTY) {
				style.setShape("hexagon");
				if (config.getPropertyNodeStyle() != null) {
					makeRenderingStyle(style, config.getPropertyNodeStyle());
				}
			}
			else if (type == GraphDataBuilder.NODE_TYPE.BLANKNODE) {
				style.setShape("diamond");
				if (config.getBlankNodeStyle() != null) {
					makeRenderingStyle(style, config.getBlankNodeStyle());
				}
			}
			else if (type == GraphDataBuilder.NODE_TYPE.LITERAL) {
				style.setShape("box");
				style.setStyle("filled");
				style.setFillcolor("lightgray");
				if (config.getLiteralNodeStyle() != null) {
					makeRenderingStyle(style, config.getLiteralNodeStyle());
				}
			}
			else {
				style.setShape("box");
				style.setStyle("rounded");
			}
		}
		catch (Exception e) {
			Config.GRAPH_HAS_ERRORS = true;
			Config.GRAPH_ERROR_MESSAGE = e.getMessage();
		}
		return style;
	}

	private static void makeRenderingStyle(RenderingStyle renderingStyle, String s) throws Exception {
		//splits the input string into each attribute allocation
		List<String> styles = new ArrayList<>();
		Matcher matcher = Pattern.compile("(([^\\s,]+)((\\s*)=(\\s*)\"(.+?)\"))").matcher(s);
		while (matcher.find()) {
			styles.add(matcher.group());
		}

		//String[] styles = s.split("(([^\\s,]+)((\\s*)=(\\s*)\"(.+?)\"))"); (([^\s,]+)((\s*)"([^"]+)"))

		if (styles.isEmpty()) {
			throw new Exception("\"" + s + "\" did not contain a valid attribute.");
		}
		else {
			for (String style : styles) {
				if (style.contains("shape")) {
					renderingStyle.setShape(extractStyleProperty(style));
				}
				else if (style.contains("fontsize")) {
					renderingStyle.setFontsize(extractStyleProperty(style));
				}
				else if (style.contains("fillcolor")) {
					renderingStyle.setFillcolor(extractStyleProperty(style));
				}
				else if (style.contains("fontcolor")) {
					renderingStyle.setFontcolor(extractStyleProperty(style));
				}
				else if (style.contains("fontstyle")) {
					RenderingStyle.Fontstyle fontstyle;
					switch (extractStyleProperty(style)) {
						case "NORMAL":
						case "normal":
							fontstyle = RenderingStyle.Fontstyle.NORMAL;
							break;
						case "BOLD":
						case "bold":
							fontstyle = RenderingStyle.Fontstyle.BOLD;
							break;
						case "ITALIC":
						case "italic":
							fontstyle = RenderingStyle.Fontstyle.ITALIC;
							break;
						case "UNDERLINING":
						case "underlining":
							fontstyle = RenderingStyle.Fontstyle.UNDERLINING;
							break;
						default:
							fontstyle = RenderingStyle.Fontstyle.NORMAL;
							break;
					}
					renderingStyle.setFontstyle(fontstyle);
				}
				else if (style.contains("style") && !style.contains("fontstyle")) {
					renderingStyle.addStyle(extractStyleProperty(style));
				}
				else {
					throw new Exception("Attribute allocation '" + style + "' could not be resolved!");
				}
			}
		}
	}

	private static String extractStyleProperty(String style) {
		String property = style.substring(style.indexOf("\"") + 1, style.length() - 1);
		property = property.contains("\"") ? property.substring(0, property.indexOf("\"")) : property;
		property = property.replace(" ", "");
		return property;
	}

	public static String getConceptFromRequest(UserContext user) {
		String concept = null;
		if (user != null) {
			concept = user.getParameter("concept");
			if (concept == null) {
				Object object = user.getSession().getAttribute("concept");
				if (object != null) {
					concept = object.toString();
				}
			}
		}
		return concept;
	}

	public static String createRelationLabel(Config config, Rdf2GoCore rdfRepository, Value relationIRI, String
			relation) {
		// is the node a literal ? edit: can a predicate node ever be a literal ??
		Literal toLiteral = null;
		try {
			toLiteral = (Literal) relationIRI;
		}
		catch (ClassCastException ignore) {
			// do nothing
		}

		String relationName = relation;
		if (toLiteral != null) {
			relationName = literalToLabel(toLiteral);
		}
		else {
			// if it is no literal look for label for the IRI
			String relationLabel = Utils.fetchLabel(config,
					relationIRI, rdfRepository);
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
			fileID += "_" + conceptFromRequest.replaceAll("\\W", "_").replaceAll(":", "");
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
