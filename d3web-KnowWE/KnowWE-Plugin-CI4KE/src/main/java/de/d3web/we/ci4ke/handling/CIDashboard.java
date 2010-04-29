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

import java.util.List;
import java.util.Map;

import org.jdom.Element;

import com.ecyrd.jspwiki.PageManager;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.event.WikiEvent;
import com.ecyrd.jspwiki.event.WikiEventListener;
import com.ecyrd.jspwiki.event.WikiEventUtils;
import com.ecyrd.jspwiki.event.WikiPageEvent;
import com.ecyrd.jspwiki.providers.VersioningFileProvider;

import de.d3web.we.ci4ke.groovy.AbstractCITestScript;
import de.d3web.we.ci4ke.handling.TestResult.TestResultType;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.jspwiki.JSPWikiKnowWEConnector;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;


public class CIDashboard {
	
//	private static Logger log = Logger.getLogger(CIDashboard.class.getName());
	
	private CIBuilder builder;
	
	private CIConfiguration config;

	private WikiEngine engine;
	
	/**
	 * the id of this CI Dashboard
	 */
	private String id;
	
	/**
	 * The title of the article on which this dashboard resides
	 */
	private String dashboardArticle;
	
	public String getDashboardArticle() {
		return dashboardArticle;
	}

	private String preconditionsMessage = "";
	

	
	/**
	 * Constructs a new CI Dashboard
	 * @param parameters
	 * @param dashboardArticleTitle The title of the article on which this CI Dashboard resides.
	 */
	public CIDashboard(Map<String, String> parameters, String dashboardArticleTitle){
		this.dashboardArticle = dashboardArticleTitle;
		
		this.engine = WikiEngine.getInstance(KnowWEEnvironment.getInstance().
				getWikiConnector().getServletContext(), null);
		this.config = new CIConfiguration(parameters, dashboardArticleTitle);
		
		this.id = this.config.get("id");
		//if this dashboard hasn't overridden the ID Parameter
		//construct a default id out of the title of the article 
		//on which the dashboard "resides" plus the title of the
		//monitored article (which are eventually the same, if no
		//specific
		this.id = this.id!=null ? this.id : dashboardArticleTitle+"."+
				this.config.getMonitoredArticleTitle();		
		
		//TODO Hässlich!
		//Muss das letzte Statement im Konstruktor sein. (Zumindest nach der ID)
		this.builder = new CIBuilder(this, this.config);
	}
	
	/**
	 * @return true, if all preconditions are met. False, otherwise.
	 */
	private boolean checkPreconditions(){
		preconditionsMessage = "";
		String errorSpan = "<span class=\"error\">%s</span>";
		
		if(config==null || engine==null){
			preconditionsMessage = String.format(errorSpan, 
				"One or more necessary objects are null!");
			return false;
		}
		//check if VersioningProvider is set
		if(!engine.getWikiProperties().getProperty(PageManager.PROP_PAGEPROVIDER).equals( 
					VersioningFileProvider.class.getSimpleName())){
			
			preconditionsMessage = String.format(errorSpan, 
				"This wiki isn't running with the VersioningFileProvider! "+
				"Because Versioning is absolutley neccessary for CI to work "+
				"properly, please set the 'jspwiki.pageProvider' "+
				"property in your 'jspwiki.properties' file to the value "+
				"'VersioningFileProvider'!!");
			return false;
		}
		
		return true;
	}

	
	public String getId() {
		return id;
	}
	
//	public KnowWEArticle getTargetArticle() {
//		return monitoredArticle;
//	}
	
//private class TestWikiEventListener implements WikiEventListener {
//
//	@Override
//	public void actionPerformed(WikiEvent event) {
//		if ((event instanceof WikiPageEvent)
//				&& (event.getType() == WikiPageEvent.POST_SAVE_END)) {
//			System.out.println("JEEAHHHWWW!");
//		}
//	}
//	
//}
	
