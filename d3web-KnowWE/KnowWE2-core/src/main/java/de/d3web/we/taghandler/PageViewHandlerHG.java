package de.d3web.we.taghandler;

import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class PageViewHandlerHG extends AbstractTagHandler {

    public PageViewHandlerHG() {
	super("pageviewgraph");
    }

    @Override
    public String getDescription() {
	return KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.PageViewHandlerHG.description");
    }

    @Override
    public String render(String topic, KnowWEUserContext user, String value, String web) {
	String baseurl = KnowWEEnvironment.getInstance().getWikiConnector()
		.getBaseUrl();
	String output = "<applet code=\"de/d3web/we/webapp/renderer/applet/OWLApplet.class\""
		+ " codebase=\""
		+ baseurl
		+ "KnowWEExtension/applets/\""
		+ " archive=\"owlapplet.jar,prefuse.jar\""
		+ " width=\"100%\" height=\"350\">"
		+ "<param name=\"url\" value=\""
		+ baseurl
		+ "KnowWE.jsp?renderer=graphml&KWiki_Topic="
		+ topic
		+ "\">"
		+ "If you can read this text, the applet is not working. Perhaps you don't"
		+ "have the Java 1.4.2 (or later) web plug-in installed?<br/>"
		+ "<h3><a href=\"http://java.com\">Get Java here.</a></h3></applet>";
	return output;
    }

}
