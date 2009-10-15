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

package de.d3web.we.taghandler;

import java.io.File;
import java.util.Map;
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
    public String getDescription(KnowWEUserContext user) {
	return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.OwlUploadHandler.description");
    }

    @Override
    public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
    	
    ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);

	Object[][] fields = { { "KnowWE.knoffice.fields.owlfile", KEY_OWL,
		"file", 50, "xml" } };

	StringBuffer html = new StringBuffer();
	// enctype='multipart/form-data'
	// accept-charset=\"UTF-8\"
	html.append("<div id='knoffice-panel' class='panel'>");
	html.append("<h3>"
		+ rb.getString("KnowWE.OwlUploadHandler.header")
		+ "</h3>");

	html.append("<form enctype=\"multipart/form-data\"  method=\"POST\" action=\"KnowWEUpload\">");
	// normal fields
	for (int i = 0; i < fields.length; i++) {
	    html.append("<label for='" + fields[i][1] + "' class='"
		    + fields[i][4] + "'>"
		    + rb.getString(fields[i][0].toString())
		    + "</label>");
	    html.append("<input id='" + fields[i][1] + "' name='"
		    + fields[i][1] + "' type='" + fields[i][2] + "' size='"
		    + fields[i][3] + "'/>");
	    html.append("<br /> \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
	}
	html.append("<p id='knoffice-show-extend' class='show-extend pointer extend-panel-down'>"
			+ rb.getString("KnowWE.renamingtool.settings")
			+ "</p>");
	html.append("<div id='knoffice-panel-extend' class='hidden'>");
	SemanticCore sc = SemanticCore.getInstance();
	File[] files = sc.getImportList();
	if (files != null) {
		html.append("<form method=\"POST\" action=\"OWLDelete\">");
	    for (File f : files) {
		html.append("<label for='filename' >" + f.getName()
			+ "</label>");
		html.append("<input type=\"hidden\" id=\"" + KEY_DELETE_OWL
			+ "\" name=\"" + KEY_DELETE_OWL + "\" value=\""
			+ f.getName() + "\" />");
		html
			.append("<input id='upload' type='submit' value='" + rb.getString("KnowWE.button.erase") + "' name='submit' class='button'/>");
		html.append("<br />");
	    }
	    html.append("</form>");

	}
	html.append("</div>");

	html
		.append("<br /><p><input id='upload' type='submit' value='" + rb.getString("KnowWE.button.upload") + "' name='submit' class='button'/></p>");
	html.append("</form>");
	// extended fields

	html.append("<br /></div>");
	return html.toString();
    }
}
