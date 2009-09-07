package de.d3web.we.taghandler;

import java.util.ResourceBundle;

import de.d3web.we.action.RefreshHTMLDialogAction;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DialogPaneTagHandler extends AbstractTagHandler {
	

	public DialogPaneTagHandler() {
		super("dialog");
	}
	
	
	@Override
	public String getDescription() {
		return D3webModule.getInstance().getKwikiBundle_d3web().getString("KnowWE.DialogPane.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
	
		String dialog = RefreshHTMLDialogAction.callDialogRenderer(topic, user.getUsername(), web);
		if(dialog == null) return null;
		
		return "<div id='dialog-panel' class=\'panel\'>"+dialog+"</div>";
	}
}
