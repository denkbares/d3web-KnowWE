/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.knowwe.rdf2go.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.PartialHierarchy;
import com.denkbares.collections.PartialHierarchyTree;
import com.denkbares.semanticcore.OptimizedMemValueFactory;
import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.semanticcore.utils.RDFUtils;
import com.denkbares.semanticcore.utils.Sparqls;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Locales;
import com.denkbares.strings.Strings;
import com.denkbares.strings.Text;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

public class Rdf2GoUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Rdf2GoUtils.class);

	private static final SimpleDateFormat XSD_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

	private static final SimpleDateFormat PRIVATE_XSD_DATE_TIME_FORMAT = getXsdDateTimeFormat();

	private static final String staticLns = generateLocalNamespace();

	/**
	 * We might want to move the value factory into the semantic core or even the underlying repository or sail, because
	 * here in the Rdf2GoCore, we cannot control all the value generation.
	 */
	private static final OptimizedMemValueFactory valueFactory = new OptimizedMemValueFactory();

	public static @NotNull String getLocalNamespace() {
		return staticLns;
	}

	private static @NotNull String generateLocalNamespace() {
		String lns;
		String baseUrl;
		try {
			baseUrl = Environment.getInstance().getWikiConnector().getBaseUrl();
			new URL(baseUrl); // check if we have a valid url or just context root
		}
		catch (Exception e) {
			LOGGER.warn("Invalid local namespace (lns), using fallback http://localhost:8080/KnowWE/");
			baseUrl = "http://localhost:8080/KnowWE/";
		}
		lns = baseUrl + "Wiki.jsp?page=";
		return lns;
	}

	public static String getReadableQuery(String query, SparqlType type) {
		query = query.replace("\n", " ").replaceAll("\t|\\s\\s+", " ");
		int start = -1;
		if (type == SparqlType.ASK) {
			start = query.toLowerCase().indexOf("ask");
		}
		else if (type == SparqlType.SELECT) {
			start = query.toLowerCase().indexOf("select");
		}
		else if (type == SparqlType.CONSTRUCT) {
			start = query.toLowerCase().indexOf("construct");
		}
		if (start == -1) {
			start = 0;
		}
		final int endIndex = query.length() - start > 75 ? start + 75 : query.length();
		return query.substring(start, endIndex) + "...";
	}

	/**
	 * Returns a {@link SimpleDateFormat} allowing to read and write ^^xsd:date
	 */
	public static SimpleDateFormat getXsdDateTimeFormat() {
		return (SimpleDateFormat) XSD_DATE_TIME_FORMAT.clone();
	}

	public static Rdf2GoCore getRdf2GoCoreForDefaultMarkupSubSection(Section<? extends Type> section) {
		Section<DefaultMarkupType> defaultMarkup = Sections.ancestor(section,
				DefaultMarkupType.class);
		if (defaultMarkup == null) {
			throw new IllegalArgumentException();
		}
		return getRdf2GoCore(defaultMarkup);
	}

	public static ValueFactory getValueFactory() {
		return valueFactory;
	}

	public static boolean isClass(Rdf2GoCore core, IRI resource) {
		String query = "ASK { <" + resource.stringValue() + "> rdf:type rdfs:Class .}";
		return core.sparqlAsk(query);
	}

	public static boolean isProperty(Rdf2GoCore core, IRI resource) {
		String query = "ASK { <" + resource.stringValue() + "> rdf:type rdf:Property .}";
		return core.sparqlAsk(query);
	}

	public static Collection<IRI> getClasses(Rdf2GoCore core, IRI instance) {
		String query = "SELECT ?class WHERE { <" + instance.stringValue() + "> rdf:type ?class .}";
		List<IRI> resultCollection = new ArrayList<>();
		TupleQueryResult result = core.sparqlSelect(query);
		for (BindingSet row : result) {
			Value aClassNode = row.getValue("class");
			IRI uri = (IRI) aClassNode;
			resultCollection.add(uri);
		}
		return resultCollection;
	}

	public static boolean instanceOf(Rdf2GoCore core, URI resource, URI classURI) {
		if (resource == null) {
			return false;
		}
		String query = "ASK { <" + resource + "> rdf:type <" + core.createIRI(classURI) + "> .}";
		return core.sparqlAsk(Collections.emptyList(), query);
	}

	/**
	 * Returns all instance of the given classes.
	 *
	 * @param core repository to scan for instances
	 * @param uris classes that instances are detected of
	 * @return all instances of all the given classes
	 */
	public static Collection<IRI> getInstances(Rdf2GoCore core, List<URI> uris) {
		String query = RDFUtils.createQueryForGetInstances(uris);
		List<IRI> resultCollection = new ArrayList<>();
		TupleQueryResult result = core.sparqlSelect(query);
		for (BindingSet row : result) {
			Value instanceNode = row.getValue("instance");
			IRI uri = (IRI) instanceNode;
			resultCollection.add(uri);
		}
		return resultCollection;
	}

	private static final String SPARQL_LABEL = """
			<%1$s> rdfs:label ?rdfsLabel .
			OPTIONAL { <%1$s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?prefLabel . }
			OPTIONAL { <%1$s> <http://www.w3.org/2004/02/skos/core#altLabel> ?altLabel . }
			BIND ( IF ( BOUND (?prefLabel), ?prefLabel,\s
			     \t\tIF ( BOUND (?altLabel), ?altLabel, ?rdfsLabel ) )
			     \t AS ?label ) .""";

	/**
	 * Returns a rdfs:label of the given concept in the given language, if existing.
	 *
	 * @param uri full uri of the concept to be labeled
	 */
	public static String getLabel(String uri, Rdf2GoCore repo, Locale... locales) {
		try {
			new java.net.URI(uri);
		}
		catch (URISyntaxException e) {
			LOGGER.error(e.getMessage());
			return uri;
		}
		return getLabel(repo.createIRI(uri), repo, locales);
	}

	/**
	 * Returns a rdfs:label of the given concept in the given language, if existing.
	 */
	public static String getLabel(IRI concept, Rdf2GoCore repo, Locale... locales) {

		// try to find language specific label
		String labelQuery = String.format(SPARQL_LABEL, concept.stringValue());

		String query = "SELECT ?label WHERE { "
				+ labelQuery
				+ "}";
		TupleQueryResult resultTable = repo.sparqlSelect(query);
		List<BindingSet> bindingSets = resultTable.getBindingSets();

		Map<Locale, String> labels = new HashMap<>();
		for (BindingSet bindingSet : bindingSets) {
			Text label = Sparqls.asText(bindingSet.getBinding("label"));
			if (label == null) continue;
			if (locales.length == 0 && label.getLanguage() == Locale.ROOT) {
				return label.getString();
			}
			labels.put(label.getLanguage(), label.getString());
		}
		Locale bestLocale = Locales.findBestLocale(Arrays.asList(locales), labels.keySet());
		return labels.get(bestLocale);
	}

	@Nullable
	public static Rdf2GoCore getRdf2GoCore(@Nullable UserContext context, Section<?> section) {
		Rdf2GoCompiler compiler = Compilers.getCompiler(context, section, Rdf2GoCompiler.class);
		if (compiler != null) {
			return compiler.getRdf2GoCore();
		}
		return null;
	}

	public static Rdf2GoCore getRdf2GoCore(Section<?> section) {
		return getRdf2GoCore(null, section);
	}

	public static Statement[] toArray(Collection<Statement> statements) {
		return statements.toArray(new Statement[0]);
	}

	/**
	 * If the string starts with a known namespace or its abbreviation, the namespace is removed
	 * (only from the start of the string).
	 *
	 * @param string the string where the namespace or its abbreviation needs to be removed
	 * @return the string without the namespace prefix
	 * @created 12.07.2012
	 */
	public static String trimNamespace(Rdf2GoCore core, String string) {
		if (string.startsWith(":")) return string.substring(1);
		for (Entry<String, String> namespaceEntry : core.getNamespacesMap().entrySet()) {
			String ns = namespaceEntry.getValue();
			if (string.startsWith(ns)) {
				string = string.substring(ns.length());
				break;
			}
			String nsAbbreviationPrefix = toNamespacePrefix(namespaceEntry.getKey());
			if (string.startsWith(nsAbbreviationPrefix)) {
				string = string.substring(nsAbbreviationPrefix.length());
				break;
			}
		}
		return string;
	}

	/**
	 * If the string ends with a known namespace data type (like ^^xsd:int...), this data type is
	 * removed from the string.
	 *
	 * @param string the string where the data type is removed
	 * @return the string without the data type
	 * @created 12.07.2012
	 */
	public static String trimDataType(Rdf2GoCore core, String string) {
		return string.replaceAll("(?:\\.0)?\\^\\^\\w+:.+$", "");
	}

	/**
	 * Normally, SPARQL supports comments using # at the start of the line. If the used repository
	 * does not support this, we can use this method instead.
	 *
	 * @param query the query where the comments should be removed
	 * @return the string without the comment
	 * @created 12.07.2012
	 */
	public static String removeSparqlComments(String query) {
		return query.replaceAll("(?m)^\\s*#.*?$", "");
	}

	/**
	 * Reduces to full URI prefix to the abbreviation of the URI.
	 *
	 * @param string the string where the namespace needs to be reduced
	 * @return the string with the prefix instead of the full namespace
	 * @created 06.12.2010
	 */
	public static String reduceNamespace(Rdf2GoCore core, String string) {
		for (Entry<String, String> cur : core.getNamespacesMap().entrySet()) {
			string = string.replaceAll("^" + Pattern.quote(cur.getValue()),
					toNamespacePrefix(cur.getKey()));
		}
		// also checking local namespace
		if (string.startsWith(core.getLocalNamespace())) {
			string = string.replace(core.getLocalNamespace(), Rdf2GoCore.LNS_ABBREVIATION + ":");
		}
		return string;
	}

	/**
	 * Reduces to full URI prefix to the abbreviation of the URI.
	 *
	 * @param uri the URI where the namespace needs to be reduced
	 * @return the string with the prefix instead of the full namespace
	 * @created 12.12.2018
	 */
	public static String reduceNamespace(Rdf2GoCore core, URI uri) {
		return reduceNamespace(core, uri.toString());
	}

	/**
	 * Creates and returns the namespace prefixes (with the namespaces known to the {@link
	 * Rdf2GoCore}) needed for a SPARQL query.
	 *
	 * @return the namespace prefixes for a SPARQL query.
	 * @created 15.07.2012
	 */
	public static String getSparqlNamespaceShorts(Collection<Namespace> namespaces) {
		StringBuilder buffy = new StringBuilder();
		for (Namespace cur : namespaces) {
			buffy.append("PREFIX ")
					.append(toNamespacePrefix(cur.getPrefix()))
					.append(" <")
					.append(cur.getName())
					.append("> \n");
		}
		return buffy.toString();
	}

	/**
	 * A prefix is the abbreviation of the namespace plus a colon. This method makes sure the given
	 * String has the colon at the end.
	 *
	 * @param namespaceAbbreviation the abbreviation with possibly no colon
	 * @return a proper prefix (abbreviation + colon)
	 * @created 15.07.2012
	 */
	public static String toNamespacePrefix(String namespaceAbbreviation) {
		if (!namespaceAbbreviation.endsWith(":")) {
			return namespaceAbbreviation + ":";
		}
		return namespaceAbbreviation;
	}

	/**
	 * Checks whether the given string starts with an prefix of a known namespace abbreviation. If
	 * it does, the abbreviation is returned (including the colon).
	 *
	 * @param string the string to be parsed for a known prefix abbreviation
	 * @return the prefix of the namespace found at the start of the given string
	 * @created 15.07.2012
	 */
	public static String parseKnownNamespacePrefix(Rdf2GoCore core, String string) {
		if (core == null) return null;

		if (string.startsWith(":")) {
			if (core.getNamespacePrefixes().containsKey(":")) {
				return ":";
			}
			return null;
		}

		for (String prefix : core.getNamespacePrefixes().keySet()) {
			if (string.startsWith(prefix)) {
				return prefix;
			}
		}

		return null;
	}

	/**
	 * Expands the namespace abbreviation prefix in the given string to a full URL prefix (if the
	 * abbreviation is known to the {@link Rdf2GoCore}). If no valid abbreviation is found, the
	 * string is returned.
	 *
	 * @param string the string with the namespace abbreviation to expand
	 * @return the string with an expanded namespace
	 * @created 04.01.2011
	 */
	public static String expandNamespace(Rdf2GoCore core, String string) {
		String prefix = parseKnownNamespacePrefix(core, string);
		if (prefix == null) return string;
		return core.getNamespacePrefixes().get(prefix) + string.substring(prefix.length());
	}

	/**
	 * Generates the tree of all classes that the concept belongs to. The tree contains all class
	 * the the concept is 'rdf:type' of. Further, if for two class in the tree 'A rdfs:subClassOf B'
	 * holds, then A is a successor of B in the generated tree.
	 *
	 * @param core    the repository to work with
	 * @param concept the concept for which the class tree should be generated
	 * @return the tree of all classes that the concept belongs to
	 */
	public static PartialHierarchyTree<IRI> getClassHierarchy(Rdf2GoCore core, IRI concept) {
		return getClassHierarchy(core, concept, "rdfs:subClassOf", "rdf:type");
	}

	/**
	 * Returns the most specific class of the concept where '<concept> rdf:type <class>' holds. For
	 * most specific one is considered to be the leaf class which has the longest path (highest
	 * depth) in the tree of all classes of the concept. If there are multiple deepest classes with
	 * same depth, the result is one of those (randomly).
	 */
	public static IRI findMostSpecificClass(Rdf2GoCore core, IRI concept) {
		return findMostSpecificClass(getClassHierarchy(core, concept));
	}

	/**
	 * Returns the most specific class the given hierarchy of classes. For most specific one is
	 * considered to be the leaf class which has the longest path (highest depth) in given
	 * hierarchy. If there are multiple deepest classes with same depth, the result is one of those
	 * (randomly).
	 */
	public static IRI findMostSpecificClass(PartialHierarchyTree<IRI> classHierarchy) {
		final Set<PartialHierarchyTree.Node<IRI>> nodes = classHierarchy.getNodes();
		int maxDepth = 0;
		PartialHierarchyTree.Node<IRI> deepestLeaf = classHierarchy.getRoot();
		for (PartialHierarchyTree.Node<IRI> node : nodes) {
			int depth = classHierarchy.getMaxDepthLevel(node);
			if (depth >= maxDepth) {
				maxDepth = depth;
				deepestLeaf = node;
			}
		}
		return deepestLeaf.getData();
	}

	/**
	 * @param core             the repository to work with
	 * @param concept          the concept for which the class tree should be generated
	 * @param subClassRelation the relation that defines the subclass hierarchy (usually
	 *                         rdfs:subClassOf)
	 * @param typeRelation     the property defining an instanceof relation (usally rdf:type)
	 * @return the tree of all classes that the concept belongs to
	 * @see de.knowwe.rdf2go.utils.Rdf2GoUtils#getClassHierarchy(de.knowwe.rdf2go.Rdf2GoCore,
	 * IRI, String, String)
	 */
	public static PartialHierarchyTree<IRI> getClassHierarchy(Rdf2GoCore core, IRI concept, String subClassRelation, String typeRelation) {
		final SubClassHierarchy subClassHierarchy = new SubClassHierarchy(core, subClassRelation);
		PartialHierarchyTree<IRI> tree = new PartialHierarchyTree<>(subClassHierarchy);

        /*
		build up tree of classes
         */
		String classQuery = "SELECT ?c WHERE { <" + concept.stringValue() + "> " + typeRelation + " ?c }";
		final TupleQueryResult queryResultTable = core.sparqlSelect(classQuery);
		for (BindingSet queryRow : queryResultTable.getBindingSets()) {
			Value c = queryRow.getValue("c");
			if (c instanceof BNode) continue;
			IRI classUri = (IRI) c;
			tree.insertNode(classUri);
		}
		return tree;
	}

	private static class SubClassHierarchy implements PartialHierarchy<IRI> {
		private final Rdf2GoCore core;
		private final String subClassRelation;

		public SubClassHierarchy(Rdf2GoCore core, String subClassRelation) {
			this.core = core;
			this.subClassRelation = subClassRelation;
		}

		@Override
		public boolean isSuccessorOf(IRI node1, IRI node2) {
			if(node1.equals(node2)) {
				return false;
			}
			return core.sparqlAsk("ASK { <" + node1.stringValue() + "> " + subClassRelation + " <" + node2.stringValue() + "> }");
		}
	}

	public static String createSparqlString(Rdf2GoCore core, String sparqlString) {
		sparqlString = removeSparqlComments(sparqlString);
		sparqlString = Strings.trim(sparqlString);

		Map<String, String> nameSpaces = core.getNamespacesMap();

		StringBuilder newSparqlString = new StringBuilder();
		StringBuilder pattern = new StringBuilder("[\\s\n\r()]<((");
		boolean first = true;
		for (String nsShort : nameSpaces.keySet()) {
			if (first) {
				first = false;
			}
			else {
				pattern.append("|");
			}
			pattern.append(nsShort);
		}
		pattern.append("):)[^ /]");
		int lastEnd = 0;
		Matcher matcher = Pattern.compile(pattern.toString()).matcher(sparqlString);
		while (matcher.find()) {
			int start = matcher.start(1);
			int end = matcher.end(2);
			String nsLong = nameSpaces.get(matcher.group(2));
			newSparqlString.append(sparqlString, lastEnd, start);
			newSparqlString.append(nsLong);
			lastEnd = end + 1;
		}

		newSparqlString.append(sparqlString.subSequence(lastEnd, sparqlString.length()));
		sparqlString = newSparqlString.toString();

		return sparqlString;
	}

	public static void addStringLiteral(Rdf2GoCore core, String subject, String predicate, String literalText, Collection<Statement> statements) {
		addStatement(core, core.createLocalIRI(subject), core.createLocalIRI(predicate), core.createLiteral(literalText), statements);
	}

	/**
	 * Creates a statement and adds it to the list of statements. Additionally, in case of RDF
	 * reasoning, the rdfs label of the object is created and added.
	 */
	public static void addStatement(Rdf2GoCore core, String subject, String predicate, String object, Collection<Statement> statements) {
		addStatement(core, core.createLocalIRI(subject), core.createLocalIRI(predicate), object, statements);
	}

	/**
	 * Creates a statement and adds it to the list of statements. Additionally, in case of RDF
	 * reasoning, the rdfs label of the object is created and added.
	 */
	public static void addStatement(Rdf2GoCore core, org.eclipse.rdf4j.model.Resource subject, IRI predicate, String object, Collection<Statement> statements) {
		addStatement(core, subject, predicate, core.createLocalIRI(object), statements);
	}

	public static void addStatement(Rdf2GoCore core, org.eclipse.rdf4j.model.Resource subject, IRI predicate, Value object, Collection<Statement> statements) {
		statements.add(core.createStatement(subject, predicate, object));
	}

	public static String getCleanedExternalForm(Identifier identifier) {
		String externalForm = identifier.toPrettyPrint();
		if (identifier.countPathElements() == 1) {
			externalForm = Strings.unquote(externalForm);
		}
		return externalForm;
	}

	public static RDFFormat syntaxForFileName(String fileName) {
		return Rio.getParserFormatForFileName(fileName).orElse(RDFFormat.RDFXML);
	}

	/**
	 * Generates SPARQL query code selecting a string literal with the given preferred label and binds it to the
	 * object. If multiple locales are provided, the other locales function as a fallback, if the Literal is not
	 * available in the first locale(s).
	 * <p></p>
	 * <b>Example:</b> You want the string literal title of an article. Normally you would write something like<br>
	 * <tt>?article lns:hasTitle ?Title</tt><br>
	 * To get it with the preferred label, use the method the following way:
	 * <tt>getPreferredLocaleLabel("?article lns:hasTitle", "?Title", getPreferredLocales())</tt><br>
	 * The title will then be bound with the correct locale.
	 *
	 * @param subjectPredicate the subject and predicate of the sparql line you would write normally
	 * @param object           the object variable name you would write normally
	 * @param locales          the locales you want the object in, sorted by priority
	 * @return SPARQL code binding the object with the desired locales string literal
	 */
	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	public static String getPreferredLocaleLabel(String subjectPredicate, String object, Locale... locales) {
		StringBuilder builder = new StringBuilder();
		for (Locale locale : locales) {
			String localeAbbreviation = locale.toString().toUpperCase();
			builder.append("  OPTIONAL {\n");
			builder.append("    " + subjectPredicate + " " + object + localeAbbreviation + " .\n");
			builder.append("    FILTER langMatches(lang(" + object + localeAbbreviation + "), \"" + localeAbbreviation + "\") .\n");
			builder.append("  }\n\n");
		}
		builder.append("  BIND ( COALESCE (");
		insertLocaleLabels(new LinkedList<>(Arrays.asList(locales)), object, builder);
		builder.append(") AS " + object + ") .\n\n");
		return builder.toString();
	}

	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	private static void insertLocaleLabels(LinkedList<Locale> locales, String labelVar, StringBuilder builder) {
		for (Locale locale : locales) {
			builder.append(labelVar + locale.toString().toUpperCase()).append(", ");
		}
		builder.append("\"\"");
	}

	private static String getDoubleAsString(Double doubleValue) {
		if (doubleValue.equals(Double.POSITIVE_INFINITY)) return "INF";
		if (doubleValue.equals(Double.NEGATIVE_INFINITY)) return "-INF";
		if (doubleValue.equals(Double.NaN)) return "NaN";
		return doubleValue.toString();
	}

	public static org.eclipse.rdf4j.model.Literal createDoubleLiteral(Rdf2GoCore core, double d) {
		return core.createDatatypeLiteral(getDoubleAsString(d), XSD.DOUBLE);
	}

	public static org.eclipse.rdf4j.model.Literal createDateTimeLiteral(Rdf2GoCore core, Date date) {
		return core.createDatatypeLiteral(createDateTimeString(date), XSD.DATETIME);
	}

	@NotNull
	public static String createDateTimeString(Date date) {
		String dateTimeString;
		synchronized (PRIVATE_XSD_DATE_TIME_FORMAT) {
			dateTimeString = PRIVATE_XSD_DATE_TIME_FORMAT.format(date);
		}
		return dateTimeString;
	}

	public static Date createDateFromDateTimeLiteral(Literal dateTimeLiteral) throws ParseException {
		synchronized (PRIVATE_XSD_DATE_TIME_FORMAT) {
			return PRIVATE_XSD_DATE_TIME_FORMAT.parse(dateTimeLiteral.stringValue());
		}
	}
}
