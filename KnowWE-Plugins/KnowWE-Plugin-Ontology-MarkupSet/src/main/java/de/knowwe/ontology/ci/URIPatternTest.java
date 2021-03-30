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
package de.knowwe.ontology.ci;

import java.util.Collection;
import java.util.LinkedList;

import com.denkbares.strings.Identifier;
import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.TestParameter;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.namespace.NamespaceAbbreviationDefinition;
import de.knowwe.ontology.kdom.resource.ResourceDefinition;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 10.01.2014
 */
public class URIPatternTest extends AbstractTest<OntologyCompiler> {

	private static final String URI_CHARS = "[^\\w\\d!#$&;=?\\[]_~-]";
	private static final String FILE_EXTENSION = ".+\\..{2,4}$";
	private static final String HASH_TAG = ".+#.+";
	private static final String VERSION_NUMBER = ".*(v|V|version|Version)\\d+(\\.?\\d+)*.*";
	private static final String QUERY_STRING = ".*\\?.+=.+(&.+=.+)?.*";

	public URIPatternTest() {
		addIgnoreParameter("Ignore-Pattern",
				TestParameter.Type.Regex, TestParameter.Mode.Mandatory,
				"Specifies a regular expression of all terms to be ignored. " +
						"Please note that the term is similar to the abbreviated uri used" +
						"in turtle markups, but uses '#' as a separator.");
	}

	@Override
	public Message execute(OntologyCompiler testObject, String[] args, String[]... ignores) throws InterruptedException {

		TerminologyManager manager = testObject.getTerminologyManager();
		Collection<Identifier> identifiers = manager.getAllDefinedTermsOfType(ResourceDefinition.class);
		Collection<Message> messages = new LinkedList<>();
		for (Identifier identifier : identifiers) {
			// check ignore patterns
			boolean isIgnored = false;
			for (String[] ignore : ignores) {
				if (ignore.length != 1) continue;
				if (identifier.toPrettyPrint().matches(ignore[0])) {
					isIgnored = true;
					break;
				}
			}
			if (!isIgnored && !isNamespaceDefinition(identifier, manager)) {
				// TODO: avoid using auto-increment
				if (matchesPattern(URI_CHARS, identifier.getPathElements())) {
					messages.add(new Message(Message.Type.FAILURE, "URI contains characters that will be encoded: " + identifier));
				}
				if (matchesPattern(FILE_EXTENSION, identifier.getLastPathElement())) {
					messages.add(new Message(Message.Type.FAILURE, "URI contains file extension: " + identifier));
				}
				if (matchesPattern(HASH_TAG, identifier.getLastPathElement())) {
					messages.add(new Message(Message.Type.FAILURE, "concept name contains #. " + identifier));
				}
				if (matchesPattern(VERSION_NUMBER, identifier.getPathElements())) {
					messages.add(new Message(Message.Type.FAILURE, "URIs shouldn't refer to a special version of a concept: "
							+ identifier));
				}
				if (matchesPattern(QUERY_STRING, identifier.getLastPathElement())) {
					messages.add(new Message(Message.Type.FAILURE, "URIs shouldn't use query strings: " + identifier));
				}
			}
		}

		if (!messages.isEmpty()) {
			return new Message(Message.Type.FAILURE, mergeMessages(messages));
		}

		return Message.SUCCESS;
	}

	private boolean isNamespaceDefinition(Identifier identifier, TerminologyManager manager) {
		Collection<Class<?>> classes = manager.getTermClasses(identifier);
		return classes.size() == 1 && classes.contains(NamespaceAbbreviationDefinition.class);
	}

	private boolean matchesPattern(String pattern, String... elements) {
		for (String element : elements) {
			if (!element.isEmpty() && element.matches(pattern)) {
				return true;
			}
		}
		return false;
	}

	private String mergeMessages(Collection<Message> errorMessages) {
		StringBuilder result = new StringBuilder();
		result.append("The following smells where detected:\n");
		for (Message message : errorMessages) {
			result.append(message.getText());
			result.append("\n");
		}
		return result.toString();
	}

	@Override
	public Class<OntologyCompiler> getTestObjectClass() {
		return OntologyCompiler.class;
	}

	@Override
	public String getDescription() {
		return "Checks for all Terms (URIs) whether they follow best practice URL patterns.";
	}
}
