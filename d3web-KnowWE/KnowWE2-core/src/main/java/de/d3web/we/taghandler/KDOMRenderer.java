package de.d3web.we.taghandler;

import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.visitor.RenderKDOMVisitor;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KDOMRenderer extends AbstractTagHandler {

	public KDOMRenderer() {
		super("renderKDOM");
	}
	
	
	@Override
	public String getDescription() {
		return KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.KDOMRenderer.description");
	}
	
	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		RenderKDOMVisitor v = new RenderKDOMVisitor();
		v.visit(KnowWEEnvironment.getInstance().getArticle(web, topic)
				.getSection());
		String data = "<b>KDOM:<b><br><br>"
				+ v.getRenderedKDOM() + "";
		return data;
	}

}
