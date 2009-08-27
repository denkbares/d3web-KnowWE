package de.d3web.KnOfficeParser.scmcbr;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.tools.ant.util.ReaderInputStream;

import de.d3web.KnOfficeParser.D3webConditionBuilder;
import de.d3web.KnOfficeParser.DefaultLexer;
import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.KnOfficeParser.KnOfficeParameterSet;
import de.d3web.KnOfficeParser.KnOfficeParser;
import de.d3web.KnOfficeParser.util.DefaultD3webLexerErrorHandler;
import de.d3web.KnOfficeParser.util.DefaultD3webParserErrorHandler;
import de.d3web.report.Message;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelationType;
/**
 * Builder für d3web und den SCMCBR Parser
 * @author Markus Friedrich
 *
 */
public class D3SCMCBRBuilder implements SCMCBRBuilder, KnOfficeParser {

	private List<Message> errors = new ArrayList<Message>();
	private String file;
	private Diagnosis currentdiag;
	private Question currentquestion;
	private QContainer currentqclass;
	private D3webConditionBuilder cb;
	private int countfindings;
	private IDObjectManagement idom;
	
	public D3SCMCBRBuilder(String file, IDObjectManagement idom) {
		this.file=file;
		this.idom=idom;
		this.cb= new D3webConditionBuilder(file, errors, idom);
	}
	
	@Override
	public Collection<Message> addKnowledge(Reader r,
			IDObjectManagement idom, KnOfficeParameterSet s) {
		this.idom=idom;
		cb.setIdom(idom);
		ReaderInputStream input = new ReaderInputStream(r);
		ANTLRInputStream istream = null;
			try {
				istream = new ANTLRInputStream(input);
			} catch (IOException e1) {
				errors.add(MessageKnOfficeGenerator.createAntlrInputError(file, 0, ""));
			}
		DefaultLexer lexer = new DefaultLexer(istream, new DefaultD3webLexerErrorHandler(errors, file));
		lexer.setNewline(true);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SCMCBR parser = new SCMCBR(tokens, this, new DefaultD3webParserErrorHandler(errors, file, "BasicLexer"));
		try {
			parser.knowledge();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		return errors;
	}

	@Override
	public Collection<Message> checkKnowledge() {
		return errors;
	}

	@Override
	public void solution(int line, String text, String name) {
		currentdiag=idom.findDiagnosis(name);
		if (currentdiag==null) {
			errors.add(MessageKnOfficeGenerator.createDiagnosisNotFoundException(file, line, text, name));
		}
	}

	@Override
	public void and(int line, String text, List<String> names, String weight,
			boolean or) {
		if (currentquestion!=null) {
			cb.condition(line, text, currentquestion.getText(), "", "=",
					names.get(0));
			for (int i=1; i<names.size(); i++) {
				cb.condition(line, text, currentquestion.getText(), "", "=",
						names.get(i));
				if (or) {
					cb.orcond(null);
				} else {
					cb.andcond(null);
				}
			}
			finding(weight);
		}
	}

	@Override
	public void answer(int line, String text, String name, String weight, String operator) {
		if (currentquestion!=null) {
			cb.condition(line, text, currentquestion.getText(), "", operator,
					name);
			finding(weight);
		}
	}

	private void finding(String weight) {
		AbstractCondition cond = cb.pop();
		if (cond==null) return;
		if (weight==null||weight.equals("")) {
			weight="[1.0]";
		}
		weight=weight.substring(1, weight.length()-1);
		XCLRelationType type;
		if (currentdiag!=null) {
			if (weight.startsWith("-")) {
				weight=weight.substring(1).trim();
				type=XCLRelationType.contradicted;
			} else if (weight.startsWith("!")) {
				weight=weight.substring(1).trim();
				type = XCLRelationType.requires;
			} else if (weight.startsWith("+")) {
				weight=weight.substring(1).trim();
				type = XCLRelationType.sufficiently;
			} else {
				type = XCLRelationType.explains;
			}
			Double value;
			try {
				value = Double.parseDouble(weight);
			} catch (NumberFormatException e) {
				//Tritt nur bei einem Parserfehler auf, dieser setzt die Fehlermelung
				value = 1.0;
			}
			XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, currentdiag, type, value);
			
		}
		countfindings++;
		
	}

	@Override
	public void in(int line, String text, List<Double> values1,
			List<Double> values2, boolean startincluded, boolean endincluded) {
		// TODO Auto-generated method stub
	}

	@Override
	public void into(int line, String text, List<Double> values1,
			List<Double> values2, boolean startincluded, boolean endincluded) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void not(int line, String text, String name, String weight) {
		if (currentquestion!=null) {
			cb.condition(line, text, currentquestion.getText(), "", "=",
					name);
			cb.notcond(null);
			finding(weight);
		}
		
	}

	@Override
	public void question(int line, String text, String name) {
		currentquestion=idom.findQuestion(name);
		if (currentquestion==null) {
			errors.add(MessageKnOfficeGenerator.createQuestionNotFoundException(file, line, text, name));
		}
	}

	@Override
	public void questionclass(int line, String text, String name) {
		currentqclass=idom.findQContainer(name);
		if (currentqclass==null) {
			errors.add(MessageKnOfficeGenerator.createQuestionClassNotFoundException(file, 	line, text, name));
		}
	}

	@Override
	public void setAmount(int line, String text, String name, Double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void threshold(int line, String text, String name, Double value1,
			Double value2) {
		// TODO Auto-generated method stub Gemeinsame Methode wie XCL?
		
	}

}
