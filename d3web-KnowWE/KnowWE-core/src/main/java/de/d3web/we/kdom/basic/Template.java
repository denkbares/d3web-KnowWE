package de.d3web.we.kdom.basic;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class Template extends AbstractXMLObjectType {

	private static Template instance;
	
	
	public Template() {
		super("Template");
		this.customRenderer = new PreRenderer();
	}

	/**
	 * @return
	 */
	public static KnowWEObjectType getInstance() {
		if (instance == null)
			instance = new Template();
		return instance;
	}
	
	private class PreRenderer extends KnowWEDomRenderer {

		@Override
		public void render(KnowWEArticle article, Section sec,
				KnowWEUserContext user, StringBuilder string) {

			string.append("{{{");
			DelegateRenderer.getInstance().render(article, sec, user, string);
			string.append("}}}");
			
		}		
	}
}
