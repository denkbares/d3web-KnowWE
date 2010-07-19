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

package de.d3web.we.kdom.decisionTree;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.tools.ant.util.ReaderInputStream;

import de.d3web.KnOfficeParser.DefaultLexer;
import de.d3web.KnOfficeParser.decisiontree.DecisionTree;
import de.d3web.KnOfficeParser.util.DefaultD3webParserErrorHandler;
import de.d3web.report.Message;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.renderer.DefaultLineNumberDeligateRenderer;
import de.d3web.we.kdom.sectionFinder.ExpandedSectionFinderResult;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class QuestionTreeANTLR extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new QuestionTreeKDOMANTLRSectionFinder();
		this.setCustomRenderer(new DefaultLineNumberDeligateRenderer());
	}

	public class QuestionTreeKDOMANTLRSectionFinder extends SectionFinder {

		private QuestionLineKDOMBuilder builder;
		private DecisionTree parser;

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			ReaderInputStream input = new ReaderInputStream(new StringReader(text));
			ANTLRInputStream istream = null;
			try {
				istream = new ANTLRInputStream(input);
			}
			catch (IOException e1) {
				// errors.add(MessageKnOfficeGenerator.createAntlrInputError(file,
				// 0, ""));
			}
			DefaultLexer lexer = new DefaultLexer(istream);
			lexer.setNewline(true);
			CommonTokenStream tokens = new CommonTokenStream(lexer);

			builder = new QuestionLineKDOMBuilder();
			parser = new DecisionTree(tokens, builder,
					new DefaultD3webParserErrorHandler(
							new ArrayList<Message>(), "file", "BasicLexer"));
			parser.setBuilder(builder);

			try {
				parser.knowledge();
			}
			catch (RecognitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Stack<ExpandedSectionFinderResult> s = builder.getSections();

			Stack<ExpandedSectionFinderResult> revert =
					new Stack<ExpandedSectionFinderResult>();
			while (s.size() > 0) {
				revert.push(s.pop());
			}

			ExpandedSectionFinderResult root =
					new ExpandedSectionFinderResult(text, new QuestionTreeANTLR(), -1);
			int offset = 0;
			while (revert.size() > 0) {
				ExpandedSectionFinderResult child = revert.pop();
				child.setStart(offset);
				root.addChild(child);
				offset += child.getText().length();
			}
			List<SectionFinderResult> list = new ArrayList<SectionFinderResult>();
			list.add(root);

			return list;
		}
	}
}
