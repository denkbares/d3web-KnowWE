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
 
package de.d3web.KnOfficeParser.visio;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.tools.ant.util.ReaderInputStream;

import de.d3web.KnOfficeParser.KnOfficeParameterSet;
import de.d3web.KnOfficeParser.KnOfficeParser;
import de.d3web.KnOfficeParser.util.DefaultD3webLexerErrorHandler;
import de.d3web.KnOfficeParser.util.DefaultD3webParserErrorHandler;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.report.Message;

public class VisioParserCaller implements KnOfficeParser {

	private List<Message> errors=new ArrayList<Message>();
	private String file;
	StringTemplateGroup templates;
	String outputString;
	
	public String getOutputString() {
		return outputString;
	}

	public VisioParserCaller(String file) throws IOException {
		this(file, "VisiotoXML.stg");
	}
	
	public VisioParserCaller(String file, String templatefile) throws IOException {
		this.file=file;
		setTemplateFile(templatefile);
	}
	
	public void setTemplateFile(String tfile) throws IOException {
		FileReader templatefile;
		templatefile = new FileReader(tfile);
		templates = new StringTemplateGroup(templatefile);
		templatefile.close();
	}
	
	@Override
	public List<Message> addKnowledge(Reader r,
			IDObjectManagement idom, KnOfficeParameterSet s) {
		ReaderInputStream input = new ReaderInputStream(r);
		ANTLRInputStream istream = null;
			try {
				istream = new ANTLRInputStream(input, "UTF-8");
			} catch (IOException e1) {
				errors.add(MessageKnOfficeGenerator.createAntlrInputError(r.toString(), 0, ""));
			}
		VisioLexer lexer = new VisioLexer(istream, new DefaultD3webLexerErrorHandler(errors, file));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		VisioParser parser = new VisioParser(tokens, new DefaultD3webParserErrorHandler(errors, file, "BasicLexer"));
		VisioParser.knowledge_return ret;
		try {
			ret = parser.knowledge();
		} catch (RecognitionException e) {
			// Sollte nicht auftreten, Fehlermeldungen werden behandelt
			e.printStackTrace();
			return errors;
		}
		CommonTree t = (CommonTree) ret.getTree();
		System.out.println(t.toStringTree());
		CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
		nodes.setTokenStream(tokens);
		VisiotoXML walker = new VisiotoXML(nodes);
		walker.setTemplateLib(templates);
		VisiotoXML.knowledge_return xmldoc;
		try {
			xmldoc = walker.knowledge();
		} catch (RecognitionException e) {
			// Sollte nicht auftreten, Fehlermeldungen erscheinen auf der Console, aber selbst die sollten nicht auftreten
			return errors;
		}
		StringTemplate output = (StringTemplate) xmldoc.getTemplate();
		outputString = output.toString();
		return errors;
	}

	@Override
	public List<Message> checkKnowledge() {
		return errors;
	}
	
	public void writeToFile(String file) throws IOException {
		FileWriter fw;
		fw = new FileWriter(file);
		fw.write(outputString);
		fw.close();
	}
	

}
