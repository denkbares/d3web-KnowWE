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

package de.d3web.we.ci4ke.handling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.d3web.we.core.KnowWEEnvironment;

public class CIBuilder {

	private CIConfiguration config;
	
	private CIDashboard assignedDashboard;
	
	/**
	 * The JDOM Document Tree of our build File
	 */
	private Document xmlDocument;
	
	/**
	 * This File is pointing to our build File
	 */
	private File xmlFile;
	
	/**
	 * The next builds numberd
	 */
	private long nextBuildNumber;
	

	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
//	private Date lastExecuted;
	
	/**
	 * 
	 * @param board
	 * @param config
	 */	
	public CIBuilder(CIDashboard board, CIConfiguration config){
		this.assignedDashboard = board;
		this.config = config;
		nextBuildNumber = 1;
	}
	
	public String executeBuild(){
		try {
			checkBuildXML();
			createNewBuildTEST();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		lastExecuted = new Date();
//		StringBuilder ret = new StringBuilder("");
//		for(Class<? extends CITest> testClass : config.getTestsToExecute())
//			try {
//				ret.append("<p>"+testClass.getSimpleName() + ": " +
//						testClass.newInstance().execute(config)+"</p>");
//			} catch (InstantiationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		return ret.toString();
		return "";
	}
	
	public void checkBuildXML() throws IOException, JDOMException{
		//check if directory exists
		String path = KnowWEEnvironment.getInstance().getContext().getRealPath("");
		File dir = new File(path,"/KnowWEExtension/tmp/ci-builds/");
		this.xmlFile = new File(dir, "builds-"+config.getMonitoredArticleTitle()+".xml");
		
		//check if build file exists
		if(!xmlFile.exists()){
			
			//if not, we have to create the file and its directory respectively
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			xmlFile.createNewFile();

			//write the basic xml-structure to the file
			Element root = new Element("builds");
			root.setAttribute(CIConfiguration.MONITORED_ARTICLE_KEY, config.getMonitoredArticleTitle());
			xmlDocument = new Document(root);
			XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
			out.output(xmlDocument, new FileWriter(xmlFile));
		}else{
			//file exists...
			xmlDocument = new SAXBuilder().build(xmlFile);
		}
	}
	
	/**
	 * <build executed="NOW()">
	 * 		<test .../>
	 * 		<test .../>
	 * </build>
	 * @throws IOException 
	 */
	public void createNewBuildTEST() throws IOException{
		
		Element build = new Element("build");
		
		Date now = new Date();
		build.setAttribute("executed",DATE_FORMAT.format(now));
		
		build.setAttribute("nr", String.valueOf(nextBuildNumber));
		nextBuildNumber++;
		
		String testname;
		TestResult result;
		for(Class<? extends CITest> testClass : config.getTestsToExecute())
			try {
				testname = testClass.getSimpleName();
				result = testClass.newInstance().execute(config);
				
				Element test = new Element("test");
				test.setAttribute("name", testname);
				test.setAttribute("result", result.getResult().toString());
				
				if(result.getTestResultMessage().length()>0)
					test.setText(result.getTestResultMessage());
				
				build.addContent(test);
//				ret.append("<p>"+testClass.getSimpleName() + ": " +
//						testClass.newInstance().execute(config)+"</p>");
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
//		System.out.println(build);
		xmlDocument.getRootElement().addContent(build);
		//root.addContent(build);
		XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
		out.output(xmlDocument, new FileWriter(xmlFile));
	}
}
