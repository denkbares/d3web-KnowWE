package de.d3web.we.taghandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Creates a list of all TagHandlers mentioned in the Hash-map knowWEDefaultTagHandlers.
 * @author Max Diez
 *
 */
public class TagHandlerListHandler extends AbstractTagHandler{
	
	public TagHandlerListHandler() {
		super("taghandlerlist");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web){
		Collection<TagHandler> coll = KnowWEEnvironment.getInstance().getDefaultTagHandlers().values();
		
		StringBuffer html = new StringBuffer();
		html.append("<div id='taghandlerlist-panel' class='panel'>" + "<h3>" 
				+ KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWETagHandlerListHandler.header") + "</h3> ");
		html.append("<table width=100% border=1>");
		html.append("<TR><TH>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWETagHandlerListHandler.table.column1_name") 
				+ "</TH><TH>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWETagHandlerListHandler.table.column2_example")
				+ "</TH><TH>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWETagHandlerListHandler.table.column3_description") + "</TH>");
		for (TagHandler th : coll) {
			String name = "no name available";
			String example = "no example available";
			String description = "no description available";
			try {
			if(th instanceof AbstractTagHandler) {
				name = ((AbstractTagHandler)th).getName();
				example =((AbstractTagHandler)th).getExampleString();
				description =  ((AbstractTagHandler)th).getDescription();			
			}
			} catch(Exception e) {
				description = "Fehler";
				System.out.println("Error by getting nformation:");
				System.out.println(e.getStackTrace());
			}
			html.append("<TR><TD>" +  name+ "</TD><TD>" + example + "</TD><TD>" + description+ "</TD></TR> \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
		}
		html.append("</table></div>");
		return html.toString();
	}
	
	@Override
	public String getDescription() {
		return KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KnowWETagHandlerListHandler.description");
	}
}
