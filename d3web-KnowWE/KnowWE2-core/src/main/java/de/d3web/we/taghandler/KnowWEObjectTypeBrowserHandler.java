package de.d3web.we.taghandler;

import java.util.List;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KnowWEObjectTypeBrowserHandler extends AbstractTagHandler{

	private KnowWEEnvironment env;
	
	public KnowWEObjectTypeBrowserHandler(KnowWEEnvironment envi) {
		super("TypeBrowser");
		this.env = envi;
	}
	
	@Override
	public String getDescription() {
		return KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWEObjectTypeBrowserHandler.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		List<KnowWEObjectType> types = env.getAllKnowWEObjectTypes();
		StringBuilder html = new StringBuilder();
		
		// Create Header
		html.append("<div id=\"KnowWEObjectTypeBrowser\" class=\"panel\"><h3>"
				+ KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWeObjectTypeBrowser.topic")
				+ "</h3>");
		html.append("<form method='post' action='' name='typebrowser'>");
		html.append("<fieldset>");
		
		// Create SelectList
		html.append("<select name=\"Auswahl\" size=\"6\">");
		
		// Create entry for every Type
		String name = "";
		for (KnowWEObjectType type : types) {
			if (!type.getName().contains(".")) {
				name = type.getClass().getPackage().getName() + ".";
			}
			name += type.getName();
			
			html.append("<option value=\"" + name + "\">" + type.getName() + "</option> \n" ); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
		}
		
		html.append("</select>");
		
		// Create a Search Button to start the Search for every Type
		html.append("<p><input type='button'"
				    + " value='" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWeObjectTypeBrowser.searchbutton") + "' name='' class='button' title='' onclick='searchTypes(\"" + types.size() +"\");'/></p>");
		
		html.append("<div id=\"TypeSearchResult\""
					+ "</div>");
		
		html.append("</fieldset> ");
		html.append("</form>");
		html.append("</div>");
		
		return html.toString();
	}
}
