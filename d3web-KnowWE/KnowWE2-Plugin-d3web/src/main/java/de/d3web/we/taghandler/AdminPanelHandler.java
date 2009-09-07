package de.d3web.we.taghandler;

import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class AdminPanelHandler extends AbstractTagHandler {
	
	public AdminPanelHandler() {
		super("adminpanel");
	}
	
	@Override
	public String getDescription() {
		return D3webModule.getInstance().getKwikiBundle_d3web().getString("KnowWE.AdminPanel.description");
	}
	
	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		
		String header = KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.tags.adminpanel.header");
		String overview = KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.tags.adminpanel.overview");
		String reset = KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.tags.adminpanel.reset");
		String parse = KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.tags.adminpanel.parseall");
		
		
		String html = 
			"<div id=\"admin-panel\" class=\"panel\"><h3>" + header + "</h3>"
		    + "<ul>"
		    + "<li><a href=\"#summarizer\" onclick='doSumAll();'>"+overview+"</a><p id=\"sumAll\"></p></li>"
		    + "<li><a href=\"#reInit\" onclick='doReInit();'>"+reset+"</a><p id=\"reInit\"></p></li>"
		    + "<li><a href=\"#parseWeb\" onclick='doParseWeb();'>"+parse+"</a><p id=\"parseWeb\"></p></li>"
		    + "</ul></div>";
//				"<hr/></li> <li> <a href=\"#summarizer\" onclick='doSumAll();'><big>"+overview+"</big></a><div id=\"sumAll\"></div></li> "
//			+ "<li> <a href=\"#reInit\" onclick='doReInit();'><big>"+reset+"</big></a><div id=\"reInit\"></div>"
//			+ "</li> <li> <a href=\"#parseWeb\" onclick='doParseWeb();'><big>"+parse+"</big></a><div id=\"parseWeb\"></div><hr/>";
		return html;
	}

}
