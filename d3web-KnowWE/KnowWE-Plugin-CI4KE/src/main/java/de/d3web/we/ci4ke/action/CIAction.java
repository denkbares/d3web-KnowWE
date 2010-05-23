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

package de.d3web.we.ci4ke.action;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.build.CIBuilder;
import de.d3web.we.ci4ke.diff.JSPWikiTraditionalDiff;
import de.d3web.we.ci4ke.handling.CITestResult.TestResultType;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class CIAction extends AbstractAction {

	
	@Override
	public void execute(ActionContext context) throws IOException {
		
		Logger.getLogger(this.getClass().getName()).info( 
				">>> execute Action angekommen! >>>");
		
		String task = String.valueOf(context.getParameter("task"));
		String dashboardID = String.valueOf(context.getParameter("id"));
		String topic = context.getKnowWEParameterMap().getTopic();
		String web = context.getKnowWEParameterMap().getWeb();
		
		if(task.equals("null") || dashboardID.equals("null"))
			throw new IOException("CIAction.execute(): Required parameters not set!");
		
		StringBuffer buffy = new StringBuffer();
		
		CIBuildPersistenceHandler handler = new CIBuildPersistenceHandler(dashboardID);
		
		if(task.equals("executeNewBuild")){
			
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
			">>> executeNewBuild angekommen! >>>");
			
			CIBuilder builder = new CIBuilder(topic, dashboardID);
			builder.executeBuild();
			
//			for(Map.Entry<String, CITestResult> testresult : 
//				resultset.getResults().entrySet()) {
//				context.getWriter().write(testresult.getKey() + 
//						" - " + testresult.getValue().toString() + "<br/>");
//			}
			
			
		}//Changes in the wiki for the selected build
		else if(task.equals("getWikiChanges")){
		
			//the version of the article of the selected build
			int articleVersionSelected = 1;
			//the version of the article of the previous build
			int articleVersionPrevious = 1;
			
			//Number of selected Build
			int buildNr = Integer.parseInt(String.valueOf(context.getParameter("nr")));
			//Number of build to compare to (the previous build number)
			int previousBuildNr = buildNr>1 ? buildNr-1 : 1;
			
			String monitoredArticleTitle = "";
			Object attrib = handler.selectSingleNode("builds/@monitoredArticle");
			if(attrib instanceof Attribute){
				monitoredArticleTitle = ((Attribute)attrib).getValue();
			}
			attrib = null;			
			
			//xPath to select the article version of a buildNumber
			String xPath = "builds/build[@nr=%s]/@articleVersion";
			
			//try to parse the selected build article version 
			attrib = handler.selectSingleNode(String.format(xPath, buildNr));
			if(attrib instanceof Attribute){
				String attrValue = ((Attribute)attrib).getValue();
				if(attrValue!=null && !attrValue.isEmpty())
					articleVersionSelected = Integer.parseInt(attrValue);
			}
			attrib = null;
			
			//try to parse the selected build article version 
			attrib = handler.selectSingleNode(String.format(xPath, previousBuildNr));
			if(attrib instanceof Attribute){
				String attrValue = ((Attribute)attrib).getValue();
				if(attrValue!=null && !attrValue.isEmpty())
					articleVersionPrevious = Integer.parseInt(attrValue);
			}
			attrib = null;
			
//			buffy.append("<h4>Unterschiede zwischen <b>Build " + buildNr + 
//					"</b> (Article Version " + articleVersionSelected + 
//					") und <b>Build " + previousBuildNr + "</b> (Article " +
//					"Version " + articleVersionPrevious + ")</h4>");
			buffy.append("<h3 style=\"background-color: #CCCCCC;\">Differences between Build " + buildNr + 
					" and Build " + previousBuildNr + "</h3>");			
			
			KnowWEWikiConnector conny = KnowWEEnvironment.getInstance().getWikiConnector();
			JSPWikiTraditionalDiff diff = new JSPWikiTraditionalDiff();
			buffy.append(diff.makeDiffHtml(
							conny.getArticleSource(monitoredArticleTitle, 
									articleVersionPrevious),
							conny.getArticleSource(monitoredArticleTitle, 
									articleVersionSelected)));
			
		}//Render the result of one (selected) build. Target: Right column
		else if(task.equals("getBuildResults")){
			
			String buildNr = String.valueOf(context.getParameter("nr"));
			String xPath = "builds/build[@nr=%s]/test";
			@SuppressWarnings("unchecked") List<?> tests = new LinkedList();
			
			try {
				Document doc = loadBuildXML(dashboardID);
				tests = XPath.selectNodes(doc, String.format(xPath, buildNr));
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			buffy.append("<h4>Results of Build #"+buildNr+"</h4>");
			buffy.append("<table id=\"buildResults\" width=\"100%\">\n");
			
			for(Object o : tests){
				if(o instanceof Element){
					Element e = (Element)o;
					
					//Render Test Result
					buffy.append("<tr><td>");
					String s = e.getAttributeValue("result");
					if(s != null && !s.equals("")){
						TestResultType buildResult = TestResultType.valueOf(s);
						buffy.append(CIUtilities.renderResultType(buildResult,16));
					}
					buffy.append("</td><td>");
					
					//Render Test-Name
					s = e.getAttributeValue("name");
					if(s != null && !s.equals("")) buffy.append("Name: "+s);
					buffy.append("</td><td>");
					
					//Render Test Message (if existent)
					s = e.getAttributeValue("message");
					if(s != null && !s.equals("")) buffy.append("Result Message: "+s);
					buffy.append("</td></tr>\n");			
				}
			}
			buffy.append("</table>");
		}
		else{
			buffy.append("@info@CIAction says: Hello World!");
		}
		
		context.getWriter().write(buffy.toString());
	}

	@Deprecated
	private Document loadBuildXML(String dashboardID) throws JDOMException, IOException{
		File buildDirectory = CIUtilities.getCIBuildDir();
		File xmlFile = new File(buildDirectory, "builds-"+dashboardID+".xml");
		return new SAXBuilder().build(xmlFile);
	}
}
