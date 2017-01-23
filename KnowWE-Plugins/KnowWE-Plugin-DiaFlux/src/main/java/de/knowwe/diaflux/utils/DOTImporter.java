/*
 * (C) Copyright 2015-2016, by Wil Selwood and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package de.knowwe.diaflux.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.EdgeProvider;
import org.jgrapht.ext.GraphImporter;
import org.jgrapht.ext.ImportException;
import org.jgrapht.ext.VertexProvider;
import org.jgrapht.graph.AbstractBaseGraph;

/**
 * Copy from {@link org.jgrapht.ext.DOTImporter}, adapted a little bit for this purpose.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Adrian Müller
 * @created 23.10.
 */
class DOTImporter<V, E>
		implements GraphImporter<V, E> {
	// Constants for the state machine
	private static final int NEXT = 0;
	private static final int HEADER = 1;
	private static final int NODE = 2;
	private static final int EDGE = 3;
	private static final int LINE_COMMENT = 4;
	private static final int BLOCK_COMMENT = 5;
	private static final int DONE = 32;

	private final VertexProvider<V> vertexProvider;
	private final EdgeProvider<V, E> edgeProvider;

	private String input;
	private int position;
	private Graph<V, E> graph;
	private Map<String, V> vertexes;
	private StringBuilder sectionBuffer;

	/**
	 * Constructs a new DOTImporter with the given providers
	 *
	 * @param vertexProvider Provider to create a vertex
	 * @param edgeProvider   Provider to create an edge
	 */
	DOTImporter(VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider) {
		this.vertexProvider = vertexProvider;
		this.edgeProvider = edgeProvider;
	}

	private static int findEndOfQuote(String input, int start) {
		int result;
		// "start" should be the opening quote
		for (result = input.indexOf('\"', start + 1); result != -1 && input.charAt(result - 1) == '\\';
			 result = input.indexOf('\"', result + 1)) {
			// if the previous character is an escape then search for char escaping the escape and so on
			int firstEscapeIndex = result - 1;
			while ((firstEscapeIndex > -1) && input.charAt(firstEscapeIndex) == '\\') {
				firstEscapeIndex--;
			}
			int numberOfEscapes = result - firstEscapeIndex + 1;
			if (numberOfEscapes % 2 == 0) {
				break;
			}
		}
		return result;
	}

	/**
	 * Read a dot formatted input and populate the provided graph.
	 * <p>
	 * The current implementation reads the whole input as a string and then parses the graph.
	 *
	 * @param graph the graph to update
	 * @param input the input reader
	 * @throws ImportException if there is a problem parsing the file.
	 */
	@Override
	public void importGraph(Graph<V, E> graph, Reader input)
			throws ImportException {
		try {
			this.input = IOUtils.toString(input);
		}
		catch (IOException e) {
			throw new ImportException("IOException: " + e.getLocalizedMessage());
		}
		this.graph = graph;
		vertexes = new HashMap<>();
		position = 0;
		sectionBuffer = new StringBuilder();
		read();
	}

	/**
	 * Read a dot formatted string and populate the provided graph.
	 *
	 * @throws ImportException if there is a problem parsing the file.
	 */
	private void read()
			throws ImportException {
		if ((input == null) || input.isEmpty()) {
			throw new ImportException("Dot string was empty");
		}

		int state = HEADER;
		int lastState = HEADER;

		while ((state != DONE) && (position < input.length())) {
			int currentState = state;
			switch (state) {
				case HEADER:
					state = processHeader();
					break;
				case NODE:
					state = processNode();
					break;
				case EDGE:
					state = processEdge();
					break;
				case LINE_COMMENT:
					state = processLineComment(lastState);
					// when we leave a line comment we need the new line to
					// still appear in the old block
					position--;
					break;
				case BLOCK_COMMENT:
					state = processBlockComment(lastState);
					break;
				case NEXT:
					state = processNext();
					break;

				// DONE not included here as we can't get to it with the while loop.
				default:
					throw new ImportException("Error importing escaped state machine");
			}

			position++;

			if (state != currentState) {
				lastState = currentState;
			}
		}

		// if we get to the end and are some how still in the header the input
		// must be invalid.
		if (state == HEADER) {
			throw new ImportException("Invalid Header");
		}
	}

	/**
	 * Process the header block.
	 *
	 * @return the new state.
	 * @throws ImportException if there is a problem with the header section.
	 */
	private int processHeader()
			throws ImportException {
		if (isStartOfLineComment()) {
			return LINE_COMMENT;
		}

		if (isStartOfBlockComment()) {
			return BLOCK_COMMENT;
		}

		char current = input.charAt(position);
		sectionBuffer.append(current);
		if (current == '{') {
			// reached the end of the header. Validate it.

			String[] headerParts = sectionBuffer.toString().split(" ", 4);
			if (headerParts.length < 3) {
				throw new ImportException("Not enough parts in header");
			}

			int i = 0;
			if (graph instanceof AbstractBaseGraph
					&& ((AbstractBaseGraph<V, E>) graph).isAllowingMultipleEdges()
					&& headerParts[i].equals("strict")) {
				throw new ImportException("graph defines strict but Multigraph given.");
			}
			else if (headerParts[i].equals("strict")) {
				i = i + 1;
			}

			if ((graph instanceof DirectedGraph) && headerParts[i].equals("graph")) {
				throw new ImportException("input asks for undirected graph and directed graph provided.");
			}
			else if (!(graph instanceof DirectedGraph) && headerParts[i].equals("digraph")) {
				throw new ImportException("input asks for directed graph but undirected graph provided.");
			}
			else if (!headerParts[i].equals("graph") && !headerParts[i].equals("digraph")) {
				throw new ImportException("unknown graph type");
			}

			sectionBuffer.setLength(0); // reset the buffer.
			return NEXT;
		}
		return HEADER;
	}

	/**
	 * When we start a new section of the graph we don't know what it is going to be. We work in
	 * here until we can work out what type of section this is.
	 *
	 * @return the next state.
	 * @throws ImportException if there is a problem with creating a node.
	 */
	private int processNext()
			throws ImportException {
		if (isStartOfLineComment()) {
			return LINE_COMMENT;
		}

		if (isStartOfBlockComment()) {
			return BLOCK_COMMENT;
		}

		char current = input.charAt(position);

		// ignore new line characters or section breaks between identified
		// sections.
		if ((current == '\n') || (current == '\r')) {
			return NEXT;
		}

		// if the buffer is currently empty skip spaces too.
		if ((sectionBuffer.length() == 0) && ((current == ' ') || (current == ';'))) {
			return NEXT;
		}

		// If we have a semi colon and some thing in the buffer we must be at
		// the end of a block. as we can't have had a dash yet we must be at the
		// end of a node.
		if (current == ';') {
			processCompleteNode(sectionBuffer.toString());
			sectionBuffer.setLength(0);
			return NEXT;
		}

		sectionBuffer.append(input.charAt(position));
		if (position < (input.length() - 1)) {
			char next = input.charAt(position + 1);
			if (current == '-') {
				if ((next == '-') && (graph instanceof DirectedGraph)) {
					throw new ImportException("graph is directed but undirected edge found");
				}
				else if ((next == '>') && !(graph instanceof DirectedGraph)) {
					throw new ImportException("graph is undirected but directed edge found");
				}
				else if ((next == '-') || (next == '>')) {
					return EDGE;
				}
			}
		}

		if (current == '[') {
			return NODE; // if this was an edge we should have found a dash before
			// here.
		}

		return NEXT;
	}

	/**
	 * Process a node entry. When we detect that we are at the end of the node create it in the
	 * graph.
	 *
	 * @return the next state.
	 * @throws ImportException if there is a problem with creating a node.
	 */
	private int processNode()
			throws ImportException {
		if (isStartOfLineComment()) {
			return LINE_COMMENT;
		}

		if (isStartOfBlockComment()) {
			return BLOCK_COMMENT;
		}

		char current = input.charAt(position);
		sectionBuffer.append(input.charAt(position));
		if (current == '"') {
			skipQuoted();
			return NODE;
		}
		if ((current == ']') || (current == ';')) {
			processCompleteNode(sectionBuffer.toString());
			sectionBuffer.setLength(0);
			return NEXT;
		}

		return NODE;
	}

	private int processEdge() throws ImportException {
		if (isStartOfLineComment()) {
			return LINE_COMMENT;
		}

		if (isStartOfBlockComment()) {
			return BLOCK_COMMENT;
		}

		char current = input.charAt(position);
		sectionBuffer.append(input.charAt(position));
		if (current == '"') {
			skipQuoted();
			return EDGE;
		}

		if ((current == ';') || (current == '\r') || (current == ']')) {
			processCompleteEdge(sectionBuffer.toString());
			sectionBuffer.setLength(0);
			return NEXT;
		}

		return EDGE;
	}

	private void skipQuoted() {
		int endOfQuote = findEndOfQuote(input, position);
		sectionBuffer.append(input.substring(position + 1, endOfQuote + 1));
		position = endOfQuote;
	}

	private int processLineComment(int returnState) {
		char current;
		int endOfLine = position;
		do {
			current = input.charAt(endOfLine++);
		} while (!(current == '\r' || current == '\n'));
		sectionBuffer.append(input.substring(position, endOfLine + 1));
		return returnState;
	}

	private int processBlockComment(int returnState) {
		char current;
		int endOfLine = position;
		do {
			current = input.charAt(endOfLine++);
		} while (!(current == '/' && input.charAt(position - 1) == '*'));
		return returnState;
	}

	private boolean isStartOfLineComment() {
		char current = input.charAt(position);
		if (current == '#') {
			return true;
		}
		else if (current == '/') {
			if (position < (input.length() - 1)) {
				if (input.charAt(position + 1) == '/') {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isStartOfBlockComment() {
		char current = input.charAt(position);
		if (current == '/') {
			if (position < (input.length() - 1)) {
				if (input.charAt(position + 1) == '*') {
					return true;
				}
			}
		}
		return false;
	}

	private void processCompleteNode(String node)
			throws ImportException {
		Map<String, String> attributes = extractAttributes(node);

		String id = node.trim();
		int bracketIndex = node.indexOf('[');
		if (bracketIndex > 0) {
			id = node.substring(0, node.indexOf('[')).trim();
		}

		V existing = vertexes.get(id);
		if (existing == null) {
			V vertex = vertexProvider.buildVertex(id, attributes);
			if (vertex != null) {
				graph.addVertex(vertex);
				vertexes.put(id, vertex);
			}
		}
		else {
			throw new ImportException(
					"Update required for vertex " + id + " but no vertexUpdater provided");
		}
	}

	private void processCompleteEdge(String edge)
			throws ImportException {
		Map<String, String> attributes = extractAttributes(edge);

		List<String> ids = extractEdgeIds(edge);

		// for each pair of ids in the list create an edge.
		for (int i = 0; i < (ids.size() - 1); i++) {
			V v1 = getVertex(ids.get(i));
			V v2 = getVertex(ids.get(i + 1));

			E resultEdge = edgeProvider.buildEdge(v1, v2, attributes.get("label"), attributes);
			graph.addEdge(v1, v2, resultEdge);
		}
	}

	// if a vertex id doesn't already exist create one for it
	// with no attributes.
	private V getVertex(String id) {
		V v = vertexes.get(id);
		if (v == null) {
			v = vertexProvider.buildVertex(id, new HashMap<>());
			graph.addVertex(v);
			vertexes.put(id, v);
		}
		return v;
	}

	private List<String> extractEdgeIds(String line) {
		String idChunk = line.trim();
		if (idChunk.endsWith(";")) {
			idChunk = idChunk.substring(0, idChunk.length() - 1);
		}
		int bracketIndex = idChunk.indexOf('[');
		if (bracketIndex > 1) {
			idChunk = idChunk.substring(0, bracketIndex).trim();
		}
		int index = 0;
		List<String> ids = new ArrayList<>();
		while (index < idChunk.length()) {
			int nextSpace = idChunk.indexOf(' ', index);
			String chunk;
			if (nextSpace > 0) { // is this the last chunk
				chunk = idChunk.substring(index, nextSpace);
				index = nextSpace + 1;
			}
			else {
				chunk = idChunk.substring(index);
				index = idChunk.length() + 1;
			}
			if (!chunk.equals("--") && !chunk.equals("->")) { // a label then?
				ids.add(chunk);
			}
		}

		return ids;
	}

	private Map<String, String> extractAttributes(String line)
			throws ImportException {
		Map<String, String> attributes = new HashMap<>();
		int bracketIndex = line.indexOf("[");
		if (bracketIndex > 0) {
			attributes =
					splitAttributes(line.substring(bracketIndex + 1, line.lastIndexOf(']')).trim());
		}
		return attributes;
	}

	private Map<String, String> splitAttributes(String section)
			throws ImportException {
		int index = 0;
		Map<String, String> result = new HashMap<>();
		while (index < section.length()) {
			// skip any leading white space
			index = skipWhiteSpace(section, index);

			// Now check for quotes
			int endOfKey = findEndOfSection(section, index, "=");
			if (endOfKey < 0) {
				throw new ImportException("Invalid attributes");
			}
			if (section.charAt(endOfKey) == '"') {
				index = index + 1;
			}

			String key = section.substring(index, endOfKey).trim();

			if ((endOfKey + 1) >= section.length()) {
				throw new ImportException("Invalid attributes");
			}

			// Attribute value may be quoted or a single word.
			// First ignore any white space before the start
			int start = skipWhiteSpace(section, endOfKey + 1);

			int endChar = findEndOfSection(section, start, ",\n\t\t");
			if (section.charAt(start) == '"') {
				start = start + 1;
			}

			if (endChar < 0) {
				endChar = section.length();
			}

			String value = section.substring(start, endChar);
			result.put(key, value);
			if (endChar < section.length() && section.charAt(endChar) == '"') {
				endChar++;
			}
			index = endChar + 1;
		}
		return result;
	}

	private int skipWhiteSpace(String section, int start)
			throws ImportException {
		int i = 0;
		while (Character.isWhitespace(section.charAt(start + i))
				|| (section.charAt(start + i) == '=')) {
			i = i + 1;
			if ((start + i) >= section.length()) {
				throw new ImportException("Invalid attributes");
			}
		}

		return start + i;
	}

	private int findEndOfSection(String section, int start, String terminator) {
		if (section.charAt(start) == '"') {
			return findEndOfQuote(section, start);
		}
		else {
			return section.indexOf(terminator, start);
		}
	}

}

// End DOTImporter.java
