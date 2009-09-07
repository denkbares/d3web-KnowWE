package de.d3web.we.taghandler;

import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class OwlDownloadHandler extends AbstractTagHandler {

    public static final String KEY_OWL = "owlfile";

    public OwlDownloadHandler() {
	super("OwlDownload");
    }
    
	@Override
	public String getDescription() {
	    return KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.OwlDownloadHandler.description");
	}

    @Override
    public String render(String topic, KnowWEUserContext user, String value, String web) {
	String prefix = "";
	String result = "";
	String icon = "<img src=\"KnowWEExtension/images/disk.png\" title=\"Owl download\" /></img>";
	result += "<a href=\"" + prefix + "OwlDownload.jsp\">" + icon + "</a>";

	return KnowWEEnvironment.maskHTML(result);	
    }
}
