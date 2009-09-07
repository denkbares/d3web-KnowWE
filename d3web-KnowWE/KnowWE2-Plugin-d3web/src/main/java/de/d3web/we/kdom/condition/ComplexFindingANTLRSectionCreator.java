package de.d3web.we.kdom.condition;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.DefaultLexer;
import de.d3web.KnOfficeParser.complexcondition.ComplexConditionSOLO;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class ComplexFindingANTLRSectionCreator extends SectionFinder {

	public ComplexFindingANTLRSectionCreator(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmpSection, Section father,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {
		InputStream stream = new ByteArrayInputStream(tmpSection.getOriginalText().getBytes());
		ANTLRInputStream input  = null;
		try {
			input = new ANTLRInputStream(stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DefaultLexer lexer = new DefaultLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ComplexConditionSOLO parser = new ComplexConditionSOLO(tokens);
		ConditionKDOMBuilder builder =new ConditionKDOMBuilder(tmpSection.getTopic(),idg); 
		parser.setBuilder(builder);
		try {
			parser.complexcondition();
		} catch (RecognitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Section s = builder.peek();
		if(s == null) {
			return null;
		}
		List<Section> list = new ArrayList<Section>();
		list.add(s);
		return list;
	}

}
