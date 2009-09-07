package de.d3web.we.taghandler;

import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>This class handles the appearance of the ReanmingTool tag.</p>
 */
public class RenamingTagHandler extends AbstractTagHandler {
	
	public RenamingTagHandler() {
		super("RenamingTool");

	}

	@Override
	public String getDescription() {
		// TODO add description for this handler
		return super.getDescription();
	}
	
	/**
	 * <p>Returns a HTML representation of the renaming tool form.</p>
	 */
	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		StringBuffer html = new StringBuffer();
		
		html.append("<div id=\"rename-panel\" class=\"panel\"><h3>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.renamingtool.redefine") + "</h3>");
		
		html.append("<form method='post' action=''>");
		html.append("<fieldset>");
//		html.append("<legend> " + kwikiBundle.getString("KnowWE.renamingtool.redefine") + " </legend>");
				
		html.append("<div class='left'>");
		html.append("<label for='renameInputField'>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.renamingtool.searchterm") + "</label>");
		html.append("<input id='renameInputField' type='text' name='TargetNamespace' value='' tabindex='1' class='field' title=''/>");
		html.append("</div>");	
		
		html.append("<div class='left'>");
		html.append("<label for='replaceInputField'>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.renamingtool.replace") + "</label>");
		html.append("<input id='replaceInputField' type='text' name='replaceTerm'  tabindex='2' class='field' title=''/>");
		html.append("</div>");
		
		html.append("<div id='search-button'>");
		html.append("<input type='button' value='" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.renamingtool.preview") + "' name='submit' tabindex='3' class='button' title='' onclick='sendRenameRequest();'/>");
		html.append("</div>");
		
		html.append("<div style='clear:both'></div>");
		
		html.append("<p id='rename-show-extend' class='pointer extend-panel-down'>");
		html.append(KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.renamingtool.settings") + "</p>");

		html.append("<div id='rename-extend-panel' class='hidden'>");
		
		html.append("<div class='left'>");
		html.append("<label for='renamePreviousInputContext'>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.renamingtool.previous") + "</label>"); 
		html.append("<input id='renamePreviousInputContext' type='text' name='' value='' tabindex='5' class='field'/>");
		html.append("</div>");
		
		html.append("<div class='left'>");
		html.append("<label for='renameAfterInputContext'>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.renamingtool.after") + "</label>");
		html.append("<input id='renameAfterInputContext' type='text' name='' value='' tabindex='6' class='field'/>");
		html.append("</div>");
		
		html.append("<div class='left'>");
	    html.append("<label for='search-sensitive'>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.renamingtool.case") + "</label>");
	    html.append("<input id='search-sensitive' type='checkbox' name='search-sensitive' tabindex='7' checked='checked'/>");
		html.append("</div>");		
		
		html.append("</div>");
		
		html.append("<input type='hidden' value='RenamingRenderer' name='action' />");
		html.append("</fieldset> ");
		html.append("</form>");
		html.append("<div id='rename-result'></div>");
		html.append("</div>");
		
		return html.toString();
	}
}