	public String render(){
		
		//do some sanity checks
		if(!checkPreconditions())
			return preconditionsMessage;
		
		KnowWERessourceLoader.getInstance().add("CI4KE.css", KnowWERessourceLoader.RESOURCE_STYLESHEET);
		KnowWERessourceLoader.getInstance().add("CIPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
		
		StringBuffer html = new StringBuffer();
		
		String title = config.getMonitoredArticleTitle();
		html.append("<div id='ci-panel' class='panel'><h3>Continuous Integration Dashboard - "
				+ title + " - Status: " + CIUtilities.renderResultType(builder.getCurrentBuildStatus(),24) + "</h3>\n");
		
		html.append("<div id='ci-content-wrapper'>");//Main content wrapper
		//html.append("<b>This is the content wrapper. It is not supposed to contain any content.</b>");
			
			//Left Column: Lists all the knowledge-base Builds of the targeted article
			html.append("<div id='ci-column-left'>");
			//html.append("Here<br/>...<br/>goes<br/>...<br/>the<br/>...<br/>left<br/>...<br/>column!<br/>");
			//html.append("Left Column: Lists the newest knowledge-base Builds of the monitored article<br/>");
			
//			Method[] methods = KnowWEEnvironment.getInstance().getWikiConnector().getClass().getMethods();
//			for(Method m : methods){
//				html.append(m.getName()+"<br/>");
//			}
			
			html.append("<form name='CIExecuteBuildForm'>");
			html.append("<input type='button' value='Neuen Build Starten!' "+
					"name='submit' class='button' onclick='fctExecuteNewBuild('"+
					getId()+"');'/>");
			html.append("</form>");
			
			html.append(renderTenNewestBuilds());			
			
//			KnowWEWikiConnector con = KnowWEEnvironment.getInstance().getWikiConnector();
//			if(con instanceof JSPWikiKnowWEConnector){
//				WikiEventUtils.addWikiEventListener(((JSPWikiKnowWEConnector) con).getWikiEngine(), 
//						WikiPageEvent.POST_SAVE_END, new TestWikiEventListener());
//			}

			html.append("</div>");
			
			//Mid Column: Contains all wiki changes of the currently selected build in diff style
//			html.append("<div id='ci-column-middle'>");
			//html.append("Here<br/>...<br/>goes<br/>...<br/>the<br/>...<br/>middle<br/>...<br/>column!<br/>");
			//html.append("Mid Column: Contains all wiki changes of the currently selected build in diff style");
			
//			KnowWEArticle a = KnowWEEnvironment.getInstance().getArticle("default_web", "XCL-Test");
//			for(String s : AbstractCITestScript.findXCListsWithLessThenXRelations(a, 4)){
//				html.append(s+"<br/>");
//			}
			//Section<TestsuiteSection> sec = monitoredArticle.getSection().findSuccessor(TestsuiteSection.class);
			
			//if(sec != null)
			//html.append(sec);
			//else
			//	html.append("sec is null!");
			
//			html.append("</div>");
			
			//Right Column: Contains all test results of the currently selected build
			html.append("<div id='ci-column-right'>");
			//html.append("Here<br/>...<br/>goes<br/>...<br/>the<br/>...<br/>right<br/>...<br/>column!<br/>..<br/>..<br/>And<br/>even<br/>more<br/>of<br/>the<br/>right<br/>column!");
//			html.append("Right Column: Contains all test results of the currently selected build<br/>");
			
			
			builder.executeBuild();
			
			html.append("</div>");
			
		html.append("</div></div>");
		
		return html.toString();
	}
	

	private String renderTenNewestBuilds(){
		StringBuffer sb = new StringBuffer("<table id=\"buildList\" width=\"100%\">\n");
		List<?> builds = builder.selectNodes("builds/build[position() > last() - 10]");
		String s;
		for(Object o : builds){
			if(o instanceof Element){
				Element e = (Element)o;
				
				//TODO Check for null
				String buildNr = e.getAttributeValue("nr");
				sb.append("<tr onclick=\"fctGetBuildResults('"+getId()+"','"+buildNr+"');\"><td>");
				
				//starting with a nice image...
				s = e.getAttributeValue("result");
				if(s != null && !s.equals("")){
					TestResultType buildResult = TestResultType.valueOf(s);
					sb.append(CIUtilities.renderResultType(buildResult,16));
				}
				sb.append("</td><td>");
				//followed by the Build Number...
				if(buildNr != null && !buildNr.equals("")) sb.append("#"+buildNr);
				sb.append("</td><td>");
				//and the build date/time
				s = e.getAttributeValue("executed");
				if(s != null && !s.equals("")) sb.append(s);
				//close table-cell
				sb.append("</td></tr>\n");
			}
		}
		sb.append("</table>\n");
		return sb.toString();
	}
	
}
