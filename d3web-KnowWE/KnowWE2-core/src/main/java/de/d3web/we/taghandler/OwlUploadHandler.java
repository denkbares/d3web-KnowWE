package de.d3web.we.taghandler;

import java.io.File;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.SemanticCore;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class OwlUploadHandler extends AbstractTagHandler {

    public static final String KEY_DELETE_OWL = "owldelete";

    public static final String KEY_OWL = "owlfile";

    public OwlUploadHandler() {
	super("OwlImport");
    }

    @Override
    public String getDescription() {
	return KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.OwlUploadHandler.description");
    }

    @Override
    public String render(String topic, KnowWEUserContext user, String vaue, String web) {

	Object[][] fields = { { "KnowWE.knoffice.fields.owlfile", KEY_OWL,
		"file", 50, "xml" } };

	StringBuffer html = new StringBuffer();
	// enctype='multipart/form-data'
	// accept-charset=\"UTF-8\"
	html.append("<div id='knoffice-panel' class='panel'>");
	html.append("<h3>"
		+ KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.tags.owlpanel.header")
		+ "</h3>");

	html
		.append("<form onsubmit='sendForm(this);' enctype=\"multipart/form-data\"  method=\"POST\" action=\"KnowWEUpload\">");
	// normal fields
	for (int i = 0; i < fields.length; i++) {
	    html.append("<label for='" + fields[i][1] + "' class='"
		    + fields[i][4] + "'>"
		    + KnowWEEnvironment.getInstance().getKwikiBundle().getString(fields[i][0].toString())
		    + "</label>");
	    html.append("<input id='" + fields[i][1] + "' name='"
		    + fields[i][1] + "' type='" + fields[i][2] + "' size='"
		    + fields[i][3] + "'/>");
	    html.append("<br /> \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
	}
	html
		.append("<p id='knoffice-show-extend' class='pointer extend-panel-down'>"
			+ KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.renamingtool.settings")
			+ "</p>");
	html.append("<div id='knoffice-extend-panel' class='hidden'>");
	SemanticCore sc = SemanticCore.getInstance();
	File[] files = sc.getImportList();
	if (files != null) {
	    html
		    .append("<form onsubmit='sendForm(this);' method=\"POST\" action=\"OWLDelete\">");
	    for (File f : files) {
		html.append("<label for='filename' >" + f.getName()
			+ "</label>");
		html.append("<input type=\"hidden\" id=\"" + KEY_DELETE_OWL
			+ "\" name=\"" + KEY_DELETE_OWL + "\" value=\""
			+ f.getName() + "\" />");
		html
			.append("<input id='upload' type='submit' value='Loeschen' name='submit' class='button'/>");
		html.append("<br />");
	    }
	    html.append("</form>");

	}
	html.append("</div>");

	html
		.append("<br /><p><input id='upload' type='submit' value='Hochladen' name='submit' class='button'/></p>");
	html.append("</form>");
	// extended fields

	html.append("<br /></div>");
	return html.toString();
    }
}
