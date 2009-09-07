package de.d3web.we.kdom.dashTree.questionnaires;

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
import de.d3web.KnOfficeParser.dashtree.DashTree;
import de.d3web.KnOfficeParser.util.DefaultD3webParserErrorHandler;
import de.d3web.report.Message;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.dashTree.DashTreeKDOMBuilder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class QuestionnairesKDOMANTLRFinder extends SectionFinder {

	private DashTreeKDOMBuilder builder;
	private DashTree parser;

	public QuestionnairesKDOMANTLRFinder(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmpSection, Section father,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {
		ReaderInputStream input = new ReaderInputStream(new StringReader(tmpSection.getOriginalText()));
		ANTLRInputStream istream = null;
			try {
				istream = new ANTLRInputStream(input);
			} catch (IOException e1) {
				//errors.add(MessageKnOfficeGenerator.createAntlrInputError(file, 0, ""));
			}
		DefaultLexer lexer = new DefaultLexer(istream);
		lexer.setNewline(true);
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		builder = new QuestionnairesKDOMBuilder(tmpSection.getTopic(), idg);
		parser = new DashTree(tokens, builder,
				new DefaultD3webParserErrorHandler(
						new ArrayList<Message>(), "file", "BasicLexer"));
		parser.setBuilder(builder);

		try {
			parser.knowledge();
		} catch (RecognitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		Stack<Section> s = builder.getSections();
		
		Stack<Section> revert = new Stack<Section>();
		while(s.size() > 0) {
		
			revert.push(s.pop());
		}
		Section root = Section.createExpandedSection(tmpSection.getOriginalText(), new QuestionnairesTreeANTLR(), null, -1, tmpSection.getTopic(), null, null, idg);
		while(revert.size() > 0) {
			Section child = revert.pop();
			root.addChild(child);
			child.setFather(root);
		}
		List<Section> list = new ArrayList<Section>();
		list.add(root);
		
		return list;
	}

}

