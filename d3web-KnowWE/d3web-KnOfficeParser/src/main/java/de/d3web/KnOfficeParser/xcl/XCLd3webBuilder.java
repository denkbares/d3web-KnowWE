/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.KnOfficeParser.xcl;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

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
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelationType;
/**
 * Klasse um mithilfe des XCL Parsers d3web Wissen zu generieren 
 * @author Markus Friedrich
 *
 */
public class XCLd3webBuilder implements KnOfficeParser, XCLBuilder {

	private String file;
	private List<Message> errors = new ArrayList<Message>();
	private D3webConditionBuilder cb;
	private int countfindings=0;
	private List<Integer> findingscountlist=new ArrayList<Integer>();
	private boolean createUncompleteFindings=true;
	public boolean isCreateUncompleteFindings() {
		return createUncompleteFindings;
	}

	public void setCreateUncompleteFindings(boolean createUncompleteFindings) {
		this.createUncompleteFindings = createUncompleteFindings;
	}

	private int errorCountSave=0;
	public D3webConditionBuilder getCb() {
		return cb;
	}

	public void setCb(D3webConditionBuilder cb) {
		this.cb = cb;
	}

	private boolean lazydiag;
	private Diagnosis currentdiag;
	private IDObjectManagement idom;
	
	private void finish() {
		if (errors.size()==0) {
			findingscountlist.add(countfindings);
			int sum=0;
			String s="";
			if (findingscountlist.size()>1) {
				//Entfernen der 0 Findings vor der ersten Solution
				findingscountlist.remove(0);
				s+=findingscountlist.get(0);
				findingscountlist.remove(0);
				sum++;
				for (Integer i: findingscountlist) {
					sum++;
					s+=", "+i;
				}
				s="("+s+")";
				errors.add(MessageKnOfficeGenerator.createXCLFinishedNote(file, sum, s));
			} else {
				errors.add(MessageKnOfficeGenerator.createXCLFinishedNote(file, sum, "()"));
			}
		}
	}
	
	public XCLd3webBuilder(String file, boolean lazydiag, boolean lazyqna, IDObjectManagement idom) {
		this.file=file;
		this.idom=idom;
		this.cb=new D3webConditionBuilder(file, errors, idom);
		this.lazydiag=lazydiag;
		cb.setLazy(lazyqna);
	}
	
	public XCLd3webBuilder(String file, IDObjectManagement idom) {
		this.file=file;
		this.idom=idom;
		cb=new D3webConditionBuilder(file, errors, idom);
	}
	
	@Override
	public List<Message> addKnowledge(Reader r,
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
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		XCL parser = new XCL(tokens, this, new DefaultD3webParserErrorHandler(errors, file, "BasicLexer"),cb);
		try {
			parser.knowledge();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		finish();
		return errors;
	}

	@Override
	public List<Message> checkKnowledge() {
		finish();
		return errors;
	}
	
	@Override	
	public void solution(int line, String linetext, String name) {
		findingscountlist.add(countfindings);
		countfindings=0;
		currentdiag=idom.findDiagnosis(name);
		if (currentdiag==null) {
			if (lazydiag) {
				currentdiag=idom.createDiagnosis(name, idom.getKnowledgeBase().getRootDiagnosis());
			}
			else {
				errors.add(MessageKnOfficeGenerator.createDiagnosisNotFoundException(file, line, linetext, name));
			}
		}
	}
	
	@Override
	public void finding(String weight) {
		// If XCLParsing for ANTLR is activated write XCLRelations 
		// to KnowledgeBase via this method
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("KnowWE_config");
			if (bundle.getString("knowwewiki.activateANTLRforXCL").equals("false")){
				return;
			}
		} catch (Exception e1) {
			// TODO fixme
		}
		
		if (!createUncompleteFindings) {
			if (errorCountSave!=errors.size()) {
				errorCountSave=errors.size();
				return;
			}
		}
		AbstractCondition cond = cb.pop();
		if (cond==null) return;
		if (weight==null||weight.equals("")) {
			weight="[1.0]";
		}
		weight=weight.substring(1, weight.length()-1);
		if (currentdiag!=null) {
			if (weight.equals("--")) {
				XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, currentdiag, XCLRelationType.contradicted);
			} else if (weight.equals("!")) {
				XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, currentdiag, XCLRelationType.requires);
			} else if (weight.equals("++")) {
				XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, currentdiag, XCLRelationType.sufficiently);
			} else {
				Double value;
				try {
					value = Double.parseDouble(weight);
				} catch (NumberFormatException e) {
					//Tritt nur bei einem Parserfehler auf, dieser setzt die Fehlermelung
					value = 1.0;
				}
				XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, currentdiag, XCLRelationType.explains, value);
			}
		}
		countfindings++;
	}
	
	@Override
	public void threshold(int line, String linetext, String type, Double value) {
		if (currentdiag!=null) {
			Collection<KnowledgeSlice> models = idom.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodXCL.class);
			for (KnowledgeSlice knowledgeSlice : models) {
				if (knowledgeSlice instanceof XCLModel) {
					if (((XCLModel) knowledgeSlice).getSolution().equals(currentdiag)) {
						if (type.equals("establishedThreshold")) {
							((XCLModel) knowledgeSlice).setEstablishedThreshold(value);
						} else if (type.equals("suggestedThreshold")) {
							((XCLModel) knowledgeSlice).setSuggestedThreshold(value);
						} else if (type.equals("minSupport")) {
							((XCLModel) knowledgeSlice).setMinSupport(value);
						} else {
							errors.add(MessageKnOfficeGenerator.createNoValidThresholdException(file, line, linetext, type));
						}
					}
				}
			}
		}
	}
}
