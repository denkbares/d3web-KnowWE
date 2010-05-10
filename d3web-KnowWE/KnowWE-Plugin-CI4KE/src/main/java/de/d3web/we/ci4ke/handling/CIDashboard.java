package de.d3web.we.ci4ke.handling;

import de.d3web.we.ci4ke.handling.CIDashboardType.CIBuildTriggers;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public class CIDashboard {
	
	private CIConfig config;
	
	public CIDashboard(Section<CIDashboardType> section){	
		this.config = (CIConfig) KnowWEUtils.
			getStoredObject(section, CIConfig.CICONFIG_STORE_KEY);
	}
	
	public String render(){
		
		KnowWERessourceLoader.getInstance().add("CI4KE.css", 
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
		KnowWERessourceLoader.getInstance().add("CIPlugin.js", 
				KnowWERessourceLoader.RESOURCE_SCRIPT);			
		
		StringBuffer html = new StringBuffer();
		
		String title = config.getMonitoredArticleTitle();
		html.append("<div id='ci-panel' class='panel'><h3>Continuous Integration Dashboard - "
				+ title + " - Status: " + /*CIUtilities.renderResultType(builder.getCurrentBuildStatus(),24) +*/ "</h3>\n");
		
		html.append("<div id='ci-content-wrapper'>");//Main content wrapper
			
			//Left Column: Lists all the knowledge-base Builds of the targeted article
			html.append("<div id='ci-column-left'>");
			
			if(config.getTrigger().equals(CIBuildTriggers.onDemand)) {
				html.append("<form name=\"CIExecuteBuildForm\">");
				html.append("<input type=\"button\" value=\"Neuen Build Starten!\" "+
						"name=\"submit\" class=\"button\" onclick=\"fctExecuteNewBuild('"+
						config.getDashboardID()+"');\"/>");
				html.append("</form>");
			}
			
			//render Builds
			html.append("</div>");
			
			html.append("<div id='ci-column-middle'>");

			html.append("</div>");			

			html.append("<div id='ci-column-right'>");

			html.append("</div>");
			
		html.append("</div></div>");
		
		return html.toString();	
	}
	
}
