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

package de.d3web.we.ci4ke.groovy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import de.d3web.report.Message;
import de.d3web.we.ci4ke.handling.CIConfiguration;
import de.d3web.we.ci4ke.handling.TestResult;
import de.d3web.we.ci4ke.handling.TestResult.TestResultType;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMNotice;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class GroovyCITestReviseSubtreeHandler implements ReviseSubTreeHandler {

//	private static Logger log = Logger.getLogger(CITestReviseSubtreeHandler.class.getName());
	
	/**
	 * Prepend the groovy-code with some import statements
	 */
	private static final String PREPEND = "import "+CIConfiguration.class.getName()+";\n"+
    					"import "+TestResult.class.getName()+";\n"+
    					"import static "+TestResultType.class.getName()+".*;\n";
	
	
	@SuppressWarnings("unchecked")
	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
		
		//TODO: check if name-annotation is unique!!!
		String testname = DefaultMarkupType.getAnnotation(s, "name");
		Map<String,Section<GroovyCITestType>> map = CIConfiguration.getAllGroovyCITestSections(KnowWEEnvironment.DEFAULT_WEB);
		if(map.containsKey(testname)){
			Section<GroovyCITestType> testSection = map.get(testname);
			if(!testSection.getId().equals(s.getId()))
				DefaultMarkupType.addErrorMessage(s, new Message(Message.ERROR, 
						"Test name '"+testname+"' is not unique!", null, -1, null));
		}
			
		
		try {
			parseGroovyCITestSection(s);
		} catch (Throwable th) {
			//th.printStackTrace();
			//System.out.println(th.getMessage());
			String errorMessageMasked = KnowWEUtils.maskHTML(KnowWEUtils.maskNewline(th.getLocalizedMessage()));
			DefaultMarkupType.addErrorMessage(s, new Message(Message.ERROR, errorMessageMasked, null, -1, null));
			return null;//new TestCouldNotBeCreated(testname, errorMessageMasked);
		}
		
		return new TestCreatedSuccessfully(testname);		
		
		//Script script = shell.parse(groovycode);
		//Object o = shell.evaluate(groovycode);
		
		//log.log(Level.INFO, "Nummer 5 lebt!");
		//System.out.println("Nummer 5 lebt!");
		//DefaultMarkupType.addErrorMessage(s, new Message(Message.ERROR, "NUMMER 5 STIRBT.", null, -1, null));
	}


	/**
	 * @param s
	 */
	public static Script parseGroovyCITestSection(Section<GroovyCITestType> s) 
		throws CompilationFailedException {
		
		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass("de.d3web.we.ci4ke.groovy.AbstractCITestScript");
		GroovyShell shell = new GroovyShell(config);
		
		String groovycode = PREPEND+DefaultMarkupType.getContent(s);
		
		return shell.parse(groovycode);
	}
	
	
	public class TestCreatedSuccessfully extends KDOMNotice{

		private String s;
		
		public TestCreatedSuccessfully(String s) {
			this.s = s;
		}
		
		@Override
		public String getVerbalization(KnowWEUserContext usercontext) {
			return "Test successfully created: "+s;
		}
	}
	
	public class TestCouldNotBeCreated extends KDOMError{

		private String s;
		
		public TestCouldNotBeCreated(String s) {
			this.s = s;
		}
		
		@Override
		public String getVerbalization(KnowWEUserContext usercontext) {
			return "Test could not be created: "+s;
		}
	}	
	
}
