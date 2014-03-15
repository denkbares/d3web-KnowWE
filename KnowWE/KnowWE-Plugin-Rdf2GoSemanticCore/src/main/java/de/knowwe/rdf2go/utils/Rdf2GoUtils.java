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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.Rdf2GoCore.Rdf2GoReasoning;

public class Rdf2GoUtils {

	public static Rdf2GoCore getRdf2GoCoreForDefaultMarkupSubSection(Section<? extends Type> section) {
		Section<DefaultMarkupType> defaultMarkup = Sections.findAncestorOfType(section,
				DefaultMarkupType.class);
		if (defaultMarkup == null) {
			throw new IllegalArgumentException();
		}
		return getRdf2GoCore(defaultMarkup);
	}

	public static Rdf2GoCore getRdf2GoCore(Section<? extends DefaultMarkupType> section) {
		String globalAnnotation = DefaultMarkupType.getAnnotation(section, Rdf2GoCore.GLOBAL);
		if (globalAnnotation != null && globalAnnotation.equals("true")) {
			return Rdf2GoCore.getInstance();
		}
		Collection<Rdf2GoCompiler> compilers = Compilers.getCompilers(section,
				Rdf2GoCompiler.class);
		if (!compilers.isEmpty()) {
			return compilers.iterator().next().getRdf2GoCore();
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
	 * If the string starts with a known namespace or its abbreviation, the
	 * namespace is removed (only from the start of the string).
	 *
	 * @param string the string where the namespace or its abbreviation needs to
	 *               be removed
	 * @return the string without the namespace prefix
	 * @created 12.07.2012
	 */
	public static String trimNamespace(Rdf2GoCore core, String string) {
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
	 * Reduces to full URI prefix to the abbreviation of the URI.
	 *
	 * @param string the string where the namespace needs to be reduced
	 * @return the string with the prefix instead of the full namespace
	 * @created 06.12.2010
	 */
	public static String reduceNamespace(Rdf2GoCore core, String string) {
		for (Entry<String, String> cur : core.getNamespaces().entrySet()) {
			string = string.replaceAll(Pattern.quote(cur.getValue()),
					toNamespacePrefix(cur.getKey()));
		}
		// also checking local namespace
		if (string.startsWith(core.getLocalNamespace())) {
			string = string.replace(core.getLocalNamespace(), Rdf2GoCore.LNS_ABBREVIATION + ":");
		}
		return string;
	}

	/**
	 * Creates and returns the namespace prefixes (with the namespaces known to
	 * the {@link Rdf2GoCore}) needed for a SPARQL query.
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
	 * A prefix is the abbreviation of the namespace plus a colon. This method
	 * makes sure the given String has the colon at the end.
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
	 * @return the prefix of the namespace found at the start of the given
	 * string
	 * @created 15.07.2012
	 */
	public static String parseKnownNamespacePrefix(Rdf2GoCore core, String string) {
		if (core != null) {
			for (String prefix : core.getNamespacePrefixes().keySet()) {
				if (string.startsWith(prefix)) {
					return prefix;
				}
			}
		}
		return null;
	}

	/**
	 * Expands the namespace abbreviation prefix in the given string to a full
	 * URL prefix (if the abbreviation is known to the {@link Rdf2GoCore}). If
	 * no valid abbreviation is found, the string is returned.
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
	 * Ensures a properly URL encoded string.
	 */
	public static String cleanUp(String string) {
		String temp = string;
		try {
			temp = URLDecoder.decode(string, "UTF-8");
		}
		catch (IllegalArgumentException e) {
		}
		catch (UnsupportedEncodingException e) {
		}
		return Strings.encodeURL(temp);
	}

	public static String createSparqlString(Rdf2GoCore core, String sparqlString) {
		sparqlString = sparqlString.trim();
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
		URI subjectUri = core.createlocalURI(subject);
		URI predicateUri = core.createlocalURI(predicate);
		Literal literal = core.createLiteral(literalText);
		addStatement(core, subjectUri, predicateUri, literal, statements);
	}

	/**
	 * Creates a statement and adds it to the list of statements. Additionally,
	 * in case of RDF reasoning, the rdfs label of the object is created and
	 * added.
	 */
	public static void addStatement(Rdf2GoCore core, String subject, String predicate, String object, Collection<Statement> statements) {
		URI subjectUri = core.createlocalURI(subject);
		URI predicateUri = core.createlocalURI(predicate);
		addStatement(core, subjectUri, predicateUri, object, statements);
	}

	/**
	 * Creates a statement and adds it to the list of statements. Additionally,
	 * in case of RDF reasoning, the rdfs label of the object is created and
	 * added.
	 */
	public static void addStatement(Rdf2GoCore core, Resource subject, URI predicate, String object, Collection<Statement> statements) {
		URI objectUri = core.createlocalURI(object);
		addStatement(core, subject, predicate, objectUri, statements);
		if (core.getReasoningType().equals(Rdf2GoReasoning.RDF)) {
			addStatement(core, objectUri, RDFS.label, core.createLiteral(object), statements);
		}
	}

	public static void addStatement(Rdf2GoCore core, Resource subject, URI predicate, Node object, Collection<Statement> statements) {
		if (core.getReasoningType().equals(Rdf2GoReasoning.RDF)) {
			createUriLabel(core, subject, statements);
			createUriLabel(core, predicate, statements);
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
