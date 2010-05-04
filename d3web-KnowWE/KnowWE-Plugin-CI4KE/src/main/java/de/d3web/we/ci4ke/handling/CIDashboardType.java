package de.d3web.we.ci4ke.handling;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class CIDashboardType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("CIDashboard");
		MARKUP.addAnnotation("monitoredArticle", true);
		MARKUP.addAnnotation("tests", true);
		MARKUP.addAnnotation("trigger",true,"onDemand","onSave","onNight");
	}
	
	public CIDashboardType() {
		super(MARKUP);
		//this.addReviseSubtreeHandler(new GroovyCITestReviseSubtreeHandler());
		this.setCustomRenderer(new DashboardRenderer());
	}
	
	private class DashboardRenderer extends KnowWEDomRenderer<CIDashboardType> {

		@Override
		public void render(KnowWEArticle article, Section<CIDashboardType> sec,
				KnowWEUserContext user, StringBuilder string) {
			
			// Render Error-Messages!
			DefaultMarkupRenderer.renderMessages(sec, string);

			
			
		}
		
	}
	
}
