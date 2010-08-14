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
import java.util.HashMap;
import java.util.List;

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
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.report.Message;

public class SolutionsBuilder implements DashTBuilder, KnOfficeParser {

	public static List<Message> parse(Reader reader,
			KnowledgeBaseManagement kbm, IDObjectManagement idom) {
		SolutionsBuilder builder = new SolutionsBuilder("", idom);
		return builder.addKnowledge(reader, idom, null);
	}

	private List<String> allowedNames;
	private List<Message> errors = new ArrayList<Message>();
	private String file;
	private HashMap<Integer, Solution> diagParents = new HashMap<Integer, Solution>();
	private IDObjectManagement idom;

	public SolutionsBuilder(String file, IDObjectManagement idom) {
		this.idom = idom;
		this.file = file;
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
		return getErrors();
	}

	/**
	 * @author Sebastian Furth
	 */
	@Override
	public void addNode(int dashes, String diag, String ref, int line,
			String diagDescription, int order) {

		Solution parent;
		Solution newDiag = idom.findSolution(diag);

		// this gets the appropriate parent for the new Diagnosis
		if (dashes == 0) {
			// only possible parent is the rootDiagnosis from KB
			parent = idom.getKnowledgeBase().getRootSolution();
		}
		else {
			// parent is HashMap entry for dashes - 1
			parent = diagParents.get(dashes - 1);
		}

		// create new diagnosis in KB if there isn't already the same diagnosis
		if (newDiag == null) {
			newDiag = idom.createSolution(ref, diag, parent);
		}

		// saves the description of the solution if available
		if (diagDescription != null) {
			newDiag.getProperties().setProperty(Property.EXPLANATION, diagDescription);
		}

		// save created diagnosis in HashMap with dashes representing the key
		// this diagnosis is the parent for diagnoses with dashes + 1
		diagParents.remove(dashes);
		diagParents.put(dashes, newDiag);

	}

	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext) {
		boolean inApostrophes = false;
		if ((des.startsWith("'")) && (des.endsWith("'"))) {
			inApostrophes = true;
			des = des.substring(1, des.length() - 1);
		}
		if (allowedNames != null && (!allowedNames.contains(des))
				&& (!inApostrophes)) {
			errors.add(MessageKnOfficeGenerator.createNameNotAllowedWarning(
					file, line, linetext, des));
		}
		if (!(type.equals("info") || type.equals("url")
				|| type.equals("info.therapy") || type.equals("synonyms"))) {
			errors.add(MessageKnOfficeGenerator.createTypeNotAllowed(file,
					line, linetext, type));
			return;
		}
	}

	@Override
	public void setallowedNames(List<String> allowedNames, int line,
			String linetext) {
		this.allowedNames = allowedNames;
	}

	public List<Message> getErrors() {
		List<Message> ret = new ArrayList<Message>(errors);

		// check if there are any unset links to descriptions
		// at the moment obsolete because links are not parsed
		// for (Tripel<String, Object, Message> t : descriptionlinks) {
		// ret.add(t.third);
		// }

		if (ret.size() == 0) {
			ret.add(MessageKnOfficeGenerator.createSolutionsParsedNote(file, 0, "",
					idom.getKnowledgeBase()
							.getSolutions().size() - 1));
		}

		return ret;
	}

	@Override
	public List<Message> checkKnowledge() {
		return getErrors();
	}

	@Override
	public void addInclude(String url, int line, String linetext) {
		// not necessary in this builder
	}

	@Override
	public void finishOldQuestionsandConditions(int dashes) {
		// not necessary in this builder
	}

	@Override
	public void line(String text) {
		// not necessary in this builder
	}

	@Override
	public void newLine() {
		// not necessary in this builder
	}

}
