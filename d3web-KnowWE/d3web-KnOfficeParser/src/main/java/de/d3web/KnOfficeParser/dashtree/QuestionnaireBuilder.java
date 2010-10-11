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

package de.d3web.KnOfficeParser.dashtree;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.tools.ant.util.ReaderInputStream;

import de.d3web.KnOfficeParser.DefaultLexer;
import de.d3web.KnOfficeParser.KnOfficeParameterSet;
import de.d3web.KnOfficeParser.KnOfficeParser;
import de.d3web.KnOfficeParser.util.DefaultD3webLexerErrorHandler;
import de.d3web.KnOfficeParser.util.DefaultD3webParserErrorHandler;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.report.Message;

/**
 * This builder handles Questionnaires/QContainers.
 * 
 * @author Markus Friedrich
 * @author Alex Legler
 */
public class QuestionnaireBuilder implements DashTBuilder, KnOfficeParser {

	private IDObjectManagement idom;
	private final String file;
	private final List<Message> errors = new ArrayList<Message>();

	/**
	 * Little internal tree to cache the QContainers, allowing for a sorted
	 * inclusion.
	 */
	private final Node cacheTree = new Node("root", null, Integer.MAX_VALUE, null);

	/**
	 * The start-up questions
	 */
	private final Map<Integer, QASet> startQContainers = new HashMap<Integer, QASet>();

	private Node prevNode = cacheTree;

	/**
	 * Internal class representing a QContainer in the hierarchy
	 * 
	 * @author Alex Legler
	 */
	class Node {

		private final String content;
		private final String id;
		private final String description;
		private final int order;
		private final List<Node> children = new LinkedList<Node>();
		private Node parent;

		/**
		 * Creates a new Node.
		 * 
		 * @param content Name of the questionnaire
		 * @param order The initial questionnaire order ([X] in the markup)
		 * @param description An optional description
		 */
		public Node(String content, String id, int order, String description) {
			this.content = content;
			this.id = id;
			this.order = order;
			this.description = description;
		}

		public int getOrder() {
			return order;
		}

		public String getContent() {
			return content;
		}

		public String getDescription() {
			return description;
		}

		public void setParent(Node p) {
			parent = p;
		}

		public Node getParent() {
			return parent;
		}

		public List<Node> getChildren() {
			return Collections.unmodifiableList(children);
		}

		public boolean addChild(Node child) {
			if (child == null || children.contains(child)) return false;

			children.add(child);
			child.setParent(this);
			return true;
		}

		public int getLevel() {
			return (parent == null) ? -1 : parent.getLevel() + 1;
		}
	}

	public QuestionnaireBuilder(String file, IDObjectManagement idom) {
		this.file = file;
		this.idom = idom;
	}

	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext) {
		throw new AssertionError("should not be called");
	}

	/**
	 * Processes a Questionnaire item
	 */
	@Override
	public void addNode(int dashes, String name, String ref, int line, String description, int order) {
		Node node = new Node(name, ref, order, description);

		// If we go one level deeper
		if (dashes > prevNode.getLevel()) {
			prevNode.addChild(node);
			// Same level
		}
		else if (dashes == prevNode.getLevel()) {
			prevNode.getParent().addChild(node);
			// One up
		}
		else {
			prevNode.getParent().getParent().addChild(node);
		}

		prevNode = node;
	}

	@Override
	public void finishOldQuestionsandConditions(int dashes) {
		throw new AssertionError("should not be called");
	}

	@Override
	public void line(String text) {
	}

	@Override
	public void newLine() {
	}

	@Override
	public void setallowedNames(List<String> allowedNames, int line,
			String linetext) {
		throw new AssertionError("should not be called");
	}

	@Override
	public List<Message> addKnowledge(Reader r,
			IDObjectManagement idom, KnOfficeParameterSet s) {
		this.idom = idom;
		ReaderInputStream input = new ReaderInputStream(r);
		ANTLRInputStream istream = null;
		try {
			istream = new ANTLRInputStream(input);
		}
		catch (IOException e1) {
			errors.add(MessageKnOfficeGenerator.createAntlrInputError(file, 0,
					""));
		}
		DefaultLexer lexer = new DefaultLexer(istream,
				new DefaultD3webLexerErrorHandler(errors, file));
		lexer.setNewline(true);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		DashTree parser = new DashTree(tokens, this,
				new DefaultD3webParserErrorHandler(errors, file, "BasicLexer"));
		try {
			parser.knowledge();
		}
		catch (RecognitionException e) {
			e.printStackTrace();
		}

		finish();

		return getErrors();
	}

	/**
	 * Adds the cached QContainer tree to the KBM and sets the ordered
	 * QContainers as start-up questionnaires.
	 */
	private void finish() {
		processNode(cacheTree, null);

		if (startQContainers.isEmpty()) return;

		List<Integer> initQuestionsIndices = new ArrayList<Integer>(startQContainers.keySet());
		Collections.sort(initQuestionsIndices);

		List<QASet> questions = new LinkedList<QASet>();

		for (Integer key : initQuestionsIndices) {
			questions.add(startQContainers.get(key));
		}

		// add previous init questions to new list
		questions.addAll(idom.getKnowledgeBase().getInitQuestions());

		// set new list
		idom.getKnowledgeBase().setInitQuestions(questions);

	}

	/**
	 * Adds a single node to the KBM.
	 * 
	 * @param n Node to process
	 * @param parent Parent node (if applicable)
	 */
	private void processNode(Node n, QASet parent) {
		List<Node> children = n.getChildren();

		if (children.size() > 0) {
			for (Node child : children) {
				QContainer q = idom.createQContainer(child.id, child.getContent(),
						(parent == null)
								? idom.getKnowledgeBase().getRootQASet()
								: parent);

				if (child.getDescription() != null) q.getInfoStore().addValue(
						BasicProperties.EXPLANATION, child.getDescription());

				if (child.getOrder() > 0) {
					if (startQContainers.keySet().contains(child.getOrder())) errors.add(MessageKnOfficeGenerator.createAmbiguousOrderError(
							file, 0, "", child.getOrder()));
					else startQContainers.put(child.getOrder(), q);
				}

				processNode(child, q);
			}
		}
	}

	public List<Message> getErrors() {
		List<Message> ret = new ArrayList<Message>(errors);
		if (ret.size() == 0) {
			ret.add(MessageKnOfficeGenerator.createQContainerParsedNote(file, 0, "",
					idom.getKnowledgeBase()
							.getQContainers().size() - 1));
		}
		return ret;
	}

	@Override
	public List<Message> checkKnowledge() {
		return getErrors();
	}

	public static List<Message> parse(Reader reader, IDObjectManagement idom) {
		QuestionnaireBuilder builder = new QuestionnaireBuilder("", idom);
		return builder.addKnowledge(reader, idom, null);
	}

	@Override
	public void addInclude(String url, int line, String linetext) {
		throw new AssertionError("should not be called");
	}

}