package de.knowwe.rdf2go.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Literal;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.util.RDFTool;
import org.ontoware.rdf2go.vocabulary.RDFS;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.Rdf2GoCore.Rdf2GoReasoning;

public class Rdf2GoUtils {

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
	 * @created 12.07.2012
	 * @param string the string where the namespace or its abbreviation needs to
	 *        be removed
	 * @return the string without the namespace prefix
	 */
	public static String trimNamespace(Rdf2GoCore core, String string) {
		for (Entry<String, String> namespaceEntry : core.getNameSpaces().entrySet()) {
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
	 * @created 06.12.2010
	 * @param string the string where the namespace needs to be reduced
	 * @return the string with the prefix instead of the full namespace
	 */
	public static String reduceNamespace(Rdf2GoCore core, String string) {
		for (Entry<String, String> cur : core.getNameSpaces().entrySet()) {
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
	 * @created 15.07.2012
	 * @return the namespace prefixes for a SPARQL query.
	 */
	public static String getSparqlNamespaceShorts(Rdf2GoCore core) {
		StringBuilder buffy = new StringBuilder();

		for (Entry<String, String> cur : core.getNameSpaces().entrySet()) {
			buffy.append("PREFIX " + toNamespacePrefix(cur.getKey()) + " <" + cur.getValue()
					+ "> \n");
		}
		return buffy.toString();
	}

	/**
	 * A prefix is the abbreviation of the namespace plus a colon. This method
	 * makes sure the given String has the colon at the end.
	 * 
	 * @created 15.07.2012
	 * @param namespaceAbbreviation the abbreviation with possibly no colon
	 * @return a proper prefix (abbreviation + colon)
	 */
	public static String toNamespacePrefix(String namespaceAbbreviation) {
		if (!namespaceAbbreviation.endsWith(":")) {
			return namespaceAbbreviation + ":";
		}
		return namespaceAbbreviation;
	}

	/**
	 * Checks whether the given string starts with an prefix consisting of a
	 * known namespace abbreviation. If it does, the abbreviation is returned
	 * (without the colon).
	 * 
	 * @created 15.07.2012
	 * @param string the string to be parsed for a known abbreviation
	 * @return the abbreviation of the namespace found at the start of the given
	 *         string
	 */
	public static String parseKnownAbbreviation(Rdf2GoCore core, String string) {
		for (String nsAbbreviation : core.getNameSpaces().keySet()) {
			String prefix = toNamespacePrefix(nsAbbreviation);
			if (string.startsWith(prefix)) {
				return nsAbbreviation;
			}
		}
		return null;
	}

	/**
	 * Expands the namespace abbreviation prefix in the given string to a full
	 * URL prefix (if the abbreviation is known to the {@link Rdf2GoCore}). If
	 * no valid abbreviation is found, the string is returned.
	 * 
	 * @created 04.01.2011
	 * @param string the string with the namespace abbreviation to expand
	 * @return the string with an expanded namespace
	 */
	public static String expandNamespace(Rdf2GoCore core, String string) {
		String knownAbbreviation = parseKnownAbbreviation(core, string);
		if (knownAbbreviation == null) return string;
		return string.replaceFirst(Pattern.quote(toNamespacePrefix(knownAbbreviation)),
				core.getNameSpaces().get(knownAbbreviation));
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

	public static String createSparqlString(String sparqlString) {
		sparqlString = sparqlString.trim();
		sparqlString = sparqlString.replaceAll("\n", " ");
		sparqlString = sparqlString.replaceAll("\r", "");

		Map<String, String> nameSpaces = Rdf2GoCore.getInstance().getNameSpaces();

		StringBuilder newSparqlString = new StringBuilder();
		StringBuilder pattern = new StringBuilder("[\\s\n\r()]<((");
		boolean first = true;
		for (String nsShort : nameSpaces.keySet()) {
			if (first) first = false;
			else pattern.append("|");
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

	public static void addStringLiteral(String subject, String predicate, String literalText, Collection<Statement> statements) {
		URI subjectUri = Rdf2GoCore.getInstance().createlocalURI(subject);
		URI predicateUri = Rdf2GoCore.getInstance().createlocalURI(predicate);
		Literal literal = Rdf2GoCore.getInstance().createLiteral(literalText);
		addStatement(subjectUri, predicateUri, literal, statements);
	}

	/**
	 * Creates a statement and adds it to the list of statements. Additionally,
	 * in case of RDF reasoning, the rdfs label of the object is created and
	 * added.
	 * 
	 */
	public static void addStatement(String subject, String predicate, String object, Collection<Statement> statements) {
		Rdf2GoCore core = Rdf2GoCore.getInstance();
		URI subjectUri = core.createlocalURI(subject);
		URI predicateUri = core.createlocalURI(predicate);
		addStatement(subjectUri, predicateUri, object, statements);
	}

	/**
	 * Creates a statement and adds it to the list of statements. Additionally,
	 * in case of RDF reasoning, the rdfs label of the object is created and
	 * added.
	 * 
	 */
	public static void addStatement(Resource subject, URI predicate, String object, Collection<Statement> statements) {
		Rdf2GoCore core = Rdf2GoCore.getInstance();
		URI objectUri = core.createlocalURI(object);
		addStatement(subject, predicate, objectUri, statements);
		if (core.getReasoningType().equals(Rdf2GoReasoning.RDF)) {
			addStatement(objectUri, RDFS.label, core.createLiteral(object), statements);
		}
	}

	public static void addStatement(Resource subject, URI predicate, Node object, Collection<Statement> statements) {
		Rdf2GoCore core = Rdf2GoCore.getInstance();
		if (core.getReasoningType().equals(Rdf2GoReasoning.RDF)) {
			createUriLabel(core, subject, statements);
			createUriLabel(core, predicate, statements);
			createUriLabel(core, object, statements);
		}
		statements.add(Rdf2GoCore.getInstance().createStatement(subject, predicate, object));
	}

	private static void createUriLabel(Rdf2GoCore core, Node node, Collection<Statement> statements) {
		if (node instanceof URI) {
			URI uriNode = node.asURI();
			String uriNodeString = uriNode.toString();
			if (uriNodeString.startsWith(core.getLocalNamespace())) {
				String stringPart = uriNodeString.substring(core.getLocalNamespace().length());
				Literal nodeLiteral = core.createLiteral(Strings.decodeURL(stringPart));
				statements.add(Rdf2GoCore.getInstance().createStatement(uriNode, RDFS.label,
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

}
