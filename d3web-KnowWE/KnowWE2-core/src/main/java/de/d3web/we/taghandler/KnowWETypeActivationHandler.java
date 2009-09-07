package de.d3web.we.taghandler;

import java.util.List;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KnowWETypeActivationHandler extends AbstractTagHandler{
	
	public KnowWETypeActivationHandler() {
		super("KnowWEObjectTypeActivator");
	}
	
	@Override
	public String getDescription() {
		return KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWEObjectTypeHandler.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		List<KnowWEObjectType> types = KnowWEEnvironment.getInstance().getAllKnowWEObjectTypes();
		StringBuilder html = new StringBuilder();
		
		html.append("<div id=\"KnowWEObjectTypeActivator\" class=\"panel\"><h3>"
				+ KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWeObjectTypeHandler.topic")
				+ "</h3>");
		html.append("<form method='post' action='' name='typeactivator'>");
		html.append("<fieldset>");
		
		// Create SelectList
		html.append("<select name=\"Auswahl\" size=\"6\">");
		
		// Iterate over Types to create Checkboxes
		String spancolor;
		String name = "";
		for (KnowWEObjectType type : types) {
			
			// get the current Activation Status and set the color
			spancolor = "green";
			if (!type.getActivationStatus()){
				spancolor = "red";
			}
			
			// get the name of the current type
			if (!type.getName().contains(".")) {
				name = type.getClass().getPackage().getName() + ".";
			}
			name += type.getName();
			
			// insert type with spancolor
			html.append("<option value=\"" + name + "\"style=\"color:" + spancolor + "\">"
						+ type.getName()
						+ "</option> \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
		}
		html.append("</select>");
		
		// button for changing
		html.append("<p><input type='button' class='button' "
					+ "value='" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWEObjectTypeActivator.changebutton") + "'"
					+ "onclick='switchTypeActivation(\"" + types.size() + "\");'/></p>");
		
		html.append("</fieldset> ");
		html.append("</form>");
		html.append("</div>");
		
		return html.toString();
	}
}
