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

import java.util.Map;

import com.ecyrd.jspwiki.event.WikiEvent;
import com.ecyrd.jspwiki.event.WikiEventListener;
import com.ecyrd.jspwiki.event.WikiEventUtils;
import com.ecyrd.jspwiki.event.WikiPageEvent;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.jspwiki.JSPWikiKnowWEConnector;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;


public class CIDashboard {
	
//	private static Logger log = Logger.getLogger(CIDashboard.class.getName());
	
	private CIConfiguration config;
	
	private CIBuilder builder;
	
	/**
	 * Constructs a new CI Dashboard
	 * @param parameters
	 * @param dashboardArticleTitle The title of the article on which this CI Dashboard resides.
	 */
	public CIDashboard(Map<String, String> parameters, String dashboardArticleTitle){
		config = new CIConfiguration(parameters, dashboardArticleTitle);
		builder = new CIBuilder(this, config);
	}

	
	public String render(){
		
		KnowWERessourceLoader.getInstance().add("CI4KE.css", KnowWERessourceLoader.RESOURCE_STYLESHEET);
		
		StringBuffer html = new StringBuffer();
		
		String title = config.getMonitoredArticleTitle();
		html.append("<div id='ci-panel' class='panel'><h3>Continuous Integration Dashboard - "+title+"</h3>\n");
		
		html.append("<div id='ci-content-wrapper'>");//Main content wrapper
		//html.append("<b>This is the content wrapper. It is not supposed to contain any content.</b>");
			
			//Left Column: Lists all the knowledge-base Builds of the targeted article
			html.append("<div id='ci-column-left'>");
			//html.append("Here<br/>...<br/>goes<br/>...<br/>the<br/>...<br/>left<br/>...<br/>column!<br/>");
			html.append("Left Column: Lists the newest knowledge-base Builds of the monitored article<br/>");
			
//			Method[] methods = KnowWEEnvironment.getInstance().getWikiConnector().getClass().getMethods();
//			for(Method m : methods){
//				html.append(m.getName()+"<br/>");
//			}
			

			
			
//			KnowWEWikiConnector con = KnowWEEnvironment.getInstance().getWikiConnector();
//			if(con instanceof JSPWikiKnowWEConnector){
//				WikiEventUtils.addWikiEventListener(((JSPWikiKnowWEConnector) con).getWikiEngine(), 
//						WikiPageEvent.POST_SAVE_END, new TestWikiEventListener());
//			}

			html.append("</div>");
			
			//Mid Column: Contains all wiki changes of the currently selected build in diff style
			html.append("<div id='ci-column-middle'>");
			//html.append("Here<br/>...<br/>goes<br/>...<br/>the<br/>...<br/>middle<br/>...<br/>column!<br/>");
			html.append("Mid Column: Contains all wiki changes of the currently selected build in diff style");
			
			
			//Section<TestsuiteSection> sec = monitoredArticle.getSection().findSuccessor(TestsuiteSection.class);
			
			//if(sec != null)
			//html.append(sec);
			//else
			//	html.append("sec is null!");
			
			html.append("</div>");
			
			//Right Column: Contains all test results of the currently selected build
			html.append("<div id='ci-column-right'>");
			//html.append("Here<br/>...<br/>goes<br/>...<br/>the<br/>...<br/>right<br/>...<br/>column!<br/>..<br/>..<br/>And<br/>even<br/>more<br/>of<br/>the<br/>right<br/>column!");
			html.append("Right Column: Contains all test results of the currently selected build<br/>");
			
			
			html.append(builder.executeBuild());
			
			html.append("</div>");
			
		html.append("</div></div>");
		
		return html.toString();
		
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
	
}
