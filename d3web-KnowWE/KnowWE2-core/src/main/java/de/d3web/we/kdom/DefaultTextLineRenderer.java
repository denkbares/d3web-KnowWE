package de.d3web.we.kdom;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DefaultTextLineRenderer extends KnowWEDomRenderer {

	
	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
	
		return /*generateQuickEditLink(topic, sec.getId(), web, user)+*/SpecialDelegateRenderer.getInstance().render(sec, user, web, topic)+generateTextField(sec);
	}
	
	private String generateTextField(Section sec) {
		
		return "";
		// OFF by now
		//return KnowWEEnvironment
		//.maskHTML("<span><textarea cols=\"30\" rows=\"1\">"+sec.getOriginalText()+"</textarea></span>");
	}

	private String generateQuickEditLink(String topic, String id, String web2, String user) {
		String icon = " <img src=KnowWEExtension/images/pencil.png title='Set QuickEdit-Mode' onclick=setQuickEditFlag('"+id+"','"+topic+"'); ></img>";

		return KnowWEEnvironment
				.maskHTML("<a>"+icon+"</a>");
		
	}

}
