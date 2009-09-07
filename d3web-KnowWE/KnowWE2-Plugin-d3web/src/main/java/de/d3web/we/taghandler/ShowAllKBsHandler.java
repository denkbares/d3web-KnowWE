package de.d3web.we.taghandler;

import java.util.ResourceBundle;

import de.d3web.we.action.KnowledgeSummerizeRenderer;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ShowAllKBsHandler extends AbstractTagHandler {	
	
	private KnowledgeSummerizeRenderer renderer = null;
	
	public ShowAllKBsHandler() {
		super("showAllKBs");
		renderer = new KnowledgeSummerizeRenderer();
	}
	
	@Override
	public String getDescription() {
		return D3webModule.getInstance().getKwikiBundle_d3web().getString("KnowWE.showAllKBs.description");
	}


	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		return renderer.perform(web);
	}

}
