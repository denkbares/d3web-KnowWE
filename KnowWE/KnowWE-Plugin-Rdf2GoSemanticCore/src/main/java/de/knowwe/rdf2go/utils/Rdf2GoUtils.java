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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.Literal;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.util.RDFTool;
import org.ontoware.rdf2go.vocabulary.RDFS;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Lockable;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.Rdf2GoCore.Rdf2GoReasoning;

public class Rdf2GoUtils {

	/**
	 * Locks the Rdf2GoCore of the given SPARQL iterator, so the underlying ontology model cannot change during
	 * iteration. Be sure to always also unlock (in a try-finally block)! If you do not unlock, nobody can write to the
	 * model again. Ever.
	 * <p><b>Explanation:</b><br>
	 * Some Rdf2Go models have extreme slow downs, if during a SPARQL query new statements are added or removed.
	 * Concurrent SPARQLs however are no problem. Therefore we use a lock that locks exclusively for writing but shared
	 * for reading.
	 * <p/>
	 * Created by Albrecht Striffler (denkbares GmbH) on 25.04.14.
	 */
	public static void lock(ClosableIterator<?> iterator) {
		if (iterator instanceof Lockable) ((Lockable) iterator).lock();
	}

	/**
	 * Unlocks the Rdf2GoCore of the given SPARQL iterator, so the underlying ontology model can change again.
	 * Be sure to always unlock in a try-finally block! If the unlock fails due to an exception, nobody can write to
	 * the model again. Ever.
	 * <p><b>Explanation:</b><br>
	 * Some Rdf2Go models have extreme slow downs, if during a SPARQL query new statements are added or removed.
	 * Concurrent SPARQLs however are no problem. Therefore we use a lock that locks exclusively for writing but shared
	 * for reading.
	 * <p/>
	 * Created by Albrecht Striffler (denkbares GmbH) on 25.04.14.
	 */
	public static void unlock(ClosableIterator<?> iterator) {
		if (iterator instanceof Lockable) ((Lockable) iterator).unlock();
	}

	/**
	 * Locks the Rdf2GoCore of the given QueryResultTable (or ClosableIterable), so the underlying ontology model cannot
	 * change during
	 * iteration. Be sure to always also unlock (in a try-finally block)! If you do not unlock, nobody can write to the
	 * model again. Ever.
	 * <p><b>Explanation:</b><br>
	 * Some Rdf2Go models have extreme slow downs, if during a SPARQL query new statements are added or removed.
	 * Concurrent SPARQLs however are no problem. Therefore we use a lock that locks exclusively for writing but shared
	 * for reading.
	 * <p/>
	 * Created by Albrecht Striffler (denkbares GmbH) on 25.04.14.
	 */
	public static void lock(ClosableIterable table) {
		if (table instanceof Lockable) ((Lockable) table).lock();
	}

	/**
	 * Unlocks the Rdf2GoCore of the QueryResultTable (or ClosableIterable), so the underlying ontology model can change
	 * again.
	 * Be sure to always unlock in a try-finally block! If the unlock fails due to an exception, nobody can write to
	 * the model again. Ever.
	 * <p><b>Explanation:</b><br>
	 * Some Rdf2Go models have extreme slow downs, if during a SPARQL query new statements are added or removed.
	 * Concurrent SPARQLs however are no problem. Therefore we use a lock that locks exclusively for writing but shared
	 * for reading.
	 * <p/>
	 * Created by Albrecht Striffler (denkbares GmbH) on 25.04.14.
	 */
	public static void unlock(ClosableIterable table) {
		if (table instanceof Lockable) ((Lockable) table).unlock();
	}

	public static Rdf2GoCore getRdf2GoCoreForDefaultMarkupSubSection(Section<? extends Type> section) {
		Section<DefaultMarkupType> defaultMarkup = Sections.ancestor(section,
				DefaultMarkupType.class);
		if (defaultMarkup == null) {
			throw new IllegalArgumentException();
		}
		return getRdf2GoCore(defaultMarkup);
	}

