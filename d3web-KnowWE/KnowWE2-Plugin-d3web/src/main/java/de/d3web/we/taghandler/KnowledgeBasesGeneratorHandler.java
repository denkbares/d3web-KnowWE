package de.d3web.we.taghandler;

import java.util.LinkedList;
import java.util.ListIterator;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class KnowledgeBasesGeneratorHandler extends AbstractTagHandler {

	private KnowWEWikiConnector wikiConnector;

	public KnowledgeBasesGeneratorHandler () {
		super("KnowledgeBasesUploader");
		
	}
	
	@Override
	public String getDescription() {
		// TODO add description for this handler
		return D3webModule.getInstance().getKwikiBundle_d3web().getString("KnowWE.KnowledgeBasesGeneratorHandler.description");
	}
	
	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		
		if(wikiConnector == null) {
			wikiConnector = KnowWEEnvironment.getInstance().getWikiConnector();
		}
		
		// update attachments
		LinkedList <String> attchmnts = wikiConnector.getAttachments();
		
		StringBuffer html = new StringBuffer();
		
		html.append("<div id=\"KnowledgeBasesGenerator\" class=\"panel\"><h3>"
				+ KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.knowledgebasesgenerator.topic")
				+ "</h3>");
		html.append("<form method='post' action=''>");
		html.append("<fieldset>");

		// as long as there are KBs available do.
		if ((attchmnts != null) && (!attchmnts.isEmpty())) {
			
			/*
			 * For each jar File create a line like This.
			 * File_newNameField_generateButton
			 */
			ListIterator<String> it = attchmnts.listIterator();
			while (it.hasNext()) {
				
				String a = it.next();
				
				html.append("<div>");
				html.append("<p><img src='KnowWEExtension/images/arrow_right.png' border='0'/> "
						+ KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.knowledgebasesgenerator.jarname") + a + "</p>");
				html.append("<label for='" + a + "'>" + "Neuer Name:" + "</label>");
				html.append("<input id='" + a + "' type='text' name='nameTerm' class='field' title=''/>");
				
				html.append("<input type='button' value='"
						+ KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.knowledgebasesgenerator.generateButton")
						+ "' name='generate' class='button' title='' onclick='doKbGenerating(\"" + a + "\");'/>");
				
				html.append("</div> \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)

			}
		} else {
			html.append("<div>");
			html.append("<p class='info box'>"
			+ KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.knowledgebasesgenerator.nokb")
			+ "</p>");
			html.append("</div>");
			
		}
		
		// div for generating info
		html.append("<div id ='GeneratingInfo'>");
		html.append("</div>");
		
		html.append("</fieldset> ");
		
		html.append("</form>");
		
		html.append("</div>");
		
		return html.toString();
	}
}
