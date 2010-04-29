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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import com.ecyrd.jspwiki.WikiEngine;

import de.d3web.we.ci4ke.handling.TestResult.TestResultType;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.core.KnowWEEnvironment;

public class CIBuilder {

	private static final String ACTUAL_BUILD_STATUS = "actualBuildStatus";
	
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
	
	/**
	 * The current status of this Build
	 */
	//TODO: Builds haben andere Resultattypen als Tests
	//---> eigenes Enum
	private TestResultType currentBuildStatus;	
	
//	private Date lastExecuted;
	
	public TestResultType getCurrentBuildStatus() {
		return currentBuildStatus;
	}


	/**
	 * 
	 * @param board
	 * @param config
	 */	
	public CIBuilder(CIDashboard board, CIConfiguration config){
		this.assignedDashboard = board;
		this.config = config;
		nextBuildNumber = 1;
		currentBuildStatus = TestResultType.SUCCESSFUL;
		try {
			checkBuildXML();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public void executeBuild(){
		try {
//			checkBuildXML();
			createNewBuild();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void checkBuildXML() throws IOException, JDOMException {
		

//		System.out.println(ciBuildsDir.toString());
		
		File buildDirectory = CIUtilities.getCIBuildDir();
		this.xmlFile = new File(buildDirectory, 
				"builds-"+this.assignedDashboard.getId()+".xml");
		
		//check if build file exists
		if(!xmlFile.exists()){
			
			//if not, we have to create the file and its directory respectively
			if(!buildDirectory.exists()){
				buildDirectory.mkdirs();
			}
			
			xmlFile.createNewFile();

			//write the basic xml-structure to the file
			Element root = new Element("builds");
			root.setAttribute(CIConfiguration.DASHBOARD_ARTICLE_KEY, this.assignedDashboard.getDashboardArticle());
			root.setAttribute(CIConfiguration.MONITORED_ARTICLE_KEY, config.getMonitoredArticleTitle());
			root.setAttribute(ACTUAL_BUILD_STATUS, this.currentBuildStatus.toString());
			//create the JDOM Tree for the new xml file and print it out
			xmlDocument = new Document(root);
			XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
			out.output(xmlDocument, new FileWriter(xmlFile));
		}else{
			//file exists...
			xmlDocument = new SAXBuilder().build(xmlFile);
			nextBuildNumber = 1;//Default
			
			//try to parse the most current build NR
			Object o = XPath.selectSingleNode(xmlDocument, "/builds/build[last()]/@nr");
			if(o instanceof Attribute){
				Attribute attr = (Attribute)o;
				String buildNr = attr.getValue();
				if(buildNr!=null && !buildNr.equals(""))
					nextBuildNumber = Long.parseLong(buildNr)+1;
			}
			
			String xmlBuildStatus = xmlDocument.getRootElement().
					getAttributeValue(ACTUAL_BUILD_STATUS);
			this.currentBuildStatus = TestResultType.valueOf(xmlBuildStatus);
//			System.out.println(test);
		}
	}
	
	/**
	 * <build executed="NOW()">
	 * 		<test .../>
	 * 		<test .../>
	 * </build>
	 * @throws IOException 
	 */
	public void createNewBuild() throws IOException{
		
		TestResultType overallResult = TestResultType.SUCCESSFUL;
		Element build = new Element("build");
		
		Date now = new Date();
		build.setAttribute("executed",DATE_FORMAT.format(now));
		
		build.setAttribute("nr", String.valueOf(nextBuildNumber));
		nextBuildNumber++;
		
		String testname;
		TestResult result;
		for(Class<? extends CITest> testClass : config.getTestsToExecute()){
			try {
				testname = testClass.getSimpleName();
				result = testClass.newInstance().execute(config);
				
				//the "worst" result of the executed tests determines 
				//the result of the build
				if(overallResult.compareTo(result.getResultType())<0)
					overallResult = result.getResultType();
				
				Element test = new Element("test");
				test.setAttribute("name", testname);
				test.setAttribute("result", result.getResultType().toString());
				
				if(result.getTestResultMessage().length()>0)
					test.setAttribute("message",result.getTestResultMessage());
				
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
		}
		//the "worst" result of the executed tests determines the result of the build
		this.currentBuildStatus = overallResult;
		xmlDocument.getRootElement().setAttribute(ACTUAL_BUILD_STATUS, 
				overallResult.toString());
		//...also, save the result of the current build as attribute
		//in the <build .../> Tag
		build.setAttribute("result", overallResult.toString());
		//add the build-element to the JDOM Tree
		xmlDocument.getRootElement().addContent(build);
		//and print it to file
		XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
		out.output(xmlDocument, new FileWriter(xmlFile));
	}
	
	public List<?> selectNodes(String xPathQuery){
		List<?> ret = null;
		try {
			ret = XPath.selectNodes(xmlDocument, xPathQuery);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
}