	public static boolean isClass(Rdf2GoCore core, URI resource) {
		String query = "ASK { " + resource.toSPARQL() + " rdf:type rdfs:Class .}";
		return core.sparqlAsk(query);
	}

	public static boolean isProperty(Rdf2GoCore core, URI resource) {
		String query = "ASK { " + resource.toSPARQL() + " rdf:type rdf:Property .}";
		return core.sparqlAsk(query);
	}

	public static Collection<URI> getClasses(Rdf2GoCore core, URI instance) {
		String query = "SELECT ?class WHERE { " + instance.toSPARQL() + " rdf:type ?class .}";
		List<URI> resultCollection = new ArrayList<URI>();
		QueryResultTable result = core.sparqlSelect(query);
		ClosableIterator<QueryRow> iterator = result.iterator();
		while (iterator.hasNext()) {
			QueryRow row = iterator.next();
			Node aClassNode = row.getValue("class");
			URI uri = aClassNode.asURI();
			resultCollection.add(uri);
		}
		return resultCollection;
	}

	public static Rdf2GoCore getRdf2GoCore(Section<?> section) {
		if (section.get() instanceof DefaultMarkupType) {
			String globalAnnotation = DefaultMarkupType.getAnnotation(section, Rdf2GoCore.GLOBAL);
			if (globalAnnotation != null && globalAnnotation.equals("true")) {
				return Rdf2GoCore.getInstance();
			}
		}
		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);
		if (compiler != null) {
			return compiler.getRdf2GoCore();
		}
		return null;
	}

	public static Statement[] toArray(Collection<Statement> statements) {
		return statements.toArray(new Statement[statements.size()]);
	}

	public static String getLocalName(Node o) {
		return RDFTool.getLabel(o);
	}

	/**
	 * If the string starts with a known namespace or its abbreviation, the namespace is removed (only from the start
	 * of
	 * the string).
	 *
	 * @param string the string where the namespace or its abbreviation needs to be removed
	 * @return the string without the namespace prefix
	 * @created 12.07.2012
	 */
	public static String trimNamespace(Rdf2GoCore core, String string) {
		if (string.startsWith(":")) return string.substring(1);
		for (Entry<String, String> namespaceEntry : core.getNamespaces().entrySet()) {
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
	 * If the string ends with a known namespace data type (like ^^xsd:int...), this data type is removed from the
	 * string.
	 *
	 * @param string the string where the data type is removed
	 * @return the string without the data type
	 * @created 12.07.2012
	 */
	public static String trimDataType(Rdf2GoCore core, String string) {
		return string.replaceAll("(?:\\.0)?\\^\\^\\w+:.+$", "");
	}

	/**
	 * Normally, SPARQL supports comments using # at the start of the line. If the used repository does not support
	 * this, we can use this method instead.
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
		for (Entry<String, String> cur : core.getNamespaces().entrySet()) {
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
	 * Creates and returns the namespace prefixes (with the namespaces known to the {@link Rdf2GoCore}) needed for a
	 * SPARQL query.
	 *
	 * @return the namespace prefixes for a SPARQL query.
	 * @created 15.07.2012
	 */
	public static String getSparqlNamespaceShorts(Rdf2GoCore core) {
		StringBuilder buffy = new StringBuilder();

		for (Entry<String, String> cur : core.getNamespaces().entrySet()) {
			buffy.append("PREFIX " + toNamespacePrefix(cur.getKey()) + " <" + cur.getValue()
					+ "> \n");
		}
		return buffy.toString();
	}

	/**
	 * A prefix is the abbreviation of the namespace plus a colon. This method makes sure the given String has the
	 * colon
	 * at the end.
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
	 * Checks whether the given string starts with an prefix of a known namespace abbreviation. If it does, the
	 * abbreviation is returned (including the colon).
	 *
	 * @param string the string to be parsed for a known prefix abbreviation
	 * @return the prefix of the namespace found at the start of the given string
	 * @created 15.07.2012
	 */
	public static String parseKnownNamespacePrefix(Rdf2GoCore core, String string) {
		if (core != null) {
			for (String prefix : core.getNamespacePrefixes().keySet()) {
				if (string.startsWith(prefix)) {
					return prefix;
				}
			}

			// also checking local namespace
			if (string.startsWith(":")) {
				return Rdf2GoCore.LNS_ABBREVIATION + ":";
			}
		}
		return null;
	}

	/**
	 * Expands the namespace abbreviation prefix in the given string to a full URL prefix (if the abbreviation is known
	 * to the {@link Rdf2GoCore}). If no valid abbreviation is found, the string is returned.
	 *
	 * @param string the string with the namespace abbreviation to expand
	 * @return the string with an expanded namespace
	 * @created 04.01.2011
	 */
	public static String expandNamespace(Rdf2GoCore core, String string) {
		if (string.startsWith(":")) string = Rdf2GoCore.LNS_ABBREVIATION + string;
		String prefix = parseKnownNamespacePrefix(core, string);
		if (prefix == null) return string;
		return core.getNamespacePrefixes().get(prefix) + string.substring(prefix.length());
	}

	public static String createSparqlString(Rdf2GoCore core, String sparqlString) {
		sparqlString = removeSparqlComments(sparqlString);
		sparqlString = Strings.trim(sparqlString);
		sparqlString = sparqlString.replaceAll("\n", " ");
		sparqlString = sparqlString.replaceAll("\r", "");

		Map<String, String> nameSpaces = core.getNamespaces();

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
			newSparqlString.append(sparqlString.substring(lastEnd, start));
			newSparqlString.append(nsLong);
			lastEnd = end + 1;
		}

		newSparqlString.append(sparqlString.subSequence(lastEnd, sparqlString.length()));
		sparqlString = newSparqlString.toString();

		return sparqlString;
	}

	public static void addStringLiteral(Rdf2GoCore core, String subject, String predicate, String literalText, Collection<Statement> statements) {
		addStatement(core, core.createlocalURI(subject), core.createlocalURI(predicate), core.createLiteral(literalText), statements);
	}

	/**
	 * Creates a statement and adds it to the list of statements. Additionally, in case of RDF reasoning, the rdfs
	 * label of the object is created and added.
	 */
	public static void addStatement(Rdf2GoCore core, String subject, String predicate, String object, Collection<Statement> statements) {
		addStatement(core, core.createlocalURI(subject), core.createlocalURI(predicate), object, statements);
	}

	/**
	 * Creates a statement and adds it to the list of statements. Additionally, in case of RDF reasoning, the rdfs
	 * label of the object is created and added.
	 */
	public static void addStatement(Rdf2GoCore core, Resource subject, URI predicate, String object, Collection<Statement> statements) {
		addStatement(core, subject, predicate, core.createlocalURI(object), statements);
	}

	public static void addStatement(Rdf2GoCore core, Resource subject, URI predicate, Node object, Collection<Statement> statements) {
		if (core.getReasoningType().equals(Rdf2GoReasoning.RDF)) {
			createUriLabel(core, subject, statements);
			// createUriLabel(core, predicate, statements);
			createUriLabel(core, object, statements);
		}
		statements.add(core.createStatement(subject, predicate, object));
	}

	private static void createUriLabel(Rdf2GoCore core, Node node, Collection<Statement> statements) {
		if (node instanceof URI) {
			URI uriNode = node.asURI();
			String uriNodeString = uriNode.toString();
			if (uriNodeString.startsWith(core.getLocalNamespace())) {
				String stringPart = uriNodeString.substring(core.getLocalNamespace().length());
				Literal nodeLiteral = core.createLiteral(Strings.decodeURL(stringPart));
				statements.add(core.createStatement(uriNode, RDFS.label,
						nodeLiteral));
			}
		}
	}

	public static String getCleanedExternalForm(Identifier identifier) {
		String externalForm = identifier.toExternalForm();
		if (identifier.countPathElements() == 1) {
			externalForm = Strings.unquote(externalForm);
		}
		return externalForm;
	}

	public static Syntax syntaxForFileName(String fileName) {
		for (Syntax syntax : Syntax.collection()) {
			if (fileName.toLowerCase().endsWith(syntax.getFilenameExtension())) {
				return syntax;
			}
		}
		return null;
	}

}
